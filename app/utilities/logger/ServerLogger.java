package utilities.logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import models.Model_ServerError;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utilities.Server;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.logger.helps_objects.Interface_Server_Logger;
import utilities.response.GlobalResult;
import utilities.slack.Slack;

import java.util.List;

public class ServerLogger extends Controller {

    private static Interface_Server_Logger logger;  // Vlastní Logger objekt definovaný konfigurací

    public static void setLogger(){

        System.out.println("setLogger - loading settings");

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {

            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();

            // Production Mode
            if (Configuration.root().getString("Server.mode").equals("production")) {

                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Logger.productionSettings")));

                logger = new Server_Logger_Production();


                return;
            }

            // Developer Mode
            else if(Configuration.root().getString("Server.mode").equals("developer") || Configuration.root().getString("Server.mode").equals("stage") ){

                configurator.doConfigure(Play.application().getFile(Play.application().configuration().getString("Logger.developerSettings")));

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
}