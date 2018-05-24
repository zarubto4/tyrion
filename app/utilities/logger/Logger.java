package utilities.logger;

public class Logger {

    private Class<?> cls;

    public Logger(Class<?> cls) {
        this.cls = cls;
    }

    public void trace(String message, Object... args) {
        ServerLogger.trace(cls, message, args);}
    public void info (String message, Object... args) {
        ServerLogger.info(cls, message, args);}
    public void debug(String message, Object... args) {
        ServerLogger.debug(cls, message, args);}
    public void warn (String message, Object... args) {
        ServerLogger.warn (cls, message, args);}

    /*
     Rather an exception should be thrown or if it is undesired,
     you can use logger.internalServerError(new Exception("Your message.")).
     New exception creates an stack trace so it is easier to track down errors and it does not pollute DB with empty bugs.
      */
    public void error(String message, Object... args) {
        ServerLogger.error(cls, message, args);
    }

    public void internalServerError(Throwable e) {
         ServerLogger.internalServerError(e);
    }
}