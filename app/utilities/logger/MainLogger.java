package utilities.logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.typesafe.config.Config;
import org.slf4j.LoggerFactory;
import utilities.enums.LogLevel;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainLogger {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static private play.Logger.ALogger logger = play.Logger.of("TYRION");

/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    private static HashMap<String, Log_Pair> list_of_objects_for_logging = new HashMap<>();

    private final Config configuration;

/* CONSTRUCTOR  --------------------------------------------------------------------------------------------------------*/

    public MainLogger(sun.security.krb5.Config configuration) {

        this.configuration = configuration;

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {

            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();

            // Production Mode
            if (configuration.getString("server.mode").equals("production")) {
                configurator.doConfigure(new File(System.getProperty("user.dir") + this.configuration.getString("logger.production")));
            } else {
                configurator.doConfigure(new File(System.getProperty("user.dir") + this.configuration.getString("logger.developer")));
            }



        } catch (JoranException je) {
            je.printStackTrace();
        }

        List<String> groups = this.configuration.getStringList("logger.logged_groups");

        System.out.println();
        System.out.println("    ");
        System.out.println("    §§     §§§   §§§   §§§  §§§§§ §§§§  _§§§§");
        System.out.println("    §§    §§ §§ §§    §§    §§___ §§_§§ §§_  ");
        System.out.println("    §§    §§ §§ §§ §§ §§ §§ §§¨¨¨ §§'§_   ¨§§");
        System.out.println("    §§§§§  §§§   §§§   §§§  §§§§§ §§ §§ §§§§'");
        System.out.println();
        System.out.println("     SETTINGS: " + configuration.getString("server.mode"));
        System.out.println();
        System.out.print  ("     GROUPS: ");

        for (int i = 0; i < groups.size(); i++) {
            System.out.print(groups.get(i) + ((i == groups.size() - 1) ? "\n" : ", "));
        }

        System.out.println("  ____________________________________________________________________________________");

        // Načtení Všech Log tříd
        for (String group : groups) {
            try {

                group = group.replaceAll("\\s+",""); // CO D2L8 TENTO REGEX????? TO NEV9M ALE ZJISTIM

                // Get List of all Groups!
                ArrayList<String> list = (ArrayList<String>) configuration.getStringList("logger.groups." + group);

                String group_config = list.get(0);

                String log_level = group_config.split("::")[0];
                String color = group_config.split("::")[1];

                // Log Level
                LogLevel default_group_log_level = LogLevel.valueOf(log_level);
                if (default_group_log_level == null) default_group_log_level = LogLevel.TRACE;

                // Color
                Color default_group_log_color;
                if (color.equals("YELLOW")) default_group_log_color = Color.yellow;
                else if (color.equals("BLUE")) default_group_log_color = Color.blue;
                else if (color.equals("GREEN")) default_group_log_color = Color.blue;
                else if (color.equals("ORANGE")) default_group_log_color = Color.orange;
                else if (color.equals("RED")) default_group_log_color = Color.red;
                else if (color.equals("PINK")) default_group_log_color = Color.pink;
                else default_group_log_color = Color.white;

                list.remove(0);

                System.out.println();
                System.out.println("     GROUP: " + group + " | LEVEL: " + default_group_log_level.name() + " | COLOR: " + color.toLowerCase());
                System.out.println();
                System.out.print  ("     CLASSES: ");

                for (int i = 1; i <= list.size(); i++) {
                    String clazz = list.get(i - 1);
                        System.out.print(clazz + (i == list.size() ? "\n" : ", "));
                    if (i != list.size() && i%3 == 0) {
                        System.out.print("\n              ");
                    }

                    Log_Pair log_pair = new Log_Pair();

                    // The Name of Object
                    log_pair.name = clazz;

                    // Set Color of Object if default_group_log_color is not set
                    // log_pair.color = default_group_log_color;


                    // Set Level of Object if default_group_log_level is not set
                    log_pair.logLevel = default_group_log_level;

                    // Add Object to HashMap
                    list_of_objects_for_logging.put(clazz, log_pair);
                }
                System.out.println("  ____________________________________________________________________________________");

            } catch (NullPointerException e) {

                e.printStackTrace();

                logger.error("############################################################################################");
                logger.error("##                                                                                        ##");
                logger.error("## Server_Logger_Developer:: Group " + group + " not found in configuration file!         ##");
                logger.error("## -->>>> (Logger.groups." + group + ")                                                   ##");
                logger.error("##  Please check log list in project folder in application.conf file                      ##");
                logger.error("##  For more details check Tyrion Wiki documentation.                                     ##");
                logger.error("##                                                                                        ##");
                logger.error("############################################################################################");
                System.out.println();

            } catch (Exception e) {
                logger.error("Error: ",e);
            }
        }
        System.out.println();
    }

    public boolean isProd() {
        return configuration.getString("server.mode").equals("production");
    }

/* IMPLEMENTS METHOD FROM INTERFACE ------------------------------------------------------------------------------------*/

    public void trace(Class<?> t_class, String log_message, Object... args) {

        if (list_of_objects_for_logging.containsKey( t_class.getSimpleName() )) {

            LogLevel log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).logLevel;
            // Color color = list_of_objects_for_logging.get(t_class.getSimpleName()).color;

            if (log_level == LogLevel.TRACE) {

                logger.trace(t_class.getSimpleName() + "::" + log_message, args);
            }
        } else {
          // System.out.println("trace neobsahuje klíč " + t_class.getSimpleName());
        }
    }

    public void debug(Class<?> t_class, String log_message, Object... args) {

        if (list_of_objects_for_logging.containsKey( t_class.getSimpleName() )) {

            LogLevel log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).logLevel;

            if (log_level == LogLevel.TRACE || log_level == LogLevel.DEBUG) {

                // Dostat sem barvičku
                logger.debug(t_class.getSimpleName() + "::" + log_message, args);

            }
        } else {
          //  System.out.println("debug neobsahuje klíč " + t_class.getSimpleName());
        }
    }


    public void info(Class<?> t_class, String log_message, Object... args) {

        if (list_of_objects_for_logging.containsKey( t_class.getSimpleName() )) {

            LogLevel log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).logLevel;

            if (log_level == LogLevel.TRACE || log_level == LogLevel.DEBUG || log_level == LogLevel.INFO) {

                logger.info(t_class.getSimpleName() + "::" + log_message, args);

            }
        }
    }

    public void warn(Class<?> t_class, String log_message, Object... args) {

        if (list_of_objects_for_logging.containsKey(t_class.getSimpleName())) {

            LogLevel log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).logLevel;

            if (log_level == LogLevel.TRACE || log_level == LogLevel.DEBUG || log_level == LogLevel.INFO || log_level == LogLevel.WARN || log_level == LogLevel.ERROR) {

                logger.warn(t_class.getSimpleName() + "::" + log_message, args);

            }
        }
    }

    // Always
    public void error(Class<?> t_class, String log_message, Object... args) {
        logger.error(t_class.getSimpleName() + "::" + log_message, args);
    }

    // Always
    public void error(Class<?> t_class, String log_message, Exception e) {
        logger.error(t_class.getSimpleName() + "::" + log_message + " :: " , e);
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public static class Log_Pair {
        public String name;
        public LogLevel logLevel;
        // public Color color;
    }
}
