package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;
import utilities.swagger.outboundClass.Swagger_Library_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Library_Version_Short_Detail;


@ApiModel(description = "Json Model for Pari With Short detail of Library and Version of Library",
        value = "Library_Library_Version_pair")
public class Swagger_Library_Library_Version_pair{

    public Swagger_Library_Short_Detail library_short_detail;
    public Swagger_Library_Version_Short_Detail library_version_short_detail;

}
