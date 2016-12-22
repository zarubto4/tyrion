package models.project.global;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.person.Model_Person;
import utilities.enums.Participant_status;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Project_participant",
        value = "Project_participant")
public class Model_ProjectParticipant extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @JsonIgnore public String id;
                         @ManyToOne(fetch = FetchType.LAZY) @JsonIgnore public Model_Project project;
                                                 @ManyToOne @JsonIgnore public Model_Person person;
        @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Participant_status state;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public String id()          { if (person == null) return null; return person.id;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public String user_email()  { if (person == null) return this.user_email; return person.mail;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public String full_name()   { if (person == null) return "Unregistered user"; return person.full_name;}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient  public String user_email;

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_ProjectParticipant.find.byId(this.id) == null) break;
        }
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_ProjectParticipant> find = new Model.Finder<>(Model_ProjectParticipant.class);

}
