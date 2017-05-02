package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import utilities.financial.Extension;
import utilities.logger.Class_Logger;

public enum Enum_ExtensionType {

    /*
    !!!
    When you add new enum value, create corresponding class in utilities.financial package.
    e.g. Pattern for enum Project is "public class  Extension_Project implements Extension"
    !!!
     */

    @EnumValue("Project")       Project,
    @EnumValue("Log")           Log,
    @EnumValue("Database")      Database,
    @EnumValue("RestApi")       RestApi;


    // All classes returned from this method (in package utilities.financial) must implement Extension interface
    @SuppressWarnings("unchecked")
    public Class<? extends Extension> getExtensionClass(){
        try {

            return (Class<? extends Extension>) Class.forName("utilities.financial.Extension_" + this.name());

        } catch (Exception e){
            terminal_logger.internalServerError("getExtensionClass:", e);
            return null;
        }
    }

    private static final Class_Logger terminal_logger = new Class_Logger(Enum_ExtensionType.class);

}