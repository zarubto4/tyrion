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
import controllers.routes;

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
        adminToken = person_login(Person.find.byId("1"));
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
                .uri(routes.SecurityController.login().toString())
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
                .uri(routes.PersonController.get_Person_Connections().toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void C_remove_person_connection() {

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.PersonController.remove_Person_Connection(FloatingPersonToken.find.where().eq("person.mail", person.mail).findList().get(1).connection_id).toString())
                .header("X-AUTH-TOKEN", FloatingPersonToken.find.where().eq("person.mail", person.mail).findList().get(1).authToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void D_logout() {

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.SecurityController.logout().toString())
                .header("X-AUTH-TOKEN", FloatingPersonToken.find.where().eq("person.mail", person.mail).findList().get(1).connection_id);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void E_clean_all_tokens() {

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.PersonController.delete_all_tokens(person.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
        userToken = person_login(person);
    }

    @Test
    public void F_admin_deactivate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.PersonController.deactivatePerson(person.id).toString())
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void G_admin_activate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.PersonController.activatePerson(person.id).toString())
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
        userToken = person_login(person);
    }

    @Test
    public void H_user_deactivate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.PersonController.deactivatePerson(person.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void I_user_activate_person() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.PersonController.activatePerson(person.id).toString())
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
                .uri(routes.PersonController.changePersonLoginProperty().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void L_user_authorize_change() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.PersonController.authorizePropertyChange(ChangePropertyToken.find.where().eq("person.id", person.id).findList().get(0).change_property_token).toString());

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
                .uri(routes.PersonController.sendPasswordRecoveryEmail().toString());

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
                .uri(routes.PersonController.personPasswordRecovery().toString());

        Result result = route(request);
        assertEquals(OK, result.status());
    }
}