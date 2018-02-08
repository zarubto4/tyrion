package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Processor", value = "Processor")
@Table(name="Processor")
public class Model_Processor extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Processor.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String processor_code;
    public int speed;

    @JsonIgnore @OneToMany(mappedBy="processor", cascade = CascadeType.ALL) public List<Model_HardwareType> hardware_types = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore                                      public boolean create_permission() {  return BaseController.person().has_permission("Processor_create"); }
    @JsonIgnore                                      public boolean read_permission()  {  return true; }

    @JsonProperty @ApiModelProperty(required = true) public boolean edit_permission()  {  return BaseController.person().has_permission("Processor_edit");   }
    @JsonProperty @ApiModelProperty(required = true) public boolean delete_permission() {  return BaseController.person().has_permission("Processor_delete"); }

    public enum Permission { Processor_create, Processor_edit, Processor_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Processor getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_Processor getById(UUID id) {

        logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Processor> find = new Finder<>(Model_Processor.class);
}
