package utilities.gsm_services.things_mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.gsm_services.things_mobile.help_class.*;
import utilities.lablel_printer_service.Printer_Api;
import utilities.logger.Logger;
import websocket.messages.homer_with_tyrion.configuration.WS_Message_Homer_Get_homer_server_configuration;

import java.lang.reflect.Array;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Controller_Things_Mobile {

    @Inject public static _BaseFormFactory baseFormFactory;
    @Inject public static Config configuration; // Its Required to set this in Server.class Component

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    // Logger
    private static final Logger logger = new Logger(Controller_Things_Mobile.class);

    private static String api_key = "9f8879bc-a700-4588-8d3c-ef3864bcfd2b"; // TODO podívám je jak se to dělá v třídě server respective kdekoliv kde pracuji s config souborem
    private static String thingsmobile_url = "https://www.thingsmobile.com/";

    // example List<String> groups = this.configuration.getStringList("logger.logged_groups");

    /* HELPER PRIVATE CLASS   ----------------------------------------------------------------------------------------------*/

    /**
     * Slouží k vytváření n klíčů jako dodatečný filtr parametr pro API thins_mobile.
     * Je nutné se přispůsobit jejich požadavkům podle application/x-www-form-urlencoded
     *
     */
    private class KeyStore {

        public KeyStore(String key, List<String> values){
            this.key = key;
            this.values = values;
        }

        public String key;
        public List<String> values = new ArrayList<>();
    }


    /* TESTER of  API  ---------------------------------------------------------------------------------------------------------*/

    /**
     * Voláním této metody se odzkouší všechny metody
     */
    public void test_of_all_apis(){

        // S touto sim_cad_id mužeme hledat a zkoušet API
        // Máme tři ID - a po tobě chci aby si se všema vyzkoušel všechny operace a vytvořil všechny možné objekty co umožnuje dokumentace
        Long sim_card_id =  882360002156971L;
        Long sim_card_ids[] =  new Long[]{882360002156971L, 882360002156969L};


        // PS podle dokumentace lze používat TAGY - Ty mi ale používat nebudeme

        //--------- SIM LIST --------
        // 1. Get All SimCards
        //TM_Sim_List_list list = sim_list();
        //if(list == null) System.err.println("Posralo se to");

        //sim_status(sim_card_id);
        sim_active(sim_card_id);
        //sim_list();
        //2. Get One SimCards
        // sim_list(sim_card_id);

        //3. Get Array of IDS of SimCards
        // sim_list(sim_card_ids); TODO

        //--------- SIM STATUS --------
        // sim_status(sim_card_id)
        // sim_status(sim_card_ids);

        // Atd..
    }

    /* Object API  ---------------------------------------------------------------------------------------------------------*/


    /**
     * This method return status of all SimCards
     */
    public TM_Sim_List_list sim_list() {
        try {

            JsonNode response = post("/services/business-api/simList");
            System.out.println("response: sim_status no filter: " + response.toString());

            if(response.get("done").asBoolean()) {

                // Tady překonverutji JsonNode na třídu
                TM_Sim_List_list help = baseFormFactory.formFromJsonWithValidation( TM_Sim_List_list.class, response.get("sims"));

                System.out.println(" Vypisuji výsledek: " + Json.toJson(help));

                return help;

            }else {
                // shit
                logger.error("sim_list:: Invalid Response: {}", response);
                return null;
            }


        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }



    public void sim_status(Long id) {
        try {

            JsonNode response = post("/services/business-api/simStatus",  new KeyStore("msisdn", new ArrayList<String>() {{
                add(id.toString());
            }}));

            System.out.println("response: sim_status no filter: " + response.toString());

            if(response.get("done").asBoolean()) {

                // Tady překonverutji JsonNode na třídu
                TM_Sim_Status_list help = baseFormFactory.formFromJsonWithValidation( TM_Sim_Status_list.class, response.get("sims"));



                System.out.println(" Vypisuji výsledek: " + Json.toJson(help));

            }else {
                // shit
                logger.error("sim_list:: Invalid Response: {}", response);

            }


        }catch (Exception e){
            logger.internalServerError(e);

        }
    }

    public void sim_status(Long ids[]) {
        try {

            JsonNode response = post("/services/business-api/simStatus", new KeyStore("name", new ArrayList<String>() {{
                add(ids.toString());
            }}));


        }catch (Exception e){
            logger.internalServerError(e);
        }
    }


    public TM_Sim_Active sim_active(Long id){
        try {

            JsonNode response = post("/services/business-api/activateSim", new KeyStore("simBarcode",new ArrayList<String>() {{
                add(id.toString());
            }}));


            if(response.get("done").asBoolean()) {

                // Tady překonverutji JsonNode na třídu
                TM_Sim_Active help = baseFormFactory.formFromJsonWithValidation( TM_Sim_Active.class, response);

                System.out.println(" Vypisuji výsledek: " + Json.toJson(help));


                return help;
            }else {

                logger.error("sim_list:: Invalid Response: {} {}", response);
                return null;
            }


        }catch (Exception e){
            logger.internalServerError(e);
        return null;
        }
    }

    public void sim_active(Long ids[]){
        try {

            JsonNode response = post("/services/business-api/activateSim", new KeyStore("name", new ArrayList<String>() {{
                add(ids.toString());
            }}));


        }catch (Exception e){
            logger.internalServerError(e);
        }
    }




    public void sim_block(Long id) {
        try {
        JsonNode response = post("/services/business-api/unblockSim", new KeyStore("name", new ArrayList<String>() {{
            add(id.toString());
        }}));

        }catch (Exception e){
            logger.internalServerError(e);
        }
    }

    public void sim_block(Long ids[]) {
        try {
            JsonNode response = post("/services/business-api/unblockSim", new KeyStore("name", new ArrayList<String>() {{
                add(ids.toString());
            }}));

        }catch (Exception e){
            logger.internalServerError(e);
        }
    }






    public void sim_credit() {}

    /*
        Protože u všech metod používáme jen POST
        vytvořili jsme si tuto pomocnou metoru, která anonymně vykoná příkaz
     */
    private static JsonNode post(String url, KeyStore... optional_keys) throws Exception {

        Map<String, List<String>> map = new HashMap<>();
        map.put("username", new ArrayList<String>() {{
            add("tomas.zaruba@byzance.cz");
        }});
        map.put("token", new ArrayList<String>() {{
            add(api_key);
        }});


        if(optional_keys.length > 0) {
            for(KeyStore store : optional_keys) {
                map.put(store.key, store.values);
            }
        }


        logger.error("Things_Mobile:: GET: URL: " + thingsmobile_url + url);
        CompletionStage<WSResponse> responsePromise = Server.injector.getInstance(WSClient.class).url(thingsmobile_url + url)
                .setContentType("application/x-www-form-urlencoded")
                .setRequestTimeout(Duration.ofSeconds(5))
                .setAuth(Server.PrintNode_apiKey)
                .setQueryString(map)
                .post(map.toString());


        Integer status = responsePromise.toCompletableFuture().get().getStatus();
        // logger.error("Things_Mobile:: POST Result Status: {}", status);
        // logger.error("Things_Mobile:: POST Result Body: {}", responsePromise.toCompletableFuture().get().getBody());

        if(status != 200 && status != 201) {
            logger.error("Things_Mobile:: Result: " + status
                    + " There is invalid response. Something is wrong. Check all details");
            throw new Exception("Things_Mobile Error Post Method");
        }

        XmlMapper xmlMapper = new XmlMapper();
        JsonNode node = xmlMapper.readTree(responsePromise.toCompletableFuture().get().getBody().getBytes());

        ObjectMapper jsonMapper = new ObjectMapper();
        String json = jsonMapper.writeValueAsString(node);

        return Json.parse(json);
    }


}
