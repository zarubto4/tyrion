package utilities.emails;

import play.Configuration;
import play.libs.mailer.Email;

public class EmailTool{


    public Email sendEmailValidation(String name, String userMail, String tokenLink){

        String html = utilities.emails.templates.html.ActivatedAccount.render(Configuration.root().getString("serverLink.Production"), tokenLink).body();

        return new Email()
                .setSubject("Validation of your account")
                .setFrom("Byzance IoT Platform <cloud_blocko_server@byzance.cz>")
                .addTo( name + "<"+ userMail +">")
                .setBodyText("A text message")
                .setBodyHtml(html);
    }

    public Email sendPasswordRecoveryEmail(String userMail, String tokenLink){

        String html = utilities.emails.templates.html.PasswordRecovery.render(Configuration.root().getString("serverLink.Production"), tokenLink).body();

        return new Email()
                .setSubject("Password Recovery")
                .setFrom("Byzance IoT Platform <cloud_blocko_server@byzance.cz>")
                .addTo("<"+ userMail +">")
                .setBodyText("A text message")
                .setBodyHtml(html);
    }

    public Email sendInvitationEmail(String userMail, String tokenLink){

        String html = utilities.emails.templates.html.InvitationEmail.render(Configuration.root().getString("serverLink.Production"), tokenLink).body();

        return new Email()
                .setSubject("Invitation to collaborate")
                .setFrom("Byzance IoT Platform <cloud_blocko_server@byzance.cz>")
                .addTo("<"+ userMail +">")
                .setBodyText("A text message")
                .setBodyHtml(html);
    }




}

