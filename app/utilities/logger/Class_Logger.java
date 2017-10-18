package utilities.logger;

public class Class_Logger{

    private Class<?> t_class;

    public Class_Logger(Class<?> t_class){
        this.t_class = t_class;
    }

    public void trace(String log_message, Object... args) {Server_Logger.trace(t_class, log_message, args);}
    public void info (String log_message, Object... args) {Server_Logger.info (t_class, log_message, args);}
    public void debug(String log_message, Object... args) {Server_Logger.debug(t_class, log_message, args);}
    public void warn (String log_message, Object... args) {Server_Logger.warn (t_class, log_message, args);}

    /*
     Rather an exception should be thrown or if it is undesired,
     you can use terminal_logger.internalServerError(new Exception("Your message.")).
     New exception creates an stack trace so it is easier to track down errors and it does not pollute DB with empty bugs.
      */
    public void error(String log_message, Object... args) {
        Server_Logger.error(t_class, log_message, args);
    }

    public void internalServerError(Throwable e){
        Server_Logger.internalServerError(e);
    }
}