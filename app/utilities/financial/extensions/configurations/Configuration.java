package utilities.financial.extensions.configurations;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.Form;
import play.i18n.Lang;
import play.libs.Json;
import utilities.enums.Enum_ExtensionType;


public interface Configuration {



    static Object getConfiguration(Enum_ExtensionType type, String configuration){

        Form<?> form;

        switch (type) {

            case project:{
                form = Form.form(Configuration_Project.class).bind(Json.parse(configuration));
                break;
            }

            case database:{
                form = Form.form(Configuration_Database.class).bind(Json.parse(configuration));
                break;
            }

            case log:{
                form = Form.form(Configuration_Log.class).bind(Json.parse(configuration));
                break;
            }

            case rest_api:{
                form = Form.form(Configuration_RestApi.class).bind(Json.parse(configuration));
                break;
            }

            case support:{
                form = Form.form(Configuration_Support.class).bind(Json.parse(configuration));
                break;
            }

            case instance:{
                form = Form.form(Configuration_Instance.class).bind(Json.parse(configuration));
                break;
            }

            case homer_server:{
                form = Form.form(Configuration_HomerServer.class).bind(Json.parse(configuration));
                break;
            }

            case participant:{
                form = Form.form(Configuration_Participant.class).bind(Json.parse(configuration));
                break;
            }

            default: throw new IllegalStateException("Extension type is unknown.");
        }

        if(form.hasErrors()) {
            throw new IllegalStateException("Error parsing product configuration. Errors: " + form.errorsAsJson(Lang.forCode("en-US")));
        }

        return form.get();

    }
}
