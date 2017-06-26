package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Homer_instance_record_status {

    @EnumValue("FUTURE")  FUTURE,
    @EnumValue("NOW")  NOW,
    @EnumValue("HISTORY")  HISTORY;

    public static Enum_Homer_instance_record_status getType(String value){

        if(value.toLowerCase().equalsIgnoreCase(FUTURE.toString().toLowerCase()    ))   return Enum_Homer_instance_record_status.FUTURE;
        if(value.toLowerCase().equalsIgnoreCase(NOW.toString().toLowerCase() ))   return Enum_Homer_instance_record_status.NOW;
        if(value.toLowerCase().equalsIgnoreCase(HISTORY.toString().toLowerCase() ))   return Enum_Homer_instance_record_status.HISTORY;
        return null;
    }

}
