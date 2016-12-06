package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;


@ApiModel(description = "Json Model for new Basic Login",
        value = "Login")
public class Login_IncomingLogin {

    @Constraints.Required
    @Constraints.Email
    @ApiModelProperty(required = true)
    public String mail;


    @Constraints.Required
    @ApiModelProperty(required = true)
    public String password;


// ----------------------------------------------------------------

    public String getMail() {
        return mail;
    }

    public String getPassword() {
        return password;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}

