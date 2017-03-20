package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Garfield_burning_state {

    @EnumValue("complete")        complete,         // Stav kdy je device 100% správně naprogramovaný a odzkoušený
    @EnumValue("in_progress")     in_progress,      // Aktuálně se procedura vykonává
    @EnumValue("broken_device")   broken_device,    //
    @EnumValue("unknown_error")   unknown_error;


    public static Enum_Garfield_burning_state get_state(String value){

        if(value.equalsIgnoreCase("complete")) return complete;
        else if(value.equalsIgnoreCase("in_progress")) return in_progress;
        else if(value.equalsIgnoreCase("broken_device")) return broken_device;
        else if(value.equalsIgnoreCase("unknown_error")) return unknown_error;
        else return null;
    }

}
