package utilities.swagger.swagger_diff_tools;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.UtilTools;
import utilities.loggy.Loggy;
import utilities.response.GlobalResult;

import java.nio.charset.StandardCharsets;

public class Swagger_diff_Controller extends Controller {


    public Result getResources_version(String version){
        try{
            String content_old = UtilTools.readFile("app/utilities/swagger/swagger_diff_tools/json_files/" + version + ".json", StandardCharsets.UTF_8);
            JsonNode old_api = Json.parse(content_old);

            return GlobalResult.result_ok(old_api);
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

}
