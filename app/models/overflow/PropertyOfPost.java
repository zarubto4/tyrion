package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PropertyOfPost extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id   @ApiModelProperty(required = true)             public String propertyOfPostId;
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)  public List<Post> posts = new ArrayList<>();


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public PropertyOfPost(String postHashTagId){ this.propertyOfPostId = postHashTagId;}

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public boolean create_permission(){  return  SecurityController.getPerson().has_permission("PropertyOfPost_create");}
    @JsonIgnore @Transient public boolean read_permission()  {  return  true; }
    @JsonIgnore @Transient public boolean edit_permission()  {  return  SecurityController.getPerson().has_permission("PropertyOfPost_edit");  }
    @JsonIgnore @Transient public boolean delete_permission(){  return  SecurityController.getPerson().has_permission("PropertyOfPost_delete");}

    public enum permissions{  PropertyOfPost_create, PropertyOfPost_edit,  PropertyOfPost_delete;}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,PropertyOfPost> find = new Finder<>(PropertyOfPost.class);
}
