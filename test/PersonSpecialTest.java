import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.routes;
import junit.framework.TestCase;
import models.person.Model_ChangePropertyToken;
import models.person.Model_FloatingPersonToken;
import models.person.Model_PasswordRecoveryToken;
import models.person.Model_Person;
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
public class PersonSpecialTest extends TestHelper {

    public static FakeApplication app;
    public static String adminToken;
    public static String userToken;
    public static Model_Person person;

    @BeforeClass
    public static void startApp() throws Exception {
        app = Helpers.fakeApplication();
        Helpers.start(app);
        adminToken = person_login(Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique());
        person = person_create();
        person_authenticate(person);
        userToken = person_login(person);
    }

    @AfterClass
    public static void stopApp() throws Exception {
        person_delete(person);
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
    public void A_login() {

        ObjectNode body = Json.newObject();

        body.put("mail", person.mail);
        body.put("password", "password123");

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.Controller_Security.login().toString())
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
                .uri(routes.Controller_Person.person_getAllConnections().toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void C_remove_person_connection() {

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Person.remove_Person_Connection(Model_FloatingPersonToken.find.where().eq("person.mail", person.mail).findList().get(1).connection_id).toString())
                .header("X-AUTH-TOKEN", Model_FloatingPersonToken.find.where().eq("person.mail", person.mail).findList().get(1).authToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void D_logout() {

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.Controller_Security.logout().toString())
                .header("X-AUTH-TOKEN", Model_FloatingPersonToken.find.where().eq("person.mail", person.mail).findList().get(1).connection_id);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void E_clean_all_tokens() {

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Person.person_removeAllConnections(person.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
        userToken = person_login(person);
    }

    @Test
    public void F_admin_deactivate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Person.person_deactivate(person.id).toString())
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void G_admin_activate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Person.person_activate(person.id).toString())
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
        userToken = person_login(person);
    }

    @Test
    public void H_user_deactivate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Person.person_deactivate(person.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void I_user_activate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Person.person_activate(person.id).toString())
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
                .uri(routes.Controller_Person.person_changeLoginProperty().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void L_user_authorize_change() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Person.person_authorizePropertyChange(Model_ChangePropertyToken.find.where().eq("person.id", person.id).findList().get(0).change_property_token).toString());

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
                .uri(routes.Controller_Person.person_passwordRecoverySendEmail().toString());

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void N_person_password_recovery() {

        ObjectNode body = Json.newObject();

        body.put("mail", person.mail);
        body.put("password", "password123");
        body.put("password_recovery_token", Model_PasswordRecoveryToken.find.where().eq("person.id", person.id).findUnique().password_recovery_token);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .bodyJson(body)
                .uri(routes.Controller_Person.person_passwordRecovery().toString());

        Result result = route(request);
        assertEquals(OK, result.status());
    }
}