package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import java.beans.Transient;
import java.util.UUID;

/**
 * Toto je schéma nového Modelu
 */
// @Entity   (We do not want to have it unnecessarily stored in the database in case of Example Model Class)
@ApiModel(value = "ExampleModelName", description = "Model of ExampleModelName - Swagger annotation documentation")
// @Table(name="ExampleModelName") (We do not want to have it unnecessarily stored in the database in case of Example Model Class)
public class _Model_ExampleModelName extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(_Model_ExampleModelName.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save :: Creating new Object");
        super.save();

        //  if create something under project
        //  if (project != null ) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo(Model_Project.class, project_id(), project_id()))).start();

    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        // Case 1.1 :: We delete the object
        super.update();

        // Case 1.2 :: After Update - we send notification to frontend (Only if it is desirable)
        //new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( _Model_ExampleModelName.class, "project.id", "model.id"))).start();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete: Delete object Id: {} ", this.id);

        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

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

    @JsonIgnore
    public static _Model_ExampleModelName getById(String id) {
        return getById(UUID.fromString(id));
    }

    @JsonIgnore
    public static _Model_ExampleModelName getById(UUID id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    private static Finder<UUID, _Model_ExampleModelName> find = new Finder<>(_Model_ExampleModelName.class);
}
