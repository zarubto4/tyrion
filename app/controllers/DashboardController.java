package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import models.compiler.Cloud_Compilation_Server;
import models.compiler.TypeOfBoard;
import models.person.Person;
import models.person.SecurityRole;
import models.project.b_program.servers.Cloud_Homer_Server;
import org.pegdown.PegDownProcessor;
import play.Application;
import play.Routes;
import play.libs.F;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.twirl.api.Html;
import utilities.Server;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_Admin;
import utilities.response.GlobalResult;
import utilities.swagger.swagger_diff_tools.Swagger_diff_Controller;
import utilities.swagger.swagger_diff_tools.servise_class.Swagger_Diff;
import utilities.webSocket.*;
import views.html.*;
import views.html.permission.permissions_summary;
import views.html.permission.role;
import views.html.super_general.login;
import views.html.super_general.main;
import views.html.boards.bootloader_settings;
import views.html.boards.board_settings;
import views.html.super_general.menu;
import views.html.user_summary.user_summary;
import views.html.websocket.instance_detail;
import views.html.websocket.websocket;
import scala.collection.JavaConversions;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
public class DashboardController extends Controller {

    @Inject Application application;

    // Logger pro zaznamenávání chyb
    static play.Logger.ALogger logger = play.Logger.of("Loggy");



// Index (úvod) ########################################################################################################

    public Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        controllers.routes.javascript.CompilationLibrariesController.new_TypeOfBoard(),
                        controllers.routes.javascript.CompilationLibrariesController.get_TypeOfBoard(),
                        controllers.routes.javascript.CompilationLibrariesController.edit_TypeOfBoard(),
                        controllers.routes.javascript.CompilationLibrariesController.delete_TypeOfBoard(),

                        controllers.routes.javascript.CompilationLibrariesController.new_Processor(),
                        controllers.routes.javascript.CompilationLibrariesController.get_Processor(),
                        controllers.routes.javascript.CompilationLibrariesController.get_Processor_All(),
                        controllers.routes.javascript.CompilationLibrariesController.update_Processor(),
                        controllers.routes.javascript.CompilationLibrariesController.delete_Processor(),

                        controllers.routes.javascript.CompilationLibrariesController.new_Producer(),
                        controllers.routes.javascript.CompilationLibrariesController.edit_Producer(),
                        controllers.routes.javascript.CompilationLibrariesController.get_Producers(),
                        controllers.routes.javascript.CompilationLibrariesController.get_Producer(),
                        controllers.routes.javascript.CompilationLibrariesController.delete_Producer(),

                        controllers.routes.javascript.ProgramingPackageController.ping_instance()
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

        Map<String, WS_BlockoServer> blockoServerMap = new HashMap<>();

        Map<String, WebSCType> map_blocko =  WebSocketController.blocko_servers;
        for (Map.Entry<String, WebSCType> entry : map_blocko.entrySet()) blockoServerMap.put(entry.getKey(), (WS_BlockoServer) entry.getValue());

        Map<String, WS_CompilerServer> compilerServerMap = new HashMap<>();
        Map<String, WebSCType> map_compile =  WebSocketController.compiler_cloud_servers;
        for (Map.Entry<String, WebSCType> entry : map_compile.entrySet()) compilerServerMap.put(entry.getKey(), (WS_CompilerServer) entry.getValue());

        Html content_html = dashboard.render(
                blockoServerMap,
                compilerServerMap,
                Cloud_Homer_Server.find.all(),
                Cloud_Compilation_Server.find.all()
        );

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
    public Result disconnect_terminal(String terminal_id){
        try {

            if (WebSocketController.incomingConnections_terminals.containsKey(terminal_id)){

                WebSocketController.incomingConnections_terminals.get(terminal_id).onClose();

                ObjectNode result = Json.newObject();
                result.put("status", "Terminal was disconnected successfully");

                return GlobalResult.result_ok(result);

            }else {

                ObjectNode result = Json.newObject();
                result.put("status", "Terminal ID is not connected now");
                return GlobalResult.result_ok(result);
            }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result disconnect_becki(String person_id, String token){
        try {

            if(WebSocketController.becki_website.containsKey(person_id)){

                WS_Becki_Website website = (WS_Becki_Website) WebSocketController.becki_website.get(person_id);
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
    public Result disconnect_homer_instance(String homer_id){
        try {

             if(WebSocketController.incomingConnections_homers.containsKey(homer_id)){

                 WebSocketController.incomingConnections_homers.get(homer_id).onClose();

                 ObjectNode result = Json.newObject();
                 result.put("status", "Homer was disconnected successfully");

                 return GlobalResult.result_ok(result);

             }else {

                 ObjectNode result = Json.newObject();
                 result.put("status", "Homer ID is not connected now");
                 return GlobalResult.result_ok(result);

             }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result disconnect_blocko_server(String identificator) {
        try {

            if (WebSocketController.blocko_servers.containsKey(identificator)) {
                WebSocketController.blocko_servers.get(identificator).onClose();

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
            if (WebSocketController.compiler_cloud_servers.containsKey(identificator)) {
                WebSocketController.compiler_cloud_servers.get(identificator).onClose();

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
    public Result ping_terminal(String terminal_id) throws TimeoutException, InterruptedException {
        try {

            if (WebSocketController.incomingConnections_terminals.containsKey(terminal_id)) {

                JsonNode result = WebSocketController.terminal_ping( WebSocketController.incomingConnections_terminals.get(terminal_id));
                return GlobalResult.result_ok(result);
            }
            else {
                ObjectNode result = Json.newObject();
                result.put("status", "Terminalr ID is not connected now");

                return GlobalResult.result_BadRequest(result);
            }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }

    }

    @Security.Authenticated(Secured_Admin.class)
    public Result ping_becki(String person_id) throws TimeoutException, InterruptedException {
        try {

            if ( WebSocketController.becki_website.containsKey(person_id)) {

                JsonNode result = WebSocketController.becki_ping( WebSocketController.becki_website.get(person_id) );
                return GlobalResult.result_ok(result);

            }else {

                ObjectNode result = Json.newObject();
                result.put("status", "Homer server ID is not connected now");

                return GlobalResult.result_BadRequest(result);
            }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }

    }

    @Security.Authenticated(Secured_Admin.class)
    public Result ping_homer_server(String identificator) throws TimeoutException, InterruptedException {
        try {

            if (WebSocketController.blocko_servers.containsKey(identificator)) {

                JsonNode result = WebSocketController.homer_server_ping((WS_BlockoServer) WebSocketController.blocko_servers.get(identificator));
                return GlobalResult.result_ok(result);
            }
            else {

                ObjectNode result = Json.newObject();
                result.put("status", "Homer server ID is not connected now");

                return GlobalResult.result_BadRequest(result);
            }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result ping_homer_instance(String instance_id) throws TimeoutException, InterruptedException {
        try {

            if (WebSocketController.incomingConnections_homers.containsKey(instance_id)) {

                JsonNode result = WebSocketController.homer_instance_ping_instance( (WS_Homer_Cloud) WebSocketController.incomingConnections_homers.get(instance_id));
                return GlobalResult.result_ok(result);
            }
            else {

                ObjectNode result = Json.newObject();
                result.put("status", "Instance is not server ID is not connected now");

                return GlobalResult.result_BadRequest(result);
            }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result ping_compilation_server(String identificator) throws TimeoutException, InterruptedException, ExecutionException {
        try {

            if (WebSocketController.compiler_cloud_servers.containsKey(identificator)) {

                JsonNode result = WebSocketController.compiler_server_ping( (WS_CompilerServer) WebSocketController.compiler_cloud_servers.get(identificator) );
                return GlobalResult.result_ok(result);
            }
            else {

                ObjectNode result = Json.newObject();
                result.put("status", "Compilation server ID is not connected now");

                return GlobalResult.result_BadRequest(result);
            }

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


    @Security.Authenticated(Secured_Admin.class)
    public Result log_out_terminal_user(String identificator) {
        System.out.println("Ještě neimplementováno");
        return GlobalResult.result_ok();
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
        return p.map((result) -> redirect("/public/bugs"));
    }

    // Odstraní konkrétní bug ze seznamu (souboru)
    public Result loggy_remove_bug(String bug_id) {
        logger.debug("Trying to upload bug to youtrack");

        Loggy.remove_error(bug_id);
        return redirect("/public/bugs");
    }

    // Vyprázdní soubory se záznamem chyb
    public Result loggy_remove_all_bugs() {
        logger.debug("Trying to remove all bugs");
        Loggy.remove_all_errors();

        return redirect("/public/bugs");
    }

// ADMIN ###############################################################################################################

    @Security.Authenticated(Secured_Admin.class)
    public Result show_web_socket_stats() {

        List<WebSCType> homers = new ArrayList<>(WebSocketController.incomingConnections_homers.values());

        List<WS_Grid_Terminal>  grids                   = new ArrayList<>(WebSocketController.incomingConnections_terminals.values()).stream().map(o -> (WS_Grid_Terminal) o).collect(Collectors.toList());
        List<WS_Becki_Website>  becki_terminals         = new ArrayList<>(WebSocketController.becki_website.values()).stream().map(o -> (WS_Becki_Website) o).collect(Collectors.toList());
        List<WS_BlockoServer>   blocko_cloud_servers    = new ArrayList<>(WebSocketController.blocko_servers.values()).stream().map(o -> (WS_BlockoServer) o).collect(Collectors.toList());
        List<WS_CompilerServer> compilation_servers     = new ArrayList<>(WebSocketController.compiler_cloud_servers.values()).stream().map(o -> (WS_CompilerServer) o).collect(Collectors.toList());

        Html content =   websocket.render(homers, grids, becki_terminals, blocko_cloud_servers , compilation_servers);
        return return_page(content);
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result  show_instance_detail(String instance_id) {

        if(!WebSocketController.incomingConnections_homers.containsKey(instance_id)) return show_web_socket_stats();

        WS_Homer_Cloud homers = (WS_Homer_Cloud) WebSocketController.incomingConnections_homers.get(instance_id);

        Html content = instance_detail.render(homers);
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
    public Result bootloader_management(String type_of_board_id){
        try {

            TypeOfBoard type_of_board = TypeOfBoard.find.byId(type_of_board_id);

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

            Person person;
            if(user_email != null && user_email.length() < 1 ){

                person = Person.find.where().eq("mail", user_email).findUnique();
                if(person == null) person = SecurityController.getPerson();

            }else {
                person = SecurityController.getPerson();
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

            SecurityRole role_object = SecurityRole.find.byId(role_id);

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
            e.printStackTrace();
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result public_code(){
        try {
            System.err.println("Snažím se vytisknout public code");

            Html public_code_content = public_code.render();
            return return_page(public_code_content);

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

