package utilities.financial.extensions.configurations;
import controllers._BaseFormFactory;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import utilities.Server;
import utilities.enums.ExtensionType;
import utilities.model.BaseModel;


public interface Configuration {

    /**
     * _BaseFormFactory
     */

    static Object getConfiguration(ExtensionType type, String configuration) {
        switch (type) {

            case project:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Project.class, Json.parse(configuration));
            }

            case database:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Database.class, Json.parse(configuration));
            }

            case log:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Log.class, Json.parse(configuration));
            }

            case rest_api:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_RestApi.class, Json.parse(configuration));
            }

            case support:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Support.class, Json.parse(configuration));
            }

            case instance:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Instance.class, Json.parse(configuration));
            }

            case homer_server:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_HomerServer.class, Json.parse(configuration));
            }

            case participant:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Configuration_Participant.class, Json.parse(configuration));
            }

            default: throw new IllegalStateException("Extension type is unknown.");
        }
    }
}
