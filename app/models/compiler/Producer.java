package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Producer extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)  public String id;
                                                            @ApiModelProperty(required = true)  public String name;
                     @Column(columnDefinition = "TEXT")     @ApiModelProperty(required = true)  public String description;

    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<TypeOfBoard> type_of_boards = new ArrayList<>();



/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<String>  type_of_boards_id() { List<String> l = new ArrayList<>();  for( TypeOfBoard m : type_of_boards)  l.add(m.id); return l;  }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return SecurityController.getPerson().has_permission("Producer_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return SecurityController.getPerson().has_permission("Producer_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return SecurityController.getPerson().has_permission("Producer_delete"); }

    public enum permissions{Producer_create, Producer_edit, Producer_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Producer> find = new Model.Finder<>(Producer.class);

}
