package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PropertyOfPost extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id   public String propertyOfPostId;
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)  public List<Post> posts = new ArrayList<>();


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public PropertyOfPost(String postHashTagId){ this.propertyOfPostId = postHashTagId;}

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty public Boolean create_permission(){  return  SecurityController.getPerson().has_permission("PropertyOfPost.create");}
    @JsonProperty public Boolean read_permission()  {  return  SecurityController.getPerson().has_permission("PropertyOfPost.read");  }
    @JsonProperty public Boolean edit_permission()  {  return  SecurityController.getPerson().has_permission("PropertyOfPost.edit");  }
    @JsonProperty public Boolean delete_permission(){  return  SecurityController.getPerson().has_permission("PropertyOfPost.delete");}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,PropertyOfPost> find = new Finder<>(PropertyOfPost.class);
}
