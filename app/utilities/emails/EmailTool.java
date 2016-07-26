package utilities.emails;

import play.libs.mailer.Email;

public class EmailTool {

    private String emailContent = "";

    // Příloha k Emailu
    private byte[] attachment;
    private String attachment_name;


    public EmailTool startParagraph(String textSize){
        emailContent += ("<p style='font-size:" + textSize + "pt; color: #969696; padding: 10px;'>");
        return this;
    }

    public EmailTool endParagraph(){
        emailContent += ("</p>");
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

    public EmailTool addLine(){
        emailContent += ("<div style='clear: both; height: 0px; width: 100%; border-top: 1px solid #eee'></div>");
        return this;
    }

    public EmailTool addLink(String link, String linkName, String textSize){
        emailContent += ("<a href='" + link + "' style='padding: 10px; width: 100% !important; color: #00a0dd !important; text-decoration: none !important; text-align: center !important; float: left; font-size:" + textSize + "pt;'>" + linkName + "</a>");
        return this;
    }

    public EmailTool addAttachment(String attachment_name, byte[] file){
        attachment = file;
        this.attachment_name = attachment_name;
        return this;
    }


    public String getEmailContent(){
        return emailContent;
    }

    public Email sendEmail(String userMail, String subject, String content){

        String html = utilities.emails.templates.html.EmailScheme.render(content).body();

        Email email = new Email()
                        .setSubject(subject)
                        .setFrom("Byzance IoT Platform <cloud_blocko_server@byzance.cz>")
                        .addTo("<"+ userMail +">")
                        .setBodyText("A text message")
                        .setBodyHtml(html);

        if(attachment != null) email.addAttachment(attachment_name, attachment, "multipart/form-data");
        return email;
    }
}
