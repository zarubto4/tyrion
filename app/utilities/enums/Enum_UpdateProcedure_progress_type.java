package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Vytvořeno speciálně pro příchozí zprávy z Homer serveru o tom, jak probíhá update hardwaru
 *
 */
public enum Enum_UpdateProcedure_progress_type {

    @EnumValue("MAKING_BACKUP")                     MAKING_BACKUP,
    @EnumValue("TRANSFER_DATA_TO_YODA")             TRANSFER_DATA_TO_YODA,
    @EnumValue("TRANSFER_DATA_FROM_YODA_TO_DEVICE") TRANSFER_DATA_FROM_YODA_TO_DEVICE,
    @EnumValue("CHECKING_RESULT")                   CHECKING_RESULT;


    @JsonCreator
    public static Enum_UpdateProcedure_progress_type fromString(String key) {

        for(Enum_UpdateProcedure_progress_type type : Enum_UpdateProcedure_progress_type.values()) {
            if(type.name().equalsIgnoreCase(key)) {
                return type;
            }
        }

        return null;
    }

}


