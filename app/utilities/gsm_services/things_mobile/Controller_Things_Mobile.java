package utilities.gsm_services.things_mobile;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.gsm_services.things_mobile.help_class.*;
import utilities.logger.Logger;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

public class Controller_Things_Mobile {

    @Inject public static _BaseFormFactory baseFormFactory;
    @Inject public static Config configuration; // Its Required to set this in Server.class Component

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    // Logger
    private static final Logger logger = new Logger(Controller_Things_Mobile.class);



// * CONFIFG VALUES -------------------------------------------------------------------------------------------------------------*/

    private static String api_key = null;
    private static String email = null;
    private static final String things_mobile_url = "https://www.thingsmobile.com";

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

/* Object API  ---------------------------------------------------------------------------------------------------------*/

    //SIM ACTIVE
    public static TM_Sim_Active sim_active(String msisdn, String simBarcode) {
        try {


            KeyStore k1 =  new KeyStore("msisdn", new ArrayList<String>() {{
                add(msisdn);
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
            TM_Sim_Active result = new TM_Sim_Active();
            result.done = false;
            result.errorMessage = e.getMessage();
            return result;
        }

    }

    //SIM BLOCK
    public static TM_Sim_Block sim_block(Long msi_number) {
        try {

            Document response = post("/services/business-api/blockSim", new KeyStore("msisdn", new ArrayList<String>() {{
                add(msi_number.toString());
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
            TM_Sim_Block result = new TM_Sim_Block();
            result.done = false;
            result.errorMessage = e.getMessage();
            return result;
        }
    }

    // SIM UNBLOCK
    public static TM_Sim_Unblock sim_unblock(Long msi_number) {
        try {

            Document response = post("/services/business-api/unblockSim", new KeyStore("msisdn", new ArrayList<String>() {{
                add(msi_number.toString());
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
            TM_Sim_Unblock result = new TM_Sim_Unblock();
            result.done = false;
            result.errorMessage = e.getMessage();
            return result;
        }
    }

    // SIM LIST
    public static TM_Sim_List_list sim_list() {
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
                        node.msisdn                      = Long.valueOf(eElement.getElementsByTagName("msisdn").item(0).getTextContent());
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
            TM_Sim_List_list result = new TM_Sim_List_list();
            result.done = false;
            result.errorMessage = e.getMessage();
            return result;
        }
    }

    //SIM STATUS
    public static TM_Sim_Status sim_status(Long msi_number) {
        try {

            Document response = post("/services/business-api/simStatus", new KeyStore("msisdn",new ArrayList<String>() {{
                add(msi_number.toString());
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
                            node_cdr.cdrTraffic     = Float.valueOf(eeElement.getElementsByTagName("cdrTraffic").item(0).getTextContent());

                            node.cdrs.add(node_cdr);

                        }
                    }
                    list.sims.add(node);
                }

                return list.sims.get(0);
            } else {

                logger.error("sim_status:: Invalid Response: {}", response);
                list.sims.add(new TM_Sim_Status());

                list.sims.get(0).done = false;
                list.sims.get(0).errorCode = Integer.valueOf( response.getElementsByTagName("errorCode").item(0).getTextContent());
                list.sims.get(0).errorMessage = response.getElementsByTagName("errorMessage").item(0).getTextContent();

                return list.sims.get(0);
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            TM_Sim_Status result = new TM_Sim_Status();
            result.done = false;
            result.errorMessage = e.getMessage();
            return result;
        }
    }

    //CREDIT
    public static TM_Sim_Credit_list sim_credit() {
        try {
            Document response = post("/services/business-api/credit");
            response.getDocumentElement().normalize();

            TM_Sim_Credit_list credit = new TM_Sim_Credit_list();

            if (response.getElementsByTagName("done").item(0).getTextContent().equals("true")) {

                credit.amount    = Double.valueOf(response.getElementsByTagName("amount").item(0).getTextContent());
                credit.currency  = response.getElementsByTagName("currency").item(0).getTextContent();

                NodeList nList = response.getElementsByTagName("historyRow");

                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    TM_Sim_Credit node = new TM_Sim_Credit();

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eeElement = (Element) nNode;

                        node.amount         = Double.valueOf(eeElement.getElementsByTagName("amount").item(0).getTextContent());
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
            TM_Sim_Credit_list list = new TM_Sim_Credit_list();
            list.done = false;
            list.errorMessage = e.getMessage();
            return list;
        }
    }

    //UPDATE SIM NAME
    public static TM_Update_Sim_Name update_sim_name(Long id, String name) {
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
            TM_Update_Sim_Name result = new TM_Update_Sim_Name();
            result.done = false;
            result.errorMessage = e.getMessage();
            return result;
        }

    }

    //UPDATE SIM TAG
    public static TM_Update_Sim_Tag update_sim_tag(Long id, String tag){
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
            TM_Update_Sim_Tag result = new TM_Update_Sim_Tag();
            result.done = false;
            result.errorMessage = e.getMessage();
            return result;
        }

    }

    //SIM SET_TRASHHOLD
    public static TM_Set_Trashhold set_trashhold(Long id, Long daily_traffic_threshold,   boolean daily_traffic_threshold_exceeded_limit,
                                                 Long monthly_traffic_threshold, boolean monthly_traffic_threshold_exceeded_limit,
                                                 Long total_traffic_threshold,   boolean total_traffic_threshold_exceeded_limit){
        try {
            KeyStore k1 =  new KeyStore("msisdn", new ArrayList<String>() {{
                add(id.toString());
            }});

            KeyStore k2 = new KeyStore("daily_traffic_threshold", new ArrayList<String>() {{
                add(daily_traffic_threshold.toString());
            }});


            KeyStore k3 = new KeyStore("monthly_traffic_threshold", new ArrayList<String>() {{
                add(monthly_traffic_threshold.toString());
            }});


            KeyStore k4 = new KeyStore("daily_traffic_threshold", new ArrayList<String>() {{
                add(total_traffic_threshold.toString());
            }});


            KeyStore k5 = new KeyStore("daily_traffic_threshold_exceeded_limit", new ArrayList<String>() {{
                add(daily_traffic_threshold_exceeded_limit ? "1" : "0");
            }});


            KeyStore k6 = new KeyStore("monthly_traffic_threshold_exceeded_limit", new ArrayList<String>() {{
                add(monthly_traffic_threshold_exceeded_limit ? "1" : "0");
            }});


            KeyStore k7 = new KeyStore("total_traffic_threshold_exceeded_limit", new ArrayList<String>() {{
                add(total_traffic_threshold_exceeded_limit ? "1" : "0");
            }});

            KeyStore[] stores = new KeyStore[7];
            stores[0] = k1;
            stores[1] = k2;
            stores[2] = k3;
            stores[3] = k4;
            stores[4] = k5;
            stores[5] = k6;
            stores[6] = k7;

            Document response = post("/services/business-api/setupSimTrafficThreeshold", stores);

            TM_Set_Trashhold node = new TM_Set_Trashhold();

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
            TM_Set_Trashhold result = new TM_Set_Trashhold();
            result.done = false;
            result.errorMessage = e.getMessage();
            return result;
        }

    }


    /**
     Metoda, která vypisuje XML a přidává k němu inputy
    */
    private static Document post(String url, KeyStore... optional_keys) throws Exception {

        System.out.println("Request Controller_Things_Mobile: url " + url + " key stores" + optional_keys);
        if(api_key == null) {
            api_key = configuration.getString("mobile_things.token");
            email = configuration.getString("mobile_things.username");
        }

        //vytvoření hashmapy která majo klíč String a jako hodnotu pole Stringů
        Map<String, List<String>> map = new HashMap<>();

        //do pole se vkládá klíč "username"
        map.put("username", new ArrayList<String>() {{
            add(email);
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


        logger.trace("Things_Mobile:: GET: URL: " + things_mobile_url + url);

        //ressponsivePromise volá třídu Server injector(ten teprv v průběhu motody najde potřebnou knihovnu) a vytváří instanci objektu WSClient ze kterého volá metodu url(vrac9 objekt WS Request reprezentující URL)
        CompletionStage<WSResponse> responsePromise = Server.injector.getInstance(WSClient.class).url(things_mobile_url + url)
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
