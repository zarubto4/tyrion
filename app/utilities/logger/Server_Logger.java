package utilities.logger;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
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
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.logger.helps_objects.Interface_Server_Logger;
import utilities.response.GlobalResult;
import utilities.slack.Slack;

import java.io.File;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class Server_Logger extends Controller {

    static private play.Logger.ALogger default_logger = play.Logger.of("TYRION");

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    @Inject
    private static WSClient wsClient; // používat přes getWSClient()

    private static String token = "";       // token na youtrack
    private static long tokenExpire = 0;    // kdy expiruje token na youtrack

    private static Interface_Server_Logger logger;  // Vlastní Loggy objekt definovaný konfigurací


    public static void set_Logger(){

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {

            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();

            // Production Mode
            if (Configuration.root().getString("Server.mode").equals("production")) {

                System.out.println("Set Production Tyrion Log Mode");
                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Loggy.productionSettings")));

                logger = new Server_Logger_Production();


                return;
            }

            // Developer Mode
            else if(Configuration.root().getString("Server.mode").equals("developer") || Configuration.root().getString("Server.mode").equals("stage") ){

                System.out.println("Set Developer Tyrion Log Mode");
                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Loggy.developerSettings")));

                logger = new Server_Logger_Developer();
                return;
            }

            System.err.println("ERROR:: Server.mode not found in configuration file!!!");
            System.exit(0);

        } catch (JoranException je) {
            je.printStackTrace();
        }
    }


/* CLASS LOGGER --------------------------------------------------------------------------------------------------------*/

    public static void trace(Class<?> t_class, String log_message, Object... args) {logger.trace(t_class, log_message, args);}
    public static void info (Class<?> t_class, String log_message, Object... args) {logger.info (t_class, log_message, args);}
    public static void debug(Class<?> t_class, String log_message, Object... args) {logger.debug(t_class, log_message, args);}
    public static void warn (Class<?> t_class, String log_message, Object... args) {logger.warn (t_class, log_message, args);}
    public static void error(Class<?> t_class, String log_message, Object... args) {logger.error(t_class, log_message, args);}

    public static void internalServerError(Class<?> t_class, String origin, Exception exception){


        String id = UUID.randomUUID().toString();

        StringBuilder description = new StringBuilder();
        StringBuilder stack_trace = new StringBuilder();
        StringBuilder cause_summary = new StringBuilder();

        String summary = "Internal Server Error - " + t_class.getName() + "::" + origin;

        description.append("\n");
        description.append("    Exception type: " + exception.getClass().getName());
        description.append("\n");
        description.append("    Exception message: " + exception.getMessage());
        description.append("\n");
        description.append("    Time: " + new Date().toString());
        description.append("\n");
        description.append("    Unique Identifier: " + id);
        description.append("\n");
        description.append("    Tyrion version: " + Server.server_version);
        description.append("\n");
        description.append("    Tyrion mode: " + Server.server_mode.name());
        description.append("\n");

        stack_trace.append("    Stack trace: \n");
        for (StackTraceElement element : exception.getStackTrace()) {    // formátování stack trace
            stack_trace.append("        ");
            stack_trace.append(element);
            stack_trace.append("\n");
        }
        stack_trace.append("\n");

        // If exception only wraps another one.
        Throwable cause = exception.getCause();
        if (cause != null) {
            cause_summary.append("    Caused by: ");
            cause_summary.append(cause.getClass().getName());
            cause_summary.append("\n");
            cause_summary.append("    Cause message: ");
            cause_summary.append(cause.getMessage());
            cause_summary.append("\n");
            cause_summary.append("    Cause stack trace: ");
            cause_summary.append("\n");

            for (StackTraceElement element : cause.getStackTrace()) {    // formátování stack trace
                cause_summary.append("        ");
                cause_summary.append(element);
                cause_summary.append("\n");
            }
        }

        error(id, summary, description.toString(), stack_trace.toString(), cause == null ? null : cause_summary.toString());
        logger.error(t_class, description.toString() + stack_trace.toString());
    }

/* CONTROLLER LOGGER ---------------------------------------------------------------------------------------------------*/

    public static Result result_internalServerError(Throwable exception, Http.RequestHeader request) {

        exception.printStackTrace();

        String id;

        while (true) { // I need Unique Value
            id = UUID.randomUUID().toString();
            if (Model_LoggyError.find.byId(id) == null) break;
        }

        StringBuilder description = new StringBuilder();     // stavění obsahu
        StringBuilder stack_trace = new StringBuilder();
        StringBuilder cause_summary = new StringBuilder();

        String summary = "Internal Server Error - " + request.method() + " " + request.path();

        description.append("\n");
        description.append("    Exception type: " + exception.getClass().getName());
        description.append("\n");
        description.append("    Exception message: " + exception.getMessage());
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

        stack_trace.append("    Stack trace: \n");
        for (StackTraceElement element : exception.getStackTrace()) {    // formátování stack trace
            stack_trace.append("        ");
            stack_trace.append(element);
            stack_trace.append("\n");
        }
        stack_trace.append("\n");

        // If exception only wraps another one.
        Throwable cause = exception.getCause();
        if (cause != null) {
            cause_summary.append("    Caused by: ");
            cause_summary.append(cause.getClass().getName());
            cause_summary.append("\n");
            cause_summary.append("    Cause message: ");
            cause_summary.append(cause.getMessage());
            cause_summary.append("\n");
            cause_summary.append("    Cause stack trace: ");
            cause_summary.append("\n");

            for (StackTraceElement element : cause.getStackTrace()) {    // formátování stack trace
                cause_summary.append("        ");
                cause_summary.append(element);
                cause_summary.append("\n");
            }
        }

        error(id, summary, description.toString(), stack_trace.toString(), cause == null ? null : cause_summary.toString());

        return GlobalResult.result_internalServerError(summary + "\n" + exception.getMessage());
    }


/* CONTROLLER OERATION -------------------------------------------------------------------------------------------------*/

/* SERVICES ------------------------------------------------------------------------------------------------------------*/

    /**
     * Saves error_message to database or increments "repetition" and sends it to Slack channel #servers if mode is not "developer".
     * @param id Identifier of model error_message
     * @param summary String title of error_message
     * @param description String details of error_message
     */
    private static void error(String id, String summary, String description, String stack_trace, String cause) {

        // Just temporary protection, later every error_message will be unique
        List<Model_LoggyError> errors = Model_LoggyError.find.where().isNotNull("stack_trace").eq("stack_trace", stack_trace).findList();
        if (errors.size() > 1) {
            for (Model_LoggyError e : errors) {
                e.delete();
            }
        }

        Model_LoggyError error = Model_LoggyError.find.where().isNotNull("stack_trace").eq("stack_trace", stack_trace).findUnique();
        if (error == null) {
            error = new Model_LoggyError(id, summary, description, stack_trace, cause); // zapíšu do databáze
            error.save();
        } else {
            error.summary = summary;
            error.description = description;
            error.repetition++;
            error.update();
        }

        if (Server.server_mode != Enum_Tyrion_Server_mode.developer) Slack.post(error);
    }

    // Vracím počet zaznamenaných bugů v souboru
    public static Integer number_of_reported_errors(){
        return Model_LoggyError.find.findRowCount();
    }

    // Vymažu všechny bugy ze souboru
    public static void clear_file(){
        File all = Play.application().getFile("logs/all.log");

        try {
            new PrintWriter(all).close(); // vymaže obsah souboru
        } catch (Exception e) {}
    }

    public static Result upload_to_youtrack(String id, String description) {
        if (System.currentTimeMillis() > tokenExpire - 10000) { // pokud nemám platný token, získám ho a metodu spustím znovu
            if (youtrack_login().get(5000).status() != 200) {
                return GlobalResult.result_badRequest("Cannot login to YouTrack");
            }
        }
        Model_LoggyError e = getError(id);
        if (e == null) return GlobalResult.result_notFound("Error not found");

        // sestavím request na nahrání
        WSRequest request = getWSClient().url(Configuration.root().getString("Loggy.youtrackUrl") + "/youtrack/rest/issue");
        request.setQueryParameter("project", Configuration.root().getString("Loggy.youtrackProject"));
        request.setQueryParameter("summary", e.summary);
        request.setQueryParameter("description", description + "\n\n" + e.description + e.stack_trace + (e.cause == null ? "" : e.cause));
        request.setHeader("Authorization", "Bearer "+token);
        F.Promise<WSResponse> promise = request.put("");
        return youtrack_checkUploadResponse(promise.get(10000), e); // zpracuje odpověď a zapíše url do erroru
    }

    public static List<Model_LoggyError> getErrors(Integer a){
        return Model_LoggyError.find.setMaxRows(a).findList();
    }

    public static List<Model_LoggyError> getErrors(){
        return Model_LoggyError.find.where().orderBy().desc("created").findList();
    }

    public static Model_LoggyError getError(String id) {
        return Model_LoggyError.find.byId(id);
    }

    private static F.Promise<Result> youtrack_login() {
        WSRequest request = getWSClient().url(Configuration.root().getString("Loggy.youtrackUrl") + "/hub/api/rest/oauth2/token");
        request.setContentType("application/x-www-form-urlencoded");
        request.setHeader("Authorization",
                "Basic "+ new String(Base64.getEncoder().encode(    // zakódování přihlašovacích údajů
                        (Configuration.root().getString("Loggy.youtrackId") + ":" + Configuration.root().getString("Loggy.youtrackSecret"))
                                .getBytes())));
        F.Promise<WSResponse> promise = request.post(               // odešlu request na token
                "response_type=token&grant_type=password"
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
            error.youtrack_url = response.getHeader("Location").replace("/rest", ""); // uložím url z odpovědi
            error.update();

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