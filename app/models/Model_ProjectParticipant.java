package models;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Participant_status;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel(value = "Project_participant", description = "Model of Project_participant")
public class Model_ProjectParticipant extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_ProjectParticipant.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @JsonIgnore public String id;
                         @ManyToOne(fetch = FetchType.LAZY) @JsonIgnore public Model_Project project;
                                                 @ManyToOne @JsonIgnore public Model_Person person;
        @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Enum_Participant_status state;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public String id()          { if (person == null) return null; return person.id;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public String user_email()  { if (person == null) return this.user_email; return person.mail;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public String full_name()   { if (person == null) return "Unregistered user"; return person.full_name;}

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient  public String user_email;

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_ProjectParticipant.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object value: {}",  this.id);
        super.update();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete :: Update object value: {}",  this.id);
        super.delete();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_ProjectParticipant> find = new Model.Finder<>(Model_ProjectParticipant.class);

}
