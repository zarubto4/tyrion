package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
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

    @JsonIgnore          public UUID hardware_type_id;    // Jaký typ hardwaru umí testovat garfield! ( Je to zatím předpokládáno na 1:1) Vazba není přes ORM!!!
    @JsonIgnore          public UUID producer_id;         // Kdo desku vyrobil!

    @JsonIgnore @Column(columnDefinition = "TEXT")
    public String configurations;

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_HardwareType hardware_type() {
        try {

            if (hardware_type_id != null) return Model_HardwareType.getById(hardware_type_id);
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
    
/* JSON IGNORE VALUES --------------------------------------------------------------------------------------------------*/
     
/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public static class Configurations {
        public Configurations() {}

        List<TestConfiguration> test = new ArrayList<>();
    }

    public static class TestConfiguration {
        public TestConfiguration() {}

        public Pins pins;
        public Power power;
    }

    public static class Pins {
        public Pins() {}

        public PinValues up;
        public PinValues down;
    }

    public static class PinValues {
        public PinValues() {}

        public List<String> x = new ArrayList<>();
        public List<String> y = new ArrayList<>();
        public List<String> z = new ArrayList<>();
    }

    public static class Power {
        public Power() {}

        public PowerSource poe_act;
        public PowerSource poe_pas;
        public PowerSource ext_pwr;
        public PowerSource usb_pwr;
    }

    public static class PowerSource {
        public PowerSource() {}

        public PowerParams vbus;
        public PowerParams v3;
        public PowerParams curr;
    }

    public static class PowerParams {
        public PowerParams() {}

        public Double min;
        public Double max;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String CHANNEL = "garfield";

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/
    
    @JsonIgnore   public void check_create_permission() throws _Base_Result_Exception {  if(!_BaseController.person().has_permission(Permission.Garfield_create.name())) throw new Result_Error_PermissionDenied();}
    @JsonProperty public void check_read_permission()   throws _Base_Result_Exception {  if(!_BaseController.person().has_permission(Permission.Garfield_read.name()))   throw new Result_Error_PermissionDenied();}
    @JsonProperty public void check_delete_permission() throws _Base_Result_Exception {  if(!_BaseController.person().has_permission(Permission.Garfield_delete.name())) throw new Result_Error_PermissionDenied();}
    @JsonProperty public void check_update_permission() throws _Base_Result_Exception {  if(!_BaseController.person().has_permission(Permission.Garfield_update.name())) throw new Result_Error_PermissionDenied();}

    public enum Permission { Garfield_create, Garfield_read, Garfield_edit, Garfield_update, Garfield_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Garfield getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_Garfield getById(UUID id) throws _Base_Result_Exception {

        Model_Garfield garfield = find.byId(id);
        if (garfield == null) throw new Result_Error_NotFound(Model_Garfield.class);

        garfield.check_read_permission();
        return garfield;

    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Garfield> find = new Finder<>(Model_Garfield.class);
}
