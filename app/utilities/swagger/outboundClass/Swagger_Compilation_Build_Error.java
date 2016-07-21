package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Compilation Result",
          value = "Compilation_Build_Error")
public class Swagger_Compilation_Build_Error {

    @ApiModelProperty(value = "Value is build_error", required = true, readOnly = true)
    public String state = "build_error";

    @ApiModelProperty(required = true, readOnly = true)
    public String filename;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer line;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer column;

    @ApiModelProperty(required = true, readOnly = true)
    public String type;

    @ApiModelProperty(required = true, readOnly = true)
    public String text;

    @ApiModelProperty(required = true, readOnly = true)
    public String codeWhitespace;

    @ApiModelProperty(required = true, readOnly = true)
    public String code;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer adjustedColumn;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer startIndex;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer endIndex;

}
