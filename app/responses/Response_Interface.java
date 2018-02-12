package responses;


import io.swagger.annotations.ApiModelProperty;

public abstract class Response_Interface {

    @ApiModelProperty(value = "code", required = true, readOnly = true)
    public Integer code;

    @ApiModelProperty(value = "Can be null! If not, you can show that to User. Server fills the message only when it is important.", required = false, readOnly = true)
    public String message;

}
