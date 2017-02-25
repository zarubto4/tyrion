package utilities.emails;

import com.google.inject.Inject;
import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MergeVar;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;
import org.apache.commons.mail.EmailAttachment;
import play.Configuration;
import play.api.Play;
import play.api.libs.mailer.MailerClient;
import play.libs.mailer.Email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmailTool {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    private MandrillApi mandrillApi = new MandrillApi(Configuration.root().getString("mandrillApiKey"));
    private MandrillMessage message = new MandrillMessage();
    private List<MergeVar> globalMergeVars = new ArrayList<>();

    private StringBuilder emailContentHtml = new StringBuilder("");

    public void send(String email, String subject){

        logger.info("EmailTool:: send():: sending email");

        Recipient recipient = new Recipient();
        recipient.setEmail(email);
        recipient.setType(Recipient.Type.TO);

        List<Recipient> recipients = new ArrayList<>();
        recipients.add(recipient);

        globalMergeVars.add(new MergeVar("html_content", emailContentHtml.toString()));
        globalMergeVars.add(new MergeVar("subject", subject));

        message.setTo(recipients);
        message.setSubject(subject);
        message.setMergeLanguage("handlebars");
        message.setGlobalMergeVars(globalMergeVars);
        message.setFromEmail("server@byzance.cz");
        message.setFromName("Byzance IoT Platform");

        try {
            MandrillMessageStatus[] messageStatusReports = mandrillApi.messages().sendTemplate("byzance-transactional", null ,message, false);
        } catch (IOException e){

            logger.error("EmailTool:: send():: IOException", e);
        } catch (MandrillApiError e){

            logger.error("EmailTool:: send():: MandrillApiException", e);
        }

    }

    public EmailTool divider(){

        emailContentHtml.append(utilities.emails.templates.html.divider.render().body());

        logger.info("EmailTool:: divider():: setting divider");

        return this;
    }

    public EmailTool text(String text){

        emailContentHtml.append(utilities.emails.templates.html.text.render(text).body());

        logger.info("EmailTool:: text():: setting text");

        return this;
    }

    public EmailTool link(String text, String link){

        emailContentHtml.append(utilities.emails.templates.html.link.render(text,link).body());

        logger.info("EmailTool:: link():: setting link");

        return this;
    }

    public static String bold(String text){

        return "<strong>" + text + "</strong>";
    }

    public String italics(String text){

        return "<em>" + text + "</em>";
    }

    public String underline(String text){

        return "<span style=\"text-decoration: underline;\">" + text + "</span>";
    }

    public String newLine(){

        return "<br>";
    }

    private Email email;
//
    public EmailTool(){
        email = new Email();
    }

    private String emailContentText = "";

    public EmailTool startParagraph(String textSize){
        emailContentHtml.append("<p style='font-size:" + textSize + "pt; color: #969696; padding: 10px;'>");
        return this;
    }

    public EmailTool endParagraph(){
        emailContentHtml.append("</p>");
        return this;
    }

    public EmailTool nextLine(){
        emailContentHtml.append("<br>");
        return this;
    }

    public EmailTool addText(String text){
        emailContentHtml.append(text);
        emailContentText += (text) + " ";
        return this;
    }

    public EmailTool addBoldText(String text){
        emailContentHtml.append("<strong>" + text + "</strong>");
        emailContentText += (text + " ");
        return this;
    }

    public EmailTool addLinkIntoText(String link, String linkName){
        emailContentHtml.append("<a href='" + link + "'>" + linkName + "</a>");
        emailContentText += (link + " ");
        return this;
    }

    public EmailTool addEmptyLineSpace(){
        emailContentHtml.append("<div style='height: 20px; width: 100%; clear: both;'></div>");
        return this;
    }

    public EmailTool addSeparatorLine(){
        emailContentHtml.append("<div style='clear: both; height: 1px; width: 100%; border-top: 1px solid #eee'></div>");
        return this;
    }

    public EmailTool addLink(String link, String linkName, String textSize){
        emailContentHtml.append("<a href='" + link + "' style='padding: 10px; width: 100% !important; color: #00a0dd !important; text-decoration: none !important; text-align: center !important; float: left; font-size:" + textSize + "pt;'>" + linkName + "</a>");
        emailContentText += (link + " ");
        return this;
    }

    public EmailTool addAttachment_PDF(String attachment_name, byte[] file){
        email .addAttachment( attachment_name , file, "application/pdf", "Simple data", EmailAttachment.ATTACHMENT);
        return this;
    }

    public void sendEmail(String userMail, String subject){


        String html = utilities.emails.templates.html.EmailScheme.render(emailContentHtml.toString(), subject).body();

                 email  .setSubject(subject)
                        .setFrom("Byzance IoT Platform <server@byzance.cz>")
                        .addTo("<"+ userMail +">")
                        .setBodyHtml(html)
                        .setBodyText(emailContentText);

        MailerClient mailerClient =  Play.current().injector().instanceOf(MailerClient.class);
        mailerClient.send(email);
    }
}
