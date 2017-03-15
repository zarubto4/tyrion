package utilities.independent_threads;

import models.compiler.Model_Board;
import models.project.b_program.instnace.Model_HomerInstance;
import models.project.b_program.servers.Model_HomerServer;
import utilities.enums.Homer_Instance_Type;
import utilities.web_socket.WS_HomerServer;
import utilities.web_socket.message_objects.homer_instance.WS_Get_summary_information;
import utilities.web_socket.message_objects.homer_instance.WS_Yoda_connected;

public class Check_Update_for_hw_on_homer extends Thread {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    WS_HomerServer ws_homerServer = null;
    Model_HomerServer model_server = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Check_Update_for_hw_on_homer(WS_HomerServer ws_homerServer, Model_HomerServer model_server){
        this.ws_homerServer = ws_homerServer;
        this.model_server = model_server;
    }


    @Override
    public void run(){

      logger.debug("Check_Update_for_hw_on_homer:: Independent Thread under server:: " + model_server.unique_identificator + " started");


      /**
       * findEachWhile zařídí, aby se do Tyriona nenahrál celý seznam ale po dávkách.
       * Tak aby nebyla zatížena operační pamět Tyriona.
       * V tomto případě kdy se server připojí po ztrátě spojení je nutné dkladně kontrolovat stav HW - předpokládá se
       * že se tak děje vždy po restartu nebo rekonfiguraci serveru v noci - proto nevadí, že je databáze tak vytížená!
       *
       * findEachWhile vyžaduje nakonci povolení k pokračování - zde je zatím natvrdo return true - ale du se dopsat
       * podmínka, třeba do budoucna kdy budeme mít časová razítka změn a budeme vědět dopředu co se asi tak mohlo změnit
       * a nemuseli tak louskat všechny instance naráz.
       */

        try {
            sleep(1000 * 30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Musím najít klasické instnace s Blockem a také virtuální instance
        Model_HomerInstance.find
                .where().eq("cloud_homer_server.unique_identificator", model_server.unique_identificator)
                        .disjunction()
                            .conjunction()
                                .eq("instance_type", Homer_Instance_Type.INDIVIDUAL)
                                .isNotNull("actual_instance")
                            .endJunction()
                            .conjunction()
                                .eq("instance_type", Homer_Instance_Type.VIRTUAL)
                                .isNotNull("boards_in_virtual_instance")
                             .endJunction()
                            .endJunction()
                .order().asc("blocko_instance_name")
                .findEachWhile( (Model_HomerInstance instance) -> {

                    // Zajímá mě stav HW

                    try {

                        logger.debug("Check_Update_for_hw_on_homer:: Run:: Instance:: " + instance.blocko_instance_name);

                        WS_Get_summary_information summary_information = instance.get_summary_information();

                        // Pokud není success - zkontroluji stav serveru a přeruším update proceduru
                        if(!summary_information.status.equals("success")){
                            if(!model_server.server_is_online()) {
                                logger.warn("Check_Update_for_hw_on_homer:: Run:: Server is probably offline");
                                return false;
                            }
                        }

                        // Ten porovnám se stavem - který se tam aktuálně očekává
                        Check_Update_for_hw_on_homer.check_Update(ws_homerServer, summary_information);

                    }catch (Exception e){

                    }
                    return true;
                });
    }


    public static void check_Update(WS_HomerServer homer_server, WS_Get_summary_information summary_information){

        logger.debug("Check_Update_for_hw_on_homer:: check_Update:: ");

        for(WS_Yoda_connected yoda_connected : summary_information.masterDeviceList){

            if(!yoda_connected.online_status) {
                logger.debug("Check_Update_for_hw_on_homer:: Device is offline check is not possible!!!");
                continue;
            }

            Model_Board yoda = Model_Board.find.byId(yoda_connected.deviceId);
            if(yoda == null){
                logger.error("Check_Update_for_hw_on_homer:: unknow Device!!! ");
            }

            System.out.print("Zasílám ke kontrole Yoda Device State:: ");
            Model_Board.hardware_firmware_state_check(homer_server, yoda, yoda_connected);

        }

    }

}
