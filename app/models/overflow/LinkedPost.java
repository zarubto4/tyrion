package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;

import javax.persistence.*;

@Entity
public class LinkedPost extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @ApiModelProperty(required = true)                          public String link_id;

    @JsonIgnore  @ManyToOne                                     public Person author;
                 @ManyToOne @ApiModelProperty(required = true)  public Post answer;  // Objekt který je odpovědí
    @JsonIgnore  @ManyToOne                                     public Post question; // Objekt na který je navěšuje již odpovězená (podobná) otázka


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return ( LinkedPost.find.where().eq("author.id", SecurityController.getPerson().id).where().eq("id", link_id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Post_delete"); }

    public enum permissions{}
/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,LinkedPost> find = new Finder<>(LinkedPost.class);



}
