package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "Garfield", description = "Model of Garfield test set")
@Table(name="Garfield")
public class Model_Garfield extends NamedModel implements Permissible {

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

            if (hardware_type_id != null) return Model_HardwareType.find.byId(hardware_type_id);
            return null;

        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Model_Producer producer() {
        try {

            if (producer_id != null) return Model_Producer.find.byId(producer_id);
            return null;

        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
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

    @Override
    public EntityType getEntityType() {
        return EntityType.GARFIELD;
    }

    @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.PUBLISH);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Garfield.class)
    public static CacheFinder<Model_Garfield> find = new CacheFinder<>(Model_Garfield.class);
}
