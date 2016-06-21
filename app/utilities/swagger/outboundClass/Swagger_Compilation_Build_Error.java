package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model for Compilation Result",
          value = "Compilation_Build_Error")
public class Swagger_Compilation_Build_Error {

    @ApiModelProperty(value = "Value is build_error", required = true, readOnly = true)
    public String state = "build_error";
    public String filename;
    public Integer line;
    public Integer column;
    public String type;
    public String text;
    public String codeWhitespace;
    public String code;
    public Integer adjustedColumn;
    public Integer startIndex;
    public Integer endIndex;
}
