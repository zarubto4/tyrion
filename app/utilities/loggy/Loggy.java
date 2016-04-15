package utilities.loggy;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import controllers.SecurityController;
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

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Loggy{

    @Inject static WSClient ws;

    // Vlastní Loggy objekt definovaný konfigurací
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    static Configuration conf = Play.application().configuration();     // rychlý přístup do konfigurace
    static String token = "";       // token na youtrack
    static long tokenExpire = 0;    // kdy expiruje token na youtrack



    public static Result result_internalServerError(Exception exception, Http.Request request) {
        StringBuilder builder = new StringBuilder();

        builder.append("Internal Server Error");
        builder.append("\n");
        builder.append("    Time: "+ new Date().toString());
        builder.append("\n");
        builder.append("    Request Type: " + request.method());
        builder.append("\n");
        builder.append("    Request Path: " + request.path());
        builder.append("\n");
        builder.append("    Unique Identificator: "+ UUID.randomUUID().toString());
        builder.append("\n");
        builder.append("    Tyrion version: "+ Server.server_version);
        builder.append("\n");
        builder.append("    Tyrion mode: "+ Server.server_mode);
        builder.append("\n");
        builder.append("    Server MAC address: "+ getMac());
        builder.append("\n");
        builder.append("    User: "+ (SecurityController.getPerson()!= null?SecurityController.getPerson().mail:"null"));
        builder.append("\n");

        builder.append("    Stack trace: \n");
        for (StackTraceElement element : exception.getStackTrace()) {
            builder.append("        " + element);
            builder.append("\n");
        }

        builder.append("\n");
        builder.append("\n");

        logger.error(builder.toString());
        return GlobalResult.internalServerError();

    }

    public static Result result_internalServerError(String problem, Http.Request request) {
        StringBuilder summaryBuilder = new StringBuilder();
        StringBuilder descriptionBuilder = new StringBuilder();
        summaryBuilder.append("Internal Server Error - ");
        fullBuilder.append("    Time: " + new Date().toString());
        fullBuilder.append("\n");
        fullBuilder.append("    Individual description: " + problem);
        fullBuilder.append("\n");
        fullBuilder.append("    Request Type: " + request.method());
        fullBuilder.append("\n");
        fullBuilder.append("    Request Path: " + request.path());
        fullBuilder.append("\n");
        fullBuilder.append("    Unique Identificator: " + UUID.randomUUID().toString());
        fullBuilder.append("\n");
        fullBuilder.append("    Tyrion version: " + Server.server_version);
        fullBuilder.append("\n");
        fullBuilder.append("    Tyrion mode: " + Server.server_mode);
        fullBuilder.append("\n");
        fullBuilder.append("    Server MAC address: " + getMac());
        fullBuilder.append("\n");
        fullBuilder.append("    User: " + (SecurityController.getPerson() != null ? SecurityController.getPerson().mail : "null"));
        fullBuilder.append("\n");

        fullBuilder.append("    Stack trace: \n");
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            fullBuilder.append("        " + element);
            fullBuilder.append("\n");
        }

        fullBuilder.append("\n");
        fullBuilder.append("\n");

        logger.error(fullBuilder.toString());
        return GlobalResult.internalServerError();
    }

    // Vracím počet zaznamenaných bugů v souboru
    public static Integer number_of_reported_bugs(){
        return 25; // TODO - dodělat načítání počtu chyb
    }

    // Vymažu bug z databáze
    public static void remove_bug(String id){
        // TODO - smazat bug z databáze
    }

    // Vymažu všechny bugy z databáze
    public static void remove_all_bugs(){
        // TODO - vyprázdnit databázi
    }

    // Vymažu všechny bugy ze souboru
    public static void clear_file(){
        // TODO - vyprázdnit soubor
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
        WSRequest request = ws.url(conf.getString("Loggy.youtrackUrl") + "/youtrack/rest/issue");
        request.setQueryParameter("project", conf.getString("Loggy.youtrackProject"));
        request.setQueryParameter("summary", e.summary;
        request.setQueryParameter("description", e.description);
        request.setHeader("Authorization", "Bearer "+token);
        F.Promise<WSResponse> promise = request.put("");
        return promise.map(response -> youtrack_checkUploadResponse(response, e)); // zpracuje odpověď a zapíše url do erroru
    }

    // Vracím v poli všechny chyby ze souboru (kde zaznamenávám chyby
    public static List<LoggyError> getErrors(Integer a){
        // TODO - dodělat načítání a separování chyb v souboru
        return new ArrayList<>();
    }

    public static LoggyError getError(String id) {
        return null;
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
        WSRequest request = ws.url(conf.getString("Loggy.youtrackUrl") + "/hub/rest/oauth2/token");
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
            error.youtrack_url = response.getHeader("Location").replace("/rest", ""); // uložím url z odpovědi
            return Results.ok("upload successful");
        }

        return Results.status(response.getStatus(), response.getBody());
    }

}


