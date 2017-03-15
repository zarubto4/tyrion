package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.Model_MProjectProgramSnapShot;

import java.util.List;

@ApiModel(description = "",
        value = "Terminal_M_Project_Snapshot")
public class Swagger_Mobile_M_Project_Snapshot {

    @ApiModelProperty(required = true, readOnly = true)  public String instance_record_id;

    @ApiModelProperty(required = true, readOnly = true)   public String b_program_name;
    @ApiModelProperty(required = true, readOnly = true)   public String b_program_description;

    @ApiModelProperty(required = true, readOnly = true)    public List<Model_MProjectProgramSnapShot> snapshots;
}
