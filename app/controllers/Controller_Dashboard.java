package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import models.compiler.Model_CompilationServer;
import models.compiler.Model_TypeOfBoard;
import models.person.Model_Person;
import models.person.Model_SecurityRole;
import models.project.b_program.instnace.Model_HomerInstance;
import models.project.b_program.servers.Model_HomerServer;
import models.project.global.Model_Product;
import models.project.global.Model_Project;
import models.project.global.financial.Model_GeneralTariff;
import models.project.global.financial.Model_GeneralTariffExtensions;
import org.pegdown.PegDownProcessor;
import play.Application;
import play.Routes;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.twirl.api.Html;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.swagger.swagger_diff_tools.Swagger_diff_Controller;
import utilities.swagger.swagger_diff_tools.servise_class.Swagger_Diff;
import utilities.webSocket.*;
import views.html.*;
import views.html.blocko.blocko_management;
import views.html.blocko.blocko_objects;
import views.html.boards.board_settings;
import views.html.boards.board_summary;
import views.html.boards.bootloader_settings;
import views.html.demo_data.demo_data_main;
import views.html.external_servers.external_servers;
import views.html.grid.grid_management;
import views.html.grid.grid_public;
import views.html.hardware_generator.generator_main;
import views.html.helpdesk_tool.project_detail;
import views.html.helpdesk_tool.user_summary;
import views.html.helpdesk_tool.product_detail;
import views.html.permission.permissions_summary;
import views.html.permission.role;
import views.html.publiccprograms.approvalprocedurecprogram;
import views.html.publiccprograms.libraries;
import views.html.publiccprograms.publiccode;
import views.html.super_general.login;
import views.html.super_general.main;
import views.html.tariffs.extension_edit;
import views.html.tariffs.tariff_edit;
import views.html.tariffs.tariffs;
import views.html.websocket.instance_detail;
import views.html.websocket.websocket;
import views.html.websocket.websocket_homer_server_detail;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * CONTROLLER je určen pro jednoduchý frontend, který slouží pro zobrazení stavu backendu, základních informací,
 * ovládání websocketu a čtení readme. Dále podpora pro porovnávání změn nad dokumentací ze Swaggeru a další.
 *
 * Obsah tohoto controlleru není dovloeno vlákadt do dokumentace Swaggeru
 *
 * */
@Api(value = "Dashboard Private Api", hidden = true)
public class Controller_Dashboard extends Controller {

    @Inject Application application;

    // Logger pro zaznamenávání chyb
    static play.Logger.ALogger logger = play.Logger.of("Loggy");



// Index (úvod) ########################################################################################################

    public Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        controllers.routes.javascript.Controller_CompilationLibraries.typeOfBoard_create(),
                        controllers.routes.javascript.Controller_CompilationLibraries.typeOfBoard_get(),
                        controllers.routes.javascript.Controller_CompilationLibraries.typeOfBoard_update(),
                        controllers.routes.javascript.Controller_CompilationLibraries.typeOfBoard_delete(),

                        controllers.routes.javascript.Controller_CompilationLibraries.processor_create(),
                        controllers.routes.javascript.Controller_CompilationLibraries.processor_get(),
                        controllers.routes.javascript.Controller_CompilationLibraries.processor_getAll(),
                        controllers.routes.javascript.Controller_CompilationLibraries.processor_update(),
                        controllers.routes.javascript.Controller_CompilationLibraries.processor_delete(),

                        controllers.routes.javascript.Controller_CompilationLibraries.new_Producer(),
                        controllers.routes.javascript.Controller_CompilationLibraries.edit_Producer(),
                        controllers.routes.javascript.Controller_CompilationLibraries.get_Producers(),
                        controllers.routes.javascript.Controller_CompilationLibraries.get_Producer(),
                        controllers.routes.javascript.Controller_CompilationLibraries.delete_Producer(),

                        controllers.routes.javascript.Controller_ProgramingPackage.ping_instance()
                )
        );
    }

    // Pomocná metoda, která skládá jednotlivé stránky dohromady
    public Result return_page( Html content){

        return ok( main.render(content) );
    }

// Index (úvod) ########################################################################################################

    // Úvodní zobrazení Dashboard
    @Security.Authenticated(Secured_Admin.class)
    public Result index() {


        // Blocko
        Map<String, WS_HomerServer> blockoServerMap = new HashMap<>();
        Map<String, WebSCType> map_blocko            =  Controller_WebSocket.homer_servers;
        for (Map.Entry<String, WebSCType> entry : map_blocko.entrySet()) blockoServerMap.put(entry.getKey(), (WS_HomerServer) entry.getValue());

        // Compilation
        Map<String, WS_CompilerServer> compilerServerMap = new HashMap<>();
        Map<String, WebSCType> map_compile =  Controller_WebSocket.compiler_cloud_servers;
        for (Map.Entry<String, WebSCType> entry : map_compile.entrySet()) compilerServerMap.put(entry.getKey(), (WS_CompilerServer) entry.getValue());

        Html content_html = dashboard.render();

        return ok( main.render(content_html) );
    }

// README ###############################################################################################################

    // Zobrazení readme podle MarkDown
    @Security.Authenticated(Secured_Admin.class)
    public Result show_readme() throws IOException {
        try {

            logger.debug("Creating show_readme.html content");

            String text = "";
            for (String line : Files.readAllLines(Paths.get("README"), StandardCharsets.UTF_8)) text += line + "\n";

            Html readme_html = readme.render(new Html(new PegDownProcessor().markdownToHtml(text)));

            logger.debug("Return show_readme.html content");

            return return_page(readme_html);

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

// API DIFF ###############################################################################################################

    // Zobrazení rozdílu mezi verzemi
    @Security.Authenticated(Secured_Admin.class)
    public Result show_diff_on_Api(String file_name_old, String file_name_new) throws IOException, NullPointerException {
        try {

            logger.debug("show_diff_on_Api diff_html content");

            List<String> fileNames = new ArrayList<>();
            File[] files = new File(application.path() + "/conf/swagger_history").listFiles();
            for (File file : files) {fileNames.add((file.getName().substring(0, file.getName().lastIndexOf('.'))).replace("_", "."));}

            if(file_name_old.equals("")) file_name_old = fileNames.get( ( fileNames.size()-2) ) ;
            if(file_name_new.equals("")) file_name_new = fileNames.get( ( fileNames.size()-1) ) ;


            Swagger_Diff swagger_diff = Swagger_diff_Controller.set_API_Changes(file_name_old, file_name_new);
            Html content = Api_Div.render(swagger_diff, fileNames);

            logger.debug("Return Api_Div.html content");
            return return_page(content);

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


// WEBSOCKET STATS ######################################################################################################

    @Security.Authenticated(Secured_Admin.class)
    public Result disconnect_becki(String person_id, String token){
        try {

            if(Controller_WebSocket.becki_website.containsKey(person_id)){

                WS_Becki_Website website = (WS_Becki_Website) Controller_WebSocket.becki_website.get(person_id);
                if( website.all_person_Connections.containsKey(token))website.all_person_Connections.get(token).onClose();

                ObjectNode result = Json.newObject();
                result.put("status", "Becki was disconnected successfully");

                return GlobalResult.result_ok(result);

            }else {

                ObjectNode result = Json.newObject();
                result.put("status", "Becki ID is not connected now");
                return GlobalResult.result_ok(result);

            }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


    @Security.Authenticated(Secured_Admin.class)
    public Result disconnect_blocko_server(String identificator) {
        try {

            if (Controller_WebSocket.homer_servers.containsKey(identificator)) {
                Controller_WebSocket.homer_servers.get(identificator).onClose();

                ObjectNode result = Json.newObject();
                result.put("status", "Blocko was disconnected successfully");

                return GlobalResult.result_ok(result);

            }else {

                ObjectNode result = Json.newObject();
                result.put("status", "Blocko ID is not connected now");
                return GlobalResult.result_ok(result);
            }

        } catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result disconnect_compilation_server(String identificator){
        try {
            if (Controller_WebSocket.compiler_cloud_servers.containsKey(identificator)) {
                Controller_WebSocket.compiler_cloud_servers.get(identificator).onClose();

                ObjectNode result = Json.newObject();
                result.put("status", "\"Compilation Server was disconnected successfully");

                return GlobalResult.result_ok(result);

            }else {

                ObjectNode result = Json.newObject();
                result.put("status", "Compilation Server ID is not connected now");
                return GlobalResult.result_ok(result);

            }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


    @Security.Authenticated(Secured_Admin.class)
    public Result ping_becki(String person_id, String token) throws TimeoutException, InterruptedException {
        try {

            if ( Controller_WebSocket.becki_website.containsKey(person_id)) {

                WS_Becki_Website ws = (WS_Becki_Website) Controller_WebSocket.becki_website.get(person_id);

                if (ws.all_person_Connections.containsKey(token)){

                    JsonNode result = Controller_WebSocket.becki_ping((WS_Becki_Single_Connection) ws.all_person_Connections.get(token));

                    return GlobalResult.result_ok(result);
                }else {

                    ObjectNode result = Json.newObject();
                    result.put("status", "Becki terminal is not connected now");

                    return GlobalResult.result_BadRequest(result);
                }

            }else {

                ObjectNode result = Json.newObject();
                result.put("status", "Becki terminal is not connected now");

                return GlobalResult.result_BadRequest(result);
            }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result ping_homer_server(String identificator) {
        try {

            Model_HomerServer server = Model_HomerServer.find.where().eq("unique_identificator", identificator).findUnique();
            JsonNode result = server.ping();
            return GlobalResult.result_ok(result);
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result ping_homer_instance(String instance_id) {
        try {

            Model_HomerInstance instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_id).findUnique();
            JsonNode result = instance.ping();
            return GlobalResult.result_ok(result);
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result ping_compilation_server(String identificator) {
        try {

            Model_CompilationServer server = Model_CompilationServer.find.where().eq("unique_identificator", identificator).findUnique();
            JsonNode result = server.ping();


            return GlobalResult.result_ok(result);


        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


// LOGGY ###############################################################################################################

    // Vykreslí šablonu s bugy
    public Result show_all_logs() {

        logger.debug("Trying to render loggy.html content");

        Html content =  loggy.render( Loggy.getErrors() );
        return return_page(content);

    }

    // Nahraje konkrétní bug na Youtrack
    public F.Promise<Result> loggy_report_bug_to_youtrack(String bug_id) {
        logger.debug("Trying to upload bug to youtrack");

        F.Promise<Result> p = Loggy.upload_to_youtrack(bug_id);
        return p.map((result) -> redirect("/admin/bugs"));
    }

    // Odstraní konkrétní bug ze seznamu (souboru)
    public Result loggy_remove_bug(String bug_id) {
        logger.debug("Removing bug");

        Loggy.remove_error(bug_id);
        return redirect("/admin/bugs");
    }

    // Vyprázdní soubory se záznamem chyb
    public Result loggy_remove_all_bugs() {
        logger.debug("Trying to remove all bugs");
        Loggy.remove_all_errors();

        return redirect("/admin/bugs");
    }

// ADMIN ###############################################################################################################

    @Security.Authenticated(Secured_Admin.class)
    public Result show_web_socket_stats() {

        List<WS_Becki_Website>  becki_terminals         = new ArrayList<>(Controller_WebSocket.becki_website.values()).stream().map(o -> (WS_Becki_Website) o).collect(Collectors.toList());
        Html content =   websocket.render(becki_terminals);
        return return_page(content);
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result  show_instance_detail(String instance_id) {

        Model_HomerInstance instance = Model_HomerInstance.find.byId(instance_id);
        if(instance == null) return show_web_socket_stats();


        Html content = instance_detail.render(instance);
        return return_page(content);
    }


    @Security.Authenticated(Secured_Admin.class)
    public Result  show_websocket_server_detail(String server_identificator) {

        Model_HomerServer server = Model_HomerServer.find.where().eq("unique_identificator",server_identificator).findUnique();
        if(server == null) return show_web_socket_stats();


        Html content = websocket_homer_server_detail.render(server);
        return return_page(content);
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result basic_board_management(){
        try {

            Html content = board_settings.render();
            return return_page ( content );

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result board_summary(){
        try {

            Html content = board_summary.render();
            return return_page ( content );

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result bootloader_management(String type_of_board_id){
        try {

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.find.byId(type_of_board_id);

            if(type_of_board == null) {

                return GlobalResult.notFoundObject("Type of Board not found!");

            }else {
                Html content = bootloader_settings.render(type_of_board);
                return return_page(content);
            }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }



    @Security.Authenticated(Secured_Admin.class)
    public Result external_servers(){
        try {

            Html external_servers_content = external_servers.render();
            return return_page(external_servers_content);

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result user_summary(String user_email){
        try {

            Model_Person person;
            if(user_email != null && !user_email.equals("")){

                person = Model_Person.find.where().eq("mail", user_email).findUnique();
                if(person == null) person = Controller_Security.getPerson();

            }else {
                person = Controller_Security.getPerson();
            }

            Html user_summary_content = user_summary.render( person );
            return return_page(user_summary_content);

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result permissions_summary(){
        try {

            Html permissions_content = permissions_summary.render();
            return return_page(permissions_content);

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


    @Security.Authenticated(Secured_Admin.class)
    public Result role(String role_id){
        try {

            Model_SecurityRole role_object = Model_SecurityRole.find.byId(role_id);

            Html permissions_content = role.render(role_object);
            return return_page(permissions_content);

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result blocko_objects(){
        try {

            Html blocko_objects_content = blocko_objects.render();
            return return_page(blocko_objects_content);

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result blocko_management(){
        try {

            Html blocko_management_content = blocko_management.render();
            return return_page(blocko_management_content);

        }catch (Exception e){
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result public_code(){
        try {

            Html public_code_content = publiccode.render();
            return return_page(public_code_content);

        }catch (Exception e){
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result public_code_approve_procedure(){
        try {

            Html public_code_content = approvalprocedurecprogram.render();
            return return_page(public_code_content);

        }catch (Exception e){
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result libraries(){
        try {

            Html libraries_content = libraries.render();
            return return_page(libraries_content);

        }catch (Exception e){
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result grid_public(){
        try {

            Html grid_public_content = grid_public.render();
            return return_page(grid_public_content);

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result mac_adress_generator(){
        try {

            Html mac_adress_content = generator_main.render();
            return return_page(mac_adress_content);

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result grid_management(){
        try {

            Html grid_management_content = grid_management.render();
            return return_page(grid_management_content);

        }catch (Exception e){
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result project_detail(String id){
        try {

            Model_Project project = Model_Project.find.byId(id);
            if (project == null) return GlobalResult.notFoundObject("Project not found");

            Html project_detail_content = project_detail.render(project);
            return return_page(project_detail_content);

        }catch (Exception e){
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result product_detail(String id){
        try {

            Model_Product product = Model_Product.find.byId(id);
            if (product == null) return GlobalResult.notFoundObject("Project not found");

            Html product_detail_content = product_detail.render(product);
            return return_page(product_detail_content);

        }catch (Exception e){
            return ok();
        }
    }

// TEST ################################################################################################################

    @Security.Authenticated(Secured_Admin.class)
    public Result test(){
        try {



            List<String> fileNames = new ArrayList<>();
            File[] files = new File(application.path() + "/test").listFiles();

            for (File file : files) {

                if(file.getName().equals(".DS_Store")) continue;
                if(file.getName().equals("resources")) continue;

                fileNames.add((file.getName().substring(0, file.getName().lastIndexOf('.'))));
            }

            Path path;

            try {
                path = Paths.get(application.path() + "/logs/test.log");
            }catch (Exception e){
                File file = new File(application.path() + "/logs/test.log");
                file.getParentFile().mkdirs();
                file.createNewFile();
                path = Paths.get(application.path() + "/logs/test.log");
            }

            String log =  new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            Html test_content = test.render(fileNames, log);
            return return_page(test_content);

        }catch (Exception e){
            e.printStackTrace();
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result demo_data(){
        try {

            Html test_content = demo_data_main.render();
            return return_page(test_content);

        }catch (Exception e){
            e.printStackTrace();
            return ok();
        }
    }




    @Security.Authenticated(Secured_Admin.class)
    public Result general_tariffs_list(){
        try {

            Html list_of_tariffs = tariffs.render();
            return return_page(list_of_tariffs);

        }catch (Exception e){
            e.printStackTrace();
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result general_tariff_edit(String general_tariff_id){
        try {

            Model_GeneralTariff tariff = Model_GeneralTariff.find.byId(general_tariff_id);

            Html list_of_tariffs = tariff_edit.render(tariff);
            return return_page(list_of_tariffs);

        }catch (Exception e){
            e.printStackTrace();
            return ok();
        }
    }


    @Security.Authenticated(Secured_Admin.class)
    public Result general_tariff_extension_edit(String extension_id){
        try {

            Model_GeneralTariffExtensions extensions = Model_GeneralTariffExtensions.find.byId(extension_id);

            if(extensions == null) return not_found();

            Html extension_page = extension_edit.render(extensions);
            return return_page(extension_page);

        }catch (Exception e){
            e.printStackTrace();
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result not_found(){
        try {

            //TODO
            return index();

        }catch (Exception e){
            e.printStackTrace();
            return ok();
        }
    }




// LOGIN ###############################################################################################################

    public Result login(){
        try {

            logger.debug("Trying to get login page");
            return ok(login.render());

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

}



