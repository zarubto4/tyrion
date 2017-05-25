package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_HardwareHomerUpdate_state {

    @EnumValue("WAITING_IN_QUE")                            WAITING_IN_QUE,
    @EnumValue("IN_PROGRESS")                               IN_PROGRESS,
    @EnumValue("SUCCESSFULLY_UPDATE")                       SUCCESSFULLY_UPDATE,
    @EnumValue("DEVICE_WAS_OFFLINE")                        DEVICE_WAS_OFFLINE,
    @EnumValue("TRANSMISSION_CRC_ERROR")                    TRANSMISSION_CRC_ERROR,
    @EnumValue("INVALID_DEVICE_STATE")                      INVALID_DEVICE_STATE,
    @EnumValue("YODA_WAS_OFFLINE")                          YODA_WAS_OFFLINE,
    @EnumValue("UPDATE_PROGRESS_STACK")                     UPDATE_PROGRESS_STACK,
    @EnumValue("DEVICE_NOT_RECONNECTED")                    DEVICE_NOT_RECONNECTED,
    @EnumValue("DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION")   DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION,
    @EnumValue("ERROR")                                     ERROR;

    public static Enum_HardwareHomerUpdate_state getUpdate_state(String value){

             if(value.equalsIgnoreCase(WAITING_IN_QUE.toString()                ))   return Enum_HardwareHomerUpdate_state.WAITING_IN_QUE;
        else if(value.equalsIgnoreCase(IN_PROGRESS.toString()                   ))   return Enum_HardwareHomerUpdate_state.IN_PROGRESS;
        else if(value.equalsIgnoreCase(SUCCESSFULLY_UPDATE.toString()           ))   return Enum_HardwareHomerUpdate_state.SUCCESSFULLY_UPDATE;
        else if(value.equalsIgnoreCase(DEVICE_WAS_OFFLINE.toString()            ))   return Enum_HardwareHomerUpdate_state.DEVICE_WAS_OFFLINE;
        else if(value.equalsIgnoreCase(INVALID_DEVICE_STATE.toString()          ))   return Enum_HardwareHomerUpdate_state.INVALID_DEVICE_STATE;
        else if(value.equalsIgnoreCase(YODA_WAS_OFFLINE.toString()              ))   return Enum_HardwareHomerUpdate_state.YODA_WAS_OFFLINE;

        else if(value.equalsIgnoreCase(TRANSMISSION_CRC_ERROR.toString()                    ))   return Enum_HardwareHomerUpdate_state.TRANSMISSION_CRC_ERROR;
        else if(value.equalsIgnoreCase(UPDATE_PROGRESS_STACK.toString()                     ))   return Enum_HardwareHomerUpdate_state.UPDATE_PROGRESS_STACK;
        else if(value.equalsIgnoreCase(DEVICE_NOT_RECONNECTED.toString()                    ))   return Enum_HardwareHomerUpdate_state.DEVICE_NOT_RECONNECTED;
        else if(value.equalsIgnoreCase(DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION.toString()   ))   return Enum_HardwareHomerUpdate_state.DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION;
        else if(value.equalsIgnoreCase(ERROR.toString()                                     ))   return Enum_HardwareHomerUpdate_state.ERROR;

        return null;
    }
}
