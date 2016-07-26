package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TypeOfConfirms extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @ApiModelProperty(required = true)                          public String id;
    @ApiModelProperty(required = true)                          public String type;
    @ApiModelProperty(required = true)                          public String color;
    @ApiModelProperty(required = true)                          public Integer size;

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)  public List<Post> posts = new ArrayList<>();

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return  SecurityController.getPerson().has_permission("TypeOfConfirms_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return  true;                                                                   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return  SecurityController.getPerson().has_permission("TypeOfConfirms_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return  SecurityController.getPerson().has_permission("TypeOfConfirms_delete"); }

    public enum permissions{  PropertyOfPost_create, PropertyOfPost_read,  PropertyOfPost_edit, PropertyOfPost_delete; }
/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,TypeOfConfirms> find = new Model.Finder<>(TypeOfConfirms.class);
}
