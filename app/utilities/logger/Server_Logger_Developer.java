package utilities.logger;


import com.google.inject.Singleton;
import play.Configuration;
import utilities.enums.Enum_Log_level;
import utilities.logger.helps_objects.Interface_Server_Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Singleton
public class Server_Logger_Developer implements Interface_Server_Logger {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static private play.Logger.ALogger logger = play.Logger.of("TYRION");

/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    private static HashMap<String, Log_Pair> list_of_objects_for_logging = new HashMap<>();

/* CONSTRUCTOR  --------------------------------------------------------------------------------------------------------*/

    public Server_Logger_Developer(){

        List<String> groups = Configuration.root().getStringList("Logger.general_log_groups");

        System.out.println();
        System.out.println("   ▄    _▄▄_ _▄▄_ _▄▄_ ▄▄▄ ▄▄▄  ▄▄_");
        System.out.println("   █    █  █ █  ▀ █  ▀ █___ █__█ █_ ▀   █");
        System.out.println("   █    █  █ █ ▄_ █ ▄_ █▀▀ ██▀   ▀▄");
        System.out.println("   █___ █__█ █__█ █__█ █___ █  █ ▄__█   █");
        System.out.println("   ▀▀▀  ▀▀   ▀▀   ▀▀  ▀▀▀ ▀  ▀  ▀▀");
        System.out.println();
        System.out.println("     SETTINGS: Developer");
        System.out.println();
        System.out.print  ("     GROUPS: ");

        for (int i = 0; i < groups.size(); i++) {
            System.out.print(groups.get(i) + ((i == groups.size() - 1) ? "\n" : ", "));
        }

        System.out.println("  ____________________________________________________________________________________");

        // Načtení Všech Log tříd
        for(String group : groups){
            try {

                group = group.replaceAll("\\s+","");

                // Get List of all Groups!
                ArrayList<String> list = (ArrayList<String>) Configuration.root().getStringList("Logger.groups." + group);

                String group_config = list.get(0);

                String log_level = group_config.split("::")[0].toLowerCase();
                String color = group_config.split("::")[1];

                // Log Level
                Enum_Log_level default_group_log_level = Enum_Log_level.fromString(log_level);
                if (default_group_log_level == null) default_group_log_level = Enum_Log_level.trace;

                // Color
                Color default_group_log_color;
                if(color.equals("YELLOW")) default_group_log_color = Color.yellow;
                else if(color.equals("BLUE")) default_group_log_color = Color.blue;
                else if(color.equals("GREEN")) default_group_log_color = Color.blue;
                else if(color.equals("ORANGE")) default_group_log_color = Color.orange;
                else if(color.equals("RED")) default_group_log_color = Color.red;
                else if(color.equals("PINK")) default_group_log_color = Color.pink;
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
                    log_pair.color = default_group_log_color;


                    // Set Level of Object if default_group_log_level is not set
                    log_pair.log_level = default_group_log_level;

                    // Add Object to HashMap
                    list_of_objects_for_logging.put(clazz, log_pair);
                }
                System.out.println("  ____________________________________________________________________________________");

            }catch (NullPointerException e){

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

            }catch (Exception e){
                logger.error("Error: ",e);
            }
        }
        System.out.println();
    }

/* IMPLEMENTS METHOD FROM INTERFACE ------------------------------------------------------------------------------------*/

    @Override
    public void trace(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.trace){

                logger.trace(t_class.getSimpleName() + "::" + log_message, args);
            }
        }
    }

    public void info(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.trace || log_level == Enum_Log_level.info){

                logger.info(t_class.getSimpleName() + "::" + log_message, args);

            }
        }
    }

    public void debug(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.trace || log_level == Enum_Log_level.info || log_level == Enum_Log_level.debug){

                logger.debug(t_class.getSimpleName() + "::" + log_message, args);

            }
        }
    }

    public void warn(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if( log_level == Enum_Log_level.trace || log_level == Enum_Log_level.info || log_level == Enum_Log_level.debug || log_level == Enum_Log_level.warn || log_level == Enum_Log_level.error){

                logger.warn(t_class.getSimpleName() + "::" + log_message, args);

            }
        }
    }

    // Always
    public void error(Class<?> t_class, String log_message, Object... args){
        logger.error(t_class.getSimpleName() + "::" + log_message, args);
    }

    // Always
    public void error(Class<?> t_class, String log_message, Exception e){
        logger.error(t_class.getSimpleName() + "::" + log_message + " :: " , e);
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public class Log_Pair{

        public String name;
        public Enum_Log_level log_level;
        public Color color;
    }
}
