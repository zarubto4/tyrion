package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "",
        value = "HardwareBackupSettings")
public class Swagger_HardwareBackupSettings {

    @Valid @ApiModelProperty(value = "List of Pairs for settings of Backup on hardware", required = true)
    public List<HardwareBackupPair> hardware_backup_pairs = new ArrayList<>();

    public static class HardwareBackupPair {

        public HardwareBackupPair() {}

        @Constraints.Required @ApiModelProperty(required = true, value = "True - for auto_backup. False for static backup. If static c_program_version_id is required!")  public boolean backup_mode;
                              @ApiModelProperty(required = false, value = "Required if backup_mode is false. C_program_version_id must be compiled and for same type of Board!")  public UUID c_program_version_id;
        @Constraints.Required @ApiModelProperty(required = true)  public UUID hardware_id;
    }
}
