package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TypeOfPost extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
                                                            public String type;

    @JsonIgnore @OneToMany(mappedBy="type", cascade = CascadeType.ALL)     public List<Post> posts = new ArrayList<>();

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty public Boolean create_permission(){  return  SecurityController.getPerson().has_permission("TypeOfPost.create");}
    @JsonProperty public Boolean read_permission()  {  return  SecurityController.getPerson().has_permission("TypeOfPost.read");  }
    @JsonProperty public Boolean edit_permission()  {  return  SecurityController.getPerson().has_permission("TypeOfPost.edit");  }
    @JsonProperty public Boolean delete_permission(){  return  SecurityController.getPerson().has_permission("TypeOfPost.delete");}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,TypeOfPost> find = new Finder<>(TypeOfPost.class);

}

