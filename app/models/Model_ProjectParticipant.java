package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.ParticipantStatus;
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

    @JsonProperty @Transient @ApiModelProperty(required = true) public UUID id()          { if (person == null) return null; return person.id;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public String mail()  { if (person == null) return this.user_email; return person.email;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public String full_name()   { if (person == null) return "Unregistered user"; return person.full_name();}

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient  public String user_email;

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<UUID, Model_ProjectParticipant> find = new Finder<>(Model_ProjectParticipant.class);

}
