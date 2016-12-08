package models.project.global;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;
import utilities.enums.Participant_status;

import javax.persistence.*;

@Entity
public class Project_participant extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @JsonIgnore public String id;
                         @ManyToOne(fetch = FetchType.LAZY) @JsonIgnore public Project project;
                                                 @ManyToOne @JsonIgnore public Person person;
        @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public Participant_status state;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public String id()          { if (person == null) return null; return person.id;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public String user_email()  { if (person == null) return this.user_email; return person.mail;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public String full_name()   { if (person == null) return "Unregistered user"; return person.full_name;}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient  public String user_email;

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Project_participant> find = new Model.Finder<>(Project_participant.class);

}
