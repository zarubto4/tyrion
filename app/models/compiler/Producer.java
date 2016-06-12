package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Producer extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String name;
                     @Column(columnDefinition = "TEXT")      public String description;

    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<TypeOfBoard> type_of_boards = new ArrayList<>();



/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public List<String>  type_of_boards_id() { List<String> l = new ArrayList<>();  for( TypeOfBoard m : type_of_boards)  l.add(m.id); return l;  }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public Boolean create_permission(){  return SecurityController.getPerson().has_permission("Producer_create"); }
    @JsonIgnore   @Transient public Boolean read_permission()  {  return true; }
    @JsonProperty @Transient public Boolean edit_permission()  {  return SecurityController.getPerson().has_permission("Producer_edit");   }
    @JsonProperty @Transient public Boolean delete_permission(){  return SecurityController.getPerson().has_permission("Producer_delete"); }

    public enum permissions{Producer_create, Producer_edit, Producer_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Producer> find = new Model.Finder<>(Producer.class);

}
