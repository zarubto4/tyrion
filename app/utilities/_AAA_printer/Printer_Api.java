package utilities._AAA_printer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.printnode.api.PrintJobJson;
import models.Model_Board;
import play.api.Play;
import play.data.Form;
import play.i18n.Lang;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities._AAA_printer.labels.Label_65_mm;
import utilities._AAA_printer.printNodeModels.*;
import utilities.logger.Class_Logger;
import web_socket.message_objects.homer_instance_with_tyrion.verification.WS_Message_WebView_token_verification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class Printer_Api {

    // Logger
    private static final Class_Logger terminal_logger = new Class_Logger(Printer_Api.class);

    private String apiKey = "6aa35f3ae351e4664417ac894d99dbd4c35a0b95";

    public Printer_Api() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// - Object API  --------------------------------------------------------------------------------------------------------------------

    public JsonNode printFile(Integer printId, int quantity, String title, ByteArrayOutputStream file){

        PrintTask printTask = new PrintTask();
        printTask.printerId = printId;
        printTask.title = title;
        printTask.content = new String(Base64.getEncoder().encode( file.toByteArray()));
        printTask.qty = quantity;
        printTask.source = "Tyrion Generated Print Task";

        return post("/printjobs", Json.toJson(printTask));
    }


    public List<Computer> get_computers(){

        JsonNode request = get("/computers");

        ObjectNode request_list = Json.newObject();
        request_list.set("computer_list", request);

        final Form<ComputerList> form = Form.form(ComputerList.class).bind(request_list);
        if (form.hasErrors()) {
            terminal_logger.internalServerError( new Exception("ComputerList: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString()));
        }

        return form.get().computer_list;
    }


    public static List<Printer> get_printers(){

        JsonNode request = get("/printers");

        ObjectNode request_list = Json.newObject();
        request_list.set("printer_list", request);

        final Form<PrinterList> form = Form.form(PrinterList.class).bind(request_list);
        if (form.hasErrors()) {
            terminal_logger.internalServerError( new Exception("PrinterList: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString()));
        }

        return form.get().printer_list;
    }

    public static Printer get_printer(Integer printer_id){

        JsonNode request = get("/printers/" + printer_id);

        ObjectNode request_list = Json.newObject();
        request_list.set("printer_list", request);

        final Form<PrinterList> form = Form.form(PrinterList.class).bind(request_list);
        if (form.hasErrors()) {
            terminal_logger.internalServerError( new Exception("PrinterList: Incoming Json from Homer server has not right Form: " + form.errorsAsJson(Lang.forCode("en-US")).toString()));
        }


        List<Printer> printers = form.get().printer_list;

        return printers.isEmpty() ? null : form.get().printer_list.get(0);
    }




// - REST API HELP METHOD --------------------------------------------------------------------------------------------------------------

    private static JsonNode put(String url, JsonNode node) {

        terminal_logger.debug("Printer_Api_put:: PUT: URL: " + Server.PrintNode_url + url + "  Json: " + node.toString());
        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.PrintNode_url + url)
                .setContentType("application/json")
                .setAuth(Server.PrintNode_apiKey)
                .setRequestTimeout(5000)
                .put(node);

        JsonNode response = responsePromise.get(5000).asJson();

        terminal_logger.debug("Printer_Api_put:: Result: " + response.toString());
        return response;

    }

    private static JsonNode post(String url, JsonNode node) {

        terminal_logger.debug("Printer_Api_put:: POST: URL: " + Server.PrintNode_url + url + "  Json: " + node.toString());
        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.PrintNode_url + url)
                .setContentType("application/json")
                .setAuth(Server.PrintNode_apiKey)
                .setRequestTimeout(5000)
                .post(node);

        JsonNode response = responsePromise.get(5000).asJson();

        terminal_logger.debug("Printer_Api_post:: Result: " + response.toString());
        return response;

    }

    private static JsonNode get(String url) {

        terminal_logger.debug("Printer_Api_put:: GET: URL: " + Server.PrintNode_url + url);
        F.Promise<WSResponse> responsePromise = Play.current().injector().instanceOf(WSClient.class).url(Server.PrintNode_url + url)
                .setContentType("application/json")
                .setRequestTimeout(5000)
                .setAuth(Server.PrintNode_apiKey)
                .get();

        JsonNode response = responsePromise.get(5000).asJson();

        terminal_logger.debug("Printer_Api_put:: Result: " + response.toString());
        return response;

    }
}
