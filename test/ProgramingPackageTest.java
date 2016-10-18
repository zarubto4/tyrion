import junit.framework.TestCase;
import models.person.FloatingPersonToken;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http.RequestBuilder;
import org.junit.Test;
import play.mvc.Result;
import play.test.*;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class ProgramingPackageTest extends WithApplication{

    Logger logger = LoggerFactory.getLogger(TestCase.class);
    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            logger.info("Test {} is running.", method.getName());
        }
        public void succeeded(FrameworkMethod method) {
            logger.info("Test {} successfully run.", method.getName());
        }
        public void failed(Throwable e, FrameworkMethod method) {
            logger.error("Test {} failed! Reason: {} a.",
                    method.getName(), e.getMessage());
        }
    };

    @Test
    public void testRequest() {
        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri("/compilation/producer/1")
                .header("X-AUTH-TOKEN", FloatingPersonToken.find.where().eq("person.mail", "admin@byzance.cz").findList().get(0).authToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

}
