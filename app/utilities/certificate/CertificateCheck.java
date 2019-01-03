package utilities.certificate;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseController;
import controllers._BaseFormFactory;
import play.Environment;
import play.libs.ws.WSClient;
import play.mvc.Result;
import utilities.model.EchoService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;

/**
 * Used to verify the ownership of the domain, so application can get a certificate.
 */
public class CertificateCheck extends _BaseController {

    private Environment environment;

// CONTROLLER CONFIGURATION ############################################################################################

    @Inject
    public CertificateCheck(Environment environment, WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService, NotificationService notificationService, EchoService echoService) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);
        this.environment = environment;
    }

// CONTROLLER CONTENT ##################################################################################################

    /**
     * This method serves to succeed in well known acme challenge.
     * @param file String name of a file that is requested by LetsEncrypt.
     * @return The control file that was placed on the server by CertBot.
     */
    public Result check(String file) {

        return ok(environment.getFile("/.well-known/acme-challenge/" + file));
    }
}
