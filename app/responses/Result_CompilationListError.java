package responses;

import io.swagger.annotations.ApiModel;
import utilities.swagger.output.Swagger_Compilation_Build_Error;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.ArrayList;
import java.util.List;


@ApiModel(value="Result_ExternalServerSideError", description="Unknown Error on external server ")
public class Result_CompilationListError extends _Swagger_Abstract_Default {

    public List<Swagger_Compilation_Build_Error> errors = new ArrayList<>();

}
