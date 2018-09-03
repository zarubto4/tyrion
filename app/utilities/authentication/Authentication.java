package utilities.authentication;

import controllers._BaseController;
import models.Model_AuthorizationToken;
import models.Model_Person;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;

import java.util.Optional;
import java.util.UUID;

public class Authentication extends Security.Authenticator {

    private static final Logger logger = new Logger(Authentication.class);

    @Override
    public String getUsername(Http.Context ctx) {
        try {
            logger.info("getUsername - authorization begins");

            Optional<String> header = ctx.request().getHeaders().get("x-auth-token");

            if (header.isPresent()) {
                UUID token = UUID.fromString(header.get());

                Model_AuthorizationToken authorizationToken = Model_AuthorizationToken.getByToken(token);
                if (authorizationToken.isValid()) {
                    Model_Person person = Model_Person.getByAuthToken(token);
                    if (person != null) {
                        ctx.args.put("person", person);
                        logger.trace("getUsername - authorization successful");
                        return person.nick_name;
                    } else {
                        logger.trace("getUsername - cannot find person by token");
                    }
                } else {
                    logger.trace("getUsername - token is invalid");
                }

            } else {
                logger.info("getUsername - authorization header is missing");
            }

        } catch (Exception e){
            if (!(e instanceof _Base_Result_Exception)) {
                logger.internalServerError(e);
            }
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        logger.warn("onUnauthorized - authorization failed for request: {} {}", ctx.request().method(), ctx.request().path());
        return _BaseController.unauthorized();
    }
}
