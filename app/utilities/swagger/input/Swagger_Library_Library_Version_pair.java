package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import models.Model_Library;
import models.Model_LibraryVersion;
import utilities.swagger.output.Swagger_Short_Reference;

@ApiModel(description = "Json Model for Pair With detail of Library and Version of Library",
        value = "Library_Library_Version_pair")
public class Swagger_Library_Library_Version_pair{

    public Swagger_Short_Reference library;
    public Swagger_Short_Reference library_version;

}
