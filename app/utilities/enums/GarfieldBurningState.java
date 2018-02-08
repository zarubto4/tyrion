package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum GarfieldBurningState {

    @EnumValue("COMPLETE")        COMPLETE,         // Stav kdy je device 100% správně naprogramovaný a odzkoušený
    @EnumValue("IN_PROGRESS")     IN_PROGRESS,      // Aktuálně se procedura vykonává
    @EnumValue("BROKEN_DEVICE")   BROKEN_DEVICE,    //
    @EnumValue("UNKNOWN_ERROR")   UNKNOWN_ERROR;


    public static GarfieldBurningState get_state(String value) {

        if (value.equalsIgnoreCase("COMPLETE")) return COMPLETE;
        else if (value.equalsIgnoreCase("IN_PROGRESS")) return IN_PROGRESS;
        else if (value.equalsIgnoreCase("broken_device")) return BROKEN_DEVICE;
        else if (value.equalsIgnoreCase("unknown_error")) return UNKNOWN_ERROR;
        else return null;
    }

}
