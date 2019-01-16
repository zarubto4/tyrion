package utilities.slack;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import play.libs.Json;
import play.libs.ws.WSClient;
import utilities.logger.Logger;

import java.time.Duration;

@Singleton
public class DefaultSlackService implements SlackService {

    private static final Logger logger = new Logger(DefaultSlackService.class);

    private final WSClient wsClient;

    private final String serverChannelUrl;
    private final String hardwareChannelUrl;
    private final String homerChannelUrl;

    @Inject
    public DefaultSlackService(WSClient wsClient, Config config) {
        this.wsClient = wsClient;
        this.serverChannelUrl = config.getString("Slack.servers");
        this.hardwareChannelUrl = config.getString("Slack.hardware");
        this.homerChannelUrl = config.getString("Slack.homer");
    }

    public void post(String message) {
        try {
            ObjectNode body = Json.newObject()
                    .put("username", "Tyrion")
                    .put("icon_emoji", ":tyrion:")
                    .put("text", "*" + message + "*")
                    .put("mrkdwn", true);

            this.send(this.serverChannelUrl, body);
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void postHomerChannel(String message) {
        try {
            ObjectNode body = Json.newObject()
                    .put("username", "Tyrion")
                    .put("icon_emoji", ":tyrion:")
                    .put("text", "*" + message + "*")
                    .put("mrkdwn", true);

            this.send(this.homerChannelUrl, body);
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void postHardwareChannel(String message) {
        try {
            ObjectNode body = Json.newObject()
                    .put("username", "Tyrion")
                    .put("icon_emoji", ":tyrion:")
                    .put("title", "Alert. Something is wrong!")
                    .put("text", "*" + message + "*")
                    .put("color", "danger")
                    .put("mrkdwn", true);

            this.send(this.hardwareChannelUrl, body);
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    private void send(String url, ObjectNode body) {
        this.wsClient.url(url).setRequestTimeout(Duration.ofSeconds(10)).post(body)
                .handle((result, exception) -> {
                    if (exception != null) {
                        logger.warn("send - posting message to Slack failed: {}", exception);
                    } else if (result.getStatus() != 200) {
                        logger.warn("send - got unexpected response: {} - {}", result.getStatus(), result.getBody());
                    }
                    return null;
                });

    }
}
