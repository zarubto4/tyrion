package utilities.financial.extensions.configurations;
import controllers._BaseFormFactory;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import utilities.enums.ExtensionType;
import utilities.model.BaseModel;


public interface Configuration {

    /**
     * _BaseFormFactory
     */

    static Object getConfiguration(ExtensionType type, String configuration) {
        switch (type) {

            case project:{
                return BaseModel.baseFormFactory.formFromJsonWithValidation(Configuration_Project.class, Json.parse(configuration));
            }

            case database:{
                return BaseModel.baseFormFactory.formFromJsonWithValidation(Configuration_Database.class, Json.parse(configuration));
            }

            case log:{
                return BaseModel.baseFormFactory.formFromJsonWithValidation(Configuration_Log.class, Json.parse(configuration));
            }

            case rest_api:{
                return BaseModel.baseFormFactory.formFromJsonWithValidation(Configuration_RestApi.class, Json.parse(configuration));
            }

            case support:{
                return BaseModel.baseFormFactory.formFromJsonWithValidation(Configuration_Support.class, Json.parse(configuration));
            }

            case instance:{
                return BaseModel.baseFormFactory.formFromJsonWithValidation(Configuration_Instance.class, Json.parse(configuration));
            }

            case homer_server:{
                return BaseModel.baseFormFactory.formFromJsonWithValidation(Configuration_Project.class, Json.parse(configuration));
            }

            case participant:{
                return BaseModel.baseFormFactory.formFromJsonWithValidation(Configuration_Participant.class, Json.parse(configuration));
            }

            default: throw new IllegalStateException("Extension type is unknown.");
        }
    }
}
