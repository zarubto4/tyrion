package utilities.enums;

import io.ebean.annotation.EnumValue;
import play.Configuration;
import utilities.financial.extensions.extensions.*;
import utilities.logger.Logger;

public enum ExtensionType {

    /*
    !!!
    When you add new enum value, create corresponding class in utilities.financial.extensions package,
    also add case of the new type to the switch statement below in getExtensionClass() method.
    !!!
     */

    @EnumValue("project")       PROJECT,
    @EnumValue("log")           LOG,
    @EnumValue("database")      DATABASE,
    @EnumValue("rest_api")      REST_API,
    @EnumValue("support")       SUPPORT,
    @EnumValue("instance")      INSTANCE,
    @EnumValue("homer_server")  HOMER_SERVER,
    @EnumValue("participant")   PARTICIPANT;

    // All classes returned from this method (in package utilities.financial.extensions) must implement Extension interface.
    public Class<? extends Extension> getExtensionClass() {
        try {

            switch (this) {
                case PROJECT:       return Extension_Project.class;
                case LOG:           return Extension_Log.class;
                case DATABASE:      return Extension_Database.class;
                case REST_API:      return Extension_RestApi.class;
                case SUPPORT:       return Extension_Support.class;
                case INSTANCE:      return Extension_Instance.class;
                case HOMER_SERVER:  return Extension_HomerServer.class;
                case PARTICIPANT:   return Extension_Participant.class;
                default: throw new Exception("This Extension Type is unhandled. Probably forgotten to add it to the switch statement in getExtensionClass() method.");
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    public String getTypeName() {
        return Configuration.root().getString("Financial.extensions." + name().toLowerCase() + ".name");
    }

    public String getTypeDescription() {
        return Configuration.root().getString("Financial.extensions." + name().toLowerCase() + ".description");
    }

    private static final Logger logger = new Logger(ExtensionType.class);
}