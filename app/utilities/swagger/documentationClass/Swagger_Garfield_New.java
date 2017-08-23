package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

/**
 * Created by zaruba on 23.08.17.
 */
@ApiModel(value = "Garfield_New", description = "Json Model for creating new Garfield.")
public class Swagger_Garfield_New {

    @Constraints.Required
    @Constraints.MinLength(value = 4, message = "The street must have at least 4 characters")
    @ApiModelProperty(required = true, value = "The street must have at least 4 characters")
    public String name;

    public String description;

    public String hardware_tester_id;

    public Integer print_label_id_1;   // 12 mm
    public Integer print_label_id_2;   // 24 mm
    public Integer print_sticker_id;   // 65 mm

    public String type_of_board_id;    // Jaký typ hardwaru umí testovat garfield!
    public String producer_id;
}
