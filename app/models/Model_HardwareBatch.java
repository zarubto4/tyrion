package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.hardware_registration_auhtority.Batch_Registration_Authority;
import utilities.hardware_registration_auhtority.Hardware_Registration_Authority;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.nio.charset.IllegalCharsetNameException;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Production Batch  ", value = "HardwareBatch")
@Table(name="HardwareBatch")
public class Model_HardwareBatch extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HardwareBatch.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String revision;                     // Kod HW revize
    public String production_batch;             // Kod HW revizedate_of_assembly
    public String assembled;                    // Den kdy došlo k sestavení
    public String pcb_manufacture_name;         // Jméno výrobce desky
    public String pcb_manufacture_id;           // Kod výrobce desky
    public String assembly_manufacture_name;    // Jméno firmy co osazovala DPS
    public String assembly_manufacture_id;      // Kod firmy co osazovala DPS

    public String customer_product_name;        // Jméno HW co bude na štítku
    public String customer_company_name;        // Jméno várobce co bude na štítku
    public String customer_company_made_description;      // Made in Czech Republic (co bude na štítku)

    public Long mac_address_start;
    public Long mac_address_end;
    @JsonIgnore  public Long latest_used_mac_address;  // Pro přiřazení je vždy nutné zvednout novou verzi - tato hodnota se dosynchronizovává se serverem

    public Long ean_number;

    @JsonIgnore @ManyToOne public Model_HardwareType hardware_type;

    @Column(columnDefinition = "TEXT")  public String description;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty
    public Long latest_used_mac_address() {

        if (latest_used_mac_address == null) {
            Hardware_Registration_Authority.synchronize_mac();
            this.refresh();
        }

        return latest_used_mac_address;
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


    @JsonIgnore
    public String get_nextMacAddress_just_for_check() throws IllegalCharsetNameException{

        if (latest_used_mac_address == null) {
            Hardware_Registration_Authority.synchronize_mac();
            this.refresh();
        }


        // Its used only for check - if some other server dont use this mac address and if its not registred in central hardware registration authority
        if (latest_used_mac_address == null) {
            return convert_to_MAC_ISO(mac_address_start);
        }

        if (latest_used_mac_address >= mac_address_end) {
            throw new IllegalCharsetNameException("All Mac Address used");
        }

        return convert_to_MAC_ISO(this.latest_used_mac_address + 1);
    }

    @JsonIgnore
    public String get_new_MacAddress() throws IllegalCharsetNameException{

        if (latest_used_mac_address == null) {
            Hardware_Registration_Authority.synchronize_mac();
            this.refresh();
        }

        if (latest_used_mac_address == null) {
            latest_used_mac_address = mac_address_start;
            this.update();
            return get_new_MacAddress();
        }

        if (latest_used_mac_address >= mac_address_end) {
            throw new IllegalCharsetNameException("All Mac Address used");
        }

        this.latest_used_mac_address = latest_used_mac_address + 1;
        update();

        return convert_to_MAC_ISO(this.latest_used_mac_address);

    }

    //Konvertor Long na ISO normu Mac addressy
    @JsonIgnore
    public static String convert_to_MAC_ISO(Long mac) {

        if (mac > 0xFFFFFFFFFFFFL || mac < 0) {
            throw new IllegalArgumentException("mac out of range");
        }

        StringBuffer m = new StringBuffer(Long.toString(mac, 16));
        while (m.length() < 12) m.insert(0, "0");

        for (int j = m.length() - 2; j >= 2; j-=2) {
            m.insert(j, ":");
        }

        return m.toString().toUpperCase();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        this.id = UUID.randomUUID();

        if (latest_used_mac_address == null) latest_used_mac_address = mac_address_start;
        Batch_Registration_Authority.register_batch(hardware_type, this);
        super.save();
    }

    @JsonIgnore
    public void save_from_central_authority() {
        super.save();
    }

    @JsonIgnore @Override public boolean delete() {
        this.deleted = true;
        super.update();
        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {  hardware_type.check_update_permission(); }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {  hardware_type.check_read_permission(); }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception { hardware_type.check_update_permission(); }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {  hardware_type.check_delete_permission(); }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_HardwareBatch getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_HardwareBatch getById(UUID id) {

        Model_HardwareBatch batch = find.byId(id);
        if (batch == null) throw new Result_Error_NotFound(Model_HardwareBatch.class);

        batch.check_read_permission();
        return batch;

    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<UUID, Model_HardwareBatch> find = new Finder<>(Model_HardwareBatch.class);


}
