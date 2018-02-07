package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.hardware_registration_auhtority.Batch_Registration_Authority;
import utilities.hardware_registration_auhtority.Hardware_Registration_Authority;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.nio.charset.IllegalCharsetNameException;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Production Batch  ", value = "TypeOfBoardBatch")
@Table(name="TypeOfBoardBatch")
public class Model_TypeOfBoard_Batch extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_TypeOfBoard_Batch.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String revision;                     // Kod HW revize
    public String production_batch;             // Kod HW revizedate_of_assembly
    public String date_of_assembly;             // Den kdy došlo k sestavení
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

    @JsonIgnore @ManyToOne() public Model_TypeOfBoard type_of_board;

    @Column(columnDefinition = "TEXT")  public String description;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty  @Transient public Long latest_used_mac_address() {

        if (latest_used_mac_address == null) {
            Hardware_Registration_Authority.synchronize_mac();
            this.refresh();
        }

        return latest_used_mac_address;
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient
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

    @JsonIgnore @Transient
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
        Batch_Registration_Authority.register_batch(type_of_board, this);
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

    @JsonIgnore                                      public boolean create_permission() {  return type_of_board.update_permission(); }
    @JsonIgnore                                      public boolean read_permission()  {  return type_of_board.read_permission(); }

    @JsonProperty @ApiModelProperty(required = true) public boolean edit_permission()  {  return type_of_board.edit_permission();   }
    @JsonProperty @ApiModelProperty(required = true) public boolean delete_permission() {  return type_of_board.delete_permission(); }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_TypeOfBoard_Batch getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_TypeOfBoard_Batch getById(UUID id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<UUID, Model_TypeOfBoard_Batch> find = new Finder<>(Model_TypeOfBoard_Batch.class);


}
