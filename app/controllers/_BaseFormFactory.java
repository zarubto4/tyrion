package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.data.Form;
import play.data.FormFactory;
import play.data.format.Formatters;
import play.i18n.MessagesApi;
import utilities.errors.Exceptions.Result_Error_InvalidBody;
import utilities.logger.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

/**
 * Helper to create better forms.
 */
@Singleton
public class _BaseFormFactory extends FormFactory {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Blocko.class);

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
    public <T> T formFromRequestWithValidation(Class<T> clazz) {

        Form<T> form = super.form(clazz);
        Form<T> bind = form.bindFromRequest();

        if (bind.hasErrors()){
            logger.warn("formFromRequestWithValidation::InvalidBody::ErrorList::{}", bind.errorsAsJson());
            throw new Result_Error_InvalidBody(form.errorsAsJson());
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
    public <T> T formFromJsonWithValidation(Class<T> clazz, JsonNode jsonNode) {

        Form<T> form =  super.form(clazz);
        Form<T> bind =  form.bind(jsonNode);

        if (bind.hasErrors()){
            logger.warn("formFromJsonWithValidation::InvalidBody::ErrorList::{}", bind.errorsAsJson());
            throw new Result_Error_InvalidBody(form.errorsAsJson());
        }

        return bind.get();
    }

}
