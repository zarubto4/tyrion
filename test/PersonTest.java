import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.TestCase;
import models.person.FloatingPersonToken;
import models.person.Person;
import models.person.ValidationToken;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;


import java.util.UUID;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class PersonTest extends TestHelper{

    public static FakeApplication app;
    public static String adminToken;
    public static Person person;

    @BeforeClass
    public static void startApp() throws Exception{
        app = Helpers.fakeApplication();
        Helpers.start(app);
        adminToken = FloatingPersonToken.find.where().eq("person.mail", "admin@byzance.cz").findList().get(0).authToken;
        person = person_create();
    }

    @AfterClass
    public static void stopApp() throws Exception{
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
    public void register_person() {

        ObjectNode body = Json.newObject();

        body.put("mail", UUID.randomUUID().toString() + "@mail.com");
        body.put("nick_name", UUID.randomUUID().toString());
        body.put("password", "password123");

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri("/coreClient/person/person")
                .bodyJson(body);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test () 
    public void email_authentication() {

        String token = ValidationToken.find.where().eq("personEmail", person.mail).findUnique().authToken;

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri("/mail_person_authentication/" + person.mail + "/" + token);

        Result result = route(request);
        assertEquals(SEE_OTHER, result.status());
    }

    @Test   
    public void mail_validation() {

        ObjectNode body = Json.newObject();

        body.put("value", "test@mail.com");
        body.put("key", "mail");

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri("/coreClient/person/validate_entity")
                .bodyJson(body)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void nick_name_validation() {

        ObjectNode body = Json.newObject();

        body.put("value", "Test_user_nickname");
        body.put("key", "nick_name");

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri("/coreClient/person/validate_entity")
                .bodyJson(body)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void edit_person_information() {

        ObjectNode body = Json.newObject();

        body.put("nick_name", "Test_user_nickname_change");
        body.put("full_name", "Test Byzance User");

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri("/coreClient/person/person/" + person.id)
                .bodyJson(body)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void get_person() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri("/coreClient/person/person/" + person.id)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void deactivate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri("/coreClient/person/person/deactivate/" + person.id)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void activate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri("/coreClient/person/person/activate/" + person.id)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void get_person_all() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri("/coreClient/person/person/all")
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }
}