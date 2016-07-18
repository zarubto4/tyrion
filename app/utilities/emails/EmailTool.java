package utilities.emails;

import play.libs.mailer.Email;

public class EmailTool{

    public Email sendEmail(String userMail, String subject, String content){

        String html = utilities.emails.templates.html.EmailScheme.render(content).body();

        return new Email()
                .setSubject(subject)
                .setFrom("Byzance IoT Platform <cloud_blocko_server@byzance.cz>")
                .addTo("<"+ userMail +">")
                .setBodyText("A text message")
                .setBodyHtml(html);
    }




}

