package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Processor",
        value = "Processor")
public class Model_Processor extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)  public String id;
                                                            @ApiModelProperty(required = true)  public String processor_name;
                @Column(columnDefinition = "TEXT")          @ApiModelProperty(required = true)  public String description;
                                                            @ApiModelProperty(required = true)  public String processor_code;
                                                            @ApiModelProperty(required = true)  public int speed;

    @JsonIgnore @OneToMany(mappedBy="processor", cascade = CascadeType.ALL) public List<Model_TypeOfBoard> type_of_boards = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_Processor.find.byId(this.id) == null) break;
        }
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return Controller_Security.get_person().has_permission("Processor_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.get_person().has_permission("Processor_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.get_person().has_permission("Processor_delete"); }

    public enum permissions{Processor_create, Processor_edit, Processor_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_Processor> find = new Finder<>(Model_Processor.class);


}
