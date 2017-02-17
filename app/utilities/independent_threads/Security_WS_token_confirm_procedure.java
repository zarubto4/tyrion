package utilities.independent_threads;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.b_program.servers.Model_HomerServer;
import play.data.Form;
import play.i18n.Lang;
import utilities.web_socket.WS_HomerServer;
import utilities.web_socket.message_objects.homer_tyrion.WS_Approve_homer_server;
import utilities.web_socket.message_objects.homer_tyrion.WS_Check_homer_server_permission;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Security_WS_token_confirm_procedure extends Thread {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    WS_HomerServer server = null;

    // Umožněno kontrolovat COmpilator i Homer server
    public Security_WS_token_confirm_procedure(WS_HomerServer server){
        this.server = server;
    }

    @Override
    public void run() {

        int number_of_tries = 1;
        while (true) {

            try {
                logger.trace("Security_WS_token_confirm_procedure:: run:: Trying to Confirm WebSocket");

                ObjectNode ask_for_token = server.super_write_with_confirmation(new WS_Check_homer_server_permission().make_request(), 1000 * 5, 0, 2);

                final Form<WS_Check_homer_server_permission> form = Form.form(WS_Check_homer_server_permission.class).bind(ask_for_token);
                if (form.hasErrors()) {
                    logger.error("Security_WS_token_confirm_procedure:: run:: Error:: Some value missing:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());
                    sleep(1000 * 10  * ++number_of_tries);
                    continue;
                }

                // Vytovření objektu
                WS_Check_homer_server_permission help = form.get();

                // Vyhledání DB reference
                Model_HomerServer check_server = Model_HomerServer.find.where().eq("hash_certificate", help.hashToken).findUnique();

                // Kontrola
                if (!check_server.unique_identificator.equals(server.server.unique_identificator)) {
                    logger.warn("Security_WS_token_confirm_procedure:: run:: Connected server has not permission");
                    sleep(1000 * 10  * ++number_of_tries);
                    continue;
                }


                ObjectNode approve_result = server.super_write_with_confirmation(new WS_Approve_homer_server().make_request(), 1000 * 5, 0, 2);
                final Form<WS_Approve_homer_server> form_approve = Form.form(WS_Approve_homer_server.class).bind(ask_for_token);
                if (form_approve.hasErrors()) {logger.error("Security_WS_token_confirm_procedure:: run:: WS_Approve_homer_server: Error:: Some value missing:: " + form_approve.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                // Vytovření objektu
                WS_Approve_homer_server help_approve = form_approve.get();


                // TODO tady nic nedělám s tím jestli se to povedlo nebo ne???

                // Změna FlagRegistru
                server.security_token_confirm = true;

                // Sesynchronizuj Configuraci serveru s tím co ví a co zná Tyrion
                server.synchronize_configuration();

                // GET state - a vyhodnocením v jakém stavu se cloud_blocko_server nachází a popřípadě
                // na něj nahraji nebo smažu nekonzistenntí clou dprogramy, které by na něm měly být
                server.server.check_after_connection();

                logger.trace("Security_WS_token_confirm_procedure:: run:: Connection procedure done!");
                break;


            }catch(NullPointerException e){
                logger.error("Security_WS_token_confirm_procedure:: run:: NullPointerException");
                e.printStackTrace();

                try {
                    sleep(1000 * 10  * ++number_of_tries);
                } catch (InterruptedException e1) {}

            }catch(ClosedChannelException e){
                logger.warn("Security_WS_token_confirm_procedure:: run:: ClosedChannelException");
                break;
            }catch(ExecutionException e){
                logger.error("Security_WS_token_confirm_procedure:: run:: ExecutionException");
                break;
            }catch(TimeoutException e){
                logger.warn("Security_WS_token_confirm_procedure:: run:: TimeoutException");
                break;
            }catch(Exception e){
                logger.error("Security_WS_token_confirm_procedure:: run:: Error", e);
                break;
            }

        }

        server.procedure = null; // Smažu se - Garbarage collector mě odstraní
    }
}
