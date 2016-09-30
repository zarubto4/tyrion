package utilities.swagger.outboundClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for details about Tariff",
          value = "Tariff_Label")
public class Swagger_Tariff_Label {

    public Swagger_Tariff_Label(){}

    @ApiModelProperty(required = true, readOnly = true, value = "Basic Text value - for each line")
    public String label;

    @ApiModelProperty(required = false, readOnly = true,  value = "If not null or empty description=\"\" value - Show this text under the mouse pointer")
    public String description;

    @ApiModelProperty(required = true, readOnly = true,  value = "icon code - AwesomeFont library")
    public String icon;

    @ApiModelProperty(required = true, readOnly = true, value = "Basic Text value - for each line")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @ApiModelProperty(required = true, readOnly = true, value = "If not null or empty description=\"\" value - Show this text under the mouse pointer")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}