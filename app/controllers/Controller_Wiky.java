package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;


import org.json.JSONObject;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import utilities.logger.Class_Logger;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Wiky.class);

     public Result test1(){
         try {

             // Chci zpracovat JSON o Pepovi
           //  JsonNode muj_json = request().body().asJson();

             String muj_sstring = request().body().asText();

             // Vypíšu si co je uvnitř
             System.out.println(  muj_sstring );


             String name;
             String prijmeni;
             Integer age;
             String street;


             // - Napiš Cyklus - Kterej


             ObjectNode co_vracim = Json.newObject();
          //   co_vracim.put("name", muj_json.get("jeho_name").textValue() + " je drsoň"   );
             co_vracim.put("sadfsadasafd", "asdfsfasdfsdfs");
          //   co_vracim.put("age" , muj_json.get("jeho_age").asInt() );


             return ok( co_vracim );

         }catch (Exception e){
             e.printStackTrace();
             return badRequest();
         }
     }


    public Result test2(){
        try {

            return ok();
        }catch (Exception e){
            e.printStackTrace();
            return badRequest();
        }

    }


    public Result test3(){
        try {

            return ok();
        }catch (Exception e){
            e.printStackTrace();
            return badRequest();
        }

    }

}
