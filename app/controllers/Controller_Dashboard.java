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
import views.html.c_program.aproval_community_procedure.approval_procedure_c_program;
import views.html.c_program.aproval_community_procedure.approval_procedure_list;
import views.html.c_program.c_libraries.library;
import views.html.c_program.c_libraries.library_list;
import views.html.c_program.c_libraries.library_version;
import views.html.c_program.public_c_programs.public_c_code;
import views.html.c_program.public_c_programs.public_c_code_list;
import views.html.demo_data.demo_data_main;
import views.html.external_servers.external_servers;
import views.html.grid.grid_management;
import views.html.grid.grid_public;
import views.html.hardware_generator.generator_main;
import views.html.helpdesk_tool.project_detail;
import views.html.helpdesk_tool.product_detail;
import views.html.helpdesk_tool.user_summary;
import views.html.helpdesk_tool.invoice;
import views.html.permission.permissions_summary;
import views.html.permission.role;
import views.html.c_program.c_program_editor;
import views.html.common.main;
import views.html.tariffs.extension_edit;
import views.html.tariffs.tariff_edit;
import views.html.tariffs.tariffs;
import views.html.tyrion_developers.Api_Div;
import views.html.tyrion_developers.readme;
import views.html.websocket.instance_detail;
import views.html.websocket.websocket;
import views.html.websocket.websocket_homer_server_detail;
import views.html.wiki.wiki;
import web_socket.message_objects.compilator_with_tyrion.WS_Message_Ping_compilation_server;
import web_socket.services.WS_Becki_Website;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * CONTROLLER je určen pro jednoduchý frontend, který slouží pro zobrazení stavu backendu, základních informací,
 * ovládání websocketu a čtení readme. Dále podpora pro porovnávání změn nad dokumentací ze Swaggeru a další.
 *
 * Obsah tohoto controlleru není dovloeno vlákadt do dokumentace Swaggeru
 *
 * */
@Api(value = "Private Admin Api", hidden = true)
@Security.Authenticated(Secured_Admin.class)
public class Controller_Dashboard extends Controller {

    @Inject Application application;

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Dashboard.class);


// Index (úvod) ########################################################################################################

    // Pomocná metoda, která skládá jednotlivé stránky dohromady
    public Result return_page(Html content){
        return ok(main.render(request().path(), content));
    }

    // Úvodní zobrazení Dashboard
    public Result index() {

        Html content_html = dashboard.render();
        return return_page(content_html);
    }

// README ###############################################################################################################

    // Zobrazení readme podle MarkDown

    public Result show_readme() throws IOException {
        try {

            terminal_logger.debug("show_readme:: Creating show_readme.html content");

            String text = "";
            for (String line : Files.readAllLines(Paths.get("README.md"), StandardCharsets.UTF_8)) text += line + "\n";

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

            file_name = file_name.replaceAll("%2F", "/");

            for (String line : Files.readAllLines(Paths.get("conf/markdown_documentation/" + file_name), StandardCharsets.UTF_8)) text += line + "\n";

            file_name = file_name.substring(file_name.lastIndexOf("/") + 1).replace("-", " ").replace("_", " ").replace(".md", "");


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

                    return GlobalResult.result_badRequest(result);
                }

            }else {

                ObjectNode result = Json.newObject();
                result.put("status", "Becki terminal is not connected now");

                return GlobalResult.result_badRequest(result);
            }

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result ping_homer_server(String unique_identificator) {
        try {

            Model_HomerServer server = Model_HomerServer.get_byId(unique_identificator);

            return GlobalResult.result_ok(Json.toJson(server.ping()));
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
        try{

            Html content =   websocket.render();
            return return_page(content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result  show_instance_detail(String instance_id) {

        try{

            Model_HomerInstance instance = Model_HomerInstance.get_byId(instance_id);
            if(instance == null) return show_web_socket_stats();

            Html content = instance_detail.render(instance);
            return return_page(content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }



    public Result  show_websocket_server_detail(String unique_identificator) {

        Model_HomerServer server = Model_HomerServer.get_byId(unique_identificator);
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

            Model_Board board = Model_Board.get_byId(board_id);
            if (board == null) return GlobalResult.result_notFound("Board not found");

            Html content = board_detail.render(board);
            return return_page(content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result bootloader_management(String type_of_board_id){
        try {

            Model_TypeOfBoard type_of_board = Model_TypeOfBoard.get_byId(type_of_board_id);

            if(type_of_board == null) {

                return GlobalResult.result_notFound("Type of Board not found!");

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

            Model_SecurityRole role_object = Model_SecurityRole.get_byId(role_id);

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
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result public_c_code_list(){
        try {

            Html public_code_content = public_c_code_list.render();
            return return_page(public_code_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result public_c_code(String c_program_id){
        try {

            Model_CProgram c_program = Model_CProgram.get_byId(c_program_id);

            Html public_code_content = public_c_code.render(c_program);
            return return_page(public_code_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result public_code_management(){
        try {

            Html public_code_content = c_program_editor.render();
            return return_page(public_code_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result public_code_approve_procedure_list(){
        try {

            Html public_code_content = approval_procedure_list.render();
            return return_page(public_code_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result public_code_approve_procedure(String c_program_id){
        try {

            Model_CProgram c_program = Model_CProgram.get_byId(c_program_id);

            Html public_code_content = approval_procedure_c_program.render(c_program);
            return return_page(public_code_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public Result public_libraries(){
        try {

            Html libraries_content = library_list.render();
            return return_page(libraries_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result public_library(String library_id){
        try {

            Model_Library model_library = Model_Library.get_byId(library_id);

            Html libraries_content = library.render(model_library);
            return return_page(libraries_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result public_library_version(String version_id){
        try {

            Model_VersionObject version = Model_VersionObject.get_byId(version_id);
            if (version == null) return GlobalResult.result_notFound("Version not found");

            Html content = library_version.render(version);
            return return_page(content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
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
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result project_detail(String id){
        try {

            Model_Project project = Model_Project.get_byId(id);
            if (project == null) return GlobalResult.result_notFound("Project not found");

            Html project_detail_content = project_detail.render(project);
            return return_page(project_detail_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result product_detail(String id){
        try {

            Model_Product product = Model_Product.get_byId(id);
            if (product == null) return GlobalResult.result_notFound("Product not found");

            Html product_detail_content = product_detail.render(product);
            return return_page(product_detail_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result invoice(String id){
        try {

            Model_Invoice inv = Model_Invoice.get_byId(id);
            if (inv == null) return GlobalResult.result_notFound("Invoice not found");

            Html content = invoice.render(inv);
            return return_page(content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result demo_data(){
        try {

            Html test_content = demo_data_main.render();
            return return_page(test_content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result tariffs(){
        try {

            Html list_of_tariffs = tariffs.render();
            return return_page(list_of_tariffs);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result tariff_edit(String tariff_id){
        try {

            Model_Tariff tariff = Model_Tariff.get_byId(tariff_id);

            Html content = tariff_edit.render(tariff);
            return return_page(content);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result extension_edit(String extension_id){
        try {

            Model_ProductExtension extensions = Model_ProductExtension.get_byId(extension_id);

            if(extensions == null) return not_found();

            Html extension_page = extension_edit.render(extensions);
            return return_page(extension_page);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result not_found(){
        try {

            terminal_logger.warn("Link Not found");
            // TODO - přesměrování na page 404 Not found http://youtrack.byzance.cz/youtrack/issue/TYRION-504
            return index();

        }catch (Exception e){
            terminal_logger.internalServerError("not_found:", e);
            return ok();
        }
    }



}



