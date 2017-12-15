package utilities.scheduler.jobs;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_BootLoader;
import models.Model_FileRecord;
import models.Model_TypeOfBoard;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import play.Configuration;
import play.api.Play;
import play.data.Form;
import play.i18n.Lang;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.logger.Class_Logger;
import utilities.swagger.documentationClass.Swagger_CompilationLibrary;
import utilities.swagger.documentationClass.Swagger_GitHubReleases;
import utilities.swagger.documentationClass.Swagger_GitHubReleases_Asset;
import utilities.swagger.documentationClass.Swagger_GitHubReleases_List;

import java.net.ConnectException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Updates & Synchronize Compilation versions from GitHub HW-Libs
 *
 * Tato třída slouží k pravidelnému dotazování GitHubu, a to bez jeho přičinění (nepodporuje WebHook).
 * Vesměs v běžném režimu je to jen jeden request, kde se porovná délka odpovědi a pokud je stejná jako ta předchozí,
 * vlákno ví, že se nic nezměnilo a vrátí se do stavu spánku. Tato synchronizace je záměrně napsaná jako automatická
 * (bez přičinění vývojáře, nebo administrátora, protože pouze synchronizuje obsah "názvy" knihoven, kterými lze buildit
 * kod v Code Editoru na Becki (zasílat komandy na Compilační server). To co se načte v této třídě je pak pomocí Cache
 * manažeru vloženo do Model_TypeofBoard - protože ten pak vrací Becki seznam dostupných kompilačních knihoven.
 *
 * Tyrion neaktualizuje!!! Obsah knihoven na kompilačních server, ty si to dělají jen a pouze sami! Můžou mít třeba milion
 * samostatných knihoven, ale dokud jim Tyrion nedá pokyn pomocí kterého mají příchozí kod kompilovat, je to jen zbytečně uložený soubor.
 * Což je samo o sobě ochranou celého flow.
 */
public class Job_CheckCompilationLibraries implements Job {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Job_CheckCompilationLibraries.class);

//**********************************************************************************************************************


    public Job_CheckCompilationLibraries(){}

    /*
        Zde ukládáme proměné, které se používají v dalších iteracích provádění Jobu
        Například se zde UDělá záznam "otisk" přišlé zprávy z Githubu - ta se porovná - a pokud jsou stejné - Job ví,
        že zpráva je tootžná a nemá smysl jí dále louskat a kontrolovat obsah a konfiguraci serveru.
    */

    // Metoda volána z implements Job  (zásobníku Jobs)
    public void execute(JobExecutionContext context) throws JobExecutionException {

        terminal_logger.info("execute: Executing Job_CheckCompilationLibraries");

        if(!check_version_thread.isAlive()) check_version_thread.start();
    }

    private Thread check_version_thread = new Thread() {

        @Override
        public void run() {
            try {

                WSClient ws = Play.current().injector().instanceOf(WSClient.class);

                terminal_logger.trace("check_version_thread: concurrent thread started on {}", new Date());

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
                        .setHeader("Authorization", "token " + Configuration.root().getString("GitHub.apiKey"))
                        .setHeader("Accept", "application/json")
                        .get()
                        .get(10000);

                if (ws_response_get_all_releases.getStatus() != 200) {
                    terminal_logger.error("Permission Error in Job_CheckCompilationLibraries. Please Check it");
                    terminal_logger.error("Error Message from Github: {}", ws_response_get_all_releases.getBody());
                    return;
                }

                // Získám seznam všech objektů z Githubu
                ObjectNode request_list = Json.newObject();
                request_list.set("list", ws_response_get_all_releases.asJson());

                final Form<Swagger_GitHubReleases_List> form = Form.form(Swagger_GitHubReleases_List.class).bind(request_list);
                if (form.hasErrors()) {
                    throw new Exception("check_version_thread: Incoming Json from GitHub has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString());
                }

                // Seznam Release z GitHubu v upravené podobě
                List<Swagger_GitHubReleases> releases = form.get().list;

                List<Model_TypeOfBoard> typeOfBoards_from_DB_not_cached = Model_TypeOfBoard.find.where().eq("removed_by_user", false).select("id").findList();
                // Do každého typu desky - který má Tyrion v DB doplní podporované knihovny

                //System.out.println("Počet procházených typeOfBoard dohromady je:: " + typeOfBoards.size());
                for (Model_TypeOfBoard typeOfBoard_from_DB_not_cached : typeOfBoards_from_DB_not_cached) {

                    Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.get_byId(typeOfBoard_from_DB_not_cached.id);

                    // Pokud není pole, vytvořím ho
                    if (typeOfBoard.cache_library_list == null) {
                        typeOfBoard.cache_library_list = new ArrayList<>();
                    }

                    // Aktuální podporované knihovny
                    List<Swagger_CompilationLibrary> library_list = typeOfBoard.supported_libraries();

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


                        // Ignorujeme všechny tagy, které se týkají bootloader
                        // ZDe mohou přibýt další ignore filtry
                        if (release.tag_name.contains("bootloader") || release.tag_name.toLowerCase().contains("bootloader")) {
                            continue;
                        }


                        String regex_apha_beta = "^(v)(\\d+\\.)(\\d+\\.)(\\d+)((-alpha|-beta)((\\.\\d+){0,3})?)?$"; // - alfa nebo beta
                        String regex_beta = "^(v)(\\d+\\.)(\\d+\\.)(\\d+)((-beta)((\\.\\d+){0,3})?)?$ ";  // - jenom Beta
                        String regex_production = "^(v)(\\d+\\.)(\\d+\\.)(\\d+)$ "; // - Jenom čistá verze


                        Pattern pattern = null;
                        if (Server.server_mode == Enum_Tyrion_Server_mode.production) {
                            pattern = Pattern.compile(regex_beta);                          // Záměrně - uživatelům to umožnujeme průběžně řešit
                        } else if (Server.server_mode == Enum_Tyrion_Server_mode.stage) {
                            pattern = Pattern.compile(regex_beta);
                        } else if (Server.server_mode == Enum_Tyrion_Server_mode.developer) {
                            pattern = Pattern.compile(regex_apha_beta);
                        }

                        if (pattern == null) {
                            terminal_logger.error("check_version_thread:: Pattern is null -  Server.server_mode not set!");
                            continue;
                        }

                        Matcher matcher = pattern.matcher(release.tag_name);

                        if (matcher.matches()) {
                            terminal_logger.info("check_version_thread:: Code Library Version TAG name {} match regex", release.tag_name);
                        } else {
                            terminal_logger.warn("check_version_thread:: Code Library Version  TAG name {} not match regex {} ", release.tag_name, pattern.pattern());
                            continue;
                        }

                        if (release.prerelease || release.draft) {
                            terminal_logger.trace("check_version_thread:: prerelease == true");
                            continue;
                        }

                        if (release.assets.size() == 0) {
                            terminal_logger.trace("check_version_thread:: not any assets - its required!");
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

                    typeOfBoard.cache_library_list.addAll(library_list_for_add);
                    typeOfBoard.update();

                }


                terminal_logger.trace("check_version_thread:: all Library type of Board synchronized");

                for (Model_TypeOfBoard typeOfBoard_from_DB_not_cached : typeOfBoards_from_DB_not_cached) {

                    Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.get_byId(typeOfBoard_from_DB_not_cached.id);

                    //System.out.println("Získávám z databáze všechny bootloadery");
                    List<Model_BootLoader> bootLoaders = typeOfBoard.boot_loaders_get_for_github_include_removed();

                    // List který budu doplnovat
                    List<Model_BootLoader> bootLoaders_for_add = new ArrayList<>();

                    // Pokud knihovnu
                    synchro_bootloaders:
                    for (Swagger_GitHubReleases release : releases) {

                        // System.out.println("V Bootloader sekci zkoumám release " + release.tag_name);

                        // Nejedná se o bootloader
                        if (!release.tag_name.contains("bootloader")) {
                            //System.out.println("Releasse neobsahuje slovo bootloader");
                            continue;
                        }

                        String[] subStrings_main_parts = release.tag_name.split("_bootloader_");
                        //System.out.println("Board Type:: " + subStrings_main_parts[0]);
                        //System.out.println("Version Type:: " + subStrings_main_parts[1]);

                        if (subStrings_main_parts[0] == null) {
                            terminal_logger.error("Required Part in Release Tag name for Booloader missing): Type Of Board (compiler_target_name) ");
                            continue;
                        }

                        if (subStrings_main_parts[1] == null) {
                            terminal_logger.error("Required Part in Release Tag name for Booloader missing): Version (v1.0.1)");
                            continue;
                        }


                        // Kontrola zda už neexistuje
                        for (Model_BootLoader bootLoader : bootLoaders) {
                            if (bootLoader.version_identificator.equals(subStrings_main_parts[1])) {
                                // Je již vytvořen
                                //System.out.println("Releasse" + release.tag_name + " už je dávno vytvořen");
                                continue synchro_bootloaders;
                            }
                        }


                        // Nejedná se o správný typ desky
                        if (!release.tag_name.contains(typeOfBoard.compiler_target_name)) {
                            //System.out.println("Releasse neobsahuje správný typ desky - očekávaný pro tento cykl je" + typeOfBoard.compiler_target_name);
                            continue;
                        }

                        // nebráno v potaz
                        if (release.prerelease || release.draft) {
                            terminal_logger.trace("check_version_thread:: prerelease == true");
                            continue;
                        }


                        Model_BootLoader new_bootLoader = new Model_BootLoader();
                        new_bootLoader.date_of_create = new Date();
                        new_bootLoader.name = release.name;
                        new_bootLoader.changing_note = release.body;
                        new_bootLoader.description = "Bootloader - automatic synchronize with GitHub Repository";
                        new_bootLoader.version_identificator = subStrings_main_parts[1];
                        new_bootLoader.type_of_board = typeOfBoard;


                        // Find in Assest required file
                        String asset_url = null;
                        for (Swagger_GitHubReleases_Asset asset : release.assets) {
                            if (asset.name.equals("bootloader.bin")) {
                                asset_url = asset.url;
                            }
                        }

                        if (asset_url == null) {
                            terminal_logger.error("check_version_thread:: Required file bootloader.bin in release {} not found", release.tag_name);
                            continue;
                        }

                        //System.out.println("Assets URL for downloading:: " + asset_url);

                        WSResponse ws_download_file = download_file(asset_url);
                        if (ws_download_file == null) {
                            terminal_logger.error("Error in downloading file");
                            continue;
                        }


                        if (ws_download_file.getStatus() != 200) {
                            terminal_logger.error("check_version_thread:: Download Bootloader [] unsuccessful", release.tag_name);
                            terminal_logger.error("reason", ws_download_file.getBody());
                            return;
                        }

                        String file_body = Model_FileRecord.get_encoded_binary_string_from_body(ws_download_file.asByteArray());

                        // Naheraji na Azure
                        String file_name = "bootloader.bin";
                        String file_path = new_bootLoader.get_Container().getName() + "/" + UUID.randomUUID().toString() + "/" + file_name;

                        terminal_logger.debug("check_version_thread:: bootLoader_uploadFile::  File Name " + file_name);
                        terminal_logger.debug("check_version_thread:: bootLoader_uploadFile::  File Path " + file_path);

                        new_bootLoader.file = Model_FileRecord.uploadAzure_File(file_body, "application/octet-stream", file_name, file_path);
                        new_bootLoader.save();

                        // Nefungovalo to korektně občas - tak se to ukládá oboustraně!
                        new_bootLoader.file.boot_loader = new_bootLoader;
                        new_bootLoader.file.update();
                        new_bootLoader.refresh();

                        bootLoaders_for_add.add(new_bootLoader);

                        typeOfBoard.boot_loaders().addAll(bootLoaders_for_add);
                        typeOfBoard.update();

                    }
                }

                terminal_logger.trace("check_version_thread:: all Bootloader in type of Board synchronized");

            } catch (F.PromiseTimeoutException e ){
                terminal_logger.error("Job_CheckCompilationLibraries:: PromiseTimeoutException! - Probably Network is unreachable", new Date());
            } catch (ConnectException e) {
                terminal_logger.error("Job_CheckCompilationLibraries:: ConnectException! - Probably Network is unreachable", new Date());
            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }

            terminal_logger.trace("check_version_thread: thread stopped on {}", new Date());
        }
    };

    public static WSResponse download_file(String assets_url){
        try {

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);

            WSResponse wsResponse = ws.url(assets_url)
                    .setHeader("Authorization", "token 4d89903b259510a1257a67d396bd4aaf10cdde6a")
                    .setHeader("Accept", "application/octet-stream")
                    .setFollowRedirects(false)
                    .get()
                    .get(10000);

            terminal_logger.trace("update_server_thread: Got file download url");
            terminal_logger.trace(wsResponse.getHeader("location"));

            // Redirect URL from response
            String url = wsResponse.getHeader("location");

            // Request for URL without query params
            WSRequest request = ws.url(url.substring(0, url.indexOf("?")))
                    .setRequestTimeout(-1);

            // Query params must be decoded and added one by one, because of bug in Play! (query was getting double encoded)
            String[] pairs = url.substring(url.indexOf("?") + 1).split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                request.setQueryParameter(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }


            return request.get().get(30, TimeUnit.MINUTES);


        }catch (Exception e){
            terminal_logger.error(e.getMessage());
            return null;
        }
    }
}