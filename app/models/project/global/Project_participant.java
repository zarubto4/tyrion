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

    @JsonProperty @Transient @ApiModelProperty(required = true) public String id()          { return person.id;}
    @JsonProperty @Transient @ApiModelProperty(required = true) @JsonInclude(JsonInclude.Include.NON_NULL) public String user_email()  { return person.mail;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public String full_name()   { return person.full_name;}

    @JsonProperty @Transient @JsonInclude(JsonInclude.Include.NON_NULL)  public String user_email;

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Project_participant> find = new Model.Finder<>(Project_participant.class);

}
