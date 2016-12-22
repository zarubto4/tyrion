package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TypeOfPost extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @ApiModelProperty(required = true)                      public String id;
    @ApiModelProperty(required = true)                      public String type;

    @JsonIgnore @OneToMany(mappedBy="type", cascade = CascadeType.ALL)     public List<Post> posts = new ArrayList<>();

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String create_permission_docs = "create: User have to own static key \"TypeOfPost_create\" ";

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return  Controller_Security.getPerson().has_permission("TypeOfPost_create");}
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  { return  Controller_Security.getPerson().has_permission("TypeOfPost_edit");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){ return  Controller_Security.getPerson().has_permission("TypeOfPost_delete");}

    public enum permissions{  TypeOfPost_create, TypeOfPost_edit, TypeOfPost_delete}
/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,TypeOfPost> find = new Finder<>(TypeOfPost.class);

}

