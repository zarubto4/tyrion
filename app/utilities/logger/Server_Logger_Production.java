package utilities.logger;


import utilities.logger.helps_objects.Interface_Server_Logger;

public class Server_Logger_Production implements Interface_Server_Logger {

    static private play.Logger.ALogger logger = play.Logger.of("TYRION");

    @Override
    public void trace(Class<?> t_class, String log_message) {
        logger.trace(t_class.getSimpleName() + ":: " + log_message);
    }

    @Override
    public void info(Class<?> t_class, String log_message) {
        logger.info(t_class.getSimpleName() + ":: " + log_message);
    }

    @Override
    public void debug(Class<?> t_class, String log_message) {
        logger.debug(t_class.getSimpleName() + ":: " + log_message);
    }

    @Override
    public void warn(Class<?> t_class, String log_message) {
        logger.warn(t_class.getSimpleName() + ":: " + log_message);
    }

    @Override
    public void error(Class<?> t_class, String log_message) {
        logger.error(t_class.getSimpleName() + ":: " + log_message);
    }

    @Override
    public void trace(Class<?> t_class, String log_message, Object... args) {
        logger.trace(t_class.getSimpleName() + ":: " + log_message, args);
    }

    @Override
    public void info(Class<?> t_class, String log_message, Object... args) {
        logger.info(t_class.getSimpleName() + ":: " + log_message, args);
    }

    @Override
    public void debug(Class<?> t_class, String log_message, Object... args) {
        logger.debug(t_class.getSimpleName() + ":: " + log_message, args);
    }

    @Override
    public void warn(Class<?> t_class, String log_message, Object... args) {
        logger.warn(t_class.getSimpleName() + ":: " + log_message, args);
    }

    @Override
    public void error(Class<?> t_class, String log_message, Object... args) {
        logger.error(t_class.getSimpleName() + ":: " + log_message, args);
    }
}
