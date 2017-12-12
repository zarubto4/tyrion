package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_HardwareHomerUpdate_state {


    // events
    @EnumValue("UPDATE_STARTED")                    UPDATE_STARTED,
    @EnumValue("UPLOAD_STARTED")                    UPLOAD_STARTED,
    @EnumValue("UPDATE_DONE")                       UPDATE_DONE,
    @EnumValue("UPDATE_FAILED")                     UPDATE_FAILED,


    // errors
    @EnumValue("ALREADY_SAME")                       ALREADY_SAME,
    @EnumValue("ERROR_NO_BINNARY")                   ERROR_NO_BINNARY,
    @EnumValue("WATCHDOG_TIMEOUT")                   WATCHDOG_TIMEOUT,
    @EnumValue("ERROR_COMMUNICATION_FAILED")         ERROR_COMMUNICATION_FAILED,
    @EnumValue("ERROR_UPLOAD_START")                 ERROR_UPLOAD_START,
    @EnumValue("ERROR_UPLOAD_DATA")                  ERROR_UPLOAD_DATA,
    @EnumValue("ERROR_UPLOAD_END")                   ERROR_UPLOAD_END,
    @EnumValue("ERROR_UPDATE_START")                 ERROR_UPDATE_START,
    @EnumValue("ERROR_UPDATE_STATUS")                ERROR_UPDATE_STATUS,
    @EnumValue("ERROR_RESTART_STATUS")               ERROR_RESTART_STATUS,
    @EnumValue("ERROR_INFO_STATUS")                  ERROR_INFO_STATUS,
    @EnumValue("NEW_VERSION_DOESNT_MATCH")           NEW_VERSION_DOESNT_MATCH,
    
    // phase
    @EnumValue("PHASE_UPLOAD_START")                  PHASE_UPLOAD_START,      // Creating backup
    @EnumValue("PHASE_FLASH_ERASING")                 PHASE_FLASH_ERASING,      // Creating backup
    @EnumValue("PHASE_FLASH_ERASED")                  PHASE_FLASH_ERASED,      // Creating backup
    @EnumValue("PHASE_WAITING")                       PHASE_WAITING,
    @EnumValue("PHASE_UPLOADING")                     PHASE_UPLOADING,
    @EnumValue("PHASE_UPLOAD_DONE")                   PHASE_UPLOAD_DONE,
    @EnumValue("PHASE_RESTARTING")                    PHASE_RESTARTING,
    @EnumValue("PHASE_CONNECTED_AFTER_RESTART")       PHASE_CONNECTED_AFTER_RESTART,
    @EnumValue("PHASE_UPDATE_DONE")                   PHASE_UPDATE_DONE;


    public static Enum_HardwareHomerUpdate_state get_state(String value){

        if(value == null){return null;}

        // events
        else if(value.equalsIgnoreCase(UPDATE_STARTED.toString()))              return Enum_HardwareHomerUpdate_state.UPDATE_STARTED;
        else if(value.equalsIgnoreCase(UPLOAD_STARTED.toString()))              return Enum_HardwareHomerUpdate_state.UPLOAD_STARTED;
        else if(value.equalsIgnoreCase(UPDATE_DONE.toString()))                 return Enum_HardwareHomerUpdate_state.UPDATE_DONE;
        else if(value.equalsIgnoreCase(UPDATE_FAILED.toString()))               return Enum_HardwareHomerUpdate_state.UPDATE_FAILED;

        // phase
        else if(value.equalsIgnoreCase(PHASE_UPLOAD_START.toString()))          return Enum_HardwareHomerUpdate_state.PHASE_UPLOAD_START;
        else if(value.equalsIgnoreCase(PHASE_FLASH_ERASING.toString()))         return Enum_HardwareHomerUpdate_state.PHASE_FLASH_ERASING;
        else if(value.equalsIgnoreCase(PHASE_FLASH_ERASED.toString()))          return Enum_HardwareHomerUpdate_state.PHASE_FLASH_ERASED;
        else if(value.equalsIgnoreCase(PHASE_WAITING.toString()))               return Enum_HardwareHomerUpdate_state.PHASE_WAITING;
        else if(value.equalsIgnoreCase(PHASE_UPLOADING.toString()))             return Enum_HardwareHomerUpdate_state.PHASE_UPLOADING;
        else if(value.equalsIgnoreCase(PHASE_UPLOAD_DONE.toString()))           return Enum_HardwareHomerUpdate_state.PHASE_UPLOAD_DONE;
        else if(value.equalsIgnoreCase(PHASE_RESTARTING.toString()))              return Enum_HardwareHomerUpdate_state.PHASE_RESTARTING;
        else if(value.equalsIgnoreCase(PHASE_CONNECTED_AFTER_RESTART.toString())) return Enum_HardwareHomerUpdate_state.PHASE_CONNECTED_AFTER_RESTART;
        else if(value.equalsIgnoreCase(PHASE_UPDATE_DONE.toString()))             return Enum_HardwareHomerUpdate_state.PHASE_UPDATE_DONE;

        // errors
        else if(value.equalsIgnoreCase(ALREADY_SAME.toString()))                 return Enum_HardwareHomerUpdate_state.ALREADY_SAME;
        else if(value.equalsIgnoreCase(ERROR_NO_BINNARY.toString()))             return Enum_HardwareHomerUpdate_state.ERROR_NO_BINNARY;
        else if(value.equalsIgnoreCase(WATCHDOG_TIMEOUT.toString()))             return Enum_HardwareHomerUpdate_state.WATCHDOG_TIMEOUT;
        else if(value.equalsIgnoreCase(ERROR_COMMUNICATION_FAILED.toString() ))  return Enum_HardwareHomerUpdate_state.ERROR_COMMUNICATION_FAILED;
        else if(value.equalsIgnoreCase(ERROR_UPLOAD_START.toString()))           return Enum_HardwareHomerUpdate_state.ERROR_UPLOAD_START;
        else if(value.equalsIgnoreCase(ERROR_UPLOAD_DATA.toString()))            return Enum_HardwareHomerUpdate_state.ERROR_UPLOAD_DATA;
        else if(value.equalsIgnoreCase(ERROR_UPLOAD_END.toString()))             return Enum_HardwareHomerUpdate_state.ERROR_UPLOAD_END;
        else if(value.equalsIgnoreCase(ERROR_UPDATE_START.toString()))           return Enum_HardwareHomerUpdate_state.ERROR_UPDATE_START;
        else if(value.equalsIgnoreCase(ERROR_UPDATE_STATUS.toString() ))         return Enum_HardwareHomerUpdate_state.ERROR_UPDATE_STATUS;
        else if(value.equalsIgnoreCase(ERROR_RESTART_STATUS.toString()))         return Enum_HardwareHomerUpdate_state.ERROR_RESTART_STATUS;
        else if(value.equalsIgnoreCase(ERROR_INFO_STATUS.toString()))            return Enum_HardwareHomerUpdate_state.ERROR_INFO_STATUS;
        else if(value.equalsIgnoreCase(NEW_VERSION_DOESNT_MATCH.toString()))     return Enum_HardwareHomerUpdate_state.NEW_VERSION_DOESNT_MATCH;


        return null;
    }
}
