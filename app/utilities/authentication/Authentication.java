package utilities.authentication;

import controllers.BaseController;
import models.Model_Person;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Logger;

import java.util.Optional;
import java.util.UUID;

public class Authentication extends Security.Authenticator {

    private static final Logger logger = new Logger(Authentication.class);

    @Override
    public String getUsername(Http.Context ctx) {

        logger.info("getUsername - authorization begins");

        Optional<String> header = ctx.request().getHeaders().get("x-auth-token");

        if (header.isPresent()) {
            UUID token = UUID.fromString(header.get());

            Model_Person person = Model_Person.getByAuthToken(token);
            if (person != null) {
                ctx.args.put("person", person); //.request()..withAttrs().addAttr(Attributes.PERSON, person);
                logger.trace("getUsername - authorization successful");
                return person.nick_name;
            }

            logger.trace("getUsername - cannot find person by token");
        } else {
            logger.info("getUsername - authorization header is missing");
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        logger.warn("onUnauthorized - authorization failed for request: {} {}", ctx.request().method(), ctx.request().path());
        return BaseController.unauthorizedEmpty();
    }
}
