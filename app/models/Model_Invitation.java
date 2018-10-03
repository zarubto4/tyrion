package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.errors.Exceptions._Base_Result_Exception;
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


    @JsonIgnore                        @ManyToOne public Model_Person owner;
    @JsonIgnore                        @ManyToOne public Model_Project project;
    @JsonIgnore @Constraints.Email                public String email;
    @JsonIgnore                                   public UUID notification_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Model_Person invited_person() {
        try {
            return Model_Person.find.query().where().eq("email", this.email).findOne();
        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

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

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Invitation.class)
    public static CacheFinder<Model_Invitation> find = new CacheFinder<>(Model_Invitation.class);
}
