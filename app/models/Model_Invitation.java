package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name="Invitation")
public class Model_Invitation extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Invitation.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/


    @JsonIgnore                        @ManyToOne public Model_Person owner;
    @JsonIgnore                        @ManyToOne public Model_Project project; // TODO ID
    @JsonIgnore @Constraints.Email                public String email;
    @JsonIgnore                                   public UUID notification_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Model_Person invited_person() {return Model_Person.find.query().where().eq("mail", this.email).findOne();}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Transient
    public void delete_notification() {
        try {

            if (notification_id != null) {

                Model_Notification notification = Model_Notification.find.byId(notification_id);
                if (notification != null) notification.delete();
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient @Override public void update() {

        logger.debug("update - updating in database, id: {}",  this.id);

        super.update();

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Invitation.class, project.id, id))).start();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Invitation getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_Invitation getById(UUID id) {
        logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Invitation> find = new Finder<>(Model_Invitation.class);
}
