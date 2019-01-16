package utilities.scheduler.jobs;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import common.ServerConfig;
import controllers._BaseFormFactory;
import models.Model_HardwareType;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;
import utilities.swagger.input.Swagger_CompilationLibrary;
import utilities.swagger.input.Swagger_GitHubReleases;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This job synchronizes compilation libraries from GitHub releases.
 */
@Scheduled("0 * * ? * *") // Every minute
public class Job_GetCompilationLibraries extends _GitHubZipHelper implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_GetCompilationLibraries.class);

//**********************************************************************************************************************

    private final ServerConfig serverConfig;

    @Inject
    public Job_GetCompilationLibraries(WSClient ws, Config config, _BaseFormFactory formFactory, ServerConfig serverConfig) {
        super(ws, config, formFactory);
        this.serverConfig = serverConfig;
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
                            continue;
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

                        Swagger_CompilationLibrary new_library = new Swagger_CompilationLibrary();
                        new_library.tag_name = release.tag_name;
                        new_library.name = release.name;
                        new_library.body = release.body;
                        new_library.draft = release.draft;
                        new_library.prerelease = release.prerelease;
                        new_library.created_at = release.created_at;
                        new_library.published_at = release.published_at;
                        library_list_for_add.add(new_library);

                    }

                    logger.trace("check_version_thread:: set supported libraries: {} ", library_list_for_add);

                    hardwareType.cache_library_list.addAll(library_list_for_add);

                    // Sorting List
                    hardwareType.cache_library_list = hardwareType.cache_library_list.stream().sorted((element1, element2) -> element2.name.compareTo(element1.name)).collect(Collectors.toList());
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



}

