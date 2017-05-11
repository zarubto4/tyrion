package utilities.slack;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_LoggyError;
import play.api.Play;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

/**
 * Class is used to post messages to Byzance Slack Team Chat
 */
public class Slack {

    public static void post(Model_LoggyError error){
        try {

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);
/*
            ObjectNode field = Json.newObject();
            field.put("title", error.summary);
            field.put("value", error.description);
            field.put("short", false);

            List<ObjectNode> fields = new ArrayList<>();
            fields.add(field);

            ObjectNode attachment = Json.newObject();
            attachment.put("fallback", "Internal Server Error");
            attachment.put("pretext", "TEST Error occurred in Tyrion");
            attachment.put("text", error.description);
            attachment.put("color", "danger");
            //attachment.set("fields", Json.toJson(fields));

            List<ObjectNode> attachments = new ArrayList<>();
            attachments.add(attachment);
*/
            ObjectNode json = Json.newObject();
            json.put("username", "Tyrion");
            json.put("icon_emoji", ":tyrion:");
            json.put("text", "*" + error.summary + "* :face_with_rolling_eyes:" + error.description);
            json.put("mrkdwn", true);
            //json.set("attachments", Json.toJson(attachments));

            WSResponse response = ws.url("https://hooks.slack.com/services/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")
                    .setRequestTimeout(10000)
                    .post(json.toString())
                    .get(10000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
