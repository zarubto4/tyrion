package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import exceptions.NotFoundException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel( value = "Invitation", description = "Model of Invitation")
@Table(name="Invitation")
public class Model_Invitation extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Invitation.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_Person owner;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_Project project;
    @Constraints.Email                              public String email;
    @JsonIgnore                                     public UUID notification_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Model_Person invited_person() {
        try {
            return Model_Person.find.query().nullable().where().eq("email", this.email).findOne();
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }



    /* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Person getOwner() {
        return isLoaded("owner") ? owner : Model_Person.find.query().where().eq("invitations.id", id).findOne();
    }

    @JsonIgnore
    public Model_Project getProject() {
        return isLoaded("project") ? project : Model_Project.find.query().where().eq("invitations.id", id).findOne();
    }

    @JsonIgnore
    public void delete_notification() {
        try {

            Model_Notification notification = Model_Notification.find.byId(notification_id);
            notification.delete();

        } catch (NotFoundException|NullPointerException e) {
            // Nothing
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Invitation.class)
    public static CacheFinder<Model_Invitation> find = new CacheFinder<>(Model_Invitation.class);
}
