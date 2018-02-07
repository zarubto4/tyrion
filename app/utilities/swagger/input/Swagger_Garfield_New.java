package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.UUID;

/**
 * Created by zaruba on 23.08.17.
 */
@ApiModel(value = "Garfield_New", description = "Json Model for creating new Garfield.")
public class Swagger_Garfield_New extends Swagger_NameAndDescription {

    public String hardware_tester_id;

    public Integer print_label_id_1;   // 12 mm
    public Integer print_label_id_2;   // 24 mm
    public Integer print_sticker_id;   // 65 mm

    public UUID type_of_board_id;    // Jaký typ hardwaru umí testovat garfield!
    public UUID producer_id;
}
