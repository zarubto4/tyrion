package utilities.authentication;

import controllers._BaseController;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utilities.logger.Logger;
import websocket.interfaces.WS_Homer;

import java.util.Optional;

public class AuthenticationHomer extends Security.Authenticator {

    private static final Logger logger = new Logger(AuthenticationHomer.class);

    @Override
    public String getUsername(Http.Context ctx) {

        logger.info("getUsername - authorization begins");

        Optional<String> header = ctx.request().getHeaders().get("x-auth-token");

        if (header.isPresent()) {
            String token = header.get();
            if (WS_Homer.token_cache.containsKey(token)) {
                return token;
            }
            logger.trace("getUsername - cannot find homer by token");
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        logger.warn("onUnauthorized - authorization failed for request: {} {}", ctx.request().method(), ctx.request().path());
        return _BaseController.unauthorized();
    }
}
