package utilities.logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Model_ServerError;
import play.Configuration;
import play.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Result;
import play.mvc.Results;
import utilities.response.GlobalResult;

import java.util.Base64;

public class YouTrack {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(YouTrack.class);

    @Inject
    private static WSClient wsClient; // používat přes getWSClient()

    private static String token = "";       // token na youtrack
    private static long tokenExpire = 0;    // kdy expiruje token na youtrack

    public static String report(Model_ServerError error) {

        terminal_logger.debug("report - new issue");

        if (System.currentTimeMillis() > tokenExpire - 10000) { // pokud nemám platný token, získám ho a metodu spustím znovu
            terminal_logger.debug("report - need login");
            if (login().get(5000).status() != 200) {
                throw new RuntimeException("Login to YouTrack was unsuccessful.");
            }
        }

        terminal_logger.debug("report - reporting issue");

        ObjectNode issue = Json.newObject()
                .put("project", Configuration.root().getString("Logger.youtrackProject"))
                .put("summary", error.summary)
                .put("description", error.description + "\n\n" + error.prettyPrint());


        WSResponse response = getWSClient().url(Configuration.root().getString("Logger.youtrackUrl") + "/youtrack/rest/issue")
                .setHeader("Content-Type", "application/json")
                //.setHeader("Authorization", "Bearer " + token)
                .setHeader("Authorization", "Bearer " + Configuration.root().getString("Logger.youtrackApiKey"))
                .put(issue)
                .get(10000);

        if (response.getStatus() == 201) {
            terminal_logger.debug("report - successfully created");
            return response.getHeader("Location").replace("/rest", ""); // uložím url z odpovědi
        }

        terminal_logger.debug("report - unsuccessful, status: {}", response.getStatus());

        return null;
    }

    private static F.Promise<Result> login() {

        terminal_logger.debug("login - requesting token");

        F.Promise<WSResponse> promise = getWSClient().url(Configuration.root().getString("Logger.youtrackUrl") + "/hub/api/rest/oauth2/token")
                .setContentType("application/x-www-form-urlencoded")
                .setHeader("Authorization", "Basic " + new String(Base64.getEncoder().encode((Configuration.root().getString("Logger.youtrackId") + ":" + Configuration.root().getString("Logger.youtrackSecret")).getBytes())))
                .post("response_type=token&grant_type=password"
                        +"&scope="+Configuration.root().getString("Logger.youtrackScopeId")
                        +"&username="+Configuration.root().getString("Logger.youtrackUsername")
                        +"&password="+Configuration.root().getString("Logger.youtrackPassword"));

        return promise.map(YouTrack::checkLoginResponse); // zpracuje odpověď od youtracku
    }

    private static Result checkLoginResponse(WSResponse response) {

        if (response.getStatus() == 200) {  // pokud úspěšné, uložím token a jeho expiraci

            terminal_logger.debug("checkLoginResponse - success");

            JsonNode content = response.asJson();
            token = content.get("access_token").asText();
            tokenExpire = System.currentTimeMillis() + content.get("expires_in").asLong()*1000;
            return GlobalResult.result_ok("login successful");
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
