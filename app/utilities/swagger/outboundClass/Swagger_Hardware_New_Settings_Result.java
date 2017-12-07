package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_TypeOfBoardFeatures;
import utilities.document_db.document_objects.DM_Board_Bootloader_DefaultConfig;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model with settings and firwmare and bootloader for Embedded hardware",
        value = "Hardware_New_Settings_Result")
public class Swagger_Hardware_New_Settings_Result {

    @ApiModelProperty(required = false, readOnly = true) public String   full_id;  // [číslo procesoru - přiloží se jen když ho zašle request (oprava vypálení)

    @ApiModelProperty(required = true, readOnly = true) public Swagger_Hardware_New_Settings_Result_Configuration configuration;
}


