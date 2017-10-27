package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Publishing_type;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "",
          value = "TypeOfBoard_ShortDetail")
public class Swagger_TypeOfBoard_ShortDetail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

}
