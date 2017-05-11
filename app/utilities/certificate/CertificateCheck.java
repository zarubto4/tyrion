package utilities.certificate;


import com.google.inject.Inject;
import play.Application;
import play.mvc.Result;

import static play.mvc.Results.ok;

/**
 * Used to verify the ownership of the domain, so application can get a certificate.
 */
public class CertificateCheck {

    @Inject
    private Application app;

    /**
     * This method serves to succeed in well known acme challenge.
     * @param file String name of a file that is requested by LetsEncrypt.
     * @return The control file that was placed on the server by CertBot.
     */
    public Result check(String file){

        return ok(app.getFile("/.well-known/acme-challenge/" + file));
    }
}
