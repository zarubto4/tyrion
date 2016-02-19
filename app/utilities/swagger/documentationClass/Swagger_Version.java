package utilities.swagger.documentationClass;

import java.util.ArrayList;

public class Swagger_Version {

    public String version_name;
    public String version_description;

    public ArrayList<PrivateFiles> files;

    private class PrivateFiles{
       public String content;
       public String fileName;
    }

}
