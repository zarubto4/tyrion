package utilities.swagger.output.filter_results;

import io.swagger.annotations.ApiModelProperty;
import utilities.swagger._Swagger_Abstract_Default;

/**
 * Created by zaruba on 01.12.17.
 */
public abstract class Filter_Common extends _Swagger_Abstract_Default {

    /* Basic Filter Value --------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, value = "First value position from all subjects. Minimum is 0.")
    public int from;

    @ApiModelProperty(required = true, readOnly = true, value = "Minimum is \"from\" Maximum is \"total\"")
    public int to;

    @ApiModelProperty(required = true, readOnly = true, value = "Total subjects")
    public int total;

    @ApiModelProperty(required = true, readOnly = true, value = "Numbers of pages, which you can call")
    public int pages;

}
