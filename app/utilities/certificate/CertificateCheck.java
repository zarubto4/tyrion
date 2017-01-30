package utilities.certificate;


import com.google.inject.Inject;
import play.Application;
import play.mvc.Result;

import static play.mvc.Results.ok;

public class CertificateCheck {

    @Inject
    private Application app;

    public Result check(String file){

        return ok(app.getFile("/.well-known/acme-challenge/" + file));
    }
}
