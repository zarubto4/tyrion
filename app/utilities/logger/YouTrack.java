package utilities.logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import models.Model_ServerError;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletionStage;

// TODO make it work [LEXA]
public class YouTrack {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(YouTrack.class);

    private WSClient ws;
    private Config config;

    @Inject
    public YouTrack(WSClient ws, Config config) {
        this.ws = ws;
        this.config = config;
    }

    private String token = "";       // token na youtrack
    private long tokenExpire = 0;    // kdy expiruje token na youtrack

    public String report(Model_ServerError error) {
        try {
            logger.debug("report - new issue");

            if (System.currentTimeMillis() > tokenExpire - 10000) { // pokud nemám platný token, získám ho a metodu spustím znovu
                logger.debug("report - need login");
                if (login()) {
                    throw new RuntimeException("Login to YouTrack was unsuccessful.");
                }
            }

            logger.debug("report - reporting issue");

            ObjectNode issue = Json.newObject()
                    .put("project", config.getString("logger.youtrackProject"))
                    .put("summary", error.name)
                    .put("description", error.description + "\n\n" + error.prettyPrint());


            logger.debug("report - request body: {}", Json.toJson(issue));

            CompletionStage<WSResponse> promise = ws.url(config.getString("logger.youtrackUrl") + "/youtrack/rest/issue")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + config.getString("logger.youtrackApiKey"))
                    .setRequestTimeout(Duration.ofSeconds(10))
                    .put(issue);

            WSResponse response = promise.toCompletableFuture().get();

            if (response.getStatus() == 201) {
                logger.debug("report - successfully created");
                return response.getSingleHeader("Location").get().replace("/rest", ""); // uložím url z odpovědi
            }

            logger.debug("report - unsuccessful, status: {}, body: {}", response.getStatus(), response.getBody());

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return null;
    }

    private boolean login() throws Exception {

        logger.debug("login - requesting token");

        CompletionStage<WSResponse> promise = ws.url(config.getString("logger.youtrackUrl") + "/hub/api/rest/oauth2/token")
                .setContentType("application/x-www-form-urlencoded")
                .addHeader("Authorization", "Basic " + new String(Base64.getEncoder().encode((config.getString("logger.youtrackId") + ":" + config.getString("logger.youtrackSecret")).getBytes())))
                .post("response_type=token&grant_type=password"
                        +"&scope=" + config.getString("logger.youtrackScopeId")
                        +"&username=" + config.getString("logger.youtrackUsername")
                        +"&password=" + config.getString("logger.youtrackPassword"));

        WSResponse response = promise.toCompletableFuture().get();

        if (response.getStatus() == 200) {  // pokud úspěšné, uložím token a jeho expiraci

            logger.debug("checkLoginResponse - success");

            JsonNode content = response.asJson();
            token = content.get("access_token").asText();
            tokenExpire = System.currentTimeMillis() + content.get("expires_in").asLong()*1000;
            return true;
        }

        return false;
    }
}
