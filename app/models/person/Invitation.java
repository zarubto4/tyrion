package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.project.global.Project;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Invitation extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
    @JsonIgnore                       @ManyToOne  public Person owner;
    @JsonIgnore                       @ManyToOne  public Project project;
    @JsonIgnore  @Constraints.Email               public String mail;
    @JsonIgnore                                   public Date time_of_creation;

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Invitation> find = new Finder<>(Invitation.class);

}
