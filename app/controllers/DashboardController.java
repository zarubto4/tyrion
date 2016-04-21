package controllers;

import models.compiler.Board;
import models.compiler.FileRecord;
import models.compiler.Producer;
import models.compiler.TypeOfBoard;
import models.grid.Screen_Size_Type;
import models.notification.Notification;
import models.overflow.Post;
import models.person.Person;
import models.project.b_program.B_Program;
import models.project.c_program.C_Program;
import models.project.global.Homer;
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
import utilities.loggy.Loggy;
import utilities.webSocket.developing.WebSCType;
import views.html.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DashboardController extends Controller {

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    Integer connectedHomers          =  WebSocketController_Incoming.incomingConnections_homers.size();
    Integer connectedTerminals       =  WebSocketController_Incoming.incomingConnections_terminals.size();
    Integer connectedCloud_servers   =  WebSocketController_Incoming.cloud_servers.size();
    Integer reported_bugs            =  Loggy.number_of_reported_errors();
    Boolean server_mode              =  Server.server_mode;
    String  server_version           =  Server.server_version;

    List<WebSCType> homers = new ArrayList<>(WebSocketController_Incoming.incomingConnections_homers.values());
    List<WebSCType> grids = new ArrayList<>( WebSocketController_Incoming.incomingConnections_terminals.values());

// Index (úvod) ########################################################################################################

    // Úvodní zobrazení Dashboard
    public Result index() {

        logger.info("Creating index.html content");

        Html menu_html = menu.render(reported_bugs,connectedHomers,connectedTerminals,connectedCloud_servers);

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
                M_Project.find.findRowCount()
        );

        logger.info("Return html content");

        return ok( main.render(menu_html,
                content_html,
                server_mode,
                server_version));

    }

// README ###############################################################################################################

    // Zobrazení readme podle MarkDown
    public Result show_readme() throws IOException {

        logger.debug("Creating show_readme.html content");

        String text = "";
        for(String line : Files.readAllLines(Paths.get("README"), StandardCharsets.UTF_8) ) text += line + "\n";

        Html menu_html   = menu.render(reported_bugs,connectedHomers,connectedTerminals,connectedCloud_servers);
        Html readme_html = readme.render( new Html( new PegDownProcessor().markdownToHtml(text) ));

        logger.debug("Return show_readme.html content");

        return ok( main.render(menu_html,
                readme_html ,
                server_mode,
                server_version));
    }


// WEBSOCKET STATS ######################################################################################################

    // Zobrazení seznamů připojených zařízení
    public Result show_web_socket_stats() {

        logger.debug("Return show_web_socket_stats.html content");

        Html menu_html = menu.render(reported_bugs,connectedHomers,connectedTerminals,connectedCloud_servers);

        return ok( main.render(menu_html,
                websocket.render(homers, grids),
                server_mode,
                server_version));
    }

    // Odpojí všechny připojené homery
    public Result disconnect_homer_all(){
        logger.debug("Trying to disconnect all homers");

        return TODO;
    }

    // Odpojí všechny připojené terminály
    public Result disconnect_terminal_all(){
        logger.debug("Trying to disconnect all terminals");
        return TODO;
    }

    // Odpojí konkrétní  terminál podle ID
    public Result disconnect_terminal(String terminal_id){
        logger.debug("Trying to disconnect terminal: " + terminal_id);
        return TODO;
    }

    // Odpojí konkrétní  homer podle ID
    public Result disconnect_homer(String homer_id){
        logger.debug("Trying to disconnect homer: " + homer_id);
        return TODO;
    }


// LOGGY ###############################################################################################################

    // Vykreslí šablonu s bugy
    public Result show_all_logs() {

        logger.debug("Trying to render loggy.html content");

        Html menu_html = menu.render(reported_bugs,connectedHomers,connectedTerminals,connectedCloud_servers);

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
