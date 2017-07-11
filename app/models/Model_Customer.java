package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.documentdb.DocumentClientException;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import utilities.Server;
import utilities.document_db.DocumentDB;
import utilities.document_db.document_objects.DM_Board_Connect;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.outboundClass.Swagger_BlockoBlock_Version_Short_Detail;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.UUID;

/**
 * Toto je schéma nového Modelu
 */

// @Entity   (We do not want to have it unnecessarily stored in the database in case of Example Model Class)
@ApiModel(value = "ExampleModelName", description = "Model of ExampleModelName - Swagger annotation documentation")
public class _Model_ExampleModelName extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(_Model_ExampleModelName.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id public String id;

    @JsonIgnore public Date date_of_create;
    @JsonIgnore public boolean removed_by_user;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


    @JsonIgnore
    public Swagger_BlockoBlock_Version_Short_Detail get_short_ExampleModelName_version(){
        try {

            Swagger_BlockoBlock_Version_Short_Detail help = new Swagger_BlockoBlock_Version_Short_Detail();

            // Something

            return help;

        }catch (Exception e){
            terminal_logger.internalServerError("get_short_ExampleModelName_version", e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        date_of_create = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (_Model_ExampleModelName.find.byId(this.id) == null) break;
        }
        super.save();

        //  if create something under project
        //  if(project != null ) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo(Model_Project.class, project_id(), project_id()))).start();

    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        // Case 1.1 :: We delete the object
        super.update();

        // Case 1.2 :: After Update - we send notification to frontend (Only if it is desirable)
        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( _Model_ExampleModelName.class, "project.id", "model.id"))).start();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete: Delete object Id: {} ", this.id);

        // Case 1.1 :: We delete the object
        super.delete();

        // Case 1.2 :: After Update - we send notification to frontend (Only if it is desirable)
        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( _Model_ExampleModelName.class, "project.id", "model.id"))).start();


        // Case 2.1 :: We delete the object with change of ORM parameter  @JsonIgnore  public boolean removed_by_user;
        this.removed_by_user = true;
        this.update();

        // Case 1.2 :: After Delete - we send notification to frontend (Only if it is desirable)
        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, "project.id", "model.id"))).start();


        // Case 3 :: In some cases, it is not possible to delete an object - it is therefore impossible to delete the object by the method
        terminal_logger.internalServerError(new Exception("This object is not legitimate to remove."));
        throw new IllegalAccessError("Delete is not supported under " + getClass().getSimpleName());

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    public void make_log_to_non_sql_database(){
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_Board_Connect.make_request(this.id), null, true);
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError("make_log_to_non_sql_database:", e);
            }
        }).start();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Create Permission is always JsonIgnore
    @JsonIgnore   @Transient public boolean create_permission()  {  return  false;  }
    @JsonProperty @Transient public boolean update_permission()  {  return  false;  }
    @JsonProperty @Transient public boolean edit_permission()  {  return  false;  }
    @JsonProperty @Transient public boolean delete_permission()  {  return  false;  }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public static _Model_ExampleModelName get_byId(String id) {
        return find.byId(id);
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
   private static Model.Finder<String, _Model_ExampleModelName> find = new Model.Finder<>(_Model_ExampleModelName.class);

}
