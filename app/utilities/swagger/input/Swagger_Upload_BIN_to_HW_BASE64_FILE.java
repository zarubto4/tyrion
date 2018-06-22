package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;
import utilities.enums.FirmwareType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for upload Bin file to Hardware with Manual Update procedure",
          value = "Upload_bin")
public class Swagger_Upload_BIN_to_HW_BASE64_FILE extends Swagger_BASE64_FILE{

   @Constraints.Required public List<UUID> hardware_ids = new ArrayList<>();
   @Constraints.Required public FirmwareType firmware_type;



}
