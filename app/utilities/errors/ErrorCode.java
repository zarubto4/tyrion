package utilities.errors;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.ebean.annotation.EnumValue;

public enum ErrorCode {


    // ---- Instance status -----
    @EnumValue("INSTANCE_NOT_FOUND")              INSTANCE_NOT_FOUND( 100 , "Instance not found"),
    @EnumValue("INSTANCE_ALREADY_EXIST")          INSTANCE_ALREADY_EXIST( 101 , "Instance already exist"),
    @EnumValue("BLOCKO_JSON_ERROR")               BLOCKO_JSON_ERROR( 102 , "Json for Blocko with Blocko program is not supporter by Blocko Engine! Server side Error!"),
    @EnumValue("HARDWARE_ALREADY_IN_ANOTHER_INSTANCE")  HARDWARE_ALREADY_IN_ANOTHER_INSTANCE( 103 , "Hardware is already registered in instance"),
    @EnumValue("HARDWARE_ALREADY_ADDED")          HARDWARE_ALREADY_ADDED( 104 , "Hardware is already added into instance"),
    @EnumValue("HARDWARE_NOT_IN_INSTANCE")        HARDWARE_NOT_IN_INSTANCE( 105 , "Hardware is not registered in instance"),

    // ---- Hardware status -----
    @EnumValue("HARDWARE_IS_OFFLINE")             HARDWARE_IS_OFFLINE( 200 , "Yoda is not Connected to Homer"),
    @EnumValue("BINARY_FILE_NOT_VALID")           BINARY_FILE_NOT_VALID( 201 , "Incoming firmware binary file is not valid"),
    @EnumValue("BINARY_TYPE_NOT_RECOGNIZE")       BINARY_TYPE_NOT_RECOGNIZE( 202 , "Binary type not recognize!"),
    @EnumValue("BINARY_FILE_NOT_FOUND")           BINARY_FILE_NOT_FOUND( 203 , "Binary file not found"),
    @EnumValue("UPDATE_PROCEDURE_TIMEOUT")        UPDATE_PROCEDURE_TIMEOUT( 204, "Update was too long"),


    // ----- Update status ----
    @EnumValue("NEW_VERSION_DOESNT_MATCH")        NEW_VERSION_DOESNT_MATCH( 984, "Version not match with required"),
    @EnumValue("NUMBER_OF_ATTEMPTS_EXCEEDED")     NUMBER_OF_ATTEMPTS_EXCEEDED( 987, "Number of attempts exceeded"),

    // ---- Tyrion status ------
    @EnumValue("TYRION_IS_OFFLINE")               TYRION_IS_OFFLINE( 300, "Tyrion is offline"),
    @EnumValue("TOKEN_IS_INVALID")                TOKEN_IS_INVALID( 301, "Token is invalid"),
    @EnumValue("INVALID_MESSAGE")                 INVALID_MESSAGE( 400, "Message is not valid. Missing or incorrect type of values"),


    // ---- Homer status ------
    @EnumValue("HOMER_IS_OFFLINE")                      HOMER_IS_OFFLINE( 502, "Homer is offline"),

    @EnumValue("WEBSOCKET_TIME_OUT_EXCEPTION")          WEBSOCKET_TIME_OUT_EXCEPTION ( 503 ,  "Time for sending Message with required response is up!"),
    @EnumValue("HOMER_NOT_EXIST")                       HOMER_NOT_EXIST ( 504 ,  "Server not Exist!"),
    @EnumValue("HOMER_SERVER_NOT_SET_FOR_HARDWARE")     HOMER_SERVER_NOT_SET_FOR_HARDWARE( 502, "Hardware is never loged before, or Tyrion didnt know where device is"),
    @EnumValue("HOMER_SERVER_NOT_SET_FOR_INSTANCE")     HOMER_SERVER_NOT_SET_FOR_INSTANCE( 503, "Instance has not set own server location"),

    @EnumValue("UNKNOWN_TOPIC")                         UNKNOWN_TOPIC                           ( 1, "Undefined Topic on HW"),
    @EnumValue("MISSING_LABEL")                         MISSING_LABEL                           ( 2, "Missing some Label"),
    @EnumValue("UNKNOWN_LABEL")                         UNKNOWN_LABEL                           ( 3, "Not recognize some Label"),
    @EnumValue("MALLOC ERROR")                          MALLOC_ERROR                            ( 4, "Not recognize some Label"),
    @EnumValue("ERROR")                                 ERROR                                   ( 50 , "Undefined Error - In Logger"),


    @EnumValue("CRITICAL_TYRION_SERVER_SIDE_ERROR")     CRITICAL_TYRION_SERVER_SIDE_ERROR( 400, "Something happen on Tyrion server side."),
    @EnumValue("COMPILATION_SERVER_IS_OFFLINE")         COMPILATION_SERVER_IS_OFFLINE(532, "Compilation server is offline");

    /*

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
    @EnumValue("ERROR_50004")                           ERROR_50004( 50004 , "Device id is missing in master_hardware list"),
    @EnumValue("ERROR_50005")                           ERROR_50005( 50005 , "Bus critical error - collision on bus cable"),

    @EnumValue("ERROR_60001")                           ERROR_60001( 600001 , "Write to external memory failed"),
    @EnumValue("ERROR_60002")                           ERROR_60002( 600002 , "Erase of external memory failed"),

    @EnumValue("INTMEM_READ_ERROR")                     INTMEM_READ_ERROR( 70000 , "Read from internal memory failed"),
    @EnumValue("INTMEM_WRITE_ERROR")                    INTMEM_WRITE_ERROR( 70001 , "Write to internal memory failed"),
    @EnumValue("INTMEM_ERASE_ERROR")                    INTMEM_ERASE_ERROR( 70002 , "Erase of internal memory failed"),

    @EnumValue("JSON_INVALID")                          JSON_INVALID( 10000 , "Missing some Label"),
    @EnumValue("EMPTY_ARRAY")                           EMPTY_ARRAY( 10001 , "Array of commands is empty");

    */
    

    @JsonCreator
    public static ErrorCode fromString(String key) {
        for (ErrorCode action : ErrorCode.values()) {
            if (action.name().equalsIgnoreCase(key)) {
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
