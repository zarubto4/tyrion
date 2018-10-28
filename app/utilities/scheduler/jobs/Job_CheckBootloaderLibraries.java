package utilities.scheduler.jobs;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import models.Model_Blob;
import models.Model_BootLoader;
import models.Model_HardwareType;
import org.apache.commons.io.FileUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utilities.logger.Logger;
import utilities.scheduler.Scheduled;
import utilities.slack.Slack;
import utilities.swagger.input.Swagger_GitHubReleases;
import utilities.swagger.input.Swagger_GitHubReleases_Asset;
import utilities.swagger.input.Swagger_GitHubReleases_List;

import java.io.*;
import java.net.ConnectException;
import java.net.URLDecoder;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This job synchronizes bootloader libraries from GitHub releases.
 */
// @Scheduled("30 0/1 * * * ?")
public class Job_CheckBootloaderLibraries extends GitHubZipHelper implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Job_CheckBootloaderLibraries.class);

//**********************************************************************************************************************


    @Inject
    public Job_CheckBootloaderLibraries(WSClient ws, Config config, _BaseFormFactory formFactory) {
        super(ws, config, formFactory);
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

                // Seznam Release z GitHubu v upravené podobě
                List<Swagger_GitHubReleases> releases = request_list();

                List<UUID> hardwareTypes_id = Model_HardwareType.find.all().stream().map(t -> t.id).collect(Collectors.toList());
                // Do každého typu desky - který má Tyrion v DB doplní podporované knihovny

                for (UUID uuid : hardwareTypes_id) {

                    Model_HardwareType hardwareType = Model_HardwareType.find.byId(uuid);

                    logger.trace("check_version_thread:: Get  Hardware Type from database: Type Name: {}",  hardwareType.name);

                    List<Model_BootLoader> bootLoaders = hardwareType.boot_loaders_get_for_github_include_removed();

                    logger.trace("check_version_thread:: For this type we have {} bootLoaders",  bootLoaders.size());

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
                            Slack.post_invalid_bootloader(release);
                            continue;
                        }

                        if (subStrings_main_parts[1] == null) {
                            logger.error("Required Part in Release Tag name for Booloader missing): Version (v1.0.1)");
                            Slack.post_invalid_bootloader(release);
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
                    }

                    hardwareType.refresh();

                    // Clean Cache
                    hardwareType.idCache().removeAll(Model_BootLoader.class);

                    //Get all bootloaders from Database
                    List<Model_BootLoader> bootloaders = hardwareType.get_bootloaders();

                    // Clean Cache
                    hardwareType.idCache().removeAll(Model_BootLoader.class);

                    // Order by date of create
                    hardwareType.idCache().add(Model_BootLoader.class, bootloaders.stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).map(o -> o.id).collect(Collectors.toList()));
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

}