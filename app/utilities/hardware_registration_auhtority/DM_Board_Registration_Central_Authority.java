package utilities.hardware_registration_auhtority;

import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.Date;

/**
 * This Object is used to save "Virtual Hardware" to central registration authority
 */
public class DM_Board_Registration_Central_Authority {

    public static final String COLLECTION_NAME = "hardware-registration-authority";

    public DM_Board_Registration_Central_Authority() {}

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String full_id;
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String mac_address;
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String hash_for_adding;
    @ApiModelProperty(required = false, readOnly = true)                       public String personal_name; // Latest know name

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String hardware_type_compiler_target_name;

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String created;
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String production_batch_id;    // Kod HW revizedate_of_assembly

    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String mqtt_password;        // Kod firmy co osazovala DPS
    @ApiModelProperty(required = true, readOnly = true) @Constraints.Required  public String mqtt_username;        // Kod firmy co osazovala DPS

    /** Optional ! - Not supported now
        CAN_REGISTER,
        NOT_EXIST,
        ALREADY_REGISTERED_IN_YOUR_ACCOUNT,
        ALREADY_REGISTERED,
        PERMANENTLY_DISABLED,
        BROKEN_DEVICE;
     */
    public String state;
}
