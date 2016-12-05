package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Project;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Invitation extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @ApiModelProperty(required = true)            public String id;
    @JsonIgnore                        @ManyToOne public Person owner;
    @ApiModelProperty(required = true) @ManyToOne public Project project;
    @JsonIgnore @Constraints.Email                public String mail;
    @JsonIgnore                                   public Date date_of_creation;
    @JsonIgnore                                   public String notification_id;

/* JSON PROPERTY -------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public Person invitedPerson(){return Person.find.where().eq("mail", this.mail).findUnique();}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Invitation> find = new Finder<>(Invitation.class);

}
