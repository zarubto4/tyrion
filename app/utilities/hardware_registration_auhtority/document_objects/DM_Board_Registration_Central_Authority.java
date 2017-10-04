package utilities.hardware_registration_auhtority.document_objects;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.Date;

public class DM_Board_Registration_Central_Authority {

    public static final String COLLECTION_NAME = "HW_Test_Registration_Collection";

    public DM_Board_Registration_Central_Authority(){}

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String board_id;
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String mac_address;
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String hash_for_adding;

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String type_of_board_compiler_target_name;
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String type_of_board_revision_name;

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public Date date_of_create;

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String revision;                               // Kod HW revize
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String production_batch;                       // Kod HW revizedate_of_assembly
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String date_of_assembly;                       // Den kdy došlo k sestavení
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String pcb_manufacture_name;                   // Jméno výrobce desky
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String pcb_manufacture_id;                     // Kod výrobce desky
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String assembly_manufacture_name;              // Jméno firmy co osazovala DPS
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String assembly_manufacture_id;                // Kod firmy co osazovala DPS
}
