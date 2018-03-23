package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

@ApiModel(description = "Json Model for Compilation Result",
          value = "Compilation_Build_Error")
public class Swagger_Compilation_Build_Error extends _Swagger_Abstract_Default {

    @ApiModelProperty(value = "Value is build_error", required = true, readOnly = true)
    public String state = "build_error";

    @ApiModelProperty(required = true, readOnly = true)
    public String file_name;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer line;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer column;

    @ApiModelProperty(required = true, readOnly = true)
    public String type;

    @ApiModelProperty(required = true, readOnly = true)
    public String text;

    @ApiModelProperty(required = true, readOnly = true)
    public String code_white_space;

    @ApiModelProperty(required = true, readOnly = true)
    public String code;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer adjusted_column;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer start_index;

    @ApiModelProperty(required = true, readOnly = true)
    public Integer end_index;

}
