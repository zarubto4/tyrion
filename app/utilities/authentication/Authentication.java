package utilities.authentication;

import controllers._BaseController;
import exceptions.NotFoundException;
import models.Model_AuthorizationToken;
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
    public Optional<String> getUsername(Http.Request req) {
        try {
            logger.info("getUsername - authorization begins");

            Optional<String> header = req.getHeaders().get("x-auth-token");

            if (header.isPresent()) {
                UUID token = UUID.fromString(header.get());

                Model_AuthorizationToken authorizationToken = Model_AuthorizationToken.getByToken(token);
                if (authorizationToken.isValid()) {

                    Model_Person person = Model_Person.getByAuthToken(token);

                    req.addAttr(Attrs_Person.PERSON, person);
                    logger.trace("getUsername - authorization successful");
                    return Optional.ofNullable(person.nick_name);

                } else {
                    logger.trace("getUsername - token is invalid");
                }

            } else {
                logger.info("getUsername - authorization header is missing");
            }

        } catch (NotFoundException e) {
            // nothing
        } catch (Exception e){
            logger.internalServerError(e);
        }

        return Optional.empty();
    }

    public Optional<Model_Person> getPersonObject(Http.Request req) {
        try {

            Model_Person person = req.attrs().get(Attrs_Person.PERSON);
            return  Optional.ofNullable(person);

        } catch (Exception e) {
            return  Optional.empty();
        }
    }

    @Override
    public Result onUnauthorized(Http.Request ctx) {
        logger.warn("onUnauthorized - authorization failed for request: {} {}", ctx.method(), ctx.path());
        return _BaseController.unauthorized();
    }
}
