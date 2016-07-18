package models.person;

import com.avaje.ebean.Model;
import models.project.global.Project;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Invitation extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                    @ManyToOne  public Person owner;
                                                    @ManyToOne  public Project project;
                                                                public String mail;
                                                                public Date time_of_creation;

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Invitation> find = new Finder<>(Invitation.class);

}
