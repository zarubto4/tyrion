package utilities.independent_threads;

import models.project.b_program.instnace.Model_HomerInstance;
import models.project.b_program.servers.Model_HomerServer;
import utilities.web_socket.WS_HomerServer;

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

        // Musím najít klasické instnace s Blockem a také virtuální instance
        Model_HomerInstance.find
                .where().eq("cloud_homer_server.unique_identificator", model_server.unique_identificator)
                .order().asc("blocko_instance_name")
                .findEachWhile( (Model_HomerInstance instance) -> {

                    // Zajímá mě stav HW

                    instance.get_summary_information();


                    // Ten porovnám se stavem - který se tam aktuálně očekává

                    // Spustím Update proceduru nebo pokračuji dál
                    return true;
                });
    }



}
