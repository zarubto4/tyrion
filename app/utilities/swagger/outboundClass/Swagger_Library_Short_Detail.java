package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.Swagger;
import models.Model_Tariff;
import models.Model_TypeOfBoard;
import utilities.swagger.documentationClass.Swagger_Board_CProgram_Pair;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for Library Short Detail",
        value = "Library_Short_Detail")
public class Swagger_Library_Short_Detail {

    @ApiModelProperty(required = true, readOnly = true)
    public String id;

    @ApiModelProperty(required = true, readOnly = true)
    public String name;

    @ApiModelProperty(required = true, readOnly = true)
    public String description;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_TypeOfBoard_ShortDetail> type_of_boards = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public boolean update_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean edit_permission;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean delete_permission;



    @JsonIgnore @Transient
    public void add_board_type(Model_TypeOfBoard typeOfBoard){

        Swagger_TypeOfBoard_ShortDetail pair = new Swagger_TypeOfBoard_ShortDetail();
        pair.name = typeOfBoard.name;
        pair.id   = typeOfBoard.id;

        type_of_boards.add(pair);
    }



 // Help class ---------------------------------------------------------------------------------------------------------

}

