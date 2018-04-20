package utilities.gsm_services.things_mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import models.Model_Invoice;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.gsm_services.things_mobile.help_class.*;
import utilities.lablel_printer_service.Printer_Api;
import utilities.logger.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import java.lang.reflect.Array;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

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
    public static class KeyStore {

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
        String simBarcode = "12345678901234567890";
        String name = "fhsdkjh";

        // PS podle dokumentace lze používat TAGY - Ty mi ale používat nebudeme

        //--------- SIM LIST --------
        // 1. Get All SimCards
         //TM_Sim_List_list list = sim_list();
        // System.out.println(Json.toJson(list));


        //--------- SIM STATUS --------

       // TM_Sim_Status_list status = sim_status(sim_card_id);
       // System.out.println(Json.toJson(status));


        //--------- SIM ACTIVE --------
        //TM_Sim_Active active = sim_active(sim_card_id, simBarcode);

       // System.out.println(Json.toJson(active));
        //System.out.println("Je aktivovana? : " + active.done);

        //--------- SIM BLOCK ---------
        //sim_block(sim_card_id);
        //sim_block(sim_card_ids);
        //TM_Sim_Block blocked = sim_block(sim_card_id);
        //System.out.println(Json.toJson(blocked));

        //--------- SIM UNBLOCK --------
        //TM_Sim_Unblock unblocked = sim_unblock(sim_card_id);
        //System.out.println(Json.toJson(unblocked));

        //--------- SIM CREDIT --------

        //TM_Sim_Credit_list credit = sim_credit();
        //System.out.println(Json.toJson(credit));

        //---------UPDATE_SIM_NAME---------
        TM_Update_Sim_Name simName = update_sim_name(sim_card_id, name);
        System.out.println(Json.toJson(simName));

        //-------- UPDATE SIM TAG---------
        TM_Update_Sim_Tag simTag = update_sim_tag(sim_card_id, name);
        System.out.println(Json.toJson(simTag));

        //sim_active(sim_card_id);

        //sim_block(sim_card_id);
        //2. Get One SimCards
        // sim_list(sim_card_id);

        //3. Get Array of IDS of SimCards
        // sim_list(sim_card_ids); TODO



        // Atd..
    }

    /* Object API  ---------------------------------------------------------------------------------------------------------*/

    //SIM ACTIVE
    public TM_Sim_Active sim_active(Long id, String simBarcode) {
        try {


            KeyStore k1 =  new KeyStore("msisdn", new ArrayList<String>() {{
                add(id.toString());
            }});

            KeyStore k2 = new KeyStore("simBarcode", new ArrayList<String>() {{
                add(simBarcode);
            }});

            KeyStore[] stores = new KeyStore[2];
            stores[0] = k1;
            stores[1] = k2;

            Document response = post("/services/business-api/activateSim", stores);

            TM_Sim_Active node = new TM_Sim_Active();
            if (response.getElementsByTagName("done").item(0).getTextContent().equals("true")) {

                node.done = true;
                return node;

            } else {

                logger.error("sim_active:: Invalid Response: {}", response);
                node.done = false;
                node.errorCode = Integer.valueOf( response.getElementsByTagName("errorCode").item(0).getTextContent());
                node.errorMessage = response.getElementsByTagName("errorMessage").item(0).getTextContent();

                return node;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }

    }

    //SIM BLOCK
    public TM_Sim_Block sim_block(Long id) {
        try {

            Document response = post("/services/business-api/blockSim", new KeyStore("msisdn", new ArrayList<String>() {{
                add(id.toString());
            }}));

            TM_Sim_Block node = new TM_Sim_Block();
            if (response.getElementsByTagName("done").item(0).getTextContent().equals("true")) {

                node.done = true;
                return node;

            } else {

                logger.error("sim_block:: Invalid Response: {}", response);
                node.done = false;
                node.errorCode = Integer.valueOf( response.getElementsByTagName("errorCode").item(0).getTextContent());
                node.errorMessage = response.getElementsByTagName("errorMessage").item(0).getTextContent();

                return node;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    //SIM UNBLOCK
    public TM_Sim_Unblock sim_unblock(Long id) {
        try {

            Document response = post("/services/business-api/unblockSim", new KeyStore("msisdn", new ArrayList<String>() {{
                add(id.toString());
            }}));

            TM_Sim_Unblock node = new TM_Sim_Unblock();
            if (response.getElementsByTagName("done").item(0).getTextContent().equals("true")) {

                node.done = true;
                return node;

            } else {

                logger.error("sim_unblock:: Invalid Response: {}", response);
                node.done = false;
                node.errorCode = Integer.valueOf( response.getElementsByTagName("errorCode").item(0).getTextContent());
                node.errorMessage = response.getElementsByTagName("errorMessage").item(0).getTextContent();

                return node;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    //SIM LIST
    public TM_Sim_List_list sim_list() {
        try {

            Document response = post("/services/business-api/simList");
            response.getDocumentElement().normalize();

            TM_Sim_List_list list = new TM_Sim_List_list();

            if (response.getElementsByTagName("done").item(0).getTextContent().equals("true")) {

                NodeList nList = response.getElementsByTagName("sim");

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp);
                    TM_Sim_List node = new TM_Sim_List();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        node.activationDate              = eElement.getElementsByTagName("activationDate").item(0).getTextContent();
                        node.balance                     = Integer.valueOf(eElement.getElementsByTagName("balance").item(0).getTextContent());
                        node.blockSimAfterExpirationDate = Integer.valueOf(eElement.getElementsByTagName("blockSimAfterExpirationDate").item(0).getTextContent());
                        node.blockSimDaily               = Integer.valueOf(eElement.getElementsByTagName("blockSimDaily").item(0).getTextContent());
                        node.blockSimMonthly             = Integer.valueOf(eElement.getElementsByTagName("blockSimMonthly").item(0).getTextContent());
                        node.blockSimTotal               = Integer.valueOf(eElement.getElementsByTagName("blockSimTotal").item(0).getTextContent());
                        node.dailyTraffic                = Integer.valueOf(eElement.getElementsByTagName("dailyTraffic").item(0).getTextContent());
                        node.dailyTrafficThreshold       = Integer.valueOf(eElement.getElementsByTagName("totalTrafficThreshold").item(0).getTextContent());
                        node.expirationDate              = eElement.getElementsByTagName("expirationDate").item(0).getTextContent();
                        node.lastConnectionDate          = eElement.getElementsByTagName("lastConnectionDate").item(0).getTextContent();
                        node.monthlyTraffic              = Integer.valueOf(eElement.getElementsByTagName("monthlyTraffic").item(0).getTextContent());
                        node.monthlyTrafficThreshold     = Integer.valueOf(eElement.getElementsByTagName("monthlyTrafficThreshold").item(0).getTextContent());
                        node.msisdn                      = eElement.getElementsByTagName("msisdn").item(0).getTextContent();
                        node.name                        = eElement.getElementsByTagName("name").item(0).getTextContent();
                        node.plan                        = eElement.getElementsByTagName("plan").item(0).getTextContent();
                        node.tag                         = eElement.getElementsByTagName("tag").item(0).getTextContent();
                        node.totalTraffic                = Integer.valueOf(eElement.getElementsByTagName("totalTraffic").item(0).getTextContent());
                        node.totalTrafficThreshold       = Integer.valueOf(eElement.getElementsByTagName("totalTrafficThreshold").item(0).getTextContent());

                        NodeList cdrs_list =  ((Element) nNode).getElementsByTagName("cdr");

                        for (int j = 0; j < cdrs_list.getLength(); j++) {


                            Element eeElement = (Element) cdrs_list.item(0);
                            TM_Sim_List_cdr node_cdr = new TM_Sim_List_cdr();

                            node_cdr.cdrImsi        = Long.valueOf(eeElement.getElementsByTagName("cdrImsi").item(0).getTextContent());
                            node_cdr.cdrDateStart   = eeElement.getElementsByTagName("cdrDateStart").item(0).getTextContent();
                            node_cdr.cdrDateStop    = eeElement.getElementsByTagName("cdrDateStop").item(0).getTextContent();
                            node_cdr.cdrNetwork     = eeElement.getElementsByTagName("cdrNetwork").item(0).getTextContent();
                            node_cdr.cdrCountry     = eeElement.getElementsByTagName("cdrCountry").item(0).getTextContent();

                            node.cdrs.add(node_cdr);
                        }
                    }

                    list.sims.add(node);

                }

                return list;

            } else {

                logger.error("sim_list:: Invalid Response: {}", response);
                list.done = false;
                list.errorCode = Integer.valueOf( response.getElementsByTagName("errorCode").item(0).getTextContent());
                list.errorMessage = response.getElementsByTagName("errorMessage").item(0).getTextContent();

                System.out.println("false");
                return list;
            }


        }catch(Exception e){
            logger.internalServerError(e);

            return null;
        }
    }

    //SIM STATUS
    //moc CDR
    public TM_Sim_Status_list sim_status(Long id) {


        try {

            Document response = post("/services/business-api/simStatus", new KeyStore("msisdn",new ArrayList<String>() {{
                add(id.toString());
            }}));

            TM_Sim_Status_list list = new TM_Sim_Status_list();

            if (response.getElementsByTagName("done").item(0).getTextContent().equals("true")) {
                NodeList nList = response.getElementsByTagName("sim");

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp);
                    TM_Sim_Status node = new TM_Sim_Status();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;

                        node.activationDate =               eElement.getElementsByTagName("activationDate").item(0).getTextContent();
                        node.balance =                      Integer.valueOf(eElement.getElementsByTagName("balance").item(0).getTextContent());
                        node.blockSimAfterExpirationDate =  Integer.valueOf(eElement.getElementsByTagName("blockSimAfterExpirationDate").item(0).getTextContent());
                        node.blockSimDaily =                Integer.valueOf(eElement.getElementsByTagName("blockSimDaily").item(0).getTextContent());
                        node.blockSimMonthly =              Integer.valueOf(eElement.getElementsByTagName("blockSimMonthly").item(0).getTextContent());
                        node.blockSimTotal =                Integer.valueOf(eElement.getElementsByTagName("blockSimTotal").item(0).getTextContent());
                        node.dailyTraffic =                 Integer.valueOf(eElement.getElementsByTagName("dailyTraffic").item(0).getTextContent());
                        node.dailyTrafficThreshold =        Integer.valueOf(eElement.getElementsByTagName("totalTrafficThreshold").item(0).getTextContent());
                        node.expirationDate =               eElement.getElementsByTagName("expirationDate").item(0).getTextContent();
                        node.lastConnectionDate =           eElement.getElementsByTagName("lastConnectionDate").item(0).getTextContent();
                        node.monthlyTraffic =               Integer.valueOf(eElement.getElementsByTagName("monthlyTraffic").item(0).getTextContent());
                        node.monthlyTrafficThreshold =      Integer.valueOf(eElement.getElementsByTagName("monthlyTrafficThreshold").item(0).getTextContent());
                        node.msisdn =                       eElement.getElementsByTagName("msisdn").item(0).getTextContent();
                        node.name =                         eElement.getElementsByTagName("name").item(0).getTextContent();
                        node.plan =                         eElement.getElementsByTagName("plan").item(0).getTextContent();
                        node.tag =                          eElement.getElementsByTagName("tag").item(0).getTextContent();
                        node.totalTraffic =                 Integer.valueOf(eElement.getElementsByTagName("totalTraffic").item(0).getTextContent());
                        node.totalTrafficThreshold =        Integer.valueOf(eElement.getElementsByTagName("totalTrafficThreshold").item(0).getTextContent());

                        NodeList cdrs_list =  ((Element) nNode).getElementsByTagName("cdr");


                        for (int j = 0; j < cdrs_list.getLength(); j++) {


                            Element eeElement = (Element) cdrs_list.item(0);
                            TM_Sim_Status_cdr node_cdr = new TM_Sim_Status_cdr();

                            node_cdr.cdrImsi        = Long.valueOf(eeElement.getElementsByTagName("cdrImsi").item(0).getTextContent());
                            node_cdr.cdrDateStart   = eeElement.getElementsByTagName("cdrDateStart").item(0).getTextContent();
                            node_cdr.cdrDateStop    = eeElement.getElementsByTagName("cdrDateStop").item(0).getTextContent();
                            node_cdr.cdrNetwork     = eeElement.getElementsByTagName("cdrNetwork").item(0).getTextContent();
                            node_cdr.cdrCountry     = eeElement.getElementsByTagName("cdrCountry").item(0).getTextContent();

                            node.cdrs.add(node_cdr);
                            System.out.println("TOTO JE node_cdr -----------" +node_cdr);
                        }
                    }
                    list.sims.add(node);
                }

                return list;
            }else{

                logger.error("sim_status:: Invalid Response: {}", response);
                list.done = false;
                list.errorCode = Integer.valueOf( response.getElementsByTagName("errorCode").item(0).getTextContent());
                list.errorMessage = response.getElementsByTagName("errorMessage").item(0).getTextContent();

                return list;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    //CREDIT
    public TM_Sim_Credit_list sim_credit() {
        try {
            Document response = post("/services/business-api/credit");
            response.getDocumentElement().normalize();

            TM_Sim_Credit_list credit = new TM_Sim_Credit_list();

            if (response.getElementsByTagName("done").item(0).getTextContent().equals("true")) {

                credit.amount    = Integer.valueOf(response.getElementsByTagName("amount").item(0).getTextContent());
                credit.currency  = response.getElementsByTagName("currency").item(0).getTextContent();

                NodeList nList = response.getElementsByTagName("historyRow");

                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    TM_Sim_Credit node = new TM_Sim_Credit();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eeElement = (Element) nNode;

                        node.amount         = Integer.valueOf(eeElement.getElementsByTagName("amount").item(0).getTextContent());
                        node.dateLoad       = eeElement.getElementsByTagName("dateLoad").item(0).getTextContent();
                        node.msisdn         = eeElement.getElementsByTagName("msisdn").item(0).getTextContent();
                        node.opDescription  = eeElement.getElementsByTagName("opDescription").item(0).getTextContent();

                    }

                    credit.historyRow.add(node);

                }

                return credit;
            } else {

                logger.error("sim_credit:: Invalid Response: {}", response);
                credit.done = false;
                credit.errorCode = Integer.valueOf( response.getElementsByTagName("errorCode").item(0).getTextContent());
                credit.errorMessage = response.getElementsByTagName("errorMessage").item(0).getTextContent();
                return credit;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    //UPDATE SIM NAME
    public TM_Update_Sim_Name update_sim_name(Long id, String name) {
        try {


            KeyStore k1 =  new KeyStore("msisdn", new ArrayList<String>() {{
                add(id.toString());
            }});

            KeyStore k2 = new KeyStore("name", new ArrayList<String>() {{
                add(name);
            }});

            KeyStore[] stores = new KeyStore[2];
            stores[0] = k1;
            stores[1] = k2;

            Document response = post("/services/business-api/updateSimName", stores);

            TM_Update_Sim_Name node = new TM_Update_Sim_Name();
            if (response.getElementsByTagName("done").item(0).getTextContent().equals("true")) {

                node.done = true;

                return node;

            } else {

                logger.error("update_sim_name:: Invalid Response: {}", response);
                node.done = false;
                node.errorCode = Integer.valueOf( response.getElementsByTagName("errorCode").item(0).getTextContent());
                node.errorMessage = response.getElementsByTagName("errorMessage").item(0).getTextContent();

                return node;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }

    }

    //UPDATE SIM TAG
    public TM_Update_Sim_Tag update_sim_tag(Long id, String tag){
        try {
            KeyStore k1 =  new KeyStore("msisdn", new ArrayList<String>() {{
                add(id.toString());
            }});

            KeyStore k2 = new KeyStore("tag", new ArrayList<String>() {{
                add(tag);
            }});

            KeyStore[] stores = new KeyStore[2];
            stores[0] = k1;
            stores[1] = k2;

            Document response = post("/services/business-api/updateSimTag", stores);

            TM_Update_Sim_Tag node = new TM_Update_Sim_Tag();

            if (response.getElementsByTagName("done").item(0).getTextContent().equals("true")) {

                node.done = true;

                return node;

            } else {

                logger.error("update_sim_tag:: Invalid Response: {}", response);
                node.done = false;
                node.errorCode = Integer.valueOf( response.getElementsByTagName("errorCode").item(0).getTextContent());
                node.errorMessage = response.getElementsByTagName("errorMessage").item(0).getTextContent();

                return node;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }

    }


    /*
    //SIM CREDIT



    //UPDATE SIM NAME
    public TM_Update_Sim_Name update_sim_name(Long id) {
        try {
            JsonNode response = post("/services/business-api/UpdateSimName", new KeyStore("msisdn", new ArrayList<String>() {{
                add(id.toString());
            }}));
            if(response.get("done").asBoolean()) {

                // Tady překonverutji JsonNode na třídu
                TM_Update_Sim_Name help = baseFormFactory.formFromJsonWithValidation( TM_Update_Sim_Name.class, response);

                System.out.println("Vypisuji výsledek: " + Json.toJson(help));


                return help;
            }else {

                logger.error("sim_list:: Invalid Response: {}", response);
                return null;
            }

        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    //UPDATE SIM TAG
    public TM_Update_Sim_Tag update_sim_tag(Long id) {
        try {
            JsonNode response = post("/services/business-api/credit", new KeyStore("name", new ArrayList<String>() {{
                add(id.toString());
            }}));
            if(response.get("done").asBoolean()) {

                // Tady překonverutji JsonNode na třídu
                TM_Update_Sim_Tag help = baseFormFactory.formFromJsonWithValidation( TM_Update_Sim_Tag.class, response);

                System.out.println(" Vypisuji výsledek: " + Json.toJson(help));


                return help;
            }else {

                logger.error("sim_list:: Invalid Response: {}", response);
                return null;
            }

        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

    //SETUP SIM EXPIRATION DATE

    //SETUP SIM TRAFFIC THRESHOLD

    */

    /**
     Metoda, která vypisuje XML a přidává k němu inputy
    */
    private static Document post(String url, KeyStore... optional_keys) throws Exception {

        //vytvoření hashmapy která majo klíč String a jako hodnotu pole Stringů
        Map<String, List<String>> map = new HashMap<>();

        //do pole se vkládá klíč "username"
        map.put("username", new ArrayList<String>() {{
            add("tomas.zaruba@byzance.cz");
        }});

        //do pole se vkládá klíč "token"
        map.put("token", new ArrayList<String>() {{
            add(api_key);
        }});


        //podmínka která v případě že je optional klíčů více než 0, tak je všechny přidá do do HasMapy map
        if(optional_keys.length > 0) {
            for(KeyStore store : optional_keys) {
                map.put(store.key, store.values);
            }
        }


        logger.error("Things_Mobile:: GET: URL: " + thingsmobile_url + url);

        //ressponsivePromise volá třídu Server injector(ten teprv v průběhu motody najde potřebnou knihovnu) a vytváří instanci objektu WSClient ze kterého volá metodu url(vrac9 objekt WS Request reprezentující URL)
        CompletionStage<WSResponse> responsePromise = Server.injector.getInstance(WSClient.class).url(thingsmobile_url + url)
               //nastavuje typ obsahu
                .setContentType("application/x-www-form-urlencoded")
                //nastavuje jak dlouho se bude pokusit připojit než vyhodí chybu
                .setRequestTimeout(Duration.ofSeconds(5))
                .setQueryString(map)
                .post(map.toString());

    //vztvářím si objekt, který teprv bude existovat a poté na něj volám metodu getStatus
        Integer status = responsePromise.toCompletableFuture().get().getStatus();

        //pokud metoda neprobehne bez problemu vyhodí chybu
        if(status != 200 && status != 201) {
            logger.error("Things_Mobile:: Result: " + status
                    + " There is invalid response. Something is wrong. Check all details");
            throw new Exception("Things_Mobile Error Post Method");
        }


       return responsePromise.toCompletableFuture().get().asXml();

    }


}
