package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Processor", value = "Processor")
@Table(name="Processor")
public class Model_Processor extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Processor.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)  public UUID id;
                                                            @ApiModelProperty(required = true)  public String processor_name;
                @Column(columnDefinition = "TEXT")          @ApiModelProperty(required = true)  public String description;
                                                            @ApiModelProperty(required = true)  public String processor_code;
                                                            @ApiModelProperty(required = true)  public int speed;

    @JsonIgnore @OneToMany(mappedBy="processor", cascade = CascadeType.ALL) public List<Model_TypeOfBoard> type_of_boards = new ArrayList<>();

    @JsonIgnore public boolean removed_by_user;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/


    @JsonIgnore @Override
    public void save() {

        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update: ID = {}",  this.id);

        super.update();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete: ID = {}",  this.id);

        super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return Controller_Security.get_person().permissions_keys.containsKey("Processor_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.get_person().permissions_keys.containsKey("Processor_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.get_person().permissions_keys.containsKey("Processor_delete"); }

    public enum permissions{Processor_create, Processor_edit, Processor_delete}


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_Processor get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_Processor> find = new Finder<>(Model_Processor.class);


}
