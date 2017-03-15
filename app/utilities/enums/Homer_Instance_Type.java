package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Homer_Instance_Type {

    @EnumValue("VIRTUAL")  VIRTUAL,
    @EnumValue("INDIVIDUAL")  INDIVIDUAL;


    public static Homer_Instance_Type getType(String value){


        if(value.toLowerCase().equalsIgnoreCase(VIRTUAL.toString().toLowerCase()    ))   return Homer_Instance_Type.VIRTUAL;
        if(value.toLowerCase().equalsIgnoreCase(INDIVIDUAL.toString().toLowerCase() ))   return Homer_Instance_Type.INDIVIDUAL;

        return null;
    }

}
