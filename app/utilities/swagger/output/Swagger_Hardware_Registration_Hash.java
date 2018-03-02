package utilities.swagger.output;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Only For Admins",
        value = "Hardware_Registration_Hash")
public class Swagger_Hardware_Registration_Hash {
    public String hash;
}
