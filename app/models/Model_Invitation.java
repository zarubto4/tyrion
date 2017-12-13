package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="Invitation")
public class Model_Invitation extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Invitation.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @ApiModelProperty(required = true)        public String id;
    @JsonIgnore                        @ManyToOne public Model_Person owner;
    @ApiModelProperty(required = true) @ManyToOne public Model_Project project; // TODO ID
    @JsonIgnore @Constraints.Email                public String mail;
    @JsonIgnore                                   public Date date_of_creation;
    @JsonIgnore                                   public String notification_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Model_Person invited_person(){return Model_Person.find.where().eq("mail", this.mail).findUnique();}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Transient
    public void delete_notification() {
        try {

            if (notification_id != null) {

                Model_Notification notification = Model_Notification.find.byId(notification_id);
                if (notification != null) notification.delete();
            }
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

   @JsonIgnore @Transient @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (find.byId(this.id) == null) break;
        }

        terminal_logger.debug("save - saving to database, id: {}", this.id);

        super.save();
    }

    @JsonIgnore @Transient @Override public void update() {

        terminal_logger.debug("update - updating in database, id: {}",  this.id);

        super.update();

        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Invitation.class, project.id, id))).start();
    }

    @JsonIgnore @Transient @Override public void delete() {

        terminal_logger.debug("delete - deleting from database, id: {}", this.id);

        super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_Invitation get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_Invitation> find = new Finder<>(Model_Invitation.class);

}
