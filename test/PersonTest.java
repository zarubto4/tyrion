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
public class PersonTest{

    public static FakeApplication app;
    public static String adminToken;

    @BeforeClass
    public static void startApp() throws Exception{
        app = Helpers.fakeApplication();
        Helpers.start(app);
        adminToken = FloatingPersonToken.find.where().eq("person.mail", "admin@byzance.cz").findList().get(0).authToken;
    }

    @AfterClass
    public static void stopApp() throws Exception{
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
    public void N0001_register_person() {

        ObjectNode body = Json.newObject();

        body.put("mail", "test@mail.com");
        body.put("nick_name", "Test_user_nickname");
        body.put("password", "password123");

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri("/coreClient/person/person")
                .bodyJson(body);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test () 
    public void N0002_email_authentication() {

        String token = ValidationToken.find.where().eq("personEmail", "test@mail.com").findList().get(0).authToken;

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri("/mail_person_authentication/test@mail.com/" + token);

        Result result = route(request);
        assertEquals(SEE_OTHER, result.status());
    }

    @Test   
    public void N0003_mail_validation() {

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
    public void N0004_nick_name_validation() {

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
    public void N0005_edit_person_information() {

        String id = Person.find.where().eq("mail", "test@mail.com").findUnique().id;

        ObjectNode body = Json.newObject();

        body.put("nick_name", "Test_user_nickname_change");
        body.put("full_name", "Test Byzance User");

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri("/coreClient/person/person/" + id)
                .bodyJson(body)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void N0006_get_person() {

        String id = Person.find.where().eq("mail", "test@mail.com").findUnique().id;

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri("/coreClient/person/person/" + id)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void N0007_deactivate_person() {

        String id = Person.find.where().eq("mail", "test@mail.com").findUnique().id;

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri("/coreClient/person/person/deactivate/" + id)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void N0008_activate_person() {

        String id = Person.find.where().eq("mail", "test@mail.com").findUnique().id;

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri("/coreClient/person/person/activate/" + id)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void N0009_get_person_all() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri("/coreClient/person/person/all")
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void N0010_login() {

        ObjectNode body = Json.newObject();

        body.put("mail", "test@mail.com");
        body.put("password", "password123");

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri("/coreClient/person/permission/login")
                .bodyJson(body)
                .header(USER_AGENT, "foo");

        route(request);
        route(request);
        route(request);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void N0011_logout() {

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri("/coreClient/person/permission/logout")
                .header("X-AUTH-TOKEN", FloatingPersonToken.find.where().eq("person.mail", "test@mail.com").findList().get(0).authToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test   
    public void N0012_delete_person() {

        String id = Person.find.where().eq("mail", "test@mail.com").findUnique().id;

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri("/coreClient/person/person/remove/" + id)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }
}