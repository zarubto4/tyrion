package utilities.logger;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import controllers.Controller_Security;
import models.Model_ServerError;
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

    public static void internalServerError(Throwable exception){

        StackTraceElement current_stack = Thread.currentThread().getStackTrace()[3]; // Find the caller origin
        error(exception, current_stack.getClassName() + "::" + current_stack.getMethodName(), null);
    }

/* CONTROLLER LOGGER ---------------------------------------------------------------------------------------------------*/

    public static Result result_internalServerError(Throwable exception, Http.RequestHeader request) {

        StackTraceElement current_stack = Thread.currentThread().getStackTrace()[2]; // Find the caller origin
        Model_ServerError error = error(exception, current_stack.getClassName() + "::" + current_stack.getMethodName(), request);

        return GlobalResult.result_internalServerError(error.message);
    }


/* CONTROLLER OERATION -------------------------------------------------------------------------------------------------*/

/* SERVICES ------------------------------------------------------------------------------------------------------------*/

    /**
     * Saves error to database or increments "repetition" and sends it to Slack channel #servers if mode is not "developer".
     * @param exception Caught exception
     * @param origin String origin, where the exception was caught.
     * @param request Http request, pass null, if unavailable
     */
    private static Model_ServerError error(Throwable exception, String origin, Http.RequestHeader request) {

        // Just temporary protection, later every error will be unique
        List<Model_ServerError> errors = Model_ServerError.find.where().isNotNull("stack_trace").eq("stack_trace", Model_ServerError.formatStackTrace(exception.getStackTrace())).findList();
        if (errors.size() > 1) {
            for (Model_ServerError e : errors) {
                e.delete();
            }
        }

        Model_ServerError error = Model_ServerError.find.where().isNotNull("stack_trace").eq("stack_trace", Model_ServerError.formatStackTrace(exception.getStackTrace())).findUnique();
        if (error == null) {
            error = new Model_ServerError(exception, origin, request); // Save to DB
            error.save();
        } else {
            error.repetition++;
            error.update();
        }

        System.err.println(error.prettyPrint());

        if (Server.server_mode != Enum_Tyrion_Server_mode.developer) Slack.post(error);

        return error;
    }

    // Vracím počet zaznamenaných bugů v souboru
    public static Integer number_of_reported_errors(){
        return Model_ServerError.find.findRowCount();
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
        Model_ServerError e = getError(id);
        if (e == null) return GlobalResult.result_notFound("Error not found");

        // sestavím request na nahrání
        WSRequest request = getWSClient().url(Configuration.root().getString("Loggy.youtrackUrl") + "/youtrack/rest/issue");
        request.setQueryParameter("project", Configuration.root().getString("Loggy.youtrackProject"));
        request.setQueryParameter("summary", e.summary);
        request.setQueryParameter("description", description + "\n\n" + e.description + e.stack_trace + (e.cause_message == null ? "" : e.cause_message));
        request.setHeader("Authorization", "Bearer "+token);
        F.Promise<WSResponse> promise = request.put("");
        return youtrack_checkUploadResponse(promise.get(10000), e); // zpracuje odpověď a zapíše url do erroru
    }

    public static List<Model_ServerError> getErrors(Integer a){
        return Model_ServerError.find.setMaxRows(a).findList();
    }

    public static List<Model_ServerError> getErrors(){
        return Model_ServerError.find.where().orderBy().desc("created").findList();
    }

    public static Model_ServerError getError(String id) {
        return Model_ServerError.find.byId(id);
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

    private static Result youtrack_checkUploadResponse(WSResponse response, Model_ServerError error) {
        if (response.getStatus() == 201) {
            error.youtrack_url = response.getHeader("Location").replace("/rest", ""); // uložím url z odpovědi
            error.update();

            logger.debug( Server_Logger.class , error.youtrack_url);

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