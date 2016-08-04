package utilities.swagger.swagger_diff_tools;


import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
            return Json.parse(IOUtils.toString(Play.application().resourceAsStream("/swagger_history/" + file_name + ".json")));

        }catch (JsonMappingException a){
            logger.error("file with Json Documentation is empty or damaged!");
            return Json.newObject();

        } catch (NullPointerException e) {
            logger.error("file with Json Documentation not found!");
            return null;
        }
    }

    // Zde budu porovnávat změny příchozích souboru API
    public static Swagger_Diff set_API_Changes(String file_name_old, String file_name_new) {
        try {

            logger.debug("Creating api_diff.html content");


            System.err.println("Nahrávám Jsony");
            JsonNode old_api = read_local_File_for_Swagger(file_name_old );
            JsonNode new_api = read_local_File_for_Swagger(file_name_new );

            System.err.println("Vytvářím DIFF objekt");
            Swagger_Diff swagger_Dif = new Swagger_Diff();
            swagger_Dif.new_Version = file_name_new.replace("_", ".");
            swagger_Dif.old_Version = file_name_old.replace("_", ".");


            final Form<Swagger_Api> form_old = Form.form(Swagger_Api.class).bind(old_api);
            Swagger_Api api_old = form_old.get();

            final Form<Swagger_Api> form_new = Form.form(Swagger_Api.class).bind(new_api);
            Swagger_Api api_new = form_new.get();

            api_new.arrange_models(new_api.get("definitions"));
            api_old.arrange_models(old_api.get("definitions"));

            // skupiny v API -------------------------------------------------------------------------------------------
            logger.debug("Checking API TAGS");
            for(Swagger_Api.Tag tag_old : api_old.tags) if(! api_new.contains_tag(tag_old.name)) swagger_Dif.removed_groups.add( tag_old.name  );
            for(Swagger_Api.Tag tag_new : api_new.tags) if(! api_old.contains_tag(tag_new.name)) swagger_Dif.add_groups.add( tag_new.name  );




            // modely v API --------------------------------------------------------------------------------------------
            logger.debug("Checking Models");
            System.err.println("Počet modelů" + api_new.models.size());
            for(String key : api_new.models.keySet()){
                System.out.println("Kontroluji model klíč: " + key);

                if(!api_old.models.containsKey(key)){
                    System.err.println("Oběvil jsem Model který je nový");
                    swagger_Dif.object_new.add( new News(key, JsonWriter.formatJson(  api_new.models.get(key).toString() ) ));
                }
                else if(api_old.models.containsKey(key) &&  !api_old.models.get(key).equals(api_new.models.get(key) )) {
                    System.err.println("Oběvil jsem Model který má změněné vlastnosti");
                    swagger_Dif.object_diffs.add( new Diffs( key,  JsonWriter.formatJson( api_old.models.get(key).toString()) ,  JsonWriter.formatJson( api_new.models.get(key).toString()) ));
                }
            }

            for(String key : api_old.models.keySet()){
                if(!api_new.models.containsKey(key)){
                    System.out.println("Oběvil jsem Model který byl smazán");
                    swagger_Dif.object_removes.add( new Remws(key,  JsonWriter.formatJson( api_old.models.get(key).toString() ) ));
                }
            }


            // API requesty --------------------------------------------------------------------------------------------
            Map<String, JsonNode> old_paths = new HashMap<>();
            Map<String, JsonNode> new_paths = new HashMap<>();

            Iterator<String> iterator_old =  old_api.get("paths").fieldNames();
            while( iterator_old.hasNext() ) {

                String name = iterator_old.next();

                old_paths.put(
                        name,
                        old_api.get("paths").get(name)
                );
            }

            Iterator<String> iterator_new =  new_api.get("paths").fieldNames();
            while( iterator_new.hasNext() ) {

                String name = iterator_new.next();

                new_paths.put(
                        name,
                        new_api.get("paths").get(name)
                );
            }


            for(String key : new_paths.keySet()){

                if(!old_paths.containsKey(key)){
                    swagger_Dif.paths_new.add( new News(key, JsonWriter.formatJson(  new_paths.get(key).toString() ) ));
                }
                else if(old_paths.containsKey(key) &&  !old_paths.get(key).toString().equals( new_paths.get(key).toString() )) {
                    swagger_Dif.paths_diffs.add( new Diffs( key,  JsonWriter.formatJson( old_paths.get(key).toString()) ,  JsonWriter.formatJson( new_paths.get(key).toString()) ));
                }
            }

            for(String key : old_paths.keySet()){
                if(!new_paths.containsKey(key)){
                    swagger_Dif.paths_removes.add( new Remws(key,  JsonWriter.formatJson( old_paths.get(key).toString() ) ));
                }
            }




            logger.debug("Return swagger_Dif Object");
            return swagger_Dif;




        }catch (Exception e ) {
            e.printStackTrace();
              throw new NullPointerException("Došlo k chybě");
        }


    }


}
