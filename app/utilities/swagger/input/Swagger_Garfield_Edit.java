package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

/**
 * Created by zaruba on 23.08.17.
 */
@ApiModel(value = "Garfield_Edit", description = "Json Model for creating new Garfield.")
public class Swagger_Garfield_Edit extends Swagger_NameAndDescription {

    public String hardware_tester_id;

    public Integer print_label_id_1;   // 12 mm
    public Integer print_label_id_2;   // 24 mm
    public Integer print_sticker_id;   // 65 mm

}
