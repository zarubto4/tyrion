package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@ApiModel( value = "GSM", description = "Model of GSM")
@Table(name="GSM")
public class Model_GSM extends TaggedModel {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GSM.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;

    public String MSINumber;


    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/



    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/
    @JsonIgnore
    public UUID get_project_id() throws _Base_Result_Exception {

        if (cache().get(Model_Project.class) == null) {
            cache().add(Model_Project.class, Model_Project.find.query().where().eq("blocks.id", id).select("id").findSingleAttributeList());
        }

        return cache().get(Model_Project.class);

    }

    @JsonIgnore
    public Model_Project get_project() throws _Base_Result_Exception {

        try {
            return Model_Project.getById(get_project_id());
        }catch (Exception e) {
            return null;
        }
    }
    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Override
    public void save() {

        logger.debug("save :: Creating new Object");
        super.save();

        //  if create something under project
        //  if (project != null ) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo(Model_Project.class, project_id(), project_id()))).start();

    }

    @JsonIgnore
    @Override
    public void update() {

        logger.debug("update :: Update object Id: {}", this.id);

        // Case 1.1 :: We delete the object
        super.update();

        // Case 1.2 :: After Update - we send notification to frontend (Only if it is desirable)
        //new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( _Model_ExampleModelName.class, "project.id", "model.id"))).start();

    }

    @JsonIgnore
    @Override
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

    @JsonIgnore @Override
    public void check_read_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore @Override
    public void check_create_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore @Override
    public void check_update_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore @Override
    public void check_delete_permission() throws _Base_Result_Exception {

    }

    public enum Permission { GSM_create, GSM_read, GSM_update, GSM_edit, GSM_delete }

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_GSM getById(UUID id) {
        return find.byId(id);
    }

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_GSM> find = new Finder<>(Model_GSM.class);

}