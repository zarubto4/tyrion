package utilities.swagger.documentationClass;


import play.data.validation.Constraints;

public class Swagger_GridObject_Approve_withChanges {

    @Constraints.Required
    public String object_id;

    @Constraints.Required
    public String state;

    @Constraints.Required
    @Constraints.MinLength(value = 8,  message = "The name must have at least 8 characters.")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters.")
    public String grid_widget_name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String grid_widget_general_description;

    @Constraints.Required
    public String grid_widget_type_of_widget_id;

    @Constraints.Required
    @Constraints.MinLength(value = 2, message = "The name must have at least 2 characters")
    @Constraints.MaxLength(value = 60, message = "The name must not have more than 60 characters")
    public String grid_widget_version_name;

    @Constraints.Required
    @Constraints.MinLength(value = 24, message = "The description must have at least 24 characters")
    @Constraints.MaxLength(value = 255, message = "The description must not have more than 255 characters.")
    public String grid_widget_version_description;

    @Constraints.Required
    public String grid_widget_design_json;

    @Constraints.Required
    public String grid_widget_logic_json;

    @Constraints.Required
    public String reason;
}
