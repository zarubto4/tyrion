package utilities.independent_threads;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.project.b_program.servers.Model_HomerServer;
import play.data.Form;
import play.libs.Json;
import utilities.webSocket.WS_HomerServer;
import utilities.webSocket.messageObjects.WS_CheckHomerServerPermission;

import java.nio.channels.ClosedChannelException;
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
                logger.warn("WS_HomerServer:: security_token_confirm_procedure:: Trying to Confirm WebSocket");

                // Požádáme o token
                ObjectNode request = Json.newObject();
                request.put("messageType", "getVerificationToken");
                request.put("messageChannel", Model_HomerServer.CHANNEL);
                ObjectNode ask_for_token = server.super_write_with_confirmation(request, 1000 * 5, 0, 2);


                final Form<WS_CheckHomerServerPermission> form = Form.form(WS_CheckHomerServerPermission.class).bind(ask_for_token);
                if (form.hasErrors()) {
                    logger.error("WS_HomerServer:: Security_token_confirm_procedure: Error:: Some value missing:: " + form.errorsAsJson().toString());
                    // Ukončim ověřování - ale nechám websocket připojený
                    return;
                }

                // Vytovření objektu
                WS_CheckHomerServerPermission help = form.get();

                // Vyhledání DB reference
                Model_HomerServer check_server = Model_HomerServer.find.where().eq("hash_certificate", help.hashToken).findUnique();

                // Kontrola
                if (!check_server.unique_identificator.equals(server.server.unique_identificator)) {
                    logger.error("Security_WS_token_confirm_procedure:: Connected server has not permission");
                    ++number_of_tries;
                    sleep(1000 * 10  * number_of_tries);
                    continue;
                }

                // Potvrzení Homer serveru, že je vše v pořádku
                ObjectNode request_2 = Json.newObject();
                request_2.put("messageType", "verificationTokenApprove");
                request_2.put("messageChannel", Model_HomerServer.CHANNEL);
                ObjectNode approve_result = server.super_write_with_confirmation(request_2, 1000 * 5, 0, 2);

                // Změna FlagRegistru
                server.security_token_confirm = true;

                // Sesynchronizuj Configuraci serveru s tím co ví a co zná Tyrion
                server.synchronize_configuration();

                // GET state - a vyhodnocením v jakém stavu se cloud_blocko_server nachází a popřípadě
                // na něj nahraji nebo smažu nekonzistenntí clou dprogramy, které by na něm měly být
                server.server.check_after_connection(server);

                logger.debug("Security_WS_token_confirm_procedure:: Connection procedure done!");
                break;

            }catch(ClosedChannelException e){
                logger.warn("WS_HomerServer:: security_token_confirm_procedure :: ClosedChannelException");

                break;
            }catch(TimeoutException e){
                logger.error("WS_HomerServer:: security_token_confirm_procedure :: TimeoutException");
                break;
            }catch(Exception e){
                logger.error("WS_HomerServer:: security_token_confirm_procedure :: Error", e);
                break;
            }

        }

        server.procedure = null; // Smažu se - Garbarage collector mě odstraní
    }
}
