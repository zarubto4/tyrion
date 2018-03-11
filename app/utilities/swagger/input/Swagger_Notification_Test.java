package utilities.swagger.input;


import play.data.validation.Constraints;

public class Swagger_Notification_Test {

    @Constraints.Required public String mail;
    @Constraints.Required public String level;
    @Constraints.Required public String importance;
    @Constraints.Required public String type;
    @Constraints.Required public String buttons;
}
