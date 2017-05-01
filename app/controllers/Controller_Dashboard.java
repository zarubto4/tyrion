package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import models.*;
import org.pegdown.PegDownProcessor;
import play.Application;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.twirl.api.Html;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.swagger.swagger_diff_tools.Swagger_diff_Controller;
import utilities.swagger.swagger_diff_tools.servise_class.Swagger_Diff;
import views.html.*;
import views.html.blocko.blocko_management;
import views.html.blocko.blocko_objects;
import views.html.boards.board_detail;
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
import views.html.helpdesk_tool.invoice;
import views.html.permission.permissions_summary;
import views.html.permission.role;
import views.html.publiccprograms.approvalprocedurecprogram;
import views.html.publiccprograms.c_program_editor;
import views.html.publiccprograms.libraries;
import views.html.publiccprograms.publiccode;
import views.html.super_general.main;
import views.html.tariffs.extension_edit;
import views.html.tariffs.tariff_edit;
import views.html.tariffs.tariffs;
import views.html.websocket.instance_detail;
import views.html.websocket.websocket;
import views.html.websocket.websocket_homer_server_detail;
import web_socket.message_objects.compilatorServer_with_tyrion.WS_Message_Ping_compilation_server;
import web_socket.message_objects.homer_instance.WS_Message_Ping_instance;
import web_socket.services.WS_Becki_Website;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
@Security.Authenticated(Secured_Admin.class)
public class Controller_Dashboard extends Controller {

    @Inject Application application;

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Board.class);


// Index (úvod) ########################################################################################################

    // Pomocná metoda, která skládá jednotlivé stránky dohromady
    public Result return_page( Html content){
        return ok( main.render(content) );
    }

    // Úvodní zobrazení Dashboard
    public Result index() {

        Html content_html = dashboard.render();
        return ok( main.render(content_html) );
    }

// README ###############################################################################################################

    // Zobrazení readme podle MarkDown

    public Result show_readme() throws IOException {
        try {

            terminal_logger.debug("show_readme:: Creating show_readme.html content");

            String text = "";
            for (String line : Files.readAllLines(Paths.get("README"), StandardCharsets.UTF_8)) text += line + "\n";

            Html readme_html = readme.render(new Html(new PegDownProcessor().markdownToHtml(text)));

            terminal_logger.debug("show_readme:: Return show_readme.html content");

            return return_page(readme_html);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result show_wiki(String file_name) throws IOException {
        try {

            terminal_logger.debug("show_wiki:: Creating wiki content");

            String text = "";

            file_name.replaceAll("%2F", "/");

            for (String line : Files.readAllLines(Paths.get("conf/markdown_documentation/" + file_name), StandardCharsets.UTF_8)){
                text += line + "\n";
            }

            file_name = file_name.substring(file_name.lastIndexOf("/") + 1);
            file_name = file_name.replaceAll("_", " ");
            file_name.replace(".markdown", " ");
            file_name.replace(".md", " ");

            Html wiki_html = wiki.render(file_name.substring(0,1).toUpperCase() + file_name.substring(1) , new Html(new PegDownProcessor().markdownToHtml(text)));

            return return_page(wiki_html);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

// API DIFF ###############################################################################################################

    // Zobrazení rozdílu mezi verzemi
    public Result show_diff_on_Api(String file_name_old, String file_name_new) throws IOException, NullPointerException {
        try {

            terminal_logger.debug("show_diff_on_Api:: diff_html content");

            List<String> fileNames = new ArrayList<>();
            File[] files = new File(application.path() + "/conf/swagger_history").listFiles();
            for (File file : files) {fileNames.add((file.getName().substring(0, file.getName().lastIndexOf('.'))).replace("_", "."));}

            if(file_name_old.equals("")) file_name_old = fileNames.get( ( fileNames.size()-2) ) ;
            if(file_name_new.equals("")) file_name_new = fileNames.get( ( fileNames.size()-1) ) ;


            Swagger_Diff swagger_diff = Swagger_diff_Controller.set_API_Changes(file_name_old, file_name_new);
            Html content = Api_Div.render(swagger_diff, fileNames);

            terminal_logger.debug("show_diff_on_Api:: eturn Api_Div.html content");
            return return_page(content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


// WEBSOCKET STATS ######################################################################################################


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
            return Server_Logger.result_internalServerError(e, request());
        }
    }



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
            return Server_Logger.result_internalServerError(e, request());
        }
    }

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
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result ping_becki(String person_id, String token) throws TimeoutException, InterruptedException {
        try {

            if ( Controller_WebSocket.becki_website.containsKey(person_id)) {

                WS_Becki_Website ws = (WS_Becki_Website) Controller_WebSocket.becki_website.get(person_id);

                if (ws.all_person_Connections.containsKey(token)){

                    return GlobalResult.result_ok();

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
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result ping_homer_server(String unique_identificator) {
        try {

            Model_HomerServer server = Model_HomerServer.get_model(unique_identificator);

            return GlobalResult.result_ok(Json.toJson(server.ping()));
        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result ping_homer_instance(String instance_id) {
        try {

            Model_HomerInstance instance = Model_HomerInstance.find.where().eq("blocko_instance_name", instance_id).findUnique();
            WS_Message_Ping_instance result = instance.ping();
            return GlobalResult.result_ok(Json.toJson(result));
        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result ping_compilation_server(String identificator) {
        try {

            Model_CompilationServer server = Model_CompilationServer.find.where().eq("unique_identificator", identificator).findUnique();
            WS_Message_Ping_compilation_server result = server.ping();

            return GlobalResult.result_ok(Json.toJson(result));


        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


// ADMIN ###############################################################################################################


    public Result show_web_socket_stats() {

        List<WS_Becki_Website>  becki_terminals         = new ArrayList<>(Controller_WebSocket.becki_website.values()).stream().map(o -> (WS_Becki_Website) o).collect(Collectors.toList());
        Html content =   websocket.render(becki_terminals);
        return return_page(content);
    }


    public Result  show_instance_detail(String instance_id) {

        Model_HomerInstance instance = Model_HomerInstance.find.byId(instance_id);
        if(instance == null) return show_web_socket_stats();


        Html content = instance_detail.render(instance);
        return return_page(content);
    }



    public Result  show_websocket_server_detail(String unique_identificator) {

        Model_HomerServer server = Model_HomerServer.get_model(unique_identificator);
        if(server == null) return show_web_socket_stats();


        Html content = websocket_homer_server_detail.render(server);
        return return_page(content);
    }

    public Result basic_board_management(){
        try {

            Html content = board_settings.render();
            return return_page ( content );

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result board_summary(){
        try {

            Html content = board_summary.render();
            return return_page ( content );

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result board_detail(String board_id){
        try {

            Html content = board_detail.render( Model_Board.get_byId(board_id) );
            return return_page ( content );

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

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
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result external_servers(){
        try {

            Html external_servers_content = external_servers.render();
            return return_page(external_servers_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result user_summary(String user_email){
        try {

            Model_Person person;
            if(user_email != null && !user_email.equals("")){

                person = Model_Person.find.where().eq("mail", user_email).findUnique();
                if(person == null) person = Controller_Security.get_person();

            }else {
                person = Controller_Security.get_person();
            }

            Html user_summary_content = user_summary.render( person );
            return return_page(user_summary_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result permissions_summary(){
        try {

            Html permissions_content = permissions_summary.render();
            return return_page(permissions_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result role(String role_id){
        try {

            Model_SecurityRole role_object = Model_SecurityRole.find.byId(role_id);

            Html permissions_content = role.render(role_object);
            return return_page(permissions_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result blocko_objects(){
        try {

            Html blocko_objects_content = blocko_objects.render();
            return return_page(blocko_objects_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result blocko_management(){
        try {

            Html blocko_management_content = blocko_management.render();
            return return_page(blocko_management_content);

        }catch (Exception e){
            return ok();
        }
    }


    public Result public_code(){
        try {

            Html public_code_content = publiccode.render();
            return return_page(public_code_content);

        }catch (Exception e){
            return ok();
        }
    }

    public Result public_code_management(){
        try {

            Html public_code_content = c_program_editor.render();
            return return_page(public_code_content);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return ok();
        }
    }

    public Result public_code_approve_procedure(){
        try {

            Html public_code_content = approvalprocedurecprogram.render();
            return return_page(public_code_content);

        }catch (Exception e){
            return ok();
        }
    }


    public Result libraries(){
        try {

            Html libraries_content = libraries.render();
            return return_page(libraries_content);

        }catch (Exception e){
            return ok();
        }
    }

    public Result grid_public(){
        try {

            Html grid_public_content = grid_public.render();
            return return_page(grid_public_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result mac_address_generator(){
        try {

            Html content = generator_main.render();
            return return_page(content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result grid_management(){
        try {

            Html grid_management_content = grid_management.render();
            return return_page(grid_management_content);

        }catch (Exception e){
            return ok();
        }
    }

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

    public Result product_detail(String id){
        try {

            Model_Product product = Model_Product.get_byId(id);
            if (product == null) return GlobalResult.notFoundObject("Product not found");

            Html product_detail_content = product_detail.render(product);
            return return_page(product_detail_content);

        }catch (Exception e){
            return ok();
        }
    }

    public Result invoice(String id){
        try {

            Model_Invoice inv = Model_Invoice.find.byId(id);
            if (inv == null) return GlobalResult.notFoundObject("Invoice not found");

            Html content = invoice.render(inv);
            return return_page(content);

        }catch (Exception e){
            return ok();
        }
    }

// TEST ################################################################################################################


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
            terminal_logger.internalServerError(e);
            return ok();
        }
    }

    public Result demo_data(){
        try {

            Html test_content = demo_data_main.render();
            return return_page(test_content);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return ok();
        }
    }

    public Result tariffs(){
        try {

            Html list_of_tariffs = tariffs.render();
            return return_page(list_of_tariffs);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return ok();
        }
    }

    public Result tariff_edit(String tariff_id){
        try {

            Model_Tariff tariff = Model_Tariff.find.byId(tariff_id);

            Html content = tariff_edit.render(tariff);
            return return_page(content);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return ok();
        }
    }

    public Result extension_edit(String extension_id){
        try {

            Model_ProductExtension extensions = Model_ProductExtension.find.byId(extension_id);

            if(extensions == null) return not_found();

            Html extension_page = extension_edit.render(extensions);
            return return_page(extension_page);

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return ok();
        }
    }

    public Result not_found(){
        try {

            terminal_logger.error("Link Not found");
            // TODO - přesměrování na page 404 Not found http://youtrack.byzance.cz/youtrack/issue/TYRION-504
            return index();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return ok();
        }
    }



}



