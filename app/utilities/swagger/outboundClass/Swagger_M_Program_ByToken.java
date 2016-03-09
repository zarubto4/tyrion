package utilities.swagger.outboundClass;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Json Model that contain values for QR code Request",
          value = "M_Program_Code")
public class Swagger_M_Program_ByToken {

    @ApiModelProperty(required = true, value = "Program in Json -> in String")
    public String program;

    public String websocket_address; // TODO řešení compilation erroru, přepsat

}
