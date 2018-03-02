package utilities.emails;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MergeVar;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MessageContent;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;
import models.Model_Customer;
import models.Model_Person;
import play.Configuration;
import utilities.logger.Logger;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Email {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger terminal_logger = new Logger(Email.class);

/* VALUE  --------------------------------------------------------------------------------------------------------------*/


    private MandrillApi mandrillApi = new MandrillApi(Configuration.root().getString("mandrillApiKey"));
    private MandrillMessage message = new MandrillMessage();
    private List<MergeVar> globalMergeVars = new ArrayList<>();

    private StringBuilder emailContentHtml = new StringBuilder();
    private StringBuilder emailContentText = new StringBuilder();

/* OPERATION ......-----------------------------------------------------------------------------------------------------*/
    
    public void sendBulk(List<String> emails, String subject) {

        terminal_logger.info("send():: sending email");

        List<Recipient> recipients = new ArrayList<>();

        for (String email : emails) {

            Recipient recipient = new Recipient();
            recipient.setEmail(email);
            recipient.setType(Recipient.Type.TO);

            recipients.add(recipient);
        }

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
            terminal_logger.info("send():: status:" + messageStatusReports[0].getStatus());
            if (messageStatusReports[0].getRejectReason() != null) {
                terminal_logger.info("send():: reject_reason:" + messageStatusReports[0].getRejectReason());
            }
        } catch (Exception e) {
            terminal_logger.internalServerError(e);
        }
    }

    public void send(List<Model_Person> persons, String subject) {

        List<String> emails = new ArrayList<>();

        persons.forEach(person -> emails.add(person.email));

        sendBulk(emails, subject);
    }

    public void send(Model_Customer customer, String subject) {

        List<String> emails = new ArrayList<>();

        customer.getEmployees().forEach(employee -> emails.add(employee.person.email));

        sendBulk(emails, subject);
    }

    public void send(Model_Person person, String subject) {

        List<String> emails = new ArrayList<>();

        emails.add(person.email);

        sendBulk(emails, subject);
    }

    public void send(String mail, String subject) {

        List<String> emails = new ArrayList<>();

        emails.add(mail);

        sendBulk(emails, subject);
    }

    public Email attachmentPDF(String name, byte[] file) {

        MessageContent content = new MessageContent();
        content.setName(name);
        content.setType("application/pdf");
        content.setContent(Base64.getEncoder().encodeToString(file));

        List<MessageContent> contents = new ArrayList<>();
        contents.add(content);

        message.setAttachments(contents);

        return this;
    }

    public Email divider() {

        emailContentHtml.append(utilities.emails.templates.html.divider.render().body());
        emailContentText.append("\n----------------------------------------------------------\n");

        terminal_logger.info("divider():: setting divider");

        return this;
    }

    public Email text(String text) {

        text("13", text);

        return this;
    }

    public Email text(String size, String text) {

        emailContentHtml.append(utilities.emails.templates.html.text.render(size + "pt",text).body());
        emailContentText.append("\n");
        emailContentText.append(text);
        emailContentText.append("\n");

        terminal_logger.info("text():: setting text");

        return this;
    }

    public Email link(String text, String link) {

        emailContentHtml.append(utilities.emails.templates.html.link.render(text,link).body());
        emailContentText.append("\n");
        emailContentText.append(link);
        emailContentText.append("\n");

        terminal_logger.info("link():: setting link");

        return this;
    }

    public static String bold(String text) {

        return "<strong>" + text + "</strong>";
    }

    public static String italics(String text) {

        return "<em>" + text + "</em>";
    }

    public static String underline(String text) {

        return "<span style=\"text-decoration: underline;\">" + text + "</span>";
    }

    public static String newLine() {

        return "<br>";
    }
}
