package responses;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result_Ok")
public class Result_Ok extends _Response_Interface {

    public Result_Ok() {}

    public Result_Ok(String message) {
        this.message = message;
    }

    @ApiModelProperty(value = "state", allowableValues = "error_person_account_is_not_validated", required = true, readOnly = true)
    public String state() {
        return "ok";
    }

    @ApiModelProperty(value = "code", allowableValues = "200", required = true, readOnly = true)
    public Integer code() {
        return 200;
    }

    @ApiModelProperty(value = "Can be null! If not, you can show that to User. Server fills the message only when it is important", required = false, readOnly = true)
    public String message() {
        if(message != null) return message;
        return "";
    }

}
