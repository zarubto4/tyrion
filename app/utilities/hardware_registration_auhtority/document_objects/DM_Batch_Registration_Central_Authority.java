package utilities.hardware_registration_auhtority.document_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

public class DM_Batch_Registration_Central_Authority {

    public static final String COLLECTION_NAME = "batch-registration-authority";

    public DM_Batch_Registration_Central_Authority(){}


    @ApiModelProperty(required = true) @Constraints.Required public UUID id;

    @ApiModelProperty(required = true) @Constraints.Required public String revision;                     // Kod HW revize
    @ApiModelProperty(required = true) @Constraints.Required public String production_batch;             // Kod HW revizedate_of_assembly
    @ApiModelProperty(required = true) @Constraints.Required public String date_of_assembly;             // Den kdy došlo k sestavení
    @ApiModelProperty(required = true) @Constraints.Required public String pcb_manufacture_name;         // Jméno výrobce desky
    @ApiModelProperty(required = true) @Constraints.Required public String pcb_manufacture_id;           // Kod výrobce desky
    @ApiModelProperty(required = true) @Constraints.Required public String assembly_manufacture_name;    // Jméno firmy co osazovala DPS
    @ApiModelProperty(required = true) @Constraints.Required public String assembly_manufacture_id;      // Kod firmy co osazovala DPS

    @ApiModelProperty(required = true) @Constraints.Required public String customer_product_name;        // Jméno HW co bude na štítku
    @ApiModelProperty(required = true) @Constraints.Required public String customer_company_name;        // Jméno várobce co bude na štítku
    @ApiModelProperty(required = true) @Constraints.Required public String customer_company_made_description;      // Made in Czech Republic (co bude na štítku)

    @ApiModelProperty(required = true) @Constraints.Required  public String mac_address_start;
    @ApiModelProperty(required = true) @Constraints.Required  public String mac_address_end;
    @ApiModelProperty(required = true) @Constraints.Required  public String latest_used_mac_address;  // Pro přiřazení je vždy nutné zvednout novou verzi - tato hodnota se dosynchronizovává se serverem

    @ApiModelProperty(required = true) @Constraints.Required  public String ean_number;
    @ApiModelProperty(required = true)                         public String description;

    // Typoe of board conections
    @ApiModelProperty(required = true) @Constraints.Required  public String type_of_board_compiler_target_name;
}

