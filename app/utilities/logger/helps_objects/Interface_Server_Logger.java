package utilities.logger.helps_objects;


public interface Interface_Server_Logger {

    void trace(Class<?> t_class, String log_message);
    void trace(Class<?> t_class, String log_message, Object... args);
    void info(Class<?> t_class, String log_message);
    void info(Class<?> t_class, String log_message, Object... args);
    void debug(Class<?> t_class, String log_message);
    void debug(Class<?> t_class, String log_message, Object... args);
    void warn(Class<?> t_class, String log_message);
    void warn(Class<?> t_class, String log_message, Object... args);
    void error(Class<?> t_class, String log_message);
    void error(Class<?> t_class, String log_message, Object... args);

}
