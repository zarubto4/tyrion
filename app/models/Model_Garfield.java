package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@ApiModel( value = "Garfield", description = "Model of Garfield test set")
@Table(name="Garfield")
public class Model_Garfield  extends Model{

/* DOCUMENTATION -------------------------------------------------------------------------------------------------------*/

     /*

        Garfield je softwarový nástroj, hardwarový tester i komponenta na tyrionovi k testování a vypalování fiwmaru do
        hardwaru a tisku štítků na hardware i na krabici. Každý garfield má svojí testovací desku i sadu tiskáren.

     */

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Garfield.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                    @Id  public UUID id;
                         public String name;
                         public String description;

    @Column(unique=true) public String hardware_tester_id;
    
                         public Integer print_label_id_1;   // 12 mm
                         public Integer print_label_id_2;   // 24 mm
                         public Integer print_sticker_id;   // 65 mm

    @JsonIgnore          public String type_of_board_id;    // Jaký typ hardwaru umí testovat garfield! ( Je to zatím předpokládáno na 1:1) Vazba není přes ORM!!!
    @JsonIgnore          public String producer_id;         // Kdo desku vyrobil!

    @JsonIgnore          public Date date_of_crate;


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @TyrionCachedList @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_TypeOfBoard type_of_board(){
        try {

            if(type_of_board_id != null) return Model_TypeOfBoard.get_byId(type_of_board_id);
            return null;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @TyrionCachedList @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_Producer producer(){
        try {

            if(producer_id != null) return Model_Producer.get_byId(producer_id);
            return null;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }
    
/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/
     
/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String CHANNEL = "garfield";

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/
    
    @JsonIgnore   @Transient public boolean create_permission() {  return  Controller_Security.get_person().permissions_keys.containsKey(permissions.Garfield_Create.name());}
    @JsonProperty @Transient public boolean edit_permission()   {  return  Controller_Security.get_person().permissions_keys.containsKey(permissions.Garfield_edit.name());}
    @JsonProperty @Transient public boolean read_permission()   {  return  Controller_Security.get_person().permissions_keys.containsKey(permissions.Garfield_read.name());}
    @JsonProperty @Transient public boolean delete_permission() {  return  Controller_Security.get_person().permissions_keys.containsKey(permissions.Garfield_delete.name());}
    @JsonProperty @Transient public boolean update_permission() {  return  Controller_Security.get_person().permissions_keys.containsKey(permissions.Garfield_update.name());}

    public enum permissions {Garfield_read, Garfield_Create, Garfield_edit, Garfield_delete, Garfield_update}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Garfield get_byId(String id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String, Model_Garfield> find = new Model.Finder<>(Model_Garfield.class);
}
