package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.SecurityController;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TypeOfConfirms extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)   public String id;
                                                              public String type;
                                                              public String color;
                                                              public Integer size;

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)  public List<Post> posts = new ArrayList<>();

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public Boolean create_permission(){  return  SecurityController.getPerson().has_permission("TypeOfConfirms_create"); }
    @JsonIgnore public Boolean read_permission()  {  return  true;                                                                   }
                public Boolean edit_permission()  {  return  SecurityController.getPerson().has_permission("TypeOfConfirms_edit");   }
                public Boolean delete_permission(){  return  SecurityController.getPerson().has_permission("TypeOfConfirms_delete"); }

    public enum permissions{  PropertyOfPost_create, PropertyOfPost_read,  PropertyOfPost_edit, PropertyOfPost_delete; }
/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,TypeOfConfirms> find = new Model.Finder<>(TypeOfConfirms.class);
}
