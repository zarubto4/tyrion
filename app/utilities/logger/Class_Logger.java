package utilities.logger;

public class Class_Logger{

    private Class<?> t_class;

    public Class_Logger(Class<?> t_class){
        this.t_class = t_class;
    }

    public void trace(String log_message) {
        Server_Logger.trace(t_class, ":: " + log_message);
    }
    public void trace(String log_message, Object... args) {
        //TODO
        Server_Logger.trace(t_class, " " + log_message);
    }


    public void info(String log_message) {
        Server_Logger.info(t_class, ":: " + log_message);
    }
    public void info(String log_message, Object... args) {
        //TODO
        Server_Logger.info(t_class, " " + log_message);
    }


    public void debug(String log_message) { Server_Logger.debug(t_class, ":: " + log_message);}
    public void debug(String log_message,  Object... args) {
        //TODO
        Server_Logger.debug(t_class, " " + log_message);
    }



    public void warn(String log_message) {
        Server_Logger.warn(t_class,  ":: " + log_message);
    }
    public void warn(String log_message, Object... args)   {
        //TODO
        Server_Logger.warn(t_class,  " " + log_message);
    }


    public void error(String log_message) {
        Server_Logger.error(t_class, ":: " + log_message);
    }
    public void error(String log_message, Object... args) {
        //TODO
        Server_Logger.error(t_class, " " + log_message);
    }




    public void internalServerError(String message, Exception e){
        new Thread(() -> {
            Server_Logger.internalServerError(t_class, message, e);
        }).start();
    }

    public void internalServerError(Exception e){
        new Thread(() -> {
            Server_Logger.internalServerError(t_class, " (Error with Undefined Message) :: ", e);
        }).start();
    }
}
