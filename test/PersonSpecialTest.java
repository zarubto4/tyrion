import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.TestCase;
import models.person.ChangePropertyToken;
import models.person.FloatingPersonToken;
import models.person.PasswordRecoveryToken;
import models.person.Person;
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


import java.util.UUID;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersonSpecialTest extends TestHelper {

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

    @Test
    public void E_clean_all_tokens() {

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri("/coreClient/person/person/clean_all_tokens/" + person.id)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
        userToken = person_login(person);
    }

    @Test
    public void F_admin_deactivate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri("/coreClient/person/person/deactivate/" + person.id)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void G_admin_activate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri("/coreClient/person/person/activate/" + person.id)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
        userToken = person_login(person);
    }

    @Test
    public void H_user_deactivate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri("/coreClient/person/person/deactivate/" + person.id)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void I_user_activate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri("/coreClient/person/person/activate/" + person.id)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(FORBIDDEN, result.status());
        userToken = person_login(person);
    }

    @Test
    public void J_change_person_login_property() {

        ObjectNode body = Json.newObject();

        body.put("property", "password");
        body.put("password", "heslo12345");

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri("/coreClient/person/changeProperty")
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void L_user_authorize_change() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri("/coreClient/authorize_change/" + ChangePropertyToken.find.where().eq("person.id", person.id).findList().get(0).change_property_token);

        Result result = route(request);
        assertEquals(SEE_OTHER, result.status());
    }

    @Test
    public void M_mail_person_password_recovery() {

        ObjectNode body = Json.newObject();

        body.put("mail", person.mail);

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .bodyJson(body)
                .uri("/coreClient/mail_person_password_recovery");

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void N_person_password_recovery() {

        ObjectNode body = Json.newObject();

        body.put("mail", person.mail);
        body.put("password", "password123");
        body.put("password_recovery_token", PasswordRecoveryToken.find.where().eq("person.id", person.id).findUnique().password_recovery_token);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .bodyJson(body)
                .uri("/coreClient/person_password_recovery");

        Result result = route(request);
        assertEquals(OK, result.status());
    }
}