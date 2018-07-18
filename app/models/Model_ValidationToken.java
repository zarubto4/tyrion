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

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @javax.persistence.Transient
    @Override public void check_read_permission()   throws _Base_Result_Exception {
        logger.error("check_read_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }
    @JsonIgnore @javax.persistence.Transient
    @Override public void check_create_permission() throws _Base_Result_Exception {
        logger.error("check_create_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }
    @JsonIgnore @javax.persistence.Transient
    @Override public void check_update_permission() throws _Base_Result_Exception {
        logger.error("check_update_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }
    @JsonIgnore @javax.persistence.Transient
    @Override public void check_delete_permission() throws _Base_Result_Exception {
        logger.error("check_delete_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_ValidationToken getById(UUID id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<UUID, Model_ValidationToken> find = new Finder<>(Model_ValidationToken.class);

}
