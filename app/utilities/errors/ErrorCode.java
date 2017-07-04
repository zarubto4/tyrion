package utilities.errors;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum ErrorCode {

    // Homer Nové
    @EnumValue("UNAUTHORIZED_CONNECTION")               UNAUTHORIZED_CONNECTION( 301, "Homer Server websocket is not authorized"),
    
    @EnumValue("UNKNOWN_TOPIC")                         UNKNOWN_TOPIC                           ( 1, "Undefined Topic on HW"),
    @EnumValue("MISSING_LABEL")                         MISSING_LABEL                           ( 2, "Missing some Label"),
    @EnumValue("ERROR")                                 ERROR                                   ( 50 , "Undefined Error - In Logger"),
    @EnumValue("INSTANCE_NOT_FOUND")                    INSTANCE_NOT_FOUND                      ( 70 , "Instance not found"),
    @EnumValue("DEVICE_NOT_FOUND")                      DEVICE_NOT_FOUND                        ( 71 , "Device not found"),
    @EnumValue("YODA_ALREADY_REGISTERED")               YODA_ALREADY_REGISTERED                 ( 80 , "YODA is already registred"),
    @EnumValue("YODA_NOT_REGISTERED_UNDER_INSTANCE")    YODA_NOT_REGISTERED_UNDER_INSTANCE      ( 81 , "YODA is not registered under instance"),

    @EnumValue("DEVICE_IS_NOT_REGISTRED_UNDER_INSTANCE")DEVICE_IS_NOT_REGISTRED_UNDER_INSTANCE  ( 202 , "Yoda or Device is not registered under instance"),
    @EnumValue("DEVICE_IS_NOT_YODA")                    DEVICE_IS_NOT_YODA                      ( 203 , "Device is not Main Board (Yoda)"),
    @EnumValue("YODA_IS_OFFLINE")                       YODA_IS_OFFLINE                         ( 204 , "Yoda is not Connected to Homer"),
    @EnumValue("DEVICE_IS_OFFLINE")                     DEVICE_IS_OFFLINE                       ( 205 , "Device is not Connected to Yoda"),
    @EnumValue("DEVICE_IS_NOT_ONLINE")                  DEVICE_IS_NOT_ONLINE                    ( 206 , "Device is not Connected to Yoda"),
    @EnumValue("DEVICE_CONNECTION_ERROR")               DEVICE_CONNECTION_ERROR                 ( 207 , "Device is not Connected to Yoda"),
    @EnumValue("DEVICE_WAS_ILLGL_CONNTD_AND_REMOVER")   DEVICE_WAS_ILLGL_CONNTD_AND_REMOVER     ( 208 , "Device was illegally connected and remover from Yoda"),
    @EnumValue("DEVICE_NOT_REGISTERED_UNDER_YODA")      DEVICE_NOT_REGISTERED_UNDER_YODA        ( 209 , "Device is connected to yoda, but communication is prohibited by homer"),
    @EnumValue("DEVICE_NOT_SAVED_UNDER_YODA")           DEVICE_NOT_SAVED_UNDER_YODA             ( 2109 , "Device is Connected to Yoda - but not saved - skipped by Homer"),

    @EnumValue("ADD_DEVICE_YODA_NOT_CONNECTED")         ADD_DEVICE_YODA_NOT_CONNECTED           ( 230 , "Yoda is not connected now, Device will be added as soon as possible"),
    @EnumValue("REMOVE_DEVICE_YODA_NOT_CONNECTED")      REMOVE_DEVICE_YODA_NOT_CONNECTED        ( 231 , "Yoda is not connected now, Device will be removed as soon as possible"),
    @EnumValue("NUMBER_OF_ATTEMPTS_EXCEEDED")           NUMBER_OF_ATTEMPTS_EXCEEDED             ( 232 , "Server attempted to do an update procedure on Device. But the maximum number of reps exceeded."),

    @EnumValue("BINARY_FILE_NOT_VALID")                 BINARY_FILE_NOT_VALID                   ( 300 , "Incoming firmware binary file is not valid"),

    @EnumValue("BLOCKO_JSON_ERROR")                     BLOCKO_JSON_ERROR                       ( 570 , "Json for Blocko with Blocko program is not supporter by Blocko Engine! Server side Error!"),
    @EnumValue("FIRMWARE_NOT_RECOGNIZE")                FIRMWARE_NOT_RECOGNIZE                  ( 580 , "Firmware type not recognize!"),
    @EnumValue("COMMAND_NOT_RECOGNIZE")                 COMMAND_NOT_RECOGNIZE                   ( 581 ,  "Command type not recognize!"),

    @EnumValue("TYRION_IS_OFFLINE")                     TYRION_IS_OFFLINE                       ( 600 ,  "Tyrion is offline!!!"),
    @EnumValue("TOKEN_IS_INVALID")                      TOKEN_IS_INVALID                        ( 601 ,  "Token is invalid!!!"),

    @EnumValue("UPDATE_PROCEDURE_TIMEOUT")              UPDATE_PROCEDURE_TIMEOUT                ( 650 ,  "Timeout exception in update procedure"),
    @EnumValue("WEBSOCKET_TIME_OUT_EXCEPTION")          WEBSOCKET_TIME_OUT_EXCEPTION            ( 670 ,  "Time for sending Message with required response is up!"),


    @EnumValue("ERROR_30001")                           ERROR_30001( 30001 , "Unknow full-id"),
    @EnumValue("ERROR_30002")                           ERROR_30002( 30002 , "Unknow shot-id"),

    @EnumValue("ERROR_30003")                           ERROR_30003( 30003 , "CRC Error "),
    @EnumValue("ERROR_30004")                           ERROR_30004( 30004 , "CRC Error "),
    @EnumValue("ERROR_30005")                           ERROR_30005( 30005 , "Add device failed"),
    @EnumValue("ERROR_30006")                           ERROR_30006( 30006 , "Remove device failed"),
    @EnumValue("ERROR_30007")                           ERROR_30007( 30007 , "Backup failed"),
    @EnumValue("ERROR_30008")                           ERROR_30008( 30008 , "Invalid device state"),
    @EnumValue("ERROR_30009")                           ERROR_30009( 30009 , "Attempt to upload binary which is already uploaded"),
    @EnumValue("ERROR_30010")                           ERROR_30010( 30010 , "Attempt to upload/start while upload/start is in progress"),

    @EnumValue("ERROR_50000")                           ERROR_50000( 50000 , "Device is not saved"),
    @EnumValue("ERROR_50001")                           ERROR_50001( 50001 , "Device with unknow interface"),
    @EnumValue("ERROR_50002")                           ERROR_50002( 50002 , "Invalid message for device"),
    @EnumValue("ERROR_50003")                           ERROR_50003( 50003 , "Device is not enumerated yet"),
    @EnumValue("ERROR_50004")                           ERROR_50004( 50004 , "Device id is missing in yoda list"),
    @EnumValue("ERROR_50005")                           ERROR_50005( 50005 , "Bus critical error - collision on bus cable"),

    @EnumValue("ERROR_60001")                           ERROR_60001( 600001 , "Write to external memory failed"),
    @EnumValue("ERROR_60002")                           ERROR_60002( 600002 , "Erase of external memory failed"),

    @EnumValue("INTMEM_READ_ERROR")                     INTMEM_READ_ERROR( 70000 , "Read from internal memory failed"),
    @EnumValue("INTMEM_WRITE_ERROR")                    INTMEM_WRITE_ERROR( 70001 , "Write to internal memory failed"),
    @EnumValue("INTMEM_ERASE_ERROR")                    INTMEM_ERASE_ERROR( 70002 , "Erase of internal memory failed"),

    @EnumValue("JSON_INVALID")                          JSON_INVALID( 10000 , "Missing some Label"),
    @EnumValue("EMPTY_ARRAY")                           EMPTY_ARRAY( 10001 , "Array of commands is empty");

    

    @JsonCreator
    public static ErrorCode fromString(String key) {
        for(ErrorCode action : ErrorCode.values()) {
            if(action.name().equalsIgnoreCase(key)) {
                return action;
            }
        }
        return null;
    }

    private final Integer error_code;
    private final String error_message;
    
    public Integer error_code() {
        return error_code;
    }

    public String error_message() {
        return error_message;
    }

    private ErrorCode(Integer error_code, String error_message) {
        this.error_code = error_code;
        this.error_message = error_message;
    }
}
