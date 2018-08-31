package utilities.financial.extensions.configurations;
import controllers._BaseFormFactory;
import play.libs.Json;
import utilities.Server;
import utilities.enums.ExtensionType;


public interface Configuration{


    static Configuration getConfiguration(ExtensionType type, String configuration) {
        switch (type) {

            case PROJECT:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Project.class, Json.parse(configuration));
            }

            case DATABASE:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Database.class, Json.parse(configuration));
            }

            case LOG:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Log.class, Json.parse(configuration));
            }

            case REST_API:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_RestApi.class, Json.parse(configuration));
            }

            case SUPPORT:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Support.class, Json.parse(configuration));
            }

            case INSTANCE:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Instance.class, Json.parse(configuration));
            }

            case HOMER_SERVER:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_HomerServer.class, Json.parse(configuration));
            }

            case PARTICIPANT:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Participant.class, Json.parse(configuration));
            }

            default: throw new IllegalStateException("Extension type is unknown.");
        }
    }
}
