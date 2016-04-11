package controllers;

import models.compiler.Board;
import models.compiler.FileRecord;
import models.compiler.Producer;
import models.compiler.TypeOfBoard;
import models.grid.Screen_Size_Type;
import models.notification.Notification;
import models.overflow.Post;
import models.persons.Person;
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

    Integer connectedHomers          =  WebSocketController_Incoming.incomingConnections_homers.size();
    Integer connectedTerminals       =  WebSocketController_Incoming.incomingConnections_terminals.size();
    Integer connectedCloud_servers   =  WebSocketController_Incoming.cloud_servers.size();
    Integer reported_bugs            =  15; // TODO Tomáš K. - doplnit
    Html menu_html                   =  menu.render(reported_bugs,connectedHomers,connectedTerminals,connectedCloud_servers);
    Boolean server_mode              =  Server.server_mode;
    String  server_version           =  Server.server_version;

    List<WebSCType> homers = new ArrayList<>(WebSocketController_Incoming.incomingConnections_homers.values());
    List<WebSCType> grids = new ArrayList<>( WebSocketController_Incoming.incomingConnections_terminals.values());


    public Result index() {

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

        return ok( main.render(menu_html,
                content_html,
                server_mode,
                server_version));

    }

    public Result show_all_logs() {

        return ok( main.render(menu_html,
                loggy.render( Loggy.getErrors(25) ), // TODO Tomáš K. doplnit - seznam
                server_mode,
                server_version));
    }

    public Result show_websocket_stats() {

        return ok( main.render(menu_html,
                websocket.render(homers, grids),  // TODO Tomáš Z. - Dodělat zobrazení pro Blocko servery
                server_mode,
                server_version));
    }

    public Result show_readme() throws IOException {

       // Scanner scanner = new Scanner( new File("files/Test") );
        String text = "";
        for(String line : Files.readAllLines(Paths.get("README"), StandardCharsets.UTF_8) ) text += line + "\n";

        Html file = new Html( new PegDownProcessor().markdownToHtml(text) );

        Html readme_html =readme.render(file);

        return ok( main.render(menu_html,
                readme_html ,
                server_mode,
                server_version));

    }


    public F.Promise<Result> upload(int id) {
        return Loggy.upload(id);
    }

    public Result deleteAll() {
        Loggy.deleteFast();
        Loggy.deleteFile();
        return redirect("/loggy");
    }

    public Result error(String description) {
        Loggy.error(description);
        return redirect("/loggy");
    }

    public Result error(String summary, String description) {
        Loggy.error(summary, description);
        return redirect("/loggy");
    }

    public F.Promise<Result> login () {
        return Loggy.login();
    }

}
