package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.blocko.Model_BlockoBlock;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(description = "Model of Producer",
        value = "Producer")
public class Model_Producer extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)  public String id;
                                                            @ApiModelProperty(required = true)  public String name;
                     @Column(columnDefinition = "TEXT")     @ApiModelProperty(required = true)  public String description;

    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<Model_TypeOfBoard> type_of_boards = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<Model_BlockoBlock> blocko_blocks = new ArrayList<>();


/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

   // @JsonProperty @Transient @ApiModelProperty(required = true) public  List<Swagger_Object_detail> type_of_boards()   { List<Swagger_Object_detail> l = new ArrayList<>();  for( TypeOfBoard m  : type_of_boards)   l.add(new Swagger_Object_detail(m.name, m.id)); return l;  }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_Producer.find.byId(this.id) == null) break;
        }
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return Controller_Security.getPerson().has_permission("Producer_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.getPerson().has_permission("Producer_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.getPerson().has_permission("Producer_delete"); }

    public enum permissions{Producer_create, Producer_edit, Producer_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_Producer> find = new Model.Finder<>(Model_Producer.class);

}
