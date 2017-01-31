package utilities.emails;

import org.apache.commons.mail.EmailAttachment;
import play.api.Play;
import play.api.libs.mailer.MailerClient;
import play.libs.mailer.Email;

public class EmailTool {


    private Email email;

    public EmailTool(){
        email = new Email();
    }

    private String emailContent = "";

    public EmailTool startParagraph(String textSize){
        emailContent += ("<p style='font-size:" + textSize + "pt; color: #969696; padding: 10px;'>");
        return this;
    }

    public EmailTool endParagraph(){
        emailContent += ("</p>");
        return this;
    }

    public EmailTool nextLine(){
        emailContent += ("<br>");
        return this;
    }

    public EmailTool addText(String text){
        emailContent += (text);
        return this;
    }

    public EmailTool addBoldText(String text){
        emailContent += ("<strong>" + text + "</strong>");
        return this;
    }

    public EmailTool addLinkIntoText(String link, String linkName){
        emailContent += ("<a href='" + link + "'>" + linkName + "</a>");
        return this;
    }

    public EmailTool addEmptyLineSpace(){
        emailContent += ("<div style='height: 20px; width: 100%; clear: both;'></div>");
        return this;
    }

    public EmailTool addSeparatorLine(){
        emailContent += ("<div style='clear: both; height: 0px; width: 100%; border-top: 1px solid #eee'></div>");
        return this;
    }

    public EmailTool addLink(String link, String linkName, String textSize){
        emailContent += ("<a href='" + link + "' style='padding: 10px; width: 100% !important; color: #00a0dd !important; text-decoration: none !important; text-align: center !important; float: left; font-size:" + textSize + "pt;'>" + linkName + "</a>");
        return this;
    }

    public EmailTool addAttachment_PDF(String attachment_name, byte[] file){
        email .addAttachment( attachment_name , file, "application/pdf", "Simple data", EmailAttachment.ATTACHMENT);
        return this;
    }

    public void sendEmail(String userMail, String subject){


        String html = utilities.emails.templates.html.EmailScheme.render(emailContent).body();

                 email  .setSubject(subject)
                        .setFrom("Byzance IoT Platform <server@byzance.cz>")
                        .addTo("<"+ userMail +">")
                        .setBodyHtml(html);

        MailerClient mailerClient =  Play.current().injector().instanceOf(MailerClient.class);
        mailerClient.send(email);
    }
}
