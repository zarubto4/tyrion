package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.WebSocketController_Incoming;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;
import models.compiler.Version_Object;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.b_program.servers.Private_Homer_Server;
import models.project.global.Project;
import utilities.webSocket.WebSCType;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class Homer_Instance extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

        @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;

                                         @JsonIgnore @ManyToOne     public Cloud_Homer_Server cloud_homer_server;
                                         @JsonIgnore @OneToOne      public Private_Homer_Server private_server;

                                                    @JsonIgnore     public String blocko_instance_name;
        @JsonIgnore @OneToOne   @JoinColumn(name="vrs_obj_id")      public Version_Object version_object;
                                         @JsonIgnore @ManyToOne()   public Project project;


    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,  value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970", example = "1466163478925")         public Date running_from;



    @OneToOne(mappedBy="private_instance",  cascade = CascadeType.MERGE, fetch = FetchType.LAZY)   public Board private_instance_board;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void setUnique_blocko_instance_name() {

            while(true){ // I need Unique Value
                this.blocko_instance_name = UUID.randomUUID().toString();
                if (Homer_Instance.find.where().eq("blocko_instance_name", blocko_instance_name ).findUnique() == null) break;
            }
    }

    @Override
    public void delete(){

        if(cloud_homer_server.server_is_online()){
            try {

                WebSocketController_Incoming.blocko_server_remove_instance( cloud_homer_server.get_server_webSocket_connection() ,blocko_instance_name);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.delete();
    }

    @JsonIgnore @Transient
    public boolean is_online(){
       return cloud_homer_server.server_is_online() && WebSocketController_Incoming.homer_online_state(blocko_instance_name);
    }

    @JsonIgnore @Transient
    public WebSCType get_instance(){
        return WebSocketController_Incoming.incomingConnections_homers.get(blocko_instance_name);
    }


/* ENUMS PARAMETERS ----------------------------------------------------------------------------------------------------*/

    /**
     Definuje typy commandů - které se dají přes tyrion zaslat do Homer serveru na konkrétní instanci.
     Homer server má terminálové příkazy pro vývojáře HW. Tak aby si mohli zkoušet svůj HW. Jelikož vzniklo grafické
     rozhraní pod tyrionem, je nutné ty samé příkazy co chodí z konzole zasílat i z tyriona. A aby jako v Homer serveru
     nebylo třeba mít pro každý příkaz metodu - zasílá se to definovanými Enum objekty,. (Na Homer serveru se udržuje
     identická struktura)
    */
    public enum TypeOfCommand {

        INFO_FIRMWARE("info_firmware"),
        INFO_BOOTLOADER("info_bootloader"),
        INFO_DATETIME("info_datetime"),
        INFO_DEVICE_COUNTER("info_device_counter"),
        INFO_AUTO_BACKUP("info_auto_backup"),
        INFO_STATE("info_state"),

        SETTINGS_DATETIME("settings_datetime"),
        SETTINGS_AUTOBACKUP("settings_autobackup"),

        COMMAND_ADD_DEVICE("command_add_device"),
        COMMAND_REMOVE_DEVICE("command_remove_device"),
        COMMAND_ADD_MASTER_DEVICE("command_add_master_device"),
        COMMAND_REMOVE_MASTER_DEVICE("command_remove_master_device"),

        COMMAND_UPLOAD_FIRMWARE("command_upload_firmware"),
        COMMAND_RESTART_DEVICE("command_restart_device"),

        COMMAND_PING_DEVICE("command_ping_device");


        private String command;

        TypeOfCommand(String command) {
            this.command = command;
        }


        public static TypeOfCommand getTypeCommand(String value){
                 if(value.equalsIgnoreCase(INFO_FIRMWARE.toString()         ))   return TypeOfCommand.INFO_FIRMWARE;
            else if(value.equalsIgnoreCase(INFO_BOOTLOADER.toString()       ))   return TypeOfCommand.INFO_BOOTLOADER;
            else if(value.equalsIgnoreCase(INFO_DATETIME.toString()         ))   return TypeOfCommand.INFO_DATETIME;
            else if(value.equalsIgnoreCase(INFO_DEVICE_COUNTER.toString()   ))   return TypeOfCommand.INFO_DEVICE_COUNTER;
            else if(value.equalsIgnoreCase(INFO_AUTO_BACKUP.toString()      ))   return TypeOfCommand.INFO_AUTO_BACKUP;
            else if(value.equalsIgnoreCase(INFO_STATE.toString()            ))   return TypeOfCommand.INFO_STATE;

            else if(value.equalsIgnoreCase(COMMAND_ADD_DEVICE.toString()            ))   return TypeOfCommand.COMMAND_ADD_DEVICE;
            else if(value.equalsIgnoreCase(COMMAND_REMOVE_DEVICE.toString()         ))   return TypeOfCommand.COMMAND_REMOVE_DEVICE;
            else if(value.equalsIgnoreCase(COMMAND_ADD_MASTER_DEVICE.toString()     ))   return TypeOfCommand.COMMAND_ADD_MASTER_DEVICE;
            else if(value.equalsIgnoreCase(COMMAND_REMOVE_MASTER_DEVICE.toString()  ))   return TypeOfCommand.COMMAND_REMOVE_MASTER_DEVICE;
            else if(value.equalsIgnoreCase(COMMAND_UPLOAD_FIRMWARE.toString()       ))   return TypeOfCommand.COMMAND_UPLOAD_FIRMWARE;
            else if(value.equalsIgnoreCase(COMMAND_RESTART_DEVICE.toString()        ))   return TypeOfCommand.COMMAND_RESTART_DEVICE;
            else if(value.equalsIgnoreCase(COMMAND_PING_DEVICE.toString()           ))   return TypeOfCommand.COMMAND_PING_DEVICE;

            return null;
        }

        public String get_command() {
            return command;
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Homer_Instance> find = new Finder<>(Homer_Instance.class);

}
