package utilities.certificate;

import com.google.inject.Inject;
import controllers._BaseController;
import play.Environment;
import play.mvc.Result;

/**
 * Used to verify the ownership of the domain, so application can get a certificate.
 */
public class CertificateCheck extends _BaseController {

    private Environment environment;

    @Inject
    public CertificateCheck(Environment environment) {
        this.environment = environment;
    }

    /**
     * This method serves to succeed in well known acme challenge.
     * @param file String name of a file that is requested by LetsEncrypt.
     * @return The control file that was placed on the server by CertBot.
     */
    public Result check(String file) {

        return ok(environment.getFile("/.well-known/acme-challenge/" + file));
    }
}
