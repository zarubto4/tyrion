package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel( value = "Garfield", description = "Model of Garfield test set")
@Table(name="Garfield")
public class Model_Garfield  extends NamedModel {

/* DOCUMENTATION -------------------------------------------------------------------------------------------------------*/

     /*
        Garfield je softwarový nástroj, hardwarový tester i komponenta na tyrionovi k testování a vypalování fiwmaru do
        hardwaru a tisku štítků na hardware i na krabici. Každý garfield má svojí testovací desku i sadu tiskáren.
     */

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Garfield.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(unique=true) public String hardware_tester_id;
    
                         public Integer print_label_id_1;   // 12 mm
                         public Integer print_label_id_2;   // 24 mm
                         public Integer print_sticker_id;   // 65 mm

    @JsonIgnore          public UUID type_of_board_id;    // Jaký typ hardwaru umí testovat garfield! ( Je to zatím předpokládáno na 1:1) Vazba není přes ORM!!!
    @JsonIgnore          public UUID producer_id;         // Kdo desku vyrobil!

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_TypeOfBoard type_of_board() {
        try {

            if (type_of_board_id != null) return Model_TypeOfBoard.getById(type_of_board_id);
            return null;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_Producer producer() {
        try {

            if (producer_id != null) return Model_Producer.getById(producer_id);
            return null;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }
    
/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/
     
/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String CHANNEL = "garfield";

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/
    
    @JsonIgnore   @Transient public boolean create_permission() {  return  BaseController.person().has_permission(Permission.Garfield_create.name());}
    @JsonProperty @Transient public boolean edit_permission()   {  return  BaseController.person().has_permission(Permission.Garfield_edit.name());}
    @JsonProperty @Transient public boolean read_permission()   {  return  BaseController.person().has_permission(Permission.Garfield_read.name());}
    @JsonProperty @Transient public boolean delete_permission() {  return  BaseController.person().has_permission(Permission.Garfield_delete.name());}
    @JsonProperty @Transient public boolean update_permission() {  return  BaseController.person().has_permission(Permission.Garfield_update.name());}

    public enum Permission { Garfield_create, Garfield_read, Garfield_edit, Garfield_update, Garfield_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Garfield getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_Garfield getById(UUID id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Garfield> find = new Finder<>(Model_Garfield.class);
}
