package filters;

import play.mvc.EssentialAction;
import play.mvc.EssentialFilter;
import utilities.Server;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executor;

/**
 * This is a simple filter that adds a header to all requests.
 */
@Singleton
public class DefaultFilter extends EssentialFilter {

    private final Executor exec;

    /**
     * @param exec This class is needed to execute code asynchronously.
     */
    @Inject
    public DefaultFilter(Executor exec) {

        this.exec = exec;
    }

    @Override
    public EssentialAction apply(EssentialAction next) {
        return EssentialAction.of(request ->
                next.apply(request).map(result -> result.withHeaders(
                        "Byzance-Api-Version", Server.version,
                        "Access-Control-Allow-Origin", "localhost:4200",
                        "Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS",
                        "Access-Control-Max-Age", "72000",
                        "Access-Control-Allow-Headers", "Access-Control-Allow-Origin, Content-Type, X-Auth-Token, Becki-Version"
                ), exec)
        );
    }
}
