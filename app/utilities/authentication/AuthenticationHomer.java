package utilities.authentication;

import com.google.inject.Inject;
import controllers._BaseController;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Logger;
import websocket.interfaces.Homer;

import java.util.Optional;
import java.util.UUID;

public class AuthenticationHomer extends Security.Authenticator {

    private static final Logger logger = new Logger(AuthenticationHomer.class);

    @Inject
    public AuthenticationHomer() {
        // TODO inject token cache
    }

    @Override
    public String getUsername(Http.Context ctx) {

        logger.info("getUsername - authorization begins");

        Optional<String> header = ctx.request().getHeaders().get("x-auth-token");

        if (header.isPresent()) {
            String token = header.get();
            if (Homer.apiKeys.containsKey(UUID.fromString(token))) {
                return token;
            }
            logger.error("getUsername - cannot find homer by token");
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        logger.warn("onUnauthorized - authorization failed for request: {} {}", ctx.request().method(), ctx.request().path());
        return _BaseController.unauthorized();
    }
}
