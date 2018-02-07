package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "Library_New", description = "Json Model for new Library")
public class Swagger_Library_New extends Swagger_NameAndDesc_ProjectIdOptional {

    @ApiModelProperty(hidden = true)
    public List<String> type_of_board_ids = new ArrayList<>();
}