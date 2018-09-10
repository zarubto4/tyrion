package utilities.financial.extensions.consumptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers._BaseFormFactory;
import play.libs.Json;
import utilities.Server;
import utilities.enums.ExtensionType;
import utilities.model.BaseModel;

/**
 * ResourceConsumption defines, how much resources we used. This information is used for example for calculating the price of an extension.
 */
public interface ResourceConsumption {

    /**
     * @return true if no resources vere used; false otherwise
     */
    @JsonIgnore
    boolean isEmpty();

    /**
     * @return human readable string
     */
    @JsonIgnore
    String toReadableString();

    static ResourceConsumption getConsumption(ExtensionType type, String consumption) {
        switch (type) {

            case PROJECT:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Consumption_Project.class, Json.parse(consumption));
            }

            case DATABASE:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Consumption_Database.class, Json.parse(consumption));
            }

            case LOG:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Consumption_Log.class, Json.parse(consumption));
            }

            case REST_API:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Consumption_RestApi.class, Json.parse(consumption));
            }

            case SUPPORT:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Consumption_Support.class, Json.parse(consumption));
            }

            case INSTANCE:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Consumption_Instance.class, Json.parse(consumption));
            }

            case HOMER_SERVER:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Consumption_HomerServer.class, Json.parse(consumption));
            }

            case PARTICIPANT:{
                return Server.injector.getInstance(_BaseFormFactory.class).formFromJsonWithValidation(Consumption_Participant.class, Json.parse(consumption));
            }

            default: throw new IllegalStateException("Extension type is unknown.");
        }
    }
}
