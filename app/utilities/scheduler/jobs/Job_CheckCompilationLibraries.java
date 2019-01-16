package utilities.scheduler.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import common.ServerConfig;
import controllers._BaseFormFactory;
import io.ebean.Expr;
import models.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.compiler.CompilationService;
import utilities.enums.ProgramType;
import utilities.logger.Logger;
import utilities.scheduler.Restrict;
import utilities.scheduler.Scheduled;
import utilities.slack.SlackService;
import utilities.swagger.input.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utilities.enums.ServerMode.STAGE;

/**
 * This job synchronizes compilation libraries from GitHub releases.
 */
@Scheduled("0 0/5 * 1/1 * ? *")
@Restrict(value = { STAGE }) // Do it only on stage
public class Job_CheckCompilationLibraries extends _GitHubZipHelper implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_CheckCompilationLibraries.class);

//**********************************************************************************************************************

    private CompilationService compilationService;
    private final ServerConfig serverConfig;
    private final SlackService slackService;

    @Inject
    public Job_CheckCompilationLibraries(WSClient ws, Config config, _BaseFormFactory formFactory, ServerConfig serverConfig,
                                         CompilationService compilationService, SlackService slackService) {
        super(ws, config, formFactory);
        this.serverConfig = serverConfig;
        this.slackService = slackService;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_CheckCompilationLibraries");

        if (!check_version_thread.isAlive()) check_version_thread.start();
    }

    private Thread check_version_thread = new Thread() {

        @Override
        public void run() {
            try {

                logger.trace("check_version_thread: concurrent thread started on {}", new Date());

                // Seznam Release z GitHubu v upravené podobě
                List<Swagger_GitHubReleases> releases = request_list();

                List<UUID> hardwareTypes_id = Model_HardwareType.find.query().select("id").findSingleAttributeList();
                // Do každého typu desky - který má Tyrion v DB doplní podporované knihovny

                for (UUID hwType_id : hardwareTypes_id) {

                    Model_HardwareType hardwareType = Model_HardwareType.find.byId(hwType_id);

                    // Pokud není pole, vytvořím ho
                    if (hardwareType.cache_library_list == null) {
                        hardwareType.cache_library_list = new ArrayList<>();
                    }

                    // Aktuální podporované knihovny
                    List<Swagger_CompilationLibrary> library_list = hardwareType.supported_libraries();

                    // Pokud knihovnu
                    synchro_libraries:
                    for (Swagger_GitHubReleases release : releases) {

                        // Projdu každý release a zda je v library listu
                        for (Swagger_CompilationLibrary library : library_list) {
                            if (library.tag_name.equals(release.tag_name)) {
                                continue synchro_libraries;
                            }
                        }


                        if (release.assets.size() == 0) {
                            logger.debug("check_version_thread: Tag version  {} not contains any assets", release.tag_name);
                            continue;
                        }

                        List<String> obsolete_versions = config.getStringList("compilation_settings.obsolete_lib_version");

                        if (obsolete_versions.contains(release.tag_name)) {
                            logger.debug("check_version_thread: Tag version  {} is mark as obsolete", release.tag_name);
                            continue;
                        }


                        if (release.tag_name == null) {
                            logger.error("check_version_thread:: Release is Damaged: {} ", Json.toJson(release).toString());
                            continue;
                        }

                        // Ignorujeme všechny tagy, které se týkají bootloader
                        // ZDe mohou přibýt další ignore filtry
                        if (release.tag_name.contains("bootloader") || release.tag_name.toLowerCase().contains("bootloader")) {
                            continue;
                        }


                        String regex_apha_beta = "^(v)(\\d+\\.)(\\d+\\.)(\\d+)((-alpha|-beta)((\\.\\d+){0,3})?)?$"; // - alfa nebo beta
                        String regex_beta = "^(v)(\\d+\\.)(\\d+\\.)(\\d+)((-beta)((\\.\\d+){0,3})?)?$";  // - jenom Beta
                        String regex_production = "^(v)(\\d+\\.)(\\d+\\.)(\\d+)$"; // - Jenom čistá verze


                        Pattern pattern = null;
                        if (serverConfig.isProduction()) {
                            pattern = Pattern.compile(regex_beta);                          // Záměrně - uživatelům to umožnujeme průběžně řešit
                        } else if (serverConfig.isStage()) {
                            pattern = Pattern.compile(regex_beta);
                        } else if (serverConfig.isDevelopment()) {
                            pattern = Pattern.compile(regex_apha_beta);
                        }

                        if (pattern == null) {
                            logger.error("check_version_thread:: Pattern is null -  Server.server_mode not set!");
                            continue;
                        }

                        Matcher matcher = pattern.matcher(release.tag_name);

                        if (matcher.matches()) {
                            logger.debug("check_version_thread:: Code Library Version TAG name {} match regex", release.tag_name);
                        } else {
                            logger.debug("check_version_thread:: Code Library Version  TAG name {} not match regex {} ", release.tag_name, pattern.pattern());
                            continue;
                        }

                        if (release.prerelease || release.draft) {
                            logger.trace("check_version_thread:: prerelease == true");
                            continue;
                        }

                        if (release.assets.size() == 0) {
                            logger.trace("check_version_thread:: not any assets - its required!");
                            continue;
                        }


                        logger.trace("check_version_thread:: setAndCompileNewPublicPrograms: release {}", release.prettyPrint());
                        try {
                            setAndCompileNewPublicPrograms(release);
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }

                }

                logger.trace("thread:check_version_thread:: all Library type of Board synchronized");

            } catch (F.PromiseTimeoutException e ) {
                logger.error("thread:: PromiseTimeoutException! - Probably Network is unreachable", new Date());
            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.trace("check_version_thread: thread stopped on {}", new Date());
        }
    };



    public void setAndCompileNewPublicPrograms(Swagger_GitHubReleases release) {
        try {

            logger.trace("setAndCompileNewPublicPrograms release: " + release.name + " Asset size:" + release.assets.size());


            if(release.assets.isEmpty()) {
                logger.trace("setAndCompileNewPublicPrograms release: " + release.name + " not contains any assets!");
                return;
            }

            logger.trace("setAndCompileNewPublicPrograms release: asset url: " + release.assets.get(0).url);


            File directory = new File(System.getProperty("user.dir") + "/files/" );

            if (!directory.exists() || directory.isDirectory()) {
                if(directory.mkdir()){
                    logger.error("setAndCompileNewPublicPrograms:: directory created");
                }
            }

            String file_path = System.getProperty("user.dir") + "/files/" +  release.tag_name;
            String file_name = file_path  + ".zip";

            WSResponse ws_download_file = download_file(release.assets.get(0).url);


            if(ws_download_file == null) {
                logger.error("check_version_thread:: download request is null");
                return;
            }

            if (ws_download_file.getStatus() != 200) {
                logger.error("check_version_thread:: Download Bootloader [] unsuccessful", release.tag_name);
                logger.error("reason", ws_download_file.getBody());
                return;
            }


            byte[] bytes = ws_download_file.asByteArray();

            ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
            baos.write(bytes, 0, bytes.length);


            // Remove before if exist
            if(!new File(file_path).exists()) {
                logger.trace("setAndCompileNewPublicPrograms file_path: " + file_path + " there is a file - it will be removed before");
                this.remove_file(file_path);
                this.remove_file(file_name);
            }


            try (OutputStream outputStream = new FileOutputStream(file_name)) {
                baos.writeTo(outputStream);
                logger.trace("setAndCompileNewPublicPrograms release: " + release.name + " successfully saved on local storage");
            } catch (Exception e) {
                e.printStackTrace();
            }


            logger.trace("setAndCompileNewPublicPrograms release: " + release.name + " try to unzip");

            this.unzip(file_name, file_path);



            logger.trace("setAndCompileNewPublicPrograms release: try to find file examples" );

            if(!new File(file_path + "/examples").exists()) {
                // Slack.post_invalid_release(release);
                logger.debug("setAndCompileNewPublicPrograms example missing");

                logger.debug("setAndCompileNewPublicPrograms Mažu:" + file_path);
                this.remove_file(file_path + "/");

                logger.debug("setAndCompileNewPublicPrograms Mažu:" + file_name);
                this.remove_file(file_name);

            }

            logger.trace("setAndCompileNewPublicPrograms soubor obsahuje složku examples");
            logger.trace("setAndCompileNewPublicPrograms path to: " + file_path + "/examples");
            logger.trace("setAndCompileNewPublicPrograms kde to najdu: : " + file_path + "/examples");

            File[] directories = new File(file_path + "/examples").listFiles(File::isDirectory);


            String error_for_slack = "";

            if(directories == null) {

                logger.trace("setAndCompileNewPublicPrograms: directories is null!");
                error_for_slack += "\n Example: *" + release.tag_name + "* not contains folder example";

            } else {

                for (File directory_with_example : directories) {

                    File readme = null;
                    File maincpp = null;
                    Swagger_GitHub_ExampleFile json = null;

                    for (final File fileEntry : directory_with_example.listFiles()) {

                        if (fileEntry.getName().equals("main.cpp")) {
                            maincpp = fileEntry;
                        }

                        if (fileEntry.getName().equals("readme.json")) {
                            readme = fileEntry;
                        }

                    }

                    if (maincpp == null || readme == null) {
                        error_for_slack += "\n Example: *" + directory_with_example.getName() + "* not contains main.cpp or readme.json file! Fix it!";
                    } else {
                        logger.trace("Example: " + directory_with_example.getName() + "is ok!");
                    }

                    Scanner scanner = new Scanner(readme, "UTF-8");
                    String text = scanner.useDelimiter("\\A").next();
                    JsonNode jsonNode = Json.parse(text);

                    Scanner scanner_cprogram = new Scanner(maincpp, "UTF-8");
                    String text_cprogram = scanner_cprogram.useDelimiter("\\A").next();

                    json = formFactory.formFromJsonWithValidation(Swagger_GitHub_ExampleFile.class, jsonNode);


                    if (json.name == null || json.name.equals("")) {
                        error_for_slack += "\n Example: *" + directory_with_example.getName() + "* - the name is not properly set!";
                        continue;
                    }

                    if (json.description == null || json.description.equals("") || json.description.equals("This should be some long description of the example")) {
                        error_for_slack += "\n Example: *" + directory_with_example.getName() + "* - the description is not properly set!";
                        //TODO continue;
                    }

                    if (json.targets.isEmpty()) {
                        error_for_slack += "\n Example: *" + directory_with_example.getName() + "* - targets is empty or not contain allowed values";
                        continue;
                    }


                    Model_CProgram c_program = Model_CProgram.find.query().nullable().where()
                            .eq("name", json.name)
                            .eq("publish_type", ProgramType.PUBLIC)
                            .disjunction()
                            .add(Expr.eq("tags.value", "Byzance"))
                            .add(Expr.eq("tags.value", "Example"))
                            .eq("hardware_type.compiler_target_name", json.targets.get(0))
                            .ne("deleted", true)
                            .findOne();

                    if (c_program == null) {
                        logger.trace("Example: " + directory_with_example.getName() + " c_program s těmito parametry neexistuje - je nutné ho vytvořit");

                        c_program = new Model_CProgram();
                        c_program.name = json.name;
                        c_program.description = json.description;
                        c_program.publish_type = ProgramType.PUBLIC;

                        Model_HardwareType hardwareType = Model_HardwareType.find.query().nullable().where().eq("compiler_target_name", json.targets.get(0)).eq("deleted", false).findOne();

                        if (hardwareType == null) {
                            logger.debug("HW Libs Example: ERROR " + directory_with_example.getName() + " not found Model_HardwareType!");
                            error_for_slack += "\n Example: *" + directory_with_example.getName() + "* - target " + json.targets + " is not valid.";
                            continue;
                        }

                        c_program.hardware_type = hardwareType;
                        c_program.save();

                        List<String> tags = new ArrayList<>();
                        tags.add("Byzance");
                        tags.add("Example");

                        c_program.setTags(tags);
                    }


                    // Create First version
                    logger.trace("Example: " + directory_with_example.getName() + " its time to create version");

                    Model_CProgramVersion version = Model_CProgramVersion.find.query().nullable().where().eq("c_program.id", c_program.id).eq("name", release.tag_name).findOne();

                    if (version != null) {
                        logger.trace("Example: " + directory_with_example.getName() + " version " + release.tag_name + " is already created");
                        continue;
                    }

                    // Create Version
                    version = new Model_CProgramVersion();
                    version.name = release.tag_name;
                    version.c_program = c_program;
                    version.publish_type = ProgramType.PUBLIC;
                    version.save();

                    Swagger_C_Program_Version_New version_program = new Swagger_C_Program_Version_New();
                    version_program.main = text_cprogram;
                    version_program.library_compilation_version = release.tag_name;

                    // Content se nahraje na Azur
                    version.file = Model_Blob.upload(Json.toJson(version_program).toString(), "application/octet-stream", "code.json", c_program.get_path());
                    version.update();

                    // Start with asynchronous compilation
                    this.compilationService.compileAsync(version, release.tag_name);
                }
            }


            if (serverConfig.isStage() && error_for_slack.length() > 0) {

                error_for_slack = "Toto je automatická zpráva kterou vygeneroval všemocný Tyrion Server. \n Podle GitHubu *" + release.author.login + "* vytvořil firmware release *" + release.tag_name + "* s následujícími chybami:." + error_for_slack;
                slackService.postHardwareChannel(error_for_slack);
                return;
            }


            // Cleare after set
            this.remove_file(file_path);
            this.remove_file(file_name);

        } catch (Exception e) {
            logger.internalServerError(e);
        }

    }



}

