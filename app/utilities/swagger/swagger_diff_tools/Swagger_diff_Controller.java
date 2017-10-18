package utilities.swagger.swagger_diff_tools;


import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import play.Application;
import play.Play;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.response.GlobalResult;
import utilities.swagger.swagger_diff_tools.servise_class.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Swagger_diff_Controller extends Controller {


/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Swagger_diff_Controller.class);

// - Oblužné metody - primárně pro Wiev Tyriona ------------------------------------------------------------------------


    public Result getResources_version(String version){
        try{
            JsonNode old_api = read_local_File_for_Swagger( version );

            return GlobalResult.result_ok(old_api);
        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    public static JsonNode read_local_File_for_Swagger(String file_name) throws IOException {
        try {

            terminal_logger.debug("Replacing dots in file name");
            file_name = file_name.replace(".", "_");
            terminal_logger.debug("File name is " + file_name);

            terminal_logger.debug("Return Json of Swagger Documentation");
            return Json.parse(IOUtils.toString(Play.application().resourceAsStream("/swagger_history/" + file_name + ".json")));

        }catch (JsonMappingException a){
            terminal_logger.internalServerError(new Exception("File with Json Documentation is empty or damaged!"));
            return Json.newObject();

        } catch (NullPointerException e) {
            terminal_logger.internalServerError(new Exception("File with Json Documentation not found!"));
            return null;
        }
    }

    public static List<String> json_docu_files(){

        List<String> fileNames = new ArrayList<>();
        File[] files = new File(play.api.Play.current().injector().instanceOf(Application.class).path() + "/conf/swagger_history").listFiles();

        for (File file : files) { fileNames.add((file.getName().substring(0, file.getName().lastIndexOf('.'))).replace("_", "."));}

        return fileNames;
    }


    // Zde budu porovnávat změny příchozích souboru API
    public static Swagger_Diff set_API_Changes(String file_name_old, String file_name_new) {
        try {

            terminal_logger.debug("Creating api_diff.html content");



            JsonNode old_api = read_local_File_for_Swagger(file_name_old );
            JsonNode new_api = read_local_File_for_Swagger(file_name_new );


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
            terminal_logger.debug("Checking API TAGS");
            for(Swagger_Api.Tag tag_old : api_old.tags) if(! api_new.contains_tag(tag_old.name)) swagger_Dif.removed_groups.add( tag_old.name  );
            for(Swagger_Api.Tag tag_new : api_new.tags) if(! api_old.contains_tag(tag_new.name)) swagger_Dif.add_groups.add( tag_new.name  );




            // modely v API --------------------------------------------------------------------------------------------
            terminal_logger.debug("Checking Models");
            for(String key : api_new.models.keySet()){


                if(!api_old.models.containsKey(key)){
                    swagger_Dif.object_new.add( new News(key, JsonWriter.formatJson(  api_new.models.get(key).toString() ) ));
                }
                else if(api_old.models.containsKey(key) &&  !api_old.models.get(key).equals(api_new.models.get(key) )) {
                      swagger_Dif.object_diffs.add( new Diffs( key,  JsonWriter.formatJson( api_old.models.get(key).toString()) ,  JsonWriter.formatJson( api_new.models.get(key).toString()) ));
                }
            }

            for(String key : api_old.models.keySet()){
                if(!api_new.models.containsKey(key)){
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




            terminal_logger.debug("Return swagger_Dif Object");
            return swagger_Dif;




        }catch (Exception e ) {
            terminal_logger.internalServerError(e);
            throw new NullPointerException("Došlo k chybě");
        }


    }


}
