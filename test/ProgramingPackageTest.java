import junit.framework.TestCase;
import models.person.FloatingPersonToken;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
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
    public TestRule watchman = new TestWatcher() {
        public void starting(Description description) {
            logger.info("Test {} is running.", description.getMethodName());
        }
        public void succeeded(Description description) {
            logger.info("Test {} successfully run.", description.getMethodName());
        }
        public void failed(Throwable e, Description description) {
            logger.error("Test {} failed! Reason: {} a.", description.getMethodName(), e.getMessage());
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
