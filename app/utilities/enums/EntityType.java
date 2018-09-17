package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum EntityType {

    @EnumValue("PERSON")                    PERSON,
    @EnumValue("PRODUCT")                   PRODUCT,
    @EnumValue("PROJECT")                   PROJECT,
    @EnumValue("FIRMWARE")                  FIRMWARE,
    @EnumValue("FIRMWARE_VERSION")          FIRMWARE_VERSION,
    @EnumValue("LIBRARY")                   LIBRARY,
    @EnumValue("LIBRARY_VERSION")           LIBRARY_VERSION,
    @EnumValue("WIDGET")                    WIDGET,
    @EnumValue("WIDGET_VERSION")            WIDGET_VERSION,
    @EnumValue("GRID_PROJECT")              GRID_PROJECT,
    @EnumValue("GRID_PROGRAM")              GRID_PROGRAM,
    @EnumValue("GRID_PROGRAM_VERSION")      GRID_PROGRAM_VERSION,
    @EnumValue("BLOCK")                     BLOCK,
    @EnumValue("BLOCK_VERSION")             BLOCK_VERSION,
    @EnumValue("BLOCKO_PROGRAM")            BLOCKO_PROGRAM,
    @EnumValue("BLOCKO_PROGRAM_VERSION")    BLOCKO_PROGRAM_VERSION,
    @EnumValue("INSTANCE")                  INSTANCE,
    @EnumValue("INSTANCE_SNAPSHOT")         INSTANCE_SNAPSHOT,
    @EnumValue("HARDWARE")                  HARDWARE,
    @EnumValue("HARDWARE_GROUP")            HARDWARE_GROUP

    // TODO all objects
}
