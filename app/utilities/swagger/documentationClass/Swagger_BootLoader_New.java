package utilities.swagger.documentationClass;

public class Swagger_BootLoader_New {

    public String name;
    public String version_identificator;
    public String changing_notes;
    public String description;



    public boolean control_identificator(){

        try {
            String[] parts = version_identificator.split("\\."); // String array, each element is text between dots
            String beforeFirstDot = parts[0];    // Text before the first dot
            if (parts.length != 3) return false;

            int low    = Integer.parseInt(parts[0]);
            int midlle = Integer.parseInt(parts[1]);
            int hight  = Integer.parseInt(parts[2]);

            if ( low  > 255 || low < 0 ||  midlle  > 255 || midlle < 0 ||  hight  > 255 || hight < 0) return false;

            return true;

        }catch (Exception e){
            return false;
        }
    }
}
