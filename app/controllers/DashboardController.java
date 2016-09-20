package controllers;

import models.compiler.Cloud_Compilation_Server;
import models.person.Person;
import models.person.SecurityRole;
import models.project.b_program.servers.Cloud_Homer_Server;
import org.bouncycastle.asn1.x509.sigi.PersonalData;
import org.pegdown.PegDownProcessor;
import play.Application;
import play.Routes;
import play.libs.F;
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
import views.html.super_general.main;
import views.html.super_general.menu;
import views.html.super_general.login;
import views.html.permission.permissions_summary;
import views.html.permission.role;
import views.html.user_summary.*;
import views.html.websocket.instance_detail;
import views.html.websocket.websocket;

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
 * ovládání websocketu a čtení readme. Dále podpora pro porovnávání změn nad dokumentací ze Swaggeru.
 * */
public class DashboardController extends Controller {

    @Inject Application application;

    // Logger pro zaznamenávání chyb
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    Integer connectedHomers          =  WebSocketController_Incoming.incomingConnections_homers.size();
    Integer connectedTerminals       =  WebSocketController_Incoming.incomingConnections_terminals.size();
    Integer connectedBecki           =  WebSocketController_Incoming.becki_website.size();
    Integer connectedBlocko_servers  =  WebSocketController_Incoming.blocko_servers.size();
    Integer connectedCompile_servers =  WebSocketController_Incoming.compiler_cloud_servers.size();
    Integer reported_bugs            =  Loggy.number_of_reported_errors();
    Boolean server_mode              =  Server.server_mode;
    String  server_version           =  Server.server_version;
    String  link_api_swagger         =  "http://swagger.byzance.cz/?url="+ Server.tyrion_serverAddress +"/api-docs";


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
                        controllers.routes.javascript.CompilationLibrariesController.delete_Producer()


                )
        );
    }

    public Result return_page( Html content){

      //  List<String> fileNames = new ArrayList<>();
       // File[] files = new File(application.path() + "/conf/swagger_history").listFiles();

     //   for (File file : files) { fileNames.add((file.getName().substring(0, file.getName().lastIndexOf('.'))).replace("_", "."));}

        Html menu_html = menu.render(reported_bugs, connectedHomers, connectedBecki, connectedTerminals, connectedBlocko_servers, connectedCompile_servers, link_api_swagger);

        return ok( main.render(menu_html,
                content,
                server_mode
                )
        );
    }

// Index (úvod) ########################################################################################################

    // Úvodní zobrazení Dashboard

    @Security.Authenticated(Secured_Admin.class)
    public Result index() {

        Map<String, WS_BlockoServer> blockoServerMap = new HashMap<>();

        Map<String, WebSCType> map_blocko =  WebSocketController_Incoming.blocko_servers;
        for (Map.Entry<String, WebSCType> entry : map_blocko.entrySet()) blockoServerMap.put(entry.getKey(), (WS_BlockoServer) entry.getValue());

        Map<String, WS_CompilerServer> compilerServerMap = new HashMap<>();
        Map<String, WebSCType> map_compile =  WebSocketController_Incoming.compiler_cloud_servers;
        for (Map.Entry<String, WebSCType> entry : map_compile.entrySet()) compilerServerMap.put(entry.getKey(), (WS_CompilerServer) entry.getValue());

        Html content_html = dashboard.render(
                blockoServerMap,
                compilerServerMap,
                Cloud_Homer_Server.find.all(),
                Cloud_Compilation_Server.find.all()
        );

        return return_page(content_html);
    }

// README ###############################################################################################################

    // Zobrazení readme podle MarkDown
    public Result show_readme() throws IOException {

        logger.debug("Creating show_readme.html content");

        String text = "";
        for(String line : Files.readAllLines(Paths.get("README"), StandardCharsets.UTF_8) ) text += line + "\n";

        Html readme_html = readme.render( new Html( new PegDownProcessor().markdownToHtml(text) ));

        logger.debug("Return show_readme.html content");

        return return_page(readme_html);
    }

// API DIFF ###############################################################################################################

    // Zobrazení rozdílu mezi verzemi
    public Result show_diff_on_Api(String file_name_old, String file_name_new) throws IOException, NullPointerException {
        try {

            logger.debug("show_diff_on_Api diff_html content");

            List<String> fileNames = new ArrayList<>();
            File[] files = new File(application.path() + "/conf/swagger_history").listFiles();
            for (File file : files) {fileNames.add((file.getName().substring(0, file.getName().lastIndexOf('.'))).replace("_", "."));}

            if(file_name_old.equals("")) file_name_old = fileNames.get( ( fileNames.size()-2) ) ;
            if(file_name_new.equals("")) file_name_new = fileNames.get( ( fileNames.size()-1) ) ;


            Swagger_Diff swagger_diff = Swagger_diff_Controller.set_API_Changes(file_name_old, file_name_new);
            Html content = Api_Div.render(swagger_diff, link_api_swagger, fileNames);

            logger.debug("Return Api_Div.html content");
            return return_page(content);

        }catch (Exception e){
            return ok("Došlo k chybě");
        }
    }


// WEBSOCKET STATS ######################################################################################################

    public Result disconnect_homer_all(){
        logger.debug("Trying to disconnect all homers");

        for (Map.Entry<String, WebSCType> entry :    WebSocketController_Incoming.incomingConnections_homers.entrySet()) {
            entry.getValue().onClose();
        }
        return GlobalResult.result_ok();
    }

    public Result disconnect_terminal_all(){
        logger.debug("Trying to disconnect all terminals");

        for (Map.Entry<String, WebSCType> entry :    WebSocketController_Incoming.incomingConnections_terminals.entrySet()) {
            entry.getValue().onClose();
        }

        return GlobalResult.result_ok();
    }

    public Result disconnect_terminal(String terminal_id){
        if(WebSocketController_Incoming.incomingConnections_terminals.containsKey(terminal_id) ) WebSocketController_Incoming.incomingConnections_terminals.get(terminal_id).onClose();
        return GlobalResult.result_ok();
    }

    public Result disconnect_becki(String person_id, String token){
        if(WebSocketController_Incoming.becki_website.containsKey(person_id)){
            WS_Becki_Website website = (WS_Becki_Website) WebSocketController_Incoming.becki_website.get(person_id);
            if( website.all_person_Connections.containsKey(token))website.all_person_Connections.get(token).onClose();
        }
        return GlobalResult.result_ok();
    }

    public Result disconnect_homer(String homer_id){
        if(WebSocketController_Incoming.incomingConnections_homers.containsKey(homer_id))   WebSocketController_Incoming.incomingConnections_homers.get(homer_id).onClose();
        return GlobalResult.result_ok();
    }

    public Result disconnect_Blocko_Server(String identificator){
        if(WebSocketController_Incoming.blocko_servers.containsKey(identificator)) WebSocketController_Incoming.blocko_servers.get(identificator).onClose();

        return GlobalResult.result_ok();
    }

    public Result disconnect_Compilation_Server(String identificator){
        if(WebSocketController_Incoming.compiler_cloud_servers.containsKey(identificator)) WebSocketController_Incoming.compiler_cloud_servers.get(identificator).onClose();

        return GlobalResult.result_ok();
    }

    public Result ping_terminal(String terminal_id) throws TimeoutException, InterruptedException {

        WebSocketController_Incoming.terminal_ping( WebSocketController_Incoming.incomingConnections_terminals.get(terminal_id) ) ;
        return GlobalResult.result_ok();
    }

    public Result ping_becki(String person_id) throws TimeoutException, InterruptedException {

       if(WebSocketController_Incoming.becki_website.containsKey(person_id))  WebSocketController_Incoming.becki_ping( WebSocketController_Incoming.becki_website.get(person_id) );
        return GlobalResult.result_ok();
    }

    public Result ping_homer(String homer_id) throws TimeoutException, InterruptedException, ExecutionException {

        if(WebSocketController_Incoming.incomingConnections_homers.containsKey(homer_id))
        WebSocketController_Incoming.homer_ping( (WebSocketController_Incoming.incomingConnections_homers.get(homer_id)));
        return GlobalResult.result_ok();
    }

    public Result ping_Blocko_Server(String identificator) throws TimeoutException, InterruptedException {
        if(WebSocketController_Incoming.blocko_servers.containsKey(identificator))
        WebSocketController_Incoming.blocko_server_ping( (WS_BlockoServer) WebSocketController_Incoming.blocko_servers.get(identificator) );
        return GlobalResult.result_ok();
    }

    public Result ping_Compilation_Server(String identificator) throws TimeoutException, InterruptedException, ExecutionException {
        if(WebSocketController_Incoming.compiler_cloud_servers.containsKey(identificator))
        WebSocketController_Incoming.compiler_server_ping( (WS_CompilerServer) WebSocketController_Incoming.compiler_cloud_servers.get(identificator) );
        return GlobalResult.result_ok();
    }

    public Result log_out_Terminal_User(String identificator) {
        System.out.println("Ještě neimplementováno");
        return GlobalResult.result_ok();
    }


    public Result send_commnad_to_instnace(String intance_id, String json_command){
        return TODO;
    }

    public Result uploud_blocko_program_to_instance(String intance_id, String json_program){
        return TODO;
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

        List<WebSCType> homers = new ArrayList<>(WebSocketController_Incoming.incomingConnections_homers.values());

        List<WS_Grid_Terminal>  grids                   = new ArrayList<>(WebSocketController_Incoming.incomingConnections_terminals.values()).stream().map(o -> (WS_Grid_Terminal) o).collect(Collectors.toList());
        List<WS_Becki_Website>  becki_terminals         = new ArrayList<>(WebSocketController_Incoming.becki_website.values()).stream().map(o -> (WS_Becki_Website) o).collect(Collectors.toList());
        List<WS_BlockoServer>   blocko_cloud_servers    = new ArrayList<>(WebSocketController_Incoming.blocko_servers.values()).stream().map(o -> (WS_BlockoServer) o).collect(Collectors.toList());
        List<WS_CompilerServer> compilation_servers     = new ArrayList<>(WebSocketController_Incoming.compiler_cloud_servers.values()).stream().map(o -> (WS_CompilerServer) o).collect(Collectors.toList());

        Html content =   websocket.render(homers, grids, becki_terminals, blocko_cloud_servers , compilation_servers);
        return return_page(content);
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result  show_instance_detail(String instance_id) {

        if(!WebSocketController_Incoming.incomingConnections_homers.containsKey(instance_id)) return show_web_socket_stats();

        WS_Homer_Cloud homers = (WS_Homer_Cloud) WebSocketController_Incoming.incomingConnections_homers.get(instance_id);

        Html content = instance_detail.render(homers);
        return return_page(content);
    }




    @Security.Authenticated(Secured_Admin.class)
    public Result basic_object_management(){
        try {

            Html content = basic_objects.render();
            return return_page ( content );

        }catch (Exception e){
            e.printStackTrace();
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result external_servers(){
        try {

            Html external_servers_content = external_servers.render();
            return return_page(external_servers_content);

        }catch (Exception e){
            e.printStackTrace();
            return ok();
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
            e.printStackTrace();
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result permissions_summary(){
        try {

            Html permissions_content = permissions_summary.render();
            return return_page(permissions_content);

        }catch (Exception e){
            e.printStackTrace();
            return ok();
        }
    }


    @Security.Authenticated(Secured_Admin.class)
    public Result role(String role_id){
        try {

            SecurityRole role_object = SecurityRole.find.byId(role_id);

            Html permissions_content = role.render(role_object);
            return return_page(permissions_content);

        }catch (Exception e){
            e.printStackTrace();
            return ok();
        }
    }

    @Security.Authenticated(Secured_Admin.class)
    public Result blocko_objects(){
        try {

            Html blocko_objects_content = blocko_objects.render();
            return return_page(blocko_objects_content);

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
            return GlobalResult.internalServerError();
        }
    }

}

