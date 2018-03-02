package utilities.swagger.input;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "Library_Record", description = "Json Model for Library_Record")
public class Swagger_Library_Record {

    public Swagger_Library_Record() {}

    public String file_name;
    public String content;

}
