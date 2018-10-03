package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@ApiModel(value = "ValidationToken", description = "Model of Validation of REST-API Token")
@Table(name="ValidationToken")
public class Model_ValidationToken extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_ValidationToken.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String email;
    public UUID token;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_ValidationToken setValidation(String email) {

        this.email = email;
        this.token = UUID.randomUUID();

        save();
        return this;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_ValidationToken> find = new Finder<>(Model_ValidationToken.class);
}
