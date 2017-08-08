package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_HardwareHomerUpdate_state {


    // events
    @EnumValue("UPDATE_STARTED")                    UPDATE_STARTED,
    @EnumValue("ERASING_FLASH_STARTED")             ERASING_FLASH_STARTED,      // Creating backup
    @EnumValue("UPLOAD_STARTED")                    UPLOAD_STARTED,
    @EnumValue("SENDING_PART")                      SENDING_PART,
    @EnumValue("UPLOAD_END")                        UPLOAD_END,
    @EnumValue("RESTART_SENT")                      RESTART_SENT,
    @EnumValue("CONNECTED_AFTER_RESTART")           CONNECTED_AFTER_RESTART,
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
    @EnumValue("PHASE_WAITING")                             PHASE_WAITING,
    @EnumValue("PHASE_FLASH_ERASING")                       PHASE_FLASH_ERASING,
    @EnumValue("PHASE_UPLOADING")                           PHASE_UPLOADING,
    @EnumValue("PHASE_RESTARTING")                          PHASE_RESTARTING;



    public static Enum_HardwareHomerUpdate_state get_state(String value){

        if(value == null){return null;}

        // events
        else if(value.equalsIgnoreCase(UPDATE_STARTED.toString()))              return Enum_HardwareHomerUpdate_state.UPDATE_STARTED;
        else if(value.equalsIgnoreCase(ERASING_FLASH_STARTED.toString()))       return Enum_HardwareHomerUpdate_state.ERASING_FLASH_STARTED;
        else if(value.equalsIgnoreCase(UPLOAD_STARTED.toString()))              return Enum_HardwareHomerUpdate_state.UPLOAD_STARTED;
        else if(value.equalsIgnoreCase(SENDING_PART.toString()))                return Enum_HardwareHomerUpdate_state.SENDING_PART;
        else if(value.equalsIgnoreCase(UPLOAD_END.toString()))                  return Enum_HardwareHomerUpdate_state.UPLOAD_END;
        else if(value.equalsIgnoreCase(RESTART_SENT.toString() ))               return Enum_HardwareHomerUpdate_state.RESTART_SENT;
        else if(value.equalsIgnoreCase(CONNECTED_AFTER_RESTART.toString()))     return Enum_HardwareHomerUpdate_state.CONNECTED_AFTER_RESTART;
        else if(value.equalsIgnoreCase(UPDATE_DONE.toString()))                 return Enum_HardwareHomerUpdate_state.UPDATE_DONE;
        else if(value.equalsIgnoreCase(UPDATE_FAILED.toString()))               return Enum_HardwareHomerUpdate_state.UPDATE_FAILED;


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


        // phase
        else if(value.equalsIgnoreCase(PHASE_WAITING.toString()))              return Enum_HardwareHomerUpdate_state.PHASE_WAITING;
        else if(value.equalsIgnoreCase(PHASE_FLASH_ERASING.toString() ))       return Enum_HardwareHomerUpdate_state.PHASE_FLASH_ERASING;
        else if(value.equalsIgnoreCase(PHASE_UPLOADING.toString()))            return Enum_HardwareHomerUpdate_state.PHASE_UPLOADING;
        else if(value.equalsIgnoreCase(PHASE_RESTARTING.toString()))           return Enum_HardwareHomerUpdate_state.PHASE_RESTARTING;

        return null;
    }
}
