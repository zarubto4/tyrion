package utilities.scheduler.jobs;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import models.Model_Blob;
import models.Model_BootLoader;
import models.Model_CProgramVersion;
import models.Model_HardwareType;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.data.FormFactory;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.ServerMode;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;
import utilities.slack.Slack;
import utilities.swagger.input.Swagger_CompilationLibrary;
import utilities.swagger.input.Swagger_GitHubReleases;
import utilities.swagger.input.Swagger_GitHubReleases_Asset;
import utilities.swagger.input.Swagger_GitHubReleases_List;

import java.net.ConnectException;
import java.net.URLDecoder;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This job synchronizes bootloader libraries from GitHub releases.
 */
@Scheduled("30 0/1 * * * ?")
public class Job_CheckBootloaderLibraries implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_CheckBootloaderLibraries.class);

//**********************************************************************************************************************

    private WSClient ws;
    private Config config;
    private _BaseFormFactory formFactory;

    @Inject
    public Job_CheckBootloaderLibraries(WSClient ws, Config config, _BaseFormFactory formFactory) {
        this.ws = ws;
        this.config = config;
        this.formFactory = formFactory;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("execute: Executing Job_CheckBootloaderLibraries");

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
                    logger.error("Permission Error in Job_CheckBootloaderLibraries. Please Check it");
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


                for (UUID uuid : hardwareTypes_id) {

                    Model_HardwareType hardwareType = Model_HardwareType.getById(uuid);

                    logger.trace("check_version_thread:: Get  Hardware Type from database: Type Name: {}",  hardwareType.name);

                    //System.out.println("Získávám z databáze všechny bootloadery");
                    List<Model_BootLoader> bootLoaders = hardwareType.boot_loaders_get_for_github_include_removed();


                    logger.trace("check_version_thread:: For this type we have {} bootLoaders",  bootLoaders.size());

                    // List který budu doplnovat
                    List<Model_BootLoader> bootLoaders_for_add = new ArrayList<>();

                    // Pokud knihovnu
                    synchro_bootloaders:
                    for (Swagger_GitHubReleases release : releases) {

                        // Nejedná se o bootloader
                        if (!release.tag_name.contains("bootloader")) {
                            logger.trace("check_version_thread:: release {} is not bootloader",  release.name );
                            continue;
                        }

                        logger.trace("check_version_thread:: release {} is bootloader",  release.name );

                        String[] subStrings_main_parts = release.tag_name.split("_bootloader_");

                        if (subStrings_main_parts[0] == null) {
                            logger.error("Required Part in Release Tag name for Booloader missing): Type Of Board (compiler_target_name) ");
                            Slack.post_invalid_bootloader(release.name);
                            continue;
                        }

                        if (subStrings_main_parts[1] == null) {
                            logger.error("Required Part in Release Tag name for Booloader missing): Version (v1.0.1)");
                            Slack.post_invalid_bootloader(release.name);
                            continue;
                        }

                        logger.trace("check_version_thread:: now we will check if booloader already exist in database",  release.name );

                        // Kontrola zda už neexistuje
                        for (Model_BootLoader bootLoader : bootLoaders) {
                            if (bootLoader.version_identifier.equals(subStrings_main_parts[1])) {
                                // Je již vytvořen
                                logger.trace("check_version_thread:: yes - its already created in database");
                                continue synchro_bootloaders;
                            }
                        }

                        logger.trace("check_version_thread:: no its not - we can create it");

                        // Nejedná se o správný typ desky
                        if (!release.tag_name.contains(hardwareType.compiler_target_name)) {
                            continue;
                        }

                        // nebráno v potaz
                        if (release.prerelease || release.draft) {
                            logger.trace("check_version_thread:: prerelease == true");
                            continue;
                        }

                        Model_BootLoader new_bootLoader = new Model_BootLoader();
                        new_bootLoader.name = release.name;
                        new_bootLoader.changing_note = release.body;
                        new_bootLoader.description = "Bootloader - automatic synchronize with GitHub Repository";
                        new_bootLoader.version_identifier = subStrings_main_parts[1];
                        new_bootLoader.hardware_type = hardwareType;

                        // Find in Assest required file
                        String asset_url = null;
                        for (Swagger_GitHubReleases_Asset asset : release.assets) {
                            if (asset.name.equals("bootloader.bin")) {
                                asset_url = asset.url;
                            }
                        }

                        if (asset_url == null) {
                            logger.error("check_version_thread:: Required file bootloader.bin in release {} not found", release.tag_name);
                            continue;
                        }

                        WSResponse ws_download_file = download_file(asset_url);
                        if (ws_download_file == null) {
                            logger.error("Error in downloading file");
                            continue;
                        }


                        if (ws_download_file.getStatus() != 200) {
                            logger.error("check_version_thread:: Download Bootloader [] unsuccessful", release.tag_name);
                            logger.error("reason", ws_download_file.getBody());
                            return;
                        }

                        String file_body = Model_Blob.get_encoded_binary_string_from_body(ws_download_file.asByteArray());

                        new_bootLoader.save();

                        // Naheraji na Azure

                        logger.debug("check_version_thread:: bootLoader_uploadFile::  File Name " + "bootloader.bin");
                        logger.debug("check_version_thread:: bootLoader_uploadFile::  File Path " + new_bootLoader.get_path());

                        new_bootLoader.file = Model_Blob.upload(file_body, "application/octet-stream",  "bootloader.bin", new_bootLoader.get_path());
                        new_bootLoader.update();

                        // Nefungovalo to korektně občas - tak se to ukládá oboustraně!
                        new_bootLoader.file.boot_loader = new_bootLoader;
                        new_bootLoader.file.update();
                        new_bootLoader.refresh();

                        bootLoaders_for_add.add(new_bootLoader);


                    }

                    hardwareType.boot_loaders().addAll(bootLoaders_for_add);
                    hardwareType.update();

                    // Clean Cache
                    hardwareType.cache().removeAll(Model_BootLoader.class);

                    //Get all bootloaders from Database
                    List<Model_BootLoader> bootloaders = hardwareType.get_bootloaders();

                    // Clean Cache
                    hardwareType.cache().removeAll(Model_BootLoader.class);

                    // Order by date of create
                    bootloaders.stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())
                            .forEach(o -> hardwareType.cache().add(Model_BootLoader.class, o.id));


                }


                logger.trace("check_version_thread:: all Bootloader in type of Board synchronized");

            } catch (F.PromiseTimeoutException e ) {
                logger.error("Job_CheckBootloaderLibraries:: PromiseTimeoutException! - Probably Network is unreachable", new Date());
            } catch (ConnectException e) {
                logger.error("Job_CheckBootloaderLibraries:: ConnectException! - Probably Network is unreachable", new Date());
            } catch (Exception e) {
                logger.internalServerError(e);
            }

            logger.trace("check_version_thread: thread stopped on {}", new Date());
        }
    };

    private WSResponse download_file(String assets_url) {
        try {

            WSResponse wsResponse = ws.url(assets_url)
                    .addHeader("Authorization", "token 4d89903b259510a1257a67d396bd4aaf10cdde6a")
                    .addHeader("Accept", "application/octet-stream")
                    .setFollowRedirects(false)
                    .get()
                    .toCompletableFuture()
                    .get();

            logger.trace("update_server_thread: Got file download url");

            String url;

            Optional<String> optional = wsResponse.getSingleHeader("location");
            if (optional.isPresent()) {
                url = optional.get();
            } else {
                return null;
            }

            logger.trace("update_server_thread - url: {}", url);

            // Request for URL without query params
            WSRequest request = ws.url(url.substring(0, url.indexOf("?")))
                    .setRequestTimeout(Duration.ofMinutes(30));

            // Query params must be decoded and added one by one, because of bug in Play! (query was getting double encoded)
            String[] pairs = url.substring(url.indexOf("?") + 1).split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                request.addQueryParameter(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }

            return request.get().toCompletableFuture().get(30, TimeUnit.MINUTES);

        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}