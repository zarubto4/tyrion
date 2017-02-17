package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model with List of Board ID",
        value = "List of Boards")
public class Swagger_UploadBinaryFileToBoard {

    @Constraints.Required
    @ApiModelProperty(required = true, value = "Only boards where type_of_board.connectible_to_internet = true ")
    public List<String> board_ids = new ArrayList<>();

    @Constraints.Required
    @ApiModelProperty(required = true)
    public String version_id;
}
