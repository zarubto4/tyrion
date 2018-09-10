package utilities.scheduler.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import io.ebean.Expr;
import models.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Lang;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.ProgramType;
import utilities.enums.ServerMode;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;
import utilities.slack.Slack;
import utilities.swagger.input.*;

import java.io.*;
import java.net.ConnectException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This job synchronizes compilation libraries from GitHub releases.
 */
///@Scheduled("0 0/5 * 1/1 * ? *")
public class Job_CheckCompilationLibraries implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_CheckCompilationLibraries.class);

//**********************************************************************************************************************

    private WSClient ws;
    private Config config;
    private _BaseFormFactory formFactory;

    @Inject
    public Job_CheckCompilationLibraries(WSClient ws, Config config, _BaseFormFactory formFactory) {
        this.ws = ws;
        this.config = config;
        this.formFactory = formFactory;
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

                /**
                 * Stáhnu si seznam všech releasů z GitHubu. Podle dohody je zde mix releasů.
                 *
                 *
                 * Distribuce knihoven:
                 *
                 * Distribuce Bootloader Verze - kde je povolená jediná vyjímka na konvenci verzování a to na začátku.
                 * Nejdříve je použit Target name a poté "mark" že jde o bootloader a verzi.
                 * Například: YODA_G3E_bootloader_v1.0.2, opět stejnou logikou -beta a alfa.
                 * Vše ostatní je automatizované. Tyrion si stáhne, překopíruje do vlasntího archivu na BLOB server,
                 * doplní si údaje a dále distribuje.
                 */
                WSResponse ws_response_get_all_releases = ws.url("https://api.github.com/repos/ByzanceIoT/hw-libs/releases")
                        .addHeader("Authorization", "token " + config.getString("GitHub.apiKey"))
                        .addHeader("Accept", "application/json")
                        .get()
                        .toCompletableFuture()
                        .get();

                if (ws_response_get_all_releases.getStatus() != 200) {
                    logger.error("Permission Error in Job_CheckCompilationLibraries. Please Check it");
                    logger.error("Error Message from Github: {}", ws_response_get_all_releases.getBody());
                    return;
                }

                // Získám seznam všech objektů z Githubu
                ObjectNode request_list = Json.newObject();
                request_list.set("list", ws_response_get_all_releases.asJson());

                // Get and Validate Object
                Swagger_GitHubReleases_List help = formFactory.formFromJsonWithValidation(Swagger_GitHubReleases_List.class, request_list);


                // Seznam Release z GitHubu v upravené podobě
                List<Swagger_GitHubReleases> releases = help.list;

                List<UUID> hardwareTypes_id = Model_HardwareType.find.query().where().findIds();
                // Do každého typu desky - který má Tyrion v DB doplní podporované knihovny

                for (UUID hwType_id : hardwareTypes_id) {

                    Model_HardwareType hardwareType = Model_HardwareType.find.byId(hwType_id);

                    // Pokud není pole, vytvořím ho
                    if (hardwareType.cache_library_list == null) {
                        hardwareType.cache_library_list = new ArrayList<>();
                    }

                    // Aktuální podporované knihovny
                    List<Swagger_CompilationLibrary> library_list = hardwareType.supported_libraries();

                    // List který budu doplnovat
                    List<Swagger_CompilationLibrary> library_list_for_add = new ArrayList<>();


                    // Pokud knihovnu
                    synchro_libraries:
                    for (Swagger_GitHubReleases release : releases) {

                        // Projdu každý release a zda je v library listu
                        for (Swagger_CompilationLibrary library : library_list) {
                            if (library.tag_name.equals(release.tag_name)) {
                                continue synchro_libraries;
                            }
                        }

                        if(release.assets.size() == 0) {
                            logger.debug("check_version_thread: Tag version  {} not contains any assets", release.tag_name);
                            continue;
                        }

                        List<String> obsolete_versions = config.getStringList("compilation_settings.obsolete_lib_version");

                        if(obsolete_versions.contains(release.tag_name)) {
                            logger.debug("check_version_thread: Tag version  {} is mark as obsolete", release.tag_name);
                            continue synchro_libraries;
                        }


                        if(release.tag_name == null) {
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
                        if (Server.mode == ServerMode.PRODUCTION) {
                            pattern = Pattern.compile(regex_beta);                          // Záměrně - uživatelům to umožnujeme průběžně řešit
                        } else if (Server.mode == ServerMode.STAGE) {
                            pattern = Pattern.compile(regex_beta);
                        } else if (Server.mode == ServerMode.DEVELOPER) {
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

                        Swagger_CompilationLibrary new_library = new Swagger_CompilationLibrary();
                        new_library.tag_name = release.tag_name;
                        new_library.name = release.name;
                        new_library.body = release.body;
                        new_library.draft = release.draft;
                        new_library.prerelease = release.prerelease;
                        new_library.created_at = release.created_at;
                        new_library.published_at = release.published_at;
                        library_list_for_add.add(new_library);

                        setAndCompileNewPublicPrograms(release);
                    }

                    hardwareType.cache_library_list.addAll(library_list_for_add);
                    hardwareType.update();



                    // Sorting List
                    List<Swagger_CompilationLibrary> libraries = new ArrayList<>();
                    hardwareType.cache_library_list.stream().sorted((element1, element2) -> element2.name.compareTo(element1.name)).collect(Collectors.toList())
                            .forEach(o -> libraries.add(o));

                    hardwareType.cache_library_list = libraries;
                }




                logger.trace("check_version_thread:: all Library type of Board synchronized");
                logger.trace("check_version_thread:: all Bootloader in type of Board synchronized");

            } catch (F.PromiseTimeoutException e ) {
                logger.error("Job_CheckCompilationLibraries:: PromiseTimeoutException! - Probably Network is unreachable", new Date());
            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.trace("check_version_thread: thread stopped on {}", new Date());
        }
    };





    public void setAndCompileNewPublicPrograms(Swagger_GitHubReleases release) {
        try {

            System.out.println("setAndCompileNewPublicPrograms release: " + release.name + " Asset size:" + release.assets.size());


            if(release.assets.isEmpty()) {
                System.out.println("setAndCompileNewPublicPrograms release: " + release.name + " not contains any assets!");
                return;
            }

            System.out.println("setAndCompileNewPublicPrograms release: asset url: " + release.assets.get(0).url);

            String file_path = System.getProperty("user.dir") + "/files/" +  release.tag_name;
            String file_name = file_path  + ".zip";

            WSResponse ws_download_file = new Job_CheckBootloaderLibraries(this.ws, this.config, this.formFactory).download_file(release.assets.get(0).url);


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
                System.out.println("setAndCompileNewPublicPrograms file_path: " + file_path + " there is a file - it will be removed before");
                new Job_CheckBootloaderLibraries(this.ws, this.config, this.formFactory).remove_file(file_path);
                new Job_CheckBootloaderLibraries(this.ws, this.config, this.formFactory).remove_file(file_name);
            }


            try (OutputStream outputStream = new FileOutputStream(file_name)) {
                baos.writeTo(outputStream);
                System.out.println("setAndCompileNewPublicPrograms release: " + release.name + " successfully saved on local storage");
            } catch (Exception e) {
                e.printStackTrace();
            }


            System.out.println("setAndCompileNewPublicPrograms release: " + release.name + " try to unzip");

            new Job_CheckBootloaderLibraries(this.ws, this.config, this.formFactory).unzip(file_name, file_path);



            System.out.println("setAndCompileNewPublicPrograms release: try to find file examples" );

            if(!new File(file_path + "/examples").exists()) {
                // Slack.post_invalid_release(release);
                System.err.println("setAndCompileNewPublicPrograms je to napíču examply chybí");

                System.err.println("setAndCompileNewPublicPrograms Mažu:" + file_path);
                new Job_CheckBootloaderLibraries(this.ws, this.config, this.formFactory).remove_file(file_path + "/");

                System.err.println("setAndCompileNewPublicPrograms Mažu:" + file_name);
                new Job_CheckBootloaderLibraries(this.ws, this.config, this.formFactory).remove_file(file_name);

            }

            System.out.println("setAndCompileNewPublicPrograms soubor obsahuje složku examples");

            File[] directories = new File(file_path + "/examples").listFiles(File::isDirectory);


            String error_for_slack = "";

            for(File directory_with_example : directories) {

                File readme = null;
                File maincpp = null;
                Swagger_GitHub_ExampleFile json = null;

                for (final File fileEntry : directory_with_example.listFiles()) {

                    if(fileEntry.getName().equals("main.cpp")) {
                        maincpp = fileEntry;
                    }

                    if(fileEntry.getName().equals("readme.json")) {
                        readme = fileEntry;
                    }

                }

                if(maincpp == null || readme == null) {
                    error_for_slack += "\n Example: *" + directory_with_example.getName() + "* not contains main.cpp or readme.json file! Fix it!";
                } else {
                    System.out.println("Example: " + directory_with_example.getName() + "is ok!");
                }

                Scanner scanner = new Scanner( readme, "UTF-8" );
                String text = scanner.useDelimiter("\\A").next();
                JsonNode jsonNode = Json.parse(text);

                Scanner scanner_cprogram = new Scanner( maincpp, "UTF-8" );
                String text_cprogram = scanner_cprogram.useDelimiter("\\A").next();

                json = formFactory.formFromJsonWithValidation(Swagger_GitHub_ExampleFile.class, jsonNode);


                if(json.name == null || json.name.equals("") ) {
                    error_for_slack += "\n Example: *" + directory_with_example.getName() + "* - the name is not properly set!";
                   continue;
                }

                if(json.description == null || json.description.equals("") || json.description.equals("This should be some long description of the example")) {
                    error_for_slack += "\n Example: *" + directory_with_example.getName() + "* - the description is not properly set!";
                   //TODO continue;
                }

                if(json.targets.isEmpty()) {
                    error_for_slack += "\n Example: *" + directory_with_example.getName() + "* - targets is empty or not contain allowed values";
                    continue;
                }


                Model_CProgram c_program = Model_CProgram.find.query().where()
                        .eq("name", json.name)
                        .eq("publish_type", ProgramType.PUBLIC)
                        .disjunction()
                            .add(Expr.eq("tags.value", "Byzance"))
                            .add(Expr.eq("tags.value", "Example"))
                        .eq("hardware_type.compiler_target_name", json.targets.get(0))
                        .ne("deleted", true)
                        .findOne();

                if (c_program == null) {
                    System.out.println("Example: "  + directory_with_example.getName() + " c_program s těmito parametry neexistuje - je nutné ho vytvořit");

                    c_program = new Model_CProgram();
                    c_program.name = json.name;
                    c_program.description = json.description;
                    c_program.publish_type = ProgramType.PUBLIC;

                    Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("compiler_target_name", json.targets.get(0)).eq("deleted", false).findOne();

                    if(hardwareType == null) {
                        System.err.println("HW Libs Example: ERROR "  + directory_with_example.getName() + " not found Model_HardwareType!");
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
                System.out.println("Example: "  + directory_with_example.getName() + " its time to create version");

                Model_CProgramVersion version = Model_CProgramVersion.find.query().where().eq("c_program.id", c_program.id).eq("name", release.tag_name).findOne();

                if(version != null) {
                    System.out.println("Example: "  + directory_with_example.getName() + " version " + release.tag_name + " is already created");
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
                version.file = Model_Blob.upload(Json.toJson(version_program).toString(), "code.json", c_program.get_path());
                version.update();

                // Start with asynchronous ccompilation
                version.compile_program_thread(release.tag_name);
            }


            if(error_for_slack.length() > 0) {
                error_for_slack = "Toto je automatická zpráva kterou vygeneroval všemocný Tyrion Server. \n Podle GitHubu *" + release.author.login + "* vytvořil firmware release *" + release.tag_name + "* s následujícíma chybama:." + error_for_slack;
                Slack.post_error(error_for_slack, Server.slack_webhook_url_channel_hardware);
                return;
            }


            // Cleare after set
            new Job_CheckBootloaderLibraries(this.ws, this.config, this.formFactory).remove_file(file_path);
            new Job_CheckBootloaderLibraries(this.ws, this.config, this.formFactory).remove_file(file_name);

        } catch (Exception e) {
            logger.internalServerError(e);
        }

    }



}

