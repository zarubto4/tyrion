package utilities.gsm_services.things_mobile;

import models.Model_GSM;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.gsm_services.things_mobile.help_json_class.*;
import utilities.gsm_services.things_mobile.helpers.TMKeyStoreSender;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_GSM_Edit;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class Controller_Things_Mobile {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    // Logger
    private static final Logger logger = new Logger(Controller_Things_Mobile.class);

// * CONFIFG VALUES -------------------------------------------------------------------------------------------------------------*/

    private static String api_key = Server.configuration.getString("mobile_things.token");
    private static String email = Server.configuration.getString("mobile_things.username");
    private static final String things_mobile_url = "https://www.thingsmobile.com";

    public static final Double price_per_MB = 3.4;

 /* HELPER PRIVATE CLASS   ----------------------------------------------------------------------------------------------*/


/* Object API  ---------------------------------------------------------------------------------------------------------*/

    /**
     *  3.1 SIM ACTIVE
     *  Aktivace se provede automaticky vždy když se najde nová simkarata která není spárovaná se systémem.
     */

    //
    public static TM_Sim_Active sim_active(Long msisdn, String simBarcode) {
        try {

            TMKeyStoreSender sender = new TMKeyStoreSender();
            sender.addKey("msisdn", msisdn.toString());
            sender.addKey("simBarcode", simBarcode);
            sender.addKey("iccid", simBarcode);

            Document response = post("/services/business-api/activateSim", sender);

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

            TMKeyStoreSender sender = new TMKeyStoreSender();
            sender.addKey("msisdn", msi_number.toString());

            Document response = post("/services/business-api/blockSim", sender);

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

            TMKeyStoreSender sender = new TMKeyStoreSender();
            sender.addKey("msisdn", msi_number.toString());


            Document response = post("/services/business-api/unblockSim", sender);

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

            Document response = post("/services/business-api/simList", new TMKeyStoreSender());
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
                        node.dailyTraffic                = Long.valueOf(eElement.getElementsByTagName("dailyTraffic").item(0).getTextContent());
                        node.dailyTrafficThreshold       = Long.valueOf(eElement.getElementsByTagName("totalTrafficThreshold").item(0).getTextContent());
                        node.expirationDate              = eElement.getElementsByTagName("expirationDate").item(0).getTextContent();
                        node.lastConnectionDate          = eElement.getElementsByTagName("lastConnectionDate").item(0).getTextContent();
                        node.monthlyTraffic              = Long.valueOf(eElement.getElementsByTagName("monthlyTraffic").item(0).getTextContent());
                        node.monthlyTrafficThreshold     = Long.valueOf(eElement.getElementsByTagName("monthlyTrafficThreshold").item(0).getTextContent());
                        node.msisdn                      = Long.valueOf(eElement.getElementsByTagName("msisdn").item(0).getTextContent());

                        // TODO Things Mobile
                        node.iccid                      = eElement.getElementsByTagName("iccid").item(0).getTextContent();

                        node.name                        = eElement.getElementsByTagName("name").item(0).getTextContent();
                        node.status                      = eElement.getElementsByTagName("status").item(0).getTextContent();
                        node.plan                        = eElement.getElementsByTagName("plan").item(0).getTextContent();
                        node.tag                         = eElement.getElementsByTagName("tag").item(0).getTextContent();
                        node.totalTraffic                = Long.valueOf(eElement.getElementsByTagName("totalTraffic").item(0).getTextContent());
                        node.totalTrafficThreshold       = Long.valueOf(eElement.getElementsByTagName("totalTrafficThreshold").item(0).getTextContent());




                        NodeList cdrs_list =  ((Element) nNode).getElementsByTagName("cdr");

                        for (int cdr_pointer = 0; cdr_pointer < cdrs_list.getLength(); cdr_pointer++) {

                            Element eeElement = (Element) cdrs_list.item(cdr_pointer);
                            TM_Sim_Status_cdr node_cdr = new TM_Sim_Status_cdr();

                            node_cdr.cdrImsi        = Long.valueOf(eeElement.getElementsByTagName("cdrImsi").item(0).getTextContent());
                            node_cdr.cdrDateStart   = eeElement.getElementsByTagName("cdrDateStart").item(0).getTextContent();
                            node_cdr.cdrDateStop    = eeElement.getElementsByTagName("cdrDateStop").item(0).getTextContent();
                            node_cdr.cdrNetwork     = eeElement.getElementsByTagName("cdrNetwork").item(0).getTextContent();
                            node_cdr.cdrCountry     = eeElement.getElementsByTagName("cdrCountry").item(0).getTextContent();
                            node_cdr.cdrTraffic     = Float.valueOf(eeElement.getElementsByTagName("cdrTraffic").item(0).getTextContent()).longValue();
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

            if(Model_GSM.tm_status_cache.containsKey(msi_number)) {
                return Model_GSM.tm_status_cache.get(msi_number);
            }

            TMKeyStoreSender sender = new TMKeyStoreSender();
            sender.addKey("msisdn", msi_number.toString());

            Document response = post("/services/business-api/simStatus", sender);

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
                        node.dailyTraffic =                 Long.valueOf(eElement.getElementsByTagName("dailyTraffic").item(0).getTextContent());
                        node.dailyTrafficThreshold =        Long.valueOf(eElement.getElementsByTagName("totalTrafficThreshold").item(0).getTextContent());
                        node.expirationDate =               eElement.getElementsByTagName("expirationDate").item(0).getTextContent();
                        node.lastConnectionDate =           eElement.getElementsByTagName("lastConnectionDate").item(0).getTextContent();
                        node.monthlyTraffic =               Long.valueOf(eElement.getElementsByTagName("monthlyTraffic").item(0).getTextContent());
                        node.monthlyTrafficThreshold =      Long.valueOf(eElement.getElementsByTagName("monthlyTrafficThreshold").item(0).getTextContent());
                        node.msisdn =                       Long.valueOf(eElement.getElementsByTagName("msisdn").item(0).getTextContent());
                        node.name =                         eElement.getElementsByTagName("name").item(0).getTextContent();
                        node.plan =                         eElement.getElementsByTagName("plan").item(0).getTextContent();
                        node.tag =                          eElement.getElementsByTagName("tag").item(0).getTextContent();
                        node.status =                       eElement.getElementsByTagName("status").item(0).getTextContent();
                        node.totalTraffic =                 Long.valueOf(eElement.getElementsByTagName("totalTraffic").item(0).getTextContent());
                        node.totalTrafficThreshold =        Long.valueOf(eElement.getElementsByTagName("totalTrafficThreshold").item(0).getTextContent());
                        node.done =                         true;

                        NodeList cdrs_list =  ((Element) nNode).getElementsByTagName("cdr");

                        System.out.println("Budu louskat cdrs");
                        System.out.println("Co mám v CDRS: size: " + ((Element) nNode).getElementsByTagName("cdrs").getLength());
                        System.out.println("Co mám v CDRS: print: " + ((Element) nNode).getElementsByTagName("cdrs").toString());

                        for (int cdr_pointer = 0; cdr_pointer < cdrs_list.getLength(); cdr_pointer++) {

                            Element eeElement = (Element) cdrs_list.item(cdr_pointer);
                            TM_Sim_Status_cdr node_cdr = new TM_Sim_Status_cdr();

                            node_cdr.cdrImsi        = Long.valueOf(eeElement.getElementsByTagName("cdrImsi").item(0).getTextContent());
                            node_cdr.cdrDateStart   = eeElement.getElementsByTagName("cdrDateStart").item(0).getTextContent();
                            node_cdr.cdrDateStop    = eeElement.getElementsByTagName("cdrDateStop").item(0).getTextContent();
                            node_cdr.cdrNetwork     = eeElement.getElementsByTagName("cdrNetwork").item(0).getTextContent();
                            node_cdr.cdrCountry     = eeElement.getElementsByTagName("cdrCountry").item(0).getTextContent();
                            node_cdr.cdrTraffic     = Float.valueOf(  eeElement.getElementsByTagName("cdrTraffic").item(0).getTextContent()).longValue();
                            node.cdrs.add(node_cdr);
                            node.done = true;

                        }
                    }
                    list.sims.add(node);

                }
                list.done = true;
                return list.sims.get(0);
            } else {

                logger.error("sim_status:: Invalid Response: {}", response);
                list.done = false;
                list.sims.add(new TM_Sim_Status());

                list.sims.get(0).done = false;
                list.sims.get(0).errorCode = Integer.valueOf( response.getElementsByTagName("errorCode").item(0).getTextContent());
                list.sims.get(0).errorMessage = response.getElementsByTagName("errorMessage").item(0).getTextContent();


                Model_GSM.tm_status_cache.put(msi_number,  list.sims.get(0));
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


    // SIM UPDATE Thresholds
    public static TM_Sim_UpdateTresHold sim_set_tresHolds(Long msi_number, Swagger_GSM_Edit treshold) {
        try {


            TMKeyStoreSender sender = new TMKeyStoreSender();
            sender.addKey("msisdn", msi_number.toString());

            sender.addKey("dailyLimit", Long.toString(treshold.daily_traffic_threshold / 1024 / 1024));
            sender.addKey("blockSimDaily", treshold.block_sim_daily ? "1" : "0");

            sender.addKey("monthlyLimit",  Long.toString( treshold.monthly_traffic_threshold / 1024 / 1024));
            sender.addKey("blockSimMonthly", treshold.block_sim_monthly ? "1" : "0");

            sender.addKey("totalLimit",  Long.toString( treshold.total_traffic_threshold / 1024 / 1024));
            sender.addKey("blockSimTotal", treshold.block_sim_total ? "1" : "0");


            Document response = post("/services/business-api/setupSimTrafficThreeshold", sender);

            TM_Sim_UpdateTresHold node = new TM_Sim_UpdateTresHold();

            // Clean Cache
            Model_GSM.tm_status_cache.remove(msi_number);

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
            TM_Sim_UpdateTresHold result = new TM_Sim_UpdateTresHold();
            result.done = false;
            result.errorMessage = e.getMessage();
            return result;
        }
    }

    //CREDIT
    /**
     * Poměrně neužitečná funkce zjištující jaké celkové útraty byzance má
     * @return
     */
    public static TM_Sim_Credit_list sim_credit() {
        try {

            Document response = post("/services/business-api/credit", new TMKeyStoreSender());
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
                        node.msisdn         = Long.valueOf(eeElement.getElementsByTagName("msisdn").item(0).getTextContent());
                        node.opDescription  = eeElement.getElementsByTagName("opDescription").item(0).getTextContent();

                    }

                    credit.historyRow.add(node);
                    credit.done = true;

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

    //UPDATE SIM NAME - We use that for set name asi ID of Model_GSM.id
    public static TM_Update_Sim_Name update_sim_name(Long msi_number, String name) {
        try {

            TMKeyStoreSender sender = new TMKeyStoreSender();
            sender.addKey("msisdn", msi_number.toString());
            sender.addKey("name", name);

            Document response = post("/services/business-api/updateSimName", sender);

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
    public static TM_Update_Sim_Tag update_sim_tag(Long msisdn, String tag) {
        try {


            TMKeyStoreSender sender = new TMKeyStoreSender();
            sender.addKey("msisdn", msisdn.toString());
            sender.addKey("name", tag);

            Document response = post("/services/business-api/updateSimTag", sender);

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



    /**
     Metoda, která vypisuje XML a přidává k němu inputy
    */
    private static Document post(String url, TMKeyStoreSender sender) throws Exception {

        logger.debug("Request Controller_Things_Mobile: url {} key stores {}", url, sender.prettyPrint());

        //vytvoření hashmapy která majo klíč String a jako hodnotu pole Stringů
        sender.addKey("username", email);
        sender.addKey("token", api_key);

        logger.trace("Things_Mobile:: GET: URL: " + things_mobile_url + url);

        //ressponsivePromise volá třídu Server injector(ten teprv v průběhu motody najde potřebnou knihovnu) a vytváří instanci objektu WSClient ze kterého volá metodu url(vrac9 objekt WS Request reprezentující URL)
        CompletionStage<WSResponse> responsePromise = Server.injector.getInstance(WSClient.class).url(things_mobile_url + url)
               //nastavuje typ obsahu
                .setContentType("application/x-www-form-urlencoded")
                //nastavuje jak dlouho se bude pokusit připojit než vyhodí chybu
                .setRequestTimeout(Duration.ofSeconds(5))
                .setQueryString(sender.getHash())
                .post(sender.getHash().toString());

        //vztvářím si objekt, který teprv bude existovat a poté na něj volám metodu getStatus
        int status = responsePromise.toCompletableFuture().get().getStatus();

        //pokud metoda neprobehne bez problemu vyhodí chybu
        if(status != 200 && status != 201) {
            logger.error("Things_Mobile:: Result: " + status
                    + " There is invalid response. Something is wrong. Check all details");
            throw new Exception("Things_Mobile Error Post Method");
        }


       return responsePromise.toCompletableFuture().get().asXml();

    }


}
