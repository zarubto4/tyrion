package utilities.slack;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_HomerServer;
import models.Model_ServerError;
import play.api.Play;
import play.libs.Json;
import play.libs.ws.WSClient;
import utilities.Server;
import utilities.swagger.input.Swagger_GitHubReleases;

import java.time.Duration;

/**
 * Class is used to post messages to Byzance Slack Team Chat
 */
public class Slack {

    /**
     * Posts an error to Byzance Slack, chanel #servers
     * @param error Model error that is being posted.
     */
    public static void post(Model_ServerError error) {
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
            json.put("text", "*" + error.name + "* :face_with_rolling_eyes:" + error.description);
            json.put("mrkdwn", true);
            //json.set("attachments", Json.toJson(attachments));

            ws.url(Server.slack_webhook_url_channel_servers)
                    .setRequestTimeout(Duration.ofSeconds(10))
                    .post(json.toString())
                    .toCompletableFuture()
                    .get();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Posts an error to Byzance Slack, chanel #hardware
     * @param release
     */
    public static void post_invalid_release(Swagger_GitHubReleases release) {
        try {

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);

            ObjectNode json = Json.newObject();
            json.put("username", "Tyrion");
            json.put("icon_emoji", ":tyrion:");
            json.put("title", "Výstražná zpráva");
            json.put("color", "danger");
            json.put("text", "Toto je automatická zpráva kterou vygeneroval všemocný Tyrion Server. \n Podle GitHubu *" + release.author.login + "* vytvořil firmware release *" + release.tag_name + "* bez požadovaných parametrů. \n" +
                    " Například soubor (dist.zip) nebo výsledný zip soubor neobsahuje složku examples");

            ws.url(Server.slack_webhook_url_channel_hardware)
                    .setRequestTimeout(Duration.ofSeconds(10))
                    .post(json.toString())
                    .toCompletableFuture()
                    .get();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Posts an error to Byzance Slack, chanel #hardware
     * @param release
     */
    public static void post_invalid_bootloader(Swagger_GitHubReleases release) {
        try {

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);

            ObjectNode json = Json.newObject();
            json.put("username", "Tyrion");
            json.put("icon_emoji", ":tyrion:");
            json.put("title", "Výstražná zpráva");
            json.put("color", "danger");
            json.put("text", "Toto je automatická zpráva kterou vygeneroval všemocný Tyrion Server. \n  Podle GitHubu *" + release.author.login + "* vytvořil Bootloader release *" + release.tag_name + "* bez požadovaných parametrů. \n" +
                    " Například soubor (dist.zip)");

            ws.url(Server.slack_webhook_url_channel_hardware)
                    .setRequestTimeout(Duration.ofSeconds(10))
                    .post(json.toString())
                    .toCompletableFuture()
                    .get();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Posts a message to Byzance Slack, chanel #servers on Stage and Production Level - Its Emergency message for DEVOPS
     * @param server Model_HomerServer message to post.
     */
    public static void homer_server_offline(Model_HomerServer server) {
        try {

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);

            ObjectNode json = Json.newObject();
            json.put("username", "Tyrion");
            json.put("icon_emoji", ":tyrion:");
            json.put("text",  "Homer Server " +"*" + server.name + "*"  +" on Stage Hierarchy is offline!!!\n Please Check it!!!");
            json.put("mrkdwn", true);

            ws.url(Server.slack_webhook_url_channel_homer)
                    .setRequestTimeout(Duration.ofSeconds(10))
                    .post(json.toString())
                    .toCompletableFuture()
                    .get();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Posts a message to Byzance Slack, chanel #servers
     * @param message String message to post.
     */
    public static void post(String message) {
        try {

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);

            ObjectNode json = Json.newObject();
            json.put("username", "Tyrion");
            json.put("icon_emoji", ":tyrion:");
            json.put("text", "*" + message + "*" );
            json.put("mrkdwn", true);

            ws.url(Server.slack_webhook_url_channel_servers)
                    .setRequestTimeout(Duration.ofSeconds(10))
                    .post(json.toString())
                    .toCompletableFuture()
                    .get();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Posts a message to Byzance Slack, chanel #servers
     * @param message String message to post.
     */
    public static void post_error(String message, String channel) {
        try {

            WSClient ws = Play.current().injector().instanceOf(WSClient.class);

            ObjectNode json = Json.newObject();
            json.put("username", "Tyrion");
            json.put("icon_emoji", ":tyrion:");
            json.put("title", "Alert. Something is wrong!");
            json.put("text", "*" + message + "*" );
            json.put("color", "danger");
            json.put("mrkdwn", true);

            ws.url(channel)
                    .setRequestTimeout(Duration.ofSeconds(10))
                    .post(json.toString())
                    .toCompletableFuture()
                    .get();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
