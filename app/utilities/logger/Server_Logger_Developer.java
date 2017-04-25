package utilities.logger;


import com.google.inject.Singleton;
import play.Configuration;
import utilities.enums.Enum_Log_level;
import utilities.logger.helps_objects.Interface_Server_Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

@Singleton
public class Server_Logger_Developer implements Interface_Server_Logger {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("TYRION");


/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    private static HashMap<String, Log_Pair> list_of_objects_for_logging = new HashMap<>();

/* CONSTRUCTOR  --------------------------------------------------------------------------------------------------------*/

    public Server_Logger_Developer(){

        // Načtení Všech Log tříd
        for(String group_name :  Configuration.root().getStringList("Loggy.general_log_groups")){
            try {


                // Get List of all Groups!
                ArrayList<String> list = (ArrayList<String>) Configuration.root().getStringList("Loggy.groups." + group_name);

                // If list not empty - Developer want log only some Logger groups!
                if(!list.isEmpty()) {

                    String group_config = list.get(0);

                    Enum_Log_level default_group_log_level = Enum_Log_level.fromString(group_config.split("::")[0]);
                    Color default_group_log_color = Color.getColor(group_config.split("::")[1]);

                    list.remove(0);

                    for (String object_for_log : list) {

                        Log_Pair log_pair = new Log_Pair();
                        String object_configuration = Configuration.root().getString("Loggy.objects." + object_for_log);

                        // If object not found - Programer Will be notified!!!
                        if (object_configuration == null) {

                            System.err.println("ERROR");
                            System.err.println("ERROR:: Server_Logger_Developer:: Object " + object_for_log + " not found in configuration file!!! (Loggy.objects." + object_for_log + ")");
                            continue;
                        }

                        // The Name of Object
                        log_pair.name = object_for_log;

                        // Set Color of Object if default_group_log_color is not set
                        if (default_group_log_color != null) {
                            log_pair.color = default_group_log_color;
                        } else {
                            Color log_pair_color = Color.getColor(object_configuration.split("::")[1].substring(2));
                            if (log_pair_color != null) log_pair.color = log_pair_color;
                            else log_pair.color = Color.BLACK;
                        }


                        // Set Level of Object if default_group_log_level is not set
                        if (default_group_log_level != null) {
                            log_pair.log_level = default_group_log_level;
                        } else {
                            Enum_Log_level log_pair_log_level = Enum_Log_level.fromString(object_configuration.split("::")[0]);
                            if (log_pair_log_level != null) log_pair.log_level = log_pair_log_level;
                            else log_pair.log_level = Enum_Log_level.debug;
                        }

                        // Add Object to HashMap
                        list_of_objects_for_logging.put(object_for_log, log_pair);
                    }

                // Developer not set any groups for logger - so system will log every objects by default parameters.
                }else {
                    for(String key: Configuration.root().getConfig("Loggy.objects").keys()){

                        Log_Pair log_pair = new Log_Pair();
                        String object_configuration = Configuration.root().getString("Loggy.objects." + key);
                        log_pair.name = key;

                        Color log_pair_color = Color.getColor(object_configuration.split("::")[1].substring(2));
                        if (log_pair_color != null) log_pair.color = log_pair_color;
                        else log_pair.color = Color.BLACK;

                        Enum_Log_level log_pair_log_level = Enum_Log_level.fromString(object_configuration.split("::")[0]);
                        if (log_pair_log_level != null) log_pair.log_level = log_pair_log_level;
                        else log_pair.log_level = Enum_Log_level.debug;

                        // Add Object to HashMap
                        list_of_objects_for_logging.put(key, log_pair);
                    }
                }


            }catch (NullPointerException e){

                logger.error("#########################################################################################");
                logger.error("##                                                                                        ");
                logger.error("## Server_Logger_Developer:: Group " + group_name + " not found in configuration file!    ");
                logger.error("## -->>>> (Loggy.groups." + group_name + ")                                               ");
                logger.error("##  Please check log list in project folder in conf.aplication.conf file                  ");
                logger.error("##  For more details check Tyrion Wiki documentation.                                     ");
                logger.error("##                                                                                        ");
                logger.error("#########################################################################################");

            }catch (Exception e ){
                logger.error("Error", e);
            }
        }

    }


/* IMPLEMENTS METHOD FROM INTERFACE ------------------------------------------------------------------------------------*/
    public void trace(Class<?> t_class, String log_message){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.trace || log_level == Enum_Log_level.info || log_level == Enum_Log_level.debug || log_level == Enum_Log_level.warn || log_level == Enum_Log_level.error ){

              logger.trace(t_class.getSimpleName() + ":: " + log_message);
            }
        }
    }


    @Override
    public void trace(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.trace || log_level == Enum_Log_level.info || log_level == Enum_Log_level.debug || log_level == Enum_Log_level.warn || log_level == Enum_Log_level.error ){

                logger.trace(t_class.getSimpleName() + ":: " + log_message, args);
            }
        }
    }


    public void info(Class<?> t_class, String log_message){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.info || log_level == Enum_Log_level.debug || log_level == Enum_Log_level.warn || log_level == Enum_Log_level.error){

                logger.info(t_class.getSimpleName() + ":: " + log_message);

            }
        }
    }

    public void info(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.info || log_level == Enum_Log_level.debug || log_level == Enum_Log_level.warn || log_level == Enum_Log_level.error){

                logger.info(t_class.getSimpleName() + ":: " + log_message, args);

            }
        }
    }



    public void debug(Class<?> t_class, String log_message){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.debug || log_level == Enum_Log_level.warn || log_level == Enum_Log_level.error){

                logger.debug(t_class.getSimpleName() + ":: " + log_message);

            }
        }
    }

    public void debug(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.debug || log_level == Enum_Log_level.warn || log_level == Enum_Log_level.error){

                logger.debug(t_class.getSimpleName() + ":: " + log_message, args);

            }
        }
    }


    public void warn(Class<?> t_class, String log_message){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.warn || log_level == Enum_Log_level.error){

                logger.warn(t_class.getSimpleName() + ":: " + log_message);

            }
        }
    }

    public void warn(Class<?> t_class, String log_message, Object... args){

        if(list_of_objects_for_logging.containsKey( t_class.getSimpleName() )){

            Enum_Log_level log_level = list_of_objects_for_logging.get(t_class.getSimpleName()).log_level;

            if(log_level == Enum_Log_level.warn || log_level == Enum_Log_level.error){

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
