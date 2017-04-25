package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;
import utilities.financial.Extension;
import utilities.loggy.Loggy;

public enum Enum_ExtensionType {

    /*
    !!!
    When you add new enum value, create corresponding class in utilities.financial package.
    e.g. Pattern for enum Project is "public class Extension_Project implements Extension"
    !!!
     */

    @EnumValue("Project")       Project,
    @EnumValue("Log")           Log,
    @EnumValue("Database")      Database;


    // All classes returned from this method (in package utilities.financial) must implement Extension interface
    @SuppressWarnings("unchecked")
    public Class<? extends Extension> getExtensionClass(){
        try {

            return (Class<? extends Extension>) Class.forName("utilities.financial.Extension_" + this.name());

        } catch (Exception e){
            Loggy.internalServerError("Enum_ExtensionType:: getExtensionClass:", e);
            return null;
        }
    }
}