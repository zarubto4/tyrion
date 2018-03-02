package utilities.lablel_printer_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.lablel_printer_service.printNodeModels.*;
import utilities.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletionStage;
// TODO zkontrolovat jestli funguje korektně [TOM]
public class Printer_Api {

    // Logger
    private static final Logger logger = new Logger(Printer_Api.class);

    private String apiKey = "6aa35f3ae351e4664417ac894d99dbd4c35a0b95";


// - Object API  --------------------------------------------------------------------------------------------------------------------

    public JsonNode printFile(Integer printId, int quantity, String title, ByteArrayOutputStream file, PrinterOption option) {

        PrintTask printTask = new PrintTask();
        printTask.printerId = printId;
        printTask.title = title;
        printTask.content = new String(Base64.getEncoder().encode( file.toByteArray()));
        printTask.qty = quantity;
        printTask.source = "Tyrion Generated Print Task";
        printTask.options = option;

        return post("/printjobs", Json.toJson(printTask));
    }


    public List<Computer> get_computers() {

        JsonNode request = get("/computers");

        ObjectNode request_list = Json.newObject();
        request_list.set("computer_list", request);

        return Json.fromJson(request_list, ComputerList.class).computer_list;
    }


    public static List<Printer> get_printers() {

        JsonNode request = get("/printers");

        ObjectNode request_list = Json.newObject();
        request_list.set("printer_list", request);

        return Json.fromJson(request_list, PrinterList.class).printer_list;
    }

    public static Printer get_printer(Integer printer_id) {

        JsonNode request = get("/printers/" + printer_id);

        ObjectNode request_list = Json.newObject();
        request_list.set("printer_list", request);

        List<Printer> printers = Json.fromJson(request_list, PrinterList.class).printer_list;
        if (printers == null) return null;
        return printers.isEmpty() ? null : printers.get(0);
    }




// - REST API HELP METHOD --------------------------------------------------------------------------------------------------------------

    private static JsonNode put(String url, JsonNode node) {
        try {
            logger.debug("Printer_Api_put:: PUT: URL: " + Server.PrintNode_url + url + "  Json: " + node.toString());
            CompletionStage<WSResponse> responsePromise = Server.injector.getInstance(WSClient.class).url(Server.PrintNode_url + url)
                    .setContentType("application/json")
                    .setAuth(Server.PrintNode_apiKey)
                    .setRequestTimeout(Duration.ofSeconds(5))
                    .put(node);

            JsonNode response = responsePromise.toCompletableFuture().get().asJson();

            logger.debug("Printer_Api_put:: Result: " + response.toString());
            return response;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    private static JsonNode post(String url, JsonNode node) {
        try {
            logger.debug("Printer_Api_put:: POST: URL: " + Server.PrintNode_url + url + "  Json: " + node.toString());
            CompletionStage<WSResponse> responsePromise = Server.injector.getInstance(WSClient.class).url(Server.PrintNode_url + url)
                    .setContentType("application/json")
                    .setAuth(Server.PrintNode_apiKey)
                    .setRequestTimeout(Duration.ofSeconds(5))
                    .post(node);

            JsonNode response = responsePromise.toCompletableFuture().get().asJson();

            logger.debug("Printer_Api_post:: Result: " + response.toString());
            return response;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    private static JsonNode get(String url) {
        try {
            logger.debug("Printer_Api_put:: GET: URL: " + Server.PrintNode_url + url);
            CompletionStage<WSResponse> responsePromise = Server.injector.getInstance(WSClient.class).url(Server.PrintNode_url + url)
                    .setContentType("application/json")
                    .setRequestTimeout(Duration.ofSeconds(5))
                    .setAuth(Server.PrintNode_apiKey)
                    .get();

            JsonNode response = responsePromise.toCompletableFuture().get().asJson();

            logger.debug("Printer_Api_put:: Result: " + response.toString());
            return response;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }
}
