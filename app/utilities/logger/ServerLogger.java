package utilities.logger;

import com.typesafe.config.Config;
import models.Model_ServerError;
import play.mvc.Controller;
import play.mvc.Http;
import exceptions.NotFoundException;
// import utilities.slack.Slack;

import java.util.List;

public class ServerLogger extends Controller {

    private static MainLogger logger;  // Vlastní Logger objekt definovaný konfigurací

    public static void init(Config configuration) {

        System.out.println("ServerLogger::init - loading settings");

        logger = new MainLogger(configuration);
    }

/* CLASS LOGGER --------------------------------------------------------------------------------------------------------*/

    public static void trace(Class<?> t_class, String log_message, Object... args) {logger.trace(t_class, log_message, args);}
    public static void info (Class<?> t_class, String log_message, Object... args) {logger.info (t_class, log_message, args);}
    public static void debug(Class<?> t_class, String log_message, Object... args) {logger.debug(t_class, log_message, args);}
    public static void warn (Class<?> t_class, String log_message, Object... args) {logger.warn (t_class, log_message, args);}
    public static void error(Class<?> t_class, String log_message, Object... args) {logger.error(t_class, log_message, args);}

    public static void internalServerError(Throwable exception) {
        StackTraceElement current_stack = Thread.currentThread().getStackTrace()[3]; // Find the caller origin
        error(exception, current_stack.getClassName() + "::" + current_stack.getMethodName(), null);
    }

/* SERVICES ------------------------------------------------------------------------------------------------------------*/

    /**
     * Saves error to database or increments "repetition" and sends it to Slack channel #servers if mode is not "developer".
     * @param exception Caught exception
     * @param origin String origin, where the exception was caught.
     * @param request Http request, pass null, if unavailable
     */
    public static Model_ServerError error(Throwable exception, String origin, Http.RequestHeader request) {

        // Just temporary protection, later every error will be unique
        List<Model_ServerError> errors = Model_ServerError.find.query().where().isNotNull("stack_trace").eq("stack_trace", Model_ServerError.formatStackTrace(exception.getStackTrace())).findList();
        if (errors.size() > 1) {
            for (Model_ServerError e : errors) {
                e.delete();
            }
        }

        Model_ServerError error;

        try {
            error = Model_ServerError.find.query().where().isNotNull("stack_trace").eq("stack_trace", Model_ServerError.formatStackTrace(exception.getStackTrace())).findOne();
            error.repetition++;
            error.update();
        } catch (NotFoundException e) {
            error = new Model_ServerError(exception, origin, request); // Save to DB
            error.save();
        }

        System.err.println(error.prettyPrint());

        // if (Server.server_mode != Enum_Tyrion_Server_mode.developer) Slack.post(error);

        return error;
    }
}