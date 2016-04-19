package utilities.loggy;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import controllers.SecurityController;
import models.loggy.LoggyError;
import play.Configuration;
import play.Play;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import utilities.Server;
import utilities.response.GlobalResult;

import java.io.File;
import java.io.PrintWriter;
import java.net.NetworkInterface;
import java.util.*;

public class Loggy{

    @Inject static WSClient wsClient; // používat přes getWSClient()

    // Vlastní Loggy objekt definovaný konfigurací
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    static Configuration conf = Play.application().configuration();     // rychlý přístup do konfigurace
    static String token = "";       // token na youtrack
    static long tokenExpire = 0;    // kdy expiruje token na youtrack



    public static Result result_internalServerError(Exception exception, Http.Request request) {

        String id = UUID.randomUUID().toString();

        StringBuilder summaryBuilder = new StringBuilder();         // stavění nadpisu
        StringBuilder descriptionBuilder = new StringBuilder();     // stavění obsahu

        summaryBuilder.append("Internal Server Error - ");
        summaryBuilder.append(exception.getClass().getName()+" - ");
        summaryBuilder.append(request.method()+" ");
        summaryBuilder.append(request.path());

        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Exception type: " +exception.getClass().getName());
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Time: " + new Date().toString());
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Request Type: " + request.method());
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Request Path: " + request.path());
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Unique Identificator: " + id);
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Tyrion version: " + Server.server_version);
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Tyrion mode: " + Server.server_mode);
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Server MAC address: " + getMac());
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    User: " + (SecurityController.getPerson() != null ? SecurityController.getPerson().mail : "null"));
        descriptionBuilder.append("\n");

        descriptionBuilder.append("    Stack trace: \n");
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {    // formátování stack trace
            descriptionBuilder.append("        " + element);
            descriptionBuilder.append("\n");
        }
        descriptionBuilder.append("\n");    // random whitespace
        descriptionBuilder.append("\n");

        String summary = summaryBuilder.toString();
        String description = descriptionBuilder.toString();

        error(id, summary, description);
        return GlobalResult.internalServerError();
    }

    public static Result result_internalServerError(String problem, Http.Request request) {
        String id = UUID.randomUUID().toString();

        StringBuilder summaryBuilder = new StringBuilder();         // stavění nadpisu
        StringBuilder descriptionBuilder = new StringBuilder();     // stavění obsahu

        summaryBuilder.append("Internal Server Error - ");
        summaryBuilder.append(problem+" - ");
        summaryBuilder.append(request.method()+" ");
        summaryBuilder.append(request.path());

        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Exception type: " + problem );
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Time: " + new Date().toString());
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Request Type: " + request.method());
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Request Path: " + request.path());
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Unique Identificator: " + id);
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Tyrion version: " + Server.server_version);
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Tyrion mode: " + Server.server_mode);
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    Server MAC address: " + getMac());
        descriptionBuilder.append("\n");
        descriptionBuilder.append("    User: " + (SecurityController.getPerson() != null ? SecurityController.getPerson().mail : "null"));
        descriptionBuilder.append("\n");

        descriptionBuilder.append("    Stack trace: \n");
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {    // formátování stack trace
            descriptionBuilder.append("        " + element);
            descriptionBuilder.append("\n");
        }
        descriptionBuilder.append("\n");    // random whitespace
        descriptionBuilder.append("\n");

        String summary = summaryBuilder.toString();
        String description = descriptionBuilder.toString();

        error(id, summary, description);
        return GlobalResult.internalServerError();
    }

    public static void error(String summary, String description) {
        error(UUID.randomUUID().toString(), summary, description);
    }

    private static void error(String id, String summary, String description) {
        logger.error(summary+"\n"+description); // zapíšu do souboru
        LoggyError error = new LoggyError(id, summary, description); // zapíšu do databáze
        error.save();
    }

    // Vracím počet zaznamenaných bugů v souboru
    public static Integer number_of_reported_errors(){
        return LoggyError.find.findRowCount();
    }

    // Vymažu bug z databáze
    public static void remove_error(String id){
        LoggyError.find.byId(id).delete();
    }

    // Vymažu všechny bugy z databáze
    public static void remove_all_errors(){
        Ebean.delete(LoggyError.find.all());
    }

    // Vymažu všechny bugy ze souboru
    public static void clear_file(){
        File all = Play.application().getFile("logs/all.log");

        try {
            new PrintWriter(all).close(); // vymaže obsah souboru
        } catch (Exception e) {}
    }

    public static F.Promise<Result> upload_to_youtrack(String id) {
        if (System.currentTimeMillis() > tokenExpire-10000) { // pokud nemám platný token, získám ho a metodu spustím znovu
            return youtrack_login().flatMap((result) -> upload_to_youtrack(id));
        }
        LoggyError e = getError(id);
        if (e == null) {
            return F.Promise.promise(Results::badRequest);
        }
        // sestavím request na nahrání
        WSRequest request = getWSClient().url(conf.getString("Loggy.youtrackUrl") + "/youtrack/rest/issue");
        request.setQueryParameter("project", conf.getString("Loggy.youtrackProject"));
        request.setQueryParameter("summary", e.summary);
        request.setQueryParameter("description", e.description);
        request.setHeader("Authorization", "Bearer "+token);
        F.Promise<WSResponse> promise = request.put("");
        return promise.map(response -> youtrack_checkUploadResponse(response, e)); // zpracuje odpověď a zapíše url do erroru
    }

    public static List<LoggyError> getErrors(Integer a){
        return LoggyError.find.setMaxRows(a).findList();
    }

    public static List<LoggyError> getErrors(){
        return LoggyError.find.all();
    }

    public static LoggyError getError(String id) {
        return LoggyError.find.byId(id);
    }

    private static String getMac() {
        StringBuilder builder = new StringBuilder();
        try {
            byte[] mac = NetworkInterface
                    .getNetworkInterfaces()
                    .nextElement()
                    .getHardwareAddress();  // byty MAC adresy

            for (int i = 0; i < mac.length; i++) {  // formátování MAC na čitelný formát
                builder.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
        }
        catch (Exception e) {
            play.Logger.error("network problem", e);
        }
        return builder.toString();
    }

    private static F.Promise<Result> youtrack_login() {
        WSRequest request = getWSClient().url(conf.getString("Loggy.youtrackUrl") + "/hub/rest/oauth2/token");
        request.setContentType("application/x-www-form-urlencoded");
        request.setHeader("Authorization",
                "Basic "+ new String(Base64.getEncoder().encode(    // zakódování přihlašovacích údajů
                        (conf.getString("Loggy.youtrackId") + ":" + conf.getString("Loggy.youtrackSecret"))
                                .getBytes())));
        F.Promise<WSResponse> promise = request.post(               // odešlu request na token
                "grant_type=password"
                        +"&scope="+conf.getString("Loggy.youtrackScopeId")
                        +"&username="+conf.getString("Loggy.youtrackUsername")
                        +"&password="+conf.getString("Loggy.youtrackPassword")
        );

        return promise.map(Loggy::youtrack_checkLoginResponse); // zpracuje odpověď od youtracku
    }

    private static Result youtrack_checkLoginResponse(WSResponse response) {
        if (response.getStatus() == 200) {  // pokud úspěšné, uložím token a jeho expiraci
            JsonNode content = response.asJson();
            token = content.get("access_token").asText();
            tokenExpire = System.currentTimeMillis() + content.get("expires_in").asLong()*1000;
            return Results.ok("login successful");
        }

        return Results.status(response.getStatus(), response.getBody());
    }

    private static Result youtrack_checkUploadResponse(WSResponse response, LoggyError error) {
        if (response.getStatus() == 201) {
            error.setYoutrack_url(response.getHeader("Location").replace("/rest", "")); // uložím url z odpovědi
            error.save();
            logger.debug(error.youtrack_url+"---"+LoggyError.find.byId(error.id).youtrack_url);
            return Results.ok("upload successful");
        }

        return Results.status(response.getStatus(), response.getBody());
    }

    private static WSClient getWSClient() {
        if(wsClient == null) { // pokud je wsClient null, vytvorím ho
            wsClient = Play.application().injector().instanceOf(WSClient.class);
        }
        return wsClient;
    }

}


