package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Hardware_update_state_from_Homer {

    @EnumValue("WAITING_IN_QUE")                            WAITING_IN_QUE("WAITING_IN_QUE"),
    @EnumValue("IN_PROGRESS")                               IN_PROGRESS("IN_PROGRESS"),
    @EnumValue("SUCCESSFULLY_UPDATE")                       SUCCESSFULLY_UPDATE("SUCCESSFULLY_UPDATE"),
    @EnumValue("DEVICE_WAS_OFFLINE")                        DEVICE_WAS_OFFLINE("DEVICE_WAS_OFFLINE"),
    @EnumValue("TRANSMISSION_CRC_ERROR")                    TRANSMISSION_CRC_ERROR("TRANSMISSION_CRC_ERROR"),
    @EnumValue("INVALID_DEVICE_STATE")                      INVALID_DEVICE_STATE("INVALID_DEVICE_STATE"),
    @EnumValue("YODA_WAS_OFFLINE")                          YODA_WAS_OFFLINE("YODA_WAS_OFFLINE"),
    @EnumValue("UPDATE_PROGRESS_STACK")                     UPDATE_PROGRESS_STACK("UPDATE_PROGRESS_STACK"),
    @EnumValue("DEVICE_NOT_RECONNECTED")                    DEVICE_NOT_RECONNECTED("DEVICE_NOT_RECONNECTED"),
    @EnumValue("DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION")   DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION("DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION"),
    @EnumValue("ERROR")                                     ERROR("ERROR");

    private String update_state;
    Enum_Hardware_update_state_from_Homer(String update_state) {
        this.update_state = update_state;
    }


    public static Enum_Hardware_update_state_from_Homer getUpdate_state(String value){

             if(value.equalsIgnoreCase(WAITING_IN_QUE.toString()                ))   return Enum_Hardware_update_state_from_Homer.WAITING_IN_QUE;
        else if(value.equalsIgnoreCase(IN_PROGRESS.toString()                   ))   return Enum_Hardware_update_state_from_Homer.IN_PROGRESS;
        else if(value.equalsIgnoreCase(SUCCESSFULLY_UPDATE.toString()           ))   return Enum_Hardware_update_state_from_Homer.SUCCESSFULLY_UPDATE;
        else if(value.equalsIgnoreCase(DEVICE_WAS_OFFLINE.toString()            ))   return Enum_Hardware_update_state_from_Homer.DEVICE_WAS_OFFLINE;
        else if(value.equalsIgnoreCase(INVALID_DEVICE_STATE.toString()          ))   return Enum_Hardware_update_state_from_Homer.INVALID_DEVICE_STATE;
        else if(value.equalsIgnoreCase(YODA_WAS_OFFLINE.toString()              ))   return Enum_Hardware_update_state_from_Homer.YODA_WAS_OFFLINE;

        else if(value.equalsIgnoreCase(TRANSMISSION_CRC_ERROR.toString()                    ))   return Enum_Hardware_update_state_from_Homer.TRANSMISSION_CRC_ERROR;
        else if(value.equalsIgnoreCase(UPDATE_PROGRESS_STACK.toString()                     ))   return Enum_Hardware_update_state_from_Homer.UPDATE_PROGRESS_STACK;
        else if(value.equalsIgnoreCase(DEVICE_NOT_RECONNECTED.toString()                    ))   return Enum_Hardware_update_state_from_Homer.DEVICE_NOT_RECONNECTED;
        else if(value.equalsIgnoreCase(DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION.toString()   ))   return Enum_Hardware_update_state_from_Homer.DEVICE_WAS_NOT_UPDATED_TO_RIGHT_VERSION;
        else if(value.equalsIgnoreCase(ERROR.toString()                                     ))   return Enum_Hardware_update_state_from_Homer.ERROR;

        return null;
    }
}
