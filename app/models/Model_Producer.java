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
@ApiModel(value = "Producer", description = "Model of Producer")
@Table(name="Producer")
public class Model_Producer extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Producer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)  public String id;
                                                            @ApiModelProperty(required = true)  public String name;
                     @Column(columnDefinition = "TEXT")     @ApiModelProperty(required = true)  public String description;

    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<Model_TypeOfBoard> type_of_boards = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<Model_BlockoBlock> blocko_blocks = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<Model_GridWidget>  grid_widgets = new ArrayList<>();

    @JsonIgnore public boolean removed_by_user;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save: Creating new Object");

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_Producer.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override
    public void update() {

        terminal_logger.debug("update: ID = {}",  this.id);

        super.update();

    }

    @JsonIgnore @Override
    public void delete() {

        terminal_logger.debug("delete: ID = {}",  this.id);

        super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission(){  return Controller_Security.get_person().permissions_keys.containsKey("Producer_create"); }
    @JsonIgnore   @Transient public boolean read_permission()  {  return true; }
    @JsonProperty @Transient public boolean edit_permission()  {  return Controller_Security.get_person().permissions_keys.containsKey("Producer_edit");   }
    @JsonProperty @Transient public boolean delete_permission(){  return Controller_Security.get_person().permissions_keys.containsKey("Producer_delete"); }

    public enum permissions{Producer_create, Producer_edit, Producer_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_Producer get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_Producer> find = new Model.Finder<>(Model_Producer.class);

}
