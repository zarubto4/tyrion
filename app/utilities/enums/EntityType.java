package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum EntityType {

    @EnumValue("ARTICLE")                   ARTICLE,
    @EnumValue("AUTHORIZATION_TOKEN")       AUTHORIZATION_TOKEN,
    @EnumValue("BOOTLOADER")                BOOTLOADER,
    @EnumValue("COMPILER")                  COMPILER,
    @EnumValue("GARFIELD")                  GARFIELD,
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
    @EnumValue("HARDWARE_GROUP")            HARDWARE_GROUP,
    @EnumValue("HARDWARE_UPDATE")           HARDWARE_UPDATE,
    @EnumValue("HARDWARE_BATCH")            HARDWARE_BATCH,
    @EnumValue("HARDWARE_TYPE")             HARDWARE_TYPE,
    @EnumValue("INVITATION")                INVITATION,
    @EnumValue("INVOICE")                   INVOICE,
    @EnumValue("PROCESSOR")                 PROCESSOR,
    @EnumValue("PRODUCER")                  PRODUCER,
    @EnumValue("NOTIFICATION")              NOTIFICATION,
    @EnumValue("ROLE")                      ROLE,
    @EnumValue("UPDATE_PROCEDURE")          UPDATE_PROCEDURE,
    @EnumValue("PRODUCT_EXTENSION")         PRODUCT_EXTENSION,
    @EnumValue("ERROR")                     ERROR,
    @EnumValue("TARIFF")                    TARIFF,
    @EnumValue("TARIFF_EXTENSION")          TARIFF_EXTENSION,
    @EnumValue("PAYMENT_DETAILS")           PAYMENT_DETAILS,
    @EnumValue("HOMER")                     HOMER,
    @EnumValue("CUSTOMER")                  CUSTOMER,
    @EnumValue("EMPLOYEE")                  EMPLOYEE,


    // TODO all objects
}
