package utilities.logger;

public class Class_Logger{

    private Class<?> t_class;

    public Class_Logger(Class<?> t_class){
        this.t_class = t_class;
    }

    public void trace(String log_message) {
        Server_Logger.trace(t_class, log_message);
    }
    public void trace(String log_message, Object... args) {Server_Logger.trace(t_class, log_message, args);}


    public void info(String log_message) {Server_Logger.info(t_class, log_message);}
    public void info(String log_message, Object... args) {Server_Logger.info(t_class, log_message, args);}


    public void debug(String log_message) { Server_Logger.debug(t_class, log_message);}
    public void debug(String log_message,  Object... args) {Server_Logger.debug(t_class, log_message, args);}



    public void warn(String log_message) {
        Server_Logger.warn(t_class,  log_message);
    }
    public void warn(String log_message, Object... args)   {Server_Logger.warn(t_class,  log_message, args);}


    public void error(String log_message) {
        Server_Logger.error(t_class, log_message);
    }
    public void error(String log_message, Object... args) {Server_Logger.error(t_class, log_message, args);}


    public void internalServerError(String message, Exception e){
        Server_Logger.internalServerError(t_class, message, e);
    }

    public void internalServerError(Exception e){
        Server_Logger.internalServerError(t_class, " (Error with Undefined Message)", e);
    }
}
