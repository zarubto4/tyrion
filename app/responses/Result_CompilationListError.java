package responses;

import io.swagger.annotations.ApiModel;
import utilities.swagger.output.Swagger_Compilation_Build_Error;

import java.util.ArrayList;
import java.util.List;


@ApiModel(value="ExternalServerSideError", description="Unknown Error on external server ")
public class Result_CompilationListError implements Response_Interface{

    public List<Swagger_Compilation_Build_Error> errors = new ArrayList<>();
}
