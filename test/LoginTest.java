import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.TestCase;
import models.person.FloatingPersonToken;
import models.person.Person;
import models.person.ValidationToken;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;


import static org.junit.Assert.*;
import static play.test.Helpers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginTest extends TestHelper {

    public static FakeApplication app;
    public static String adminToken;
    public static String userToken;
    public static Person person;

    @BeforeClass
    public static void startApp() throws Exception {
        app = Helpers.fakeApplication();
        Helpers.start(app);
        adminToken = FloatingPersonToken.find.where().eq("person.mail", "admin@byzance.cz").findList().get(0).authToken;
        person = person_create();
        person_authenticate(person);
        userToken = person_login(person);
    }

    @AfterClass
    public static void stopApp() throws Exception {
        person_delete(person);
        Helpers.stop(app);
    }

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
    public void A_login() {

        ObjectNode body = Json.newObject();

        body.put("mail", person.mail);
        body.put("password", "password123");

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri("/coreClient/person/permission/login")
                .bodyJson(body)
                .header(USER_AGENT, "foo");

        route(request);
        route(request);
        route(request);
        route(request);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void B_get_person_connections() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri("/coreClient/connections")
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void C_remove_person_connection() {

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri("/coreClient/connection/" + FloatingPersonToken.find.where().eq("person.mail", person.mail).findList().get(1).connection_id)
                .header("X-AUTH-TOKEN", FloatingPersonToken.find.where().eq("person.mail", person.mail).findList().get(1).authToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void D_logout() {

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri("/coreClient/person/permission/logout")
                .header("X-AUTH-TOKEN", FloatingPersonToken.find.where().eq("person.mail", person.mail).findList().get(1).connection_id);

        Result result = route(request);
        assertEquals(OK, result.status());
    }
}