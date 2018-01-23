package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.UUID;

@Entity
@ApiModel(value = "InstanceSnapshot", description = "Model of InstanceSnapshot")
@Table(name="InstanceSnapshot")
public class Model_InstanceSnapshot extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_InstanceSnapshot.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id public UUID id;

    @JsonIgnore public Date created;
    @JsonIgnore public boolean removed_by_user;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save - inserting into db");

        this.created = new Date();

        //  if create something under project
        //  if(project != null ) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo(Model_Project.class, project_id(), project_id()))).start();

    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update - updating db ,id: {}",  this.id);

        super.update();

        // Case 1.2 :: After Update - we send notification to frontend (Only if it is desirable)
        // new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_InstanceSnapshot.class, "project.id", "model.id"))).start();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete - Delete object Id: {} ", this.id);

        // Case 1.1 :: We delete the object
        super.delete();

        // Case 1.2 :: After Update - we send notification to frontend (Only if it is desirable)
        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_InstanceSnapshot.class, "project.id", "model.id"))).start();


        // Case 2.1 :: We delete the object with change of ORM parameter  @JsonIgnore  public boolean removed_by_user;
        //this.removed_by_user = true;
        //this.update();

        // Case 1.2 :: After Delete - we send notification to frontend (Only if it is desirable)
        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, "project.id", "model.id"))).start();


        // Case 3 :: In some cases, it is not possible to delete an object - it is therefore impossible to delete the object by the method
        //terminal_logger.internalServerError(new Exception("This object is not legitimate to remove."));
        //throw new IllegalAccessError("Delete is not supported under " + getClass().getSimpleName());

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Create Permission is always JsonIgnore
    @JsonIgnore   @Transient public boolean create_permission()  {  return  false;  }
    @JsonProperty @Transient public boolean update_permission()  {  return  false;  }
    @JsonProperty @Transient public boolean edit_permission()    {  return  false;  }
    @JsonProperty @Transient public boolean delete_permission()  {  return  false;  }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public static Model_InstanceSnapshot get_byId(String id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    private static Model.Finder<String, Model_InstanceSnapshot> find = new Model.Finder<>(Model_InstanceSnapshot.class);
}
