package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.data.validation.Constraints;
import utilities.cache.CacheField;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel( value = "Invitation", description = "Model of Invitation")
@Table(name="Invitation")
public class Model_Invitation extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Invitation.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/


    @JsonIgnore                        @ManyToOne public Model_Person owner;
    @JsonIgnore                        @ManyToOne public Model_Project project;
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

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Invitation_create.name())) return;
        if(!_BaseController.personId().equals(this.id)) throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Invitation_update.name())) return;
        if(!_BaseController.personId().equals(this.id)) throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_update_permission()  {
        if(_BaseController.person().has_permission(Permission.Invitation_update.name())) return;
        if(!_BaseController.personId().equals(this.id)) throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void  check_delete_permission() throws _Base_Result_Exception  {
        if(_BaseController.person().has_permission(Permission.Invitation_delete.name())) return;
        if(!_BaseController.personId().equals(this.id)) throw new Result_Error_PermissionDenied();
    }

    public enum Permission { Invitation_create, Invitation_read, Invitation_update, Invitation_delete }


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_Invitation.class)
    public static Cache<UUID, Model_Invitation> cache;

    public static Model_Invitation getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_Invitation getById(UUID id) throws _Base_Result_Exception {

        Model_Invitation invitation = cache.get(id);
        if (invitation == null) {

            invitation = find.byId(id);
            if (invitation == null)  throw new Result_Error_NotFound(Model_Invitation.class);

            cache.put(id, invitation);
        }

        // Check Permission
        invitation.check_read_permission();
        return invitation;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Invitation> find = new Finder<>(Model_Invitation.class);
}
