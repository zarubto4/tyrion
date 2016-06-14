package controllers;

import models.blocko.Cloud_Blocko_Server;
import models.compiler.*;
import models.grid.Screen_Size_Type;
import models.notification.Notification;
import models.overflow.Post;
import models.person.Person;
import models.project.b_program.B_Program;
import models.project.b_program.Homer;
import models.project.c_program.C_Program;
import models.project.global.Project;
import models.project.m_program.Grid_Terminal;
import models.project.m_program.M_Program;
import models.project.m_program.M_Project;
import org.pegdown.PegDownProcessor;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import utilities.Server;
import utilities.UtilTools;
import utilities.loggy.Loggy;
import utilities.swagger.swagger_diff_tools.servise_class.Swagger_Diff;
import utilities.webSocket.*;
import views.html.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * CONTROLLER je určen pro jednoduchý frontend, který slouží pro zobrazení stavu backendu, základních informací,
 * ovládání websocketu a čtení readme. Dále podpora pro porovnávání změn nad dokumentací ze Swaggeru.
 * */
public class DashboardController extends Controller {

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

    // Úvodní zobrazení Dashboard
    public Result index() {

        Html menu_html = menu.render(reported_bugs, connectedHomers, connectedBecki, connectedTerminals, connectedBlocko_servers, connectedCompile_servers, link_api_swagger);

        Map<String, WS_BlockoServer> blockoServerMap = new HashMap<>();

        Map<String, WebSCType> map_blocko =  WebSocketController_Incoming.blocko_servers;
        for (Map.Entry<String, WebSCType> entry : map_blocko.entrySet()) blockoServerMap.put(entry.getKey(), (WS_BlockoServer) entry.getValue());

        Map<String, WS_CompilerServer> compilerServerMap = new HashMap<>();

        Map<String, WebSCType> map_compile =  WebSocketController_Incoming.compiler_cloud_servers;
        for (Map.Entry<String, WebSCType> entry : map_compile.entrySet()) compilerServerMap.put(entry.getKey(), (WS_CompilerServer) entry.getValue());

        Html content_html = dashboard.render(
                Person.find.findRowCount(),
                Project.find.findRowCount(),
                Homer.find.findRowCount(),
                B_Program.find.findRowCount(),
                Board.find.findRowCount(),
                FileRecord.find.findRowCount(),
                Producer.find.findRowCount(),
                TypeOfBoard.find.findRowCount(),
                Screen_Size_Type.find.findRowCount(),
                Notification.find.findRowCount(),
                Post.find.findRowCount(),
                C_Program.find.findRowCount(),
                Grid_Terminal.find.findRowCount(),
                M_Program.find.findRowCount(),
                M_Project.find.findRowCount(),
                blockoServerMap,
                Cloud_Blocko_Server.find.all(),
                compilerServerMap,
                Cloud_Compilation_Server.find.all()
        );

        return ok( main.render(menu_html, content_html, server_mode, server_version));
    }

// README ###############################################################################################################

    // Zobrazení readme podle MarkDown
    public Result show_readme() throws IOException {

        logger.debug("Creating show_readme.html content");

        String text = "";
        for(String line : Files.readAllLines(Paths.get("README"), StandardCharsets.UTF_8) ) text += line + "\n";

        Html menu_html   = menu.render(reported_bugs,connectedHomers, connectedBecki, connectedTerminals, connectedBlocko_servers,connectedCompile_servers, link_api_swagger);
        Html readme_html = readme.render( new Html( new PegDownProcessor().markdownToHtml(text) ));

        logger.debug("Return show_readme.html content");

        return ok( main.render(menu_html,
                readme_html ,
                server_mode,
                server_version));
    }

// API DIFF ###############################################################################################################

    // Zobrazení readme podle MarkDown
    public Result show_diff_on_Api() throws IOException, NullPointerException {
        try {
            logger.debug("show_diff_on_Api diff_html content");

            String text = "";
            for (String line : Files.readAllLines(Paths.get("README"), StandardCharsets.UTF_8)) text += line + "\n";

            Html menu_html = menu.render(reported_bugs, connectedHomers, connectedBecki, connectedTerminals, connectedBlocko_servers, connectedCompile_servers, link_api_swagger);
            Swagger_Diff swagger_diff = UtilTools.set_API_Changes();
            Html content = Api_Div.render(swagger_diff, link_api_swagger);
            logger.debug("Return show_readme.html content");

            return ok(main.render(menu_html,
                    content,
                    server_mode,
                    server_version));
        }catch (Exception e){
            return ok("Došlo k chybě");
        }
    }


// WEBSOCKET STATS ######################################################################################################


    public Result show_web_socket_stats() {

        logger.debug("Return show_web_socket_stats.html content");

        Html menu_html = menu.render(reported_bugs,connectedHomers, connectedBecki, connectedTerminals, connectedBlocko_servers, connectedCompile_servers, link_api_swagger);


        List<WebSCType> homers = new ArrayList<>(WebSocketController_Incoming.incomingConnections_homers.values());

        List<WS_Grid_Terminal> grids = new ArrayList<>();
        for(WebSCType o  : new ArrayList<>(WebSocketController_Incoming.incomingConnections_terminals.values()) ) grids.add((WS_Grid_Terminal) o);


        List<WS_Becki_Website> becki_terminals = new ArrayList<>();
        for(WebSCType o  : new ArrayList<>(WebSocketController_Incoming.becki_website.values()) ) becki_terminals.add((WS_Becki_Website) o);


        List<WS_BlockoServer> blocko_cloud_servers = new ArrayList<>();
        for(WebSCType o  : new ArrayList<>(WebSocketController_Incoming.blocko_servers.values()) ) blocko_cloud_servers.add( (WS_BlockoServer) o);


        List<WS_CompilerServer> compilation_servers = new ArrayList<>();
        for(WebSCType o  : new ArrayList<>(WebSocketController_Incoming.compiler_cloud_servers.values()) ) compilation_servers.add( (WS_CompilerServer) o);



        return ok( main.render(menu_html,
                websocket.render(homers, grids, becki_terminals, blocko_cloud_servers , compilation_servers),
                server_mode,
                server_version));
    }

    public Result disconnect_homer_all(){
        logger.debug("Trying to disconnect all homers");

        for (Map.Entry<String, WebSCType> entry :    WebSocketController_Incoming.incomingConnections_homers.entrySet()) {
            entry.getValue().onClose();
        }
        return show_web_socket_stats();
    }

    public Result disconnect_terminal_all(){
        logger.debug("Trying to disconnect all terminals");

        for (Map.Entry<String, WebSCType> entry :    WebSocketController_Incoming.incomingConnections_terminals.entrySet()) {
            entry.getValue().onClose();
        }

        return show_web_socket_stats();
    }

    public Result disconnect_terminal(String terminal_id){
        if(WebSocketController_Incoming.incomingConnections_terminals.containsKey(terminal_id) ) WebSocketController_Incoming.incomingConnections_terminals.get(terminal_id).onClose();
        return show_web_socket_stats();
    }

    public Result disconnect_becki(String person_id, String token){
        if(WebSocketController_Incoming.becki_website.containsKey(person_id)){
            WS_Becki_Website website = (WS_Becki_Website) WebSocketController_Incoming.becki_website.get(person_id);
            if( website.all_person_Connections.containsKey(token))website.all_person_Connections.get(token).onClose();
        }
        return show_web_socket_stats();
    }

    public Result disconnect_homer(String homer_id){
        if(WebSocketController_Incoming.incomingConnections_homers.containsKey(homer_id))   WebSocketController_Incoming.incomingConnections_homers.get(homer_id).onClose();
        return show_web_socket_stats();
    }

    public Result disconnect_Blocko_Server(String identificator){
        if(WebSocketController_Incoming.blocko_servers.containsKey(identificator)) WebSocketController_Incoming.blocko_servers.get(identificator).onClose();

        return show_web_socket_stats();
    }

    public Result disconnect_Compilation_Server(String identificator){
        if(WebSocketController_Incoming.compiler_cloud_servers.containsKey(identificator)) WebSocketController_Incoming.compiler_cloud_servers.get(identificator).onClose();

        return show_web_socket_stats();
    }

    public Result ping_terminal(String terminal_id) throws TimeoutException, InterruptedException {

        WebSocketController_Incoming.terminal_ping( WebSocketController_Incoming.incomingConnections_terminals.get(terminal_id) ) ;
        return show_web_socket_stats();
    }

    public Result ping_becki(String person_id) throws TimeoutException, InterruptedException {

       if(WebSocketController_Incoming.becki_website.containsKey(person_id))  WebSocketController_Incoming.becki_ping( WebSocketController_Incoming.becki_website.get(person_id) );
       return redirect("/public/websocket");
    }


    public Result ping_homer(String homer_id) throws TimeoutException, InterruptedException {
        if(WebSocketController_Incoming.incomingConnections_homers.containsKey(homer_id))
        WebSocketController_Incoming.homer_ping( homer_id);

        return show_web_socket_stats();
    }

    public Result ping_Blocko_Server(String identificator) throws TimeoutException, InterruptedException {
        if(WebSocketController_Incoming.blocko_servers.containsKey(identificator))
        WebSocketController_Incoming.blocko_server_ping( (WS_BlockoServer) WebSocketController_Incoming.blocko_servers.get(identificator) );
        return show_web_socket_stats();
    }

    public Result ping_Compilation_Server(String identificator)  throws TimeoutException, InterruptedException {
        if(WebSocketController_Incoming.compiler_cloud_servers.containsKey(identificator))
        WebSocketController_Incoming.compiler_server_ping( (WS_CompilerServer) WebSocketController_Incoming.compiler_cloud_servers.get(identificator) );
        return show_web_socket_stats();
    }

    public Result log_out_Terminal_User(String identificator) {
        System.out.println("Ještě neimplementováno");
        return  show_web_socket_stats();
    }

// LOGGY ###############################################################################################################

    // Vykreslí šablonu s bugy
    public Result show_all_logs() {

        logger.debug("Trying to render loggy.html content");

        Html menu_html = menu.render(reported_bugs,connectedHomers, connectedBecki, connectedTerminals, connectedBlocko_servers, connectedCompile_servers, link_api_swagger);

        return ok( main.render(menu_html,
                loggy.render( Loggy.getErrors() ),
                server_mode,
                server_version));
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

}
