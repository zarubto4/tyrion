package utilities.emails;

import play.Configuration;
import play.api.libs.mailer.MailerClient;
import play.libs.mailer.Email;
import play.mvc.Controller;

import javax.inject.Inject;

public class EmailTool{


    public Email sendEmailValidation(String name, String userMail, String tokenLink){
        String html = utilities.emails.templates.html.ActivatedAccount.render(Configuration.root().getString("serverLink.Production"), tokenLink).body();

        return new Email()
                .setSubject("Validation of your account")
                .setFrom("Byzance IoT Platform <server@byzance.cz>")
                .addTo( name + "<"+ userMail +">")
                .setBodyText("A text message")
                .setBodyHtml(html);
    }

}

