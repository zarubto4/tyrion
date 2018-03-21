package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Hardware;
import models.Model_HomerServer;
import play.data.Form;
import play.data.FormFactory;
import play.data.format.Formatters;
import play.i18n.Lang;
import play.i18n.MessagesApi;
import utilities.errors.Exceptions.Result_Error_InvalidBody;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import websocket.WS_Interface;
import websocket.WS_Message;
import websocket.interfaces.WS_Homer;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import java.io.IOException;

/**
 * Helper to create better forms.
 */
@Singleton
public class _BaseFormFactory extends FormFactory {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(_BaseFormFactory.class);

// Methods  ##############################################################################################################

    @Inject
    public _BaseFormFactory(MessagesApi messagesApi, Formatters formatters, Validator validator) {
       super(messagesApi, formatters, validator);
    }


    /**
     * Automaticaly get Json from request, and valid that with hasErrors immediately in one step
     * @param <T>   the type of value in the form.
     * @param clazz    the class to map to a form.
     * @return a new form that wraps the specified class.
     */
    public <T> T formFromRequestWithValidation(Class<T> clazz) throws _Base_Result_Exception {

        Form<T> form = super.form(clazz);
        Form<T> bind = form.bindFromRequest();

        if (bind.hasErrors()){
            logger.error("formFromJsonWithValidation::InvalidBody::JsonFor ParserControl:: {}", bind.toString());
            JsonNode node_errors = bind.errorsAsJson(Lang.forCode("en-US"));
            logger.error("formFromJsonWithValidation::InvalidBody::ErrorList::{}", node_errors.toString());
            throw new Result_Error_InvalidBody(node_errors);
        }

        return bind.get();
    }

    /**
     * Binds Json data to this form - that is, handles form submission.
     * @param clazz
     * @param jsonNode
     * @param <T>
     * @return a copy of this form filled with the new data
     */
    public <T> T formFromJsonWithValidation(Class<T> clazz, JsonNode jsonNode) throws _Base_Result_Exception {

        Form<T> form =  super.form(clazz);
        Form<T> bind =  form.bind(jsonNode);

        if (bind.hasErrors()){
            logger.error("formFromJsonWithValidation::InvalidBody::JsonFor ParserControl:: {}", jsonNode.toString());
            logger.error("formFromJsonWithValidation::InvalidBody::ErrorList::{}", bind.errorsAsJson(Lang.forCode("en-US")).toString());
            throw new Result_Error_InvalidBody(bind.errorsAsJson(Lang.forCode("en-US")));
        }

        return bind.get();
    }


    /**
     * Binds Json data to this form - that is, handles form submission.
     * Special Method with Response to Websocket
     * @param clazz
     * @param jsonNode
     * @param <T>
     * @return a copy of this form filled with the new data
     */
    public <T> T formFromJsonWithValidation(Model_HomerServer server, Class<T> clazz, JsonNode jsonNode) throws _Base_Result_Exception, IOException {

        Form<T> form =  super.form(clazz);
        Form<T> bind =  form.bind(jsonNode);

        if (bind.hasErrors()){
            logger.error("formFromJsonWithValidation::InvalidBody::JsonFor ParserControl:: {}", jsonNode.toString());
            logger.error("formFromJsonWithValidation::InvalidBody::ErrorList::{}", bind.errorsAsJson(Lang.forCode("en-US")).toString());
            ObjectNode error = (ObjectNode) new ObjectMapper().readTree(jsonNode.asText());

            if (jsonNode.has("message_id")) {
                server.write_without_confirmation(jsonNode.get("message_id").asText(), error);
            } else {
                server.write_without_confirmation(error);
            }

            throw new Result_Error_InvalidBody(bind.errorsAsJson());

        }

        return bind.get();
    }



    /**
     * Binds Json data to this form - that is, handles form submission.
     * Special Method with Response to Websocket
     * @param clazz
     * @param jsonNode
     * @param <T>
     * @return a copy of this form filled with the new data
     */
    public <T> T formFromJsonWithValidation(WS_Interface ws_interface, Class<T> clazz, JsonNode jsonNode) throws _Base_Result_Exception, IOException {

        Form<T> form =  super.form(clazz);
        Form<T> bind =  form.bind(jsonNode);

        if (bind.hasErrors()){
            logger.error("formFromJsonWithValidation::InvalidBody::JsonFor ParserControl:: {}", jsonNode.toString());
            logger.error("formFromJsonWithValidation::InvalidBody::ErrorList::{}", bind.errorsAsJson(Lang.forCode("en-US")).toString());
            ObjectNode error = (ObjectNode) new ObjectMapper().readTree(jsonNode.asText());

            if (jsonNode.has("message_id")) {
                error.put("message_id", jsonNode.get("message_id").asText());
                ws_interface.send(error);
            } else {
                ws_interface.send(error);
            }

            throw new Result_Error_InvalidBody(bind.errorsAsJson());
        }

        return bind.get();
    }


}
