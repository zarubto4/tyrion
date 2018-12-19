package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

@ApiModel(description = "Json Model for new Hardware Batch",
          value = "HardwareBatch_New")
public class Swagger_HardwareBatch_New {

    @Constraints.Required @ApiModelProperty(required = true) public String revision;
    @Constraints.Required @ApiModelProperty(required = true) public String production_batch;
    @Constraints.Required    @ApiModelProperty(required = true, value = "unixTime", readOnly = true, dataType = "integer", example = "1536424319") public Long date_of_assembly;

    @Constraints.Required @ApiModelProperty(required = true) public String pcb_manufacture_name;
    @Constraints.Required @ApiModelProperty(required = true) public String pcb_manufacture_id;

    @Constraints.Required @ApiModelProperty(required = true) public String assembly_manufacture_name;
    @Constraints.Required @ApiModelProperty(required = true) public String assembly_manufacture_id;

    @Constraints.Required @ApiModelProperty(required = true) public String customer_product_name;
    @Constraints.Required @ApiModelProperty(required = true) public String customer_company_name;
    @Constraints.Required @ApiModelProperty(required = true) public String customer_company_made_description;

    @Constraints.Required @ApiModelProperty(required = true) public String mac_address_start;
    @Constraints.Required @ApiModelProperty(required = true) public String mac_address_end;

    @Constraints.Required @ApiModelProperty(required = true) public String ean_number;
                          @ApiModelProperty(required = false) public String description;

}
