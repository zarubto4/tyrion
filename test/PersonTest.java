import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.routes;
import junit.framework.TestCase;
import models.person.Model_Person;
import models.person.Model_ValidationToken;
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

    public static Model_Person person;
    public static String userToken;

    public static Model_Person randomPerson;
    public static String randomUserToken;

    @BeforeClass
    public static void startApp() throws Exception{

        app = Helpers.fakeApplication();
        Helpers.start(app);

        adminToken = person_login(Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique());

        person = person_create();
        person_authenticate(person);
        userToken = person_login(person);

        randomPerson = person_create();
        person_authenticate(randomPerson);
        randomUserToken = person_login(randomPerson);
    }

    @AfterClass
    public static void stopApp() throws Exception{
        person_delete(person);
        person_delete(randomPerson);
        person_token_delete(adminToken);
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
            logger.error("Test {} failed! Reason: {}.", description.getMethodName(), e.getMessage());
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
                .uri(routes.Controller_Person.person_create().toString())
                .bodyJson(body);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void email_authentication() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Person.person_emailAuthentication(Model_ValidationToken.find.where().eq("personEmail", person.mail).findUnique().authToken).toString());

        Result result = route(request);
        assertEquals(SEE_OTHER, result.status());
    }

    @Test
    public void admin_email_admin_authentication() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Person.person_validEmail(person.id).toString())
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void user_email_admin_authentication() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Person.person_validEmail(person.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test   
    public void mail_validation() {

        ObjectNode body = Json.newObject();

        body.put("value", "test@mail.com");
        body.put("key", "mail");

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.Controller_Person.person_validateProperty().toString())
                .bodyJson(body);

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
                .uri(routes.Controller_Person.person_validateProperty().toString())
                .bodyJson(body);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void user_edit_person_information() {

        ObjectNode body = Json.newObject();

        body.put("nick_name", "Test_user_nickname_change");
        body.put("full_name", "Test Byzance User");

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Person.person_update(person.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_edit_person_information() {

        ObjectNode body = Json.newObject();

        body.put("nick_name", "Test_user_nickname_random_change");
        body.put("full_name", "Test Byzance User");

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Person.person_update(person.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", randomUserToken);

        Result result = route(request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test   
    public void get_person() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Person.person_get(person.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void get_person_all() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Person.person_getAll().toString())
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void admin_delete_person() {

        Model_Person p = person_create();

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Person.person_delete(p.id).toString())
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void user_delete_person() {

        Model_Person p = person_create();

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Person.person_delete(p.id).toString())
                .header("X-AUTH-TOKEN", person_login(p));

        Result result = route(request);
        person_delete(p);
        assertEquals(FORBIDDEN, result.status());
    }
}