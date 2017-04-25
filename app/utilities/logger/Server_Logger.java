package utilities.logger;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import controllers.Controller_Dashboard;
import controllers.Controller_Security;
import models.Model_LoggyError;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Play;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;
import utilities.Server;
import utilities.logger.helps_objects.Interface_Server_Logger;
import utilities.response.GlobalResult;
import views.html.loggy;
import views.html.super_general.main;

import java.io.File;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class Server_Logger extends Controller {

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    @Inject
    static WSClient wsClient; // používat přes getWSClient()

    static String token = "";       // token na youtrack
    static long tokenExpire = 0;    // kdy expiruje token na youtrack

    private static Interface_Server_Logger logger;                      // Vlastní Loggy objekt definovaný konfigurací


    public static void set_Logger(){

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();

            // Production Mode
            if (Configuration.root().getString("Server.mode").equals("production")) {

                logger = new Server_Logger_Production();
                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Logback.productionSettings")));
                return;
            }

            // Developer Mode
            else if(Configuration.root().getString("Server.mode").equals("developer") || Configuration.root().getString("Server.mode").equals("stage") ){

                logger = new Server_Logger_Developer();
                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Logback.developerSettings")));
                return;
            }

            System.err.println("ERROR:: Server.mode not found in configuration file!!!");
            System.exit(0);

        } catch (JoranException je) {}
    }


/* CLASS LOGGER --------------------------------------------------------------------------------------------------------*/

    public static void trace(Class<?> t_class, String log_message) {
        logger.trace(t_class, log_message);
    }
    public static void trace(Class<?> t_class, String log_message, Object... args ) { logger.trace(t_class, log_message, args);}

    public static void info(Class<?> t_class, String log_message) {logger.info(t_class, log_message);}
    public static void info(Class<?> t_class, String log_message, Object... args) {logger.info(t_class, log_message, args);}

    public static void debug(Class<?> t_class, String log_message) {logger.debug(t_class, log_message);}
    public static void debug(Class<?> t_class, String log_message, Object... args ) {logger.debug(t_class, log_message, args);}

    public static void warn(Class<?> t_class, String log_message) {
        logger.warn(t_class, log_message);
    }
    public static void warn(Class<?> t_class, String log_message, Object... args ) {logger.warn(t_class, log_message, args);}

    public static void error(Class<?> t_class, String log_message) {
        error(t_class, log_message, null);
        logger.error(t_class, log_message);
    }
    public static void error(Class<?> t_class, String log_message, Object... args ) {

        String id = UUID.randomUUID().toString();

        StringBuilder description = new StringBuilder();     // stavění obsahu

        // TODO tady nic nedělám s Object... args ???
        String summary = "Internal Server Error - " + log_message;

        description.append("\n");
        description.append("    Time: " + new Date().toString());
        description.append("\n");
        description.append("    Unique Identifier: " + id);
        description.append("\n");
        description.append("    Tyrion version: " + Server.server_version);
        description.append("\n");
        description.append("    Tyrion mode: " + Server.server_mode.name());
        description.append("\n");

        error(id, summary, description.toString());
        logger.error(t_class, log_message, args);
    }

    public static void internalServerError(Class<?> t_class, String origin, Exception exception){

        String id = UUID.randomUUID().toString();

        StringBuilder description = new StringBuilder();     // stavění obsahu

        String summary = "Internal Server Error - " + origin + " - " + exception.getClass().getName();

        description.append("\n");
        description.append("    Exception type: " + exception.getClass().getName());
        description.append("\n");
        description.append("    Time: " + new Date().toString());
        description.append("\n");
        description.append("    Unique Identifier: " + id);
        description.append("\n");
        description.append("    Tyrion version: " + Server.server_version);
        description.append("\n");
        description.append("    Tyrion mode: " + Server.server_mode.name());
        description.append("\n");

        description.append("    Stack trace: \n");
        for (StackTraceElement element : exception.getStackTrace()) {    // formátování stack trace
            description.append("        ");
            description.append(element);
            description.append("\n");
        }
        description.append("\n");    // random whitespace

        error(id, summary, description.toString());
        logger.error(t_class, description.toString());
    }

/* CONTROLLER LOGGER ---------------------------------------------------------------------------------------------------*/

    public static Result result_internalServerError(Exception exception, Http.Request request) {

        String id;

        while (true) { // I need Unique Value
            id = UUID.randomUUID().toString();
            if (Model_LoggyError.find.byId(id) == null) break;
        }


        StringBuilder description = new StringBuilder();     // stavění obsahu

        String summary = "Internal Server Error - " + exception.getClass().getName() + " - " + request.method() + " " + request.path();

        description.append("\n");
        description.append("    Exception type: " + exception.getClass().getName());
        description.append("\n");
        description.append("    Exception message: " +exception.getMessage());
        description.append("\n");
        description.append("    Time: " + new Date().toString());
        description.append("\n");
        description.append("    Request Type: " + request.method());
        description.append("\n");
        description.append("    Request Path: " + request.path());
        description.append("\n");
        description.append("    Unique Identifier: " + id);
        description.append("\n");
        description.append("    Tyrion version: " + Server.server_version);
        description.append("\n");
        description.append("    Tyrion mode: " + Server.server_mode.name());
        description.append("\n");
        description.append("    User: " + (Controller_Security.get_person() != null ? Controller_Security.get_person().mail : "null"));
        description.append("\n");

        description.append("    Stack trace: \n");
        for (StackTraceElement element : exception.getStackTrace()) {    // formátování stack trace
            description.append("        ");
            description.append(element);
            description.append("\n");
        }
        description.append("\n");

        error(id, summary, description.toString());

        return GlobalResult.result_InternalServerError(summary + "\n" + exception.getMessage());
    }


/* CONTROLLER OERATION ---------------------------------------------------------------------------------------------------*/


    // Vykreslí šablonu s bugy
    public Result show_all_logs() {
        Html content =  loggy.render( Server_Logger.getErrors() );
        return ok(  main.render(content) );
    }

    // Nahraje konkrétní bug na Youtrack
    public F.Promise<Result> loggy_report_bug_to_youtrack(String bug_id) {


        F.Promise<Result> p = Server_Logger.upload_to_youtrack(bug_id);
        return p.map((result) -> redirect("/admin/bugs"));
    }

    // Odstraní konkrétní bug ze seznamu (souboru)
    public Result loggy_remove_bug(String bug_id) {

        Server_Logger.remove_error(bug_id);
        return redirect("/admin/bugs");
    }

    // Vyprázdní soubory se záznamem chyb
    public Result loggy_remove_all_bugs() {

        Server_Logger.remove_all_errors();

        return redirect("/admin/bugs");
    }


/* SERVICES ---------------------------------------------------------------------------------------------------*/

    private static void error(String id, String summary, String description) {

        Model_LoggyError error = new Model_LoggyError(id, summary, description); // zapíšu do databáze
        error.save();

    }

    // Vracím počet zaznamenaných bugů v souboru
    public static Integer number_of_reported_errors(){
        return Model_LoggyError.find.findRowCount();
    }

    // Vymažu bug z databáze
    public static void remove_error(String id){
        Model_LoggyError.find.byId(id).delete();
    }

    // Vymažu všechny bugy z databáze
    public static void remove_all_errors(){
        Ebean.delete(Model_LoggyError.find.all());
    }

    // Vymažu všechny bugy ze souboru
    public static void clear_file(){
        File all = Play.application().getFile("logs/all.log");

        try {
            new PrintWriter(all).close(); // vymaže obsah souboru
        } catch (Exception e) {}
    }

    public static F.Promise<Result> upload_to_youtrack(String id) {
        if (System.currentTimeMillis() > tokenExpire-10000) { // pokud nemám platný token, získám ho a metodu spustím znovu
            return youtrack_login().flatMap((result) -> upload_to_youtrack(id));
        }
        Model_LoggyError e = getError(id);
        if (e == null) {
            return F.Promise.promise(Results::badRequest);
        }
        // sestavím request na nahrání
        WSRequest request = getWSClient().url(Configuration.root().getString("Loggy.youtrackUrl") + "/youtrack/rest/issue");
        request.setQueryParameter("project", Configuration.root().getString("Loggy.youtrackProject"));
        request.setQueryParameter("summary", e.summary);
        request.setQueryParameter("description", e.description);
        request.setHeader("Authorization", "Bearer "+token);
        F.Promise<WSResponse> promise = request.put("");
        return promise.map(response -> youtrack_checkUploadResponse(response, e)); // zpracuje odpověď a zapíše url do erroru
    }

    public static List<Model_LoggyError> getErrors(Integer a){
        return Model_LoggyError.find.setMaxRows(a).findList();
    }

    public static List<Model_LoggyError> getErrors(){
        return Model_LoggyError.find.all();
    }

    public static Model_LoggyError getError(String id) {
        return Model_LoggyError.find.byId(id);
    }

    private static F.Promise<Result> youtrack_login() {
        WSRequest request = getWSClient().url(Configuration.root().getString("Loggy.youtrackUrl") + "/hub/rest/oauth2/token");
        request.setContentType("application/x-www-form-urlencoded");
        request.setHeader("Authorization",
                "Basic "+ new String(Base64.getEncoder().encode(    // zakódování přihlašovacích údajů
                        (Configuration.root().getString("Loggy.youtrackId") + ":" + Configuration.root().getString("Loggy.youtrackSecret"))
                                .getBytes())));
        F.Promise<WSResponse> promise = request.post(               // odešlu request na token
                "grant_type=password"
                        +"&scope="+Configuration.root().getString("Loggy.youtrackScopeId")
                        +"&username="+Configuration.root().getString("Loggy.youtrackUsername")
                        +"&password="+Configuration.root().getString("Loggy.youtrackPassword")
        );

        return promise.map(Server_Logger::youtrack_checkLoginResponse); // zpracuje odpověď od youtracku
    }

    private static Result youtrack_checkLoginResponse(WSResponse response) {
        if (response.getStatus() == 200) {  // pokud úspěšné, uložím token a jeho expiraci
            JsonNode content = response.asJson();
            token = content.get("access_token").asText();
            tokenExpire = System.currentTimeMillis() + content.get("expires_in").asLong()*1000;
            return GlobalResult.result_ok("login successful");
        }

        return Results.status(response.getStatus(), response.getBody());
    }

    private static Result youtrack_checkUploadResponse(WSResponse response, Model_LoggyError error) {
        if (response.getStatus() == 201) {
            error.setYoutrack_url(response.getHeader("Location").replace("/rest", "")); // uložím url z odpovědi
            error.save();

            logger.debug( Server_Logger.class , error.youtrack_url +  "---" + Model_LoggyError.find.byId(error.id).youtrack_url);

            return GlobalResult.result_ok("upload successful");
        }

        return Results.status(response.getStatus(), response.getBody());
    }

    private static WSClient getWSClient() {
        if(wsClient == null) { // pokud je wsClient null, vytvorím ho
            wsClient = Play.application().injector().instanceOf(WSClient.class);
        }
        return wsClient;
    }
}
