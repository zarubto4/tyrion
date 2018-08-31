package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.UUID;

@ApiModel(description = "Filter data for searching in Extension Financial Events",
        value = "ExtensionFinancialEventSearch")
public class Swagger_ExtensionFinancialEvent_Search {
    @ApiModelProperty(required = false)
    public UUID product_id;

    @ApiModelProperty(required = false)
    public UUID invoice_id;

    @ApiModelProperty(required = false)
    public UUID extension_id;

    @ApiModelProperty(required = false)
    public Date from;

    @ApiModelProperty(required = false)
    public Date to;
}
