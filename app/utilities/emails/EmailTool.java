package utilities.emails;

import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;

public class EmailTool {

    @Inject MailerClient mailerClient;

    public void sendEmail(String subject, String to, String html) {

        Email email = new Email()
                .setSubject(subject)
                .setFrom("Byzance")
                .addTo(to) // persons email
                .setBodyHtml(html);

        mailerClient.send(email);
    }

}

