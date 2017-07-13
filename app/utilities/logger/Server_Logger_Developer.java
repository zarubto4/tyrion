package utilities.logger;


import com.google.inject.Singleton;
import models.Model_Person;
import play.Configuration;
import utilities.Server;
import utilities.enums.Enum_Log_level;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.logger.helps_objects.Interface_Server_Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

@Singleton
public class Server_Logger_Developer implements Interface_Server_Logger {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static private play.Logger.ALogger logger = play.Logger.of("TYRION");
/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    private static HashMap<String, Log_Pair> list_of_objects_for_logging = new HashMap<>();

/* CONSTRUCTOR  --------------------------------------------------------------------------------------------------------*/

    public Server_Logger_Developer(){

        logger.warn("     Logger Settings:: Developer");

        // Načtení Všech Log tříd
        for(String group_name :  Configuration.root().getStringList("Loggy.general_log_groups")){

            try {

                group_name.replaceAll("\\s+","");

                logger.warn("        Actual Group:: Name               :: " + group_name);

                // Get List of all Groups!
                ArrayList<String> list = (ArrayList<String>) Configuration.root().getStringList("Loggy.groups." + group_name);

                String group_config = list.get(0);

                String log_level = group_config.split("::")[0].toLowerCase();
                String color = group_config.split("::")[1];

                // Log Level
                Enum_Log_level default_group_log_level = Enum_Log_level.fromString(log_level);
                if(default_group_log_level == null) default_group_log_level = Enum_Log_level.trace;
                logger.warn("                    :: Log Level in Group :: " + default_group_log_level.name());

                // Color
                Color default_group_log_color;
                if(color.equals("YELLOW")) default_group_log_color = Color.yellow;
                else if(color.equals("BLUE")) default_group_log_color = Color.blue;
                else if(color.equals("GREEN")) default_group_log_color = Color.blue;
                else if(color.equals("ORANGE")) default_group_log_color = Color.orange;
                else if(color.equals("RED")) default_group_log_color = Color.red;
                else if(color.equals("PINK")) default_group_log_color = Color.pink;
                else default_group_log_color = Color.white;
                logger.warn("                    :: Log Color in Group :: " + color);

                list.remove(0);


                logger.warn("                    :: Log Class in Group :: ");

                for (String object_for_log : list) {

                    logger.warn("                                          :: " + object_for_log);

                    Log_Pair log_pair = new Log_Pair();

                    // The Name of Object
                    log_pair.name = object_for_log;

                    // Set Color of Object if default_group_log_color is not set
                    log_pair.color = default_group_log_color;


                    // Set Level of Object if default_group_log_level is not set
                    log_pair.log_level = default_group_log_level;

                    // Add Object to HashMap
                    list_of_objects_for_logging.put(object_for_log, log_pair);

                }


            }catch (NullPointerException e){

                e.printStackTrace();

                logger.error("############################################################################################");
                logger.error("##                                                                                        ##");
                logger.error("## Server_Logger_Developer:: Group " + group_name + " not found in configuration file!    ");
                logger.error("## -->>>> (Loggy.groups." + group_name + ")                                               ");
                logger.error("##  Please check log list in project folder in conf.aplication.conf file                  ##");
                logger.error("##  For more details check Tyrion Wiki documentation.                                     ##");
                logger.error("##                                                                                        ##");
                logger.error("############################################################################################");
                System.out.println();

            }catch (Exception e ){
                logger.error("Error: ",e);
            }
        }

    }


/* IMPLEMENTS METHOD FROM INTERFACE ------------------------------------------------------------------------------------*/

    @Override
    public void trace(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.trace){

                logger.trace(t_class.getSimpleName() + ":: " + log_message, args);
            }
        }
    }

    public void info(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.trace || log_level == Enum_Log_level.info){

                logger.info(t_class.getSimpleName() + ":: " + log_message, args);

            }
        }
    }

    public void debug(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.trace || log_level == Enum_Log_level.info || log_level == Enum_Log_level.debug){

                logger.debug(t_class.getSimpleName() + ":: " + log_message, args);

            }
        }
    }

    public void warn(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if( log_level == Enum_Log_level.trace || log_level == Enum_Log_level.info || log_level == Enum_Log_level.debug || log_level == Enum_Log_level.warn || log_level == Enum_Log_level.error){

                logger.warn(t_class.getSimpleName() + ":: " + log_message, args);

            }
        }
    }

    // Always
    public void error(Class<?> t_class, String log_message){
        logger.error(t_class.getSimpleName() + ":: " + log_message);
    }

    public void error(Class<?> t_class, String log_message, Object... args){
        logger.error(t_class.getSimpleName() + ":: " + log_message, args);
    }

    // Always
    public void error(Class<?> t_class, String log_message, Exception e){
        logger.error(t_class.getSimpleName() + ":: " + log_message + " :: " , e);
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public class Log_Pair{

        public String name;
        public Enum_Log_level log_level;
        public Color color;
    }

}
