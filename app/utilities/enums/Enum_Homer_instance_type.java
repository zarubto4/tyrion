package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Homer_instance_type {

    @EnumValue("VIRTUAL")  VIRTUAL,
    @EnumValue("INDIVIDUAL")  INDIVIDUAL;


    public static Enum_Homer_instance_type getType(String value){

        if(value.toLowerCase().equalsIgnoreCase(VIRTUAL.toString().toLowerCase()    ))   return Enum_Homer_instance_type.VIRTUAL;
        if(value.toLowerCase().equalsIgnoreCase(INDIVIDUAL.toString().toLowerCase() ))   return Enum_Homer_instance_type.INDIVIDUAL;

        return null;
    }

}
