package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import utilities.financial.extensions.*;
import utilities.logger.Class_Logger;

public enum Enum_ExtensionType {

    /*
    !!!
    When you add new enum value, create corresponding class in utilities.financial.extensions package,
    also add case of the new type to the switch statement below in getExtensionClass() method.
    !!!
     */

    @EnumValue("project")       project,
    @EnumValue("log")           log,
    @EnumValue("database")      database,
    @EnumValue("rest_api")      rest_api,
    @EnumValue("support")       support,
    @EnumValue("instance")      instance,
    @EnumValue("homer_server")  homer_server,
    @EnumValue("participant")   participant;

    // All classes returned from this method (in package utilities.financial.extensions) must implement Extension interface.
    public Class<? extends Extension> getExtensionClass(){
        try {

            switch (this) {
                case project:       return Extension_Project.class;
                case log:           return Extension_Log.class;
                case database:      return Extension_Database.class;
                case rest_api:      return Extension_RestApi.class;
                case support:       return Extension_Support.class;
                case instance:      return Extension_Instance.class;
                case homer_server:  return Extension_HomerServer.class;
                case participant:   return Extension_Participant.class;
                default: throw new Exception("This Extension Type is unhandled. Probably forgotten to add it to the switch statement in getExtensionClass() method.");
            }

        } catch (Exception e){
            terminal_logger.internalServerError("getExtensionClass:", e);
            return null;
        }
    }

    private static final Class_Logger terminal_logger = new Class_Logger(Enum_ExtensionType.class);
}