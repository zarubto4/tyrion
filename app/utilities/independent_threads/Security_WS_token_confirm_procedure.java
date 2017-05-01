package utilities.independent_threads;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import play.data.Form;
import play.i18n.Lang;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homerServer_with_tyrion.WS_Message_Approve_homer_server;
import web_socket.message_objects.homerServer_with_tyrion.WS_Message_Check_homer_server_permission;
import web_socket.services.WS_HomerServer;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Security_WS_token_confirm_procedure extends Thread {
    
/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Security_WS_token_confirm_procedure.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

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
                terminal_logger.debug("run:: Trying to Confirm WebSocket");

                ObjectNode ask_for_token = server.super_write_with_confirmation(new WS_Message_Check_homer_server_permission().make_request(), 1000 * 5, 0, 2);

                final Form<WS_Message_Check_homer_server_permission> form = Form.form(WS_Message_Check_homer_server_permission.class).bind(ask_for_token);
                if (form.hasErrors()) {
                    terminal_logger.error("run:: Error:: Some value missing:: " + form.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());
                    sleep(1000 * 10  * ++number_of_tries);
                    server.close();
                    break;
                }

                // Vytovření objektu
                WS_Message_Check_homer_server_permission help = form.get();

                terminal_logger.debug("run:: Trying to Confirm WebSocket:: Result from Server:: " + ask_for_token.toString());

                // Vyhledání DB reference
                Model_HomerServer check_server = Model_HomerServer.find.where().eq("hash_certificate", help.hashToken).findUnique();

                // Kontrola
                if (!check_server.unique_identificator.equals( server.identifikator )) {
                    terminal_logger.warn("run:: Connected server has not permission");
                    sleep(1000 * 10  * ++number_of_tries);
                    continue;
                }


                ObjectNode approve_result = server.super_write_with_confirmation(new WS_Message_Approve_homer_server().make_request(), 1000 * 5, 0, 2);
                final Form<WS_Message_Approve_homer_server> form_approve = Form.form(WS_Message_Approve_homer_server.class).bind(ask_for_token);
                if (form_approve.hasErrors()) {terminal_logger.error("run:: WS_Approve_homer_server: Error:: Some value missing:: " + form_approve.errorsAsJson(new Lang( new play.api.i18n.Lang("en", "US"))).toString());return;}

                // Vytovření objektu
                WS_Message_Approve_homer_server help_approve = form_approve.get();


                // TODO tady nic nedělám s tím jestli se to povedlo nebo ne???

                // Změna FlagRegistru
                server.security_token_confirm = true;
                check_server.make_log_connect();

                // Sesynchronizuj Configuraci serveru s tím co ví a co zná Tyrion
                server.synchronize_configuration();

                // GET state - a vyhodnocením v jakém stavu se cloud_blocko_server nachází a popřípadě
                // na něj nahraji nebo smažu nekonzistenntí clou dprogramy, které by na něm měly být
                Model_HomerServer.get_model(server.identifikator).check_after_connection();

                terminal_logger.trace("run:: Connection procedure done!");
                break;


            }catch(NullPointerException e){
                terminal_logger.internalServerError(e);
                try {
                    sleep(1000 * 10  * ++number_of_tries);
                } catch (InterruptedException e1) {}

            }catch(ClosedChannelException e){
                terminal_logger.warn("run:: ClosedChannelException");
                break;
            }catch(ExecutionException e){
                terminal_logger.warn("run:: ExecutionException");
                break;
            }catch(TimeoutException e){
                terminal_logger.warn("run:: TimeoutException");
                break;
            }catch(Exception e){
                terminal_logger.internalServerError(e);
                break;
            }

        }

        server.procedure = null; // Smažu se - Garbarage collector mě odstraní
    }
}
