package utilities.swagger.input;

import io.swagger.annotations.ApiModel;
import models.Model_Library;
import models.Model_LibraryVersion;

@ApiModel(description = "Json Model for Pari With Short detail of Library and Version of Library",
        value = "Library_Library_Version_pair")
public class Swagger_Library_Library_Version_pair{

    public Model_Library library;
    public Model_LibraryVersion library_version;

}
