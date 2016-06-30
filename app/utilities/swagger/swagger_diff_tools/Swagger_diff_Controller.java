package utilities.swagger.swagger_diff_tools;


import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import play.Play;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.loggy.Loggy;
import utilities.response.GlobalResult;
import utilities.swagger.swagger_diff_tools.servise_class.*;

import java.io.IOException;

public class Swagger_diff_Controller extends Controller {

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public Result getResources_version(String version){
        try{
            JsonNode old_api = read_local_File_for_Swagger( version );

            return GlobalResult.result_ok(old_api);
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


    public static JsonNode read_local_File_for_Swagger(String file_name) throws IOException {
        try {

            logger.debug("Replacing dots in file name");
            file_name = file_name.replace(".", "_");
            logger.debug("File name is " + file_name);

            logger.debug("Return Json of Swagger Documentation");
            return Json.parse( IOUtils.toString(Play.application().resourceAsStream("/swagger_history/" +  file_name +  ".json")) );

        } catch (NullPointerException e) {
            logger.debug("file with Json Documentation not found!");
            return null;
        }
    }

    // Zde budu porovnávat změny příchozích souboru API
    public static Swagger_Diff set_API_Changes() {
        try {

            logger.debug("Creating api_diff.html content");

            String file_name_old = "1.06.05";
            String file_name_new = "1.06.06";

            JsonNode old_api = read_local_File_for_Swagger(file_name_old );
            JsonNode new_api = read_local_File_for_Swagger(file_name_new );

            logger.debug("old: " + old_api);
            logger.debug("new: " + new_api);


            Swagger_Diff swagger_Dif = new Swagger_Diff();
            swagger_Dif.new_Version = file_name_new;
            swagger_Dif.old_Version = file_name_old;

            final Form<Swagger_Api> form_old = Form.form(Swagger_Api.class).bind(old_api);
            Swagger_Api api_old = form_old.get();

            final Form<Swagger_Api> form_new = Form.form(Swagger_Api.class).bind(new_api);
            Swagger_Api api_new = form_new.get();


            logger.debug("Checking API TAGS");
            for(Swagger_Api.Tag tag_old : api_old.tags) if(! api_new.contains_tag(tag_old.name)) swagger_Dif.add_groups.add( tag_old.name  );
            for(Swagger_Api.Tag tag_new : api_new.tags) if(! api_old.contains_tag(tag_new.name)) swagger_Dif.removed_groups.add( tag_new.name  );

            logger.debug("Checking Models");
            api_old.arrange_models( old_api.get("definitions") );
            api_new.arrange_models( new_api.get("definitions") );

            System.out.println("těch je v old: " + api_old.models.size());
            System.out.println("těch je v new: " + api_new.models.size());

            for(String key : api_new.models.keySet()){

                if(!api_old.models.containsKey(key)){
                    System.out.println("Stará verze neobsahuje něco z nové a tak budu zobrazovat NEW ");
                    swagger_Dif.object_new.add( new News(key, JsonWriter.formatJson(  api_new.models.get(key).toString() ) ));
                }
                else if(api_old.models.containsKey(key) &&  !api_old.models.get(key).equals(api_new.models.get(key) )) {

                    System.out.println("Vládám diferenci");

                    swagger_Dif.diffs.add( new Diffs( key,  JsonWriter.formatJson( api_old.models.get(key).toString()) ,  JsonWriter.formatJson( api_new.models.get(key).toString()) ));
                }
            }

            for(String key : api_old.models.keySet()){
                if(!api_new.models.containsKey(key)){
                    System.out.println("Stará verze obsahuje něco co nové ne a tak budu zobrazovat v Removes ");
                    swagger_Dif.object_removes.add( new Remws(key,  JsonWriter.formatJson( api_old.models.get(key).toString() ) ));
                }
            }

//

            //--------------------------------------------------------------------------------------------------

            logger.debug("Return swagger_Dif Object");
            return swagger_Dif;

        }catch (Exception e ) {
            e.printStackTrace();
              throw new NullPointerException("Došlo k chybě");
        }


    }


}
