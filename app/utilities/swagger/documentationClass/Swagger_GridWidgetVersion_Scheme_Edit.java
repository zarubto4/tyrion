package utilities.swagger.documentationClass;

import play.data.validation.Constraints;


public class Swagger_GridWidgetVersion_Scheme_Edit {

    @Constraints.Required
    public String design_json;

    @Constraints.Required
    public String logic_json;
}
