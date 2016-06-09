package utilities.swagger.swagger_diff_tools;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import play.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.loggy.Loggy;
import utilities.response.GlobalResult;

import java.io.IOException;

public class Swagger_diff_Controller extends Controller {


    public Result getResources_version(String version){
        try{
            String content_old = read_local_File_for_Swagger( version );
            JsonNode old_api = Json.parse(content_old);

            return GlobalResult.result_ok(old_api);
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


    public String read_local_File_for_Swagger(String file_name) throws IOException {
        try {

            file_name = file_name.replace(".", "_");
            System.out.println("Jméno souboru je " +file_name);

            return  IOUtils.toString(Play.application().resourceAsStream("/swagger_history/" +  file_name +  ".json"));

        } catch (NullPointerException e) {
            System.out.println("Null point exception");
            return null;
        }
    }


}
