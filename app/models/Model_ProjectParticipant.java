package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.ParticipantStatus;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel(value = "Project_participant", description = "Model of Project_participant")
@Table(name="ProjectParticipant")
public class Model_ProjectParticipant extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_ProjectParticipant.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                         @ManyToOne(fetch = FetchType.LAZY) @JsonIgnore public Model_Project project;
                                                 @ManyToOne @JsonIgnore public Model_Person person;
        @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public ParticipantStatus state;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public UUID id()            { if (person == null) return null; return person.id;}
    @JsonProperty @ApiModelProperty(required = true) public String email()        { if (person == null) return this.user_email; return person.email;}
    @JsonProperty @ApiModelProperty(required = true) public String full_name()   { if (person == null) return "Unregistered user"; return person.full_name();}

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient  public String user_email;

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_ProjectParticipant> find = new Finder<>(Model_ProjectParticipant.class);

}
