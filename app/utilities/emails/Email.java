package utilities.emails;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MergeVar;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MessageContent;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;
import play.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Email {

    // Loger
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    private MandrillApi mandrillApi = new MandrillApi(Configuration.root().getString("mandrillApiKey"));
    private MandrillMessage message = new MandrillMessage();
    private List<MergeVar> globalMergeVars = new ArrayList<>();

    private StringBuilder emailContentHtml = new StringBuilder();
    private StringBuilder emailContentText = new StringBuilder();

    public void send(String email, String subject){

        logger.info("EmailTool:: send():: sending email");

        Recipient recipient = new Recipient();
        recipient.setEmail(email);
        recipient.setType(Recipient.Type.TO);

        List<Recipient> recipients = new ArrayList<>();
        recipients.add(recipient);

        globalMergeVars.add(new MergeVar("html_content", emailContentHtml.toString()));
        globalMergeVars.add(new MergeVar("text_content", emailContentText.toString()));
        globalMergeVars.add(new MergeVar("subject", subject));

        message.setTo(recipients);
        message.setSubject(subject);

        message.setMergeLanguage("handlebars");
        message.setGlobalMergeVars(globalMergeVars);

        message.setFromEmail("server@byzance.cz");
        message.setFromName("Byzance IoT Platform");

        try {
            MandrillMessageStatus[] messageStatusReports = mandrillApi.messages().sendTemplate("byzance-transactional", null ,message, false);
            logger.info("Email:: send():: status:" + messageStatusReports[0].getStatus());
            if (messageStatusReports[0].getRejectReason() != null){
                logger.info("Email:: send():: reject_reason:" + messageStatusReports[0].getRejectReason());
            }
        } catch (IOException e){

            logger.error("EmailTool:: send():: IOException", e);
        } catch (MandrillApiError e){

            logger.error("EmailTool:: send():: MandrillApiException", e);
        }

    }

    public Email attachmentPDF(String name, byte[] file){

        MessageContent content = new MessageContent();
        content.setName(name);
        content.setType("application/pdf");
        content.setContent(Base64.getEncoder().encodeToString(file));

        List<MessageContent> contents = new ArrayList<>();
        contents.add(content);

        message.setAttachments(contents);

        return this;
    }

    public Email divider(){

        emailContentHtml.append(utilities.emails.templates.html.divider.render().body());
        emailContentText.append("\n----------------------------------------------------------\n");

        logger.info("EmailTool:: divider():: setting divider");

        return this;
    }

    public Email text(String text){

        text("13", text);

        return this;
    }

    public Email text(String size, String text){

        emailContentHtml.append(utilities.emails.templates.html.text.render(size + "pt",text).body());
        emailContentText.append("\n");
        emailContentText.append(text);
        emailContentText.append("\n");

        logger.info("EmailTool:: text():: setting text");

        return this;
    }

    public Email link(String text, String link){

        emailContentHtml.append(utilities.emails.templates.html.link.render(text,link).body());
        emailContentText.append("\n");
        emailContentText.append(link);
        emailContentText.append("\n");

        logger.info("EmailTool:: link():: setting link");

        return this;
    }

    public static String bold(String text){

        return "<strong>" + text + "</strong>";
    }

    public static String italics(String text){

        return "<em>" + text + "</em>";
    }

    public static String underline(String text){

        return "<span style=\"text-decoration: underline;\">" + text + "</span>";
    }

    public static String newLine(){

        return "<br>";
    }
}
