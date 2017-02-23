package utilities.errors;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum ErrorCode {

    /**
    static UNKNOWN_TOPIC                            = new ErrorCode( 1 , "Neznámí Topic");
    static MISSING_LABEL                            = new ErrorCode( 2 , "Missing some Label");

    static ERROR                                    = new ErrorCode( 50 , "Undefined Error - In Logger");

    static INSTANCE_NOT_FOUND                       = new ErrorCode( 70 , "Instance not found");
    static DEVICE_NOT_FOUND                         = new ErrorCode( 71 , "Device not found");
    static YODA_ALREADY_REGISTERED                  = new ErrorCode( 80 , "YODA is already registred");
    static YODA_NOT_REGISTERED_UNDER_INSTANCE       = new ErrorCode( 81 , "YODA is not registered under instance");

    static DEVICE_IS_NOT_REGISTRED_UNDER_INSTANCE   = new ErrorCode( 202 , "Yoda or Device is not registered under instance");
    static DEVICE_IS_NOT_YODA                       = new ErrorCode( 203 , "Device is not Main Board (Yoda)");
    static YODA_IS_OFFLINE                          = new ErrorCode( 204 , "Yoda is not Connected to Homer");
    static DEVICE_IS_OFFLINE                        = new ErrorCode( 205 , "Device is not Connected to Yoda");
    static DEVICE_IS_NOT_ONLINE                     = new ErrorCode( 206 , "Device is not Connected to Yoda");
    static DEVICE_CONNECTION_ERROR                  = new ErrorCode( 207 , "Device is not Connected to Yoda");
    static DEVICE_WAS_ILLGL_CONNTD_AND_REMOVER      = new ErrorCode( 208 , "Device was illegally connected and remover from Yoda");
    static DEVICE_NOT_REGISTERED_UNDER_YODA         = new ErrorCode( 209 , "Device is connected to yoda, but communication is prohibited by homer");
    static DEVICE_NOT_SAVED_UNDER_YODA              = new ErrorCode( 2109 , "Device is Connected to Yoda - but not saved - skipped by Homer");

    static ADD_DEVICE_YODA_NOT_CONNECTED            = new ErrorCode( 230 , "Yoda is not connected now, Device will be added as soon as possible");
    static REMOVE_DEVICE_YODA_NOT_CONNECTED         = new ErrorCode( 231 , "Yoda is not connected now, Device will be removed as soon as possible");


    static BINARY_FILE_NOT_VALID        = new ErrorCode( 300 , "Incoming firmware binary file is not valid");

    static BLOCKO_JSON_ERROR            = new ErrorCode( 570 , "Json for Blocko with Blocko program is not supporter by Blocko Engine! Server side Error!");
    static FIRMWARE_NOT_RECOGNIZE       = new ErrorCode( 580 , "Firmware type not recognize!");
    static COMMAND_NOT_RECOGNIZE        = new ErrorCode( 581 ,  "Command type not recognize!");

    static TYRION_IS_OFFLINE            = new ErrorCode( 600 ,  "Tyrion is offline!!!");
    static TOKEN_IS_INVALID             = new ErrorCode( 601 ,  "Token is invalid!!!");

    static ERROR_30001       = new ErrorCode( 30001 , "Unknow full-id");
    static ERROR_30002       = new ErrorCode( 30002 , "Unknow shot-id");

    static ERROR_30003       = new ErrorCode( 30003 , "CRC Error ");
    static ERROR_30004       = new ErrorCode( 30004 , "CRC Error ");
    static ERROR_30005       = new ErrorCode( 30005 , "Add device failed");
    static ERROR_30006       = new ErrorCode( 30006 , "Remove device failed");
    static ERROR_30007       = new ErrorCode( 30007 , "Backup failed");
    static ERROR_30008       = new ErrorCode( 30008 , "Invalid device state");

    static ERROR_50000       = new ErrorCode( 50000 , "Device is not saved");
    static ERROR_50001       = new ErrorCode( 50001 , "Device with unknow interface");
    static ERROR_50002       = new ErrorCode( 50002 , "Invalid message for device");
    static ERROR_50003       = new ErrorCode( 50003 , "Device is not enumerated yet");
    static ERROR_50004	     = new ErrorCode( 50004 , "Device id is missing in yoda list");
    static ERROR_50005	     = new ErrorCode( 50005 , "Bus critical error - collision on bus cable");

    static ERROR_60001	     = new ErrorCode( 600001 , "Missing Part in packet transport to YODA");

    static JSON_INVALID	     = new ErrorCode( 10000 , "Missing some Label");
    static EMPTY_ARRAY	     = new ErrorCode( 10001 , "Array of commands is empty");

    */



    @EnumValue("accept_project_invitation")   accept_project_invitation,
    @EnumValue("reject_project_invitation")   reject_project_invitation,
    @EnumValue("confirm_notification")        confirm_notification;

    @JsonCreator
    public static ErrorCode fromString(String key) {
        for(ErrorCode action : ErrorCode.values()) {
            if(action.name().equalsIgnoreCase(key)) {
                return action;
            }
        }
        return null;
    }
}
