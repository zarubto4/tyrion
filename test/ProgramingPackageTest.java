import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.TestCase;
import models.person.Invitation;
import models.person.Person;
import models.project.global.Product;
import models.project.global.Project;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Http.RequestBuilder;
import org.junit.Test;
import play.mvc.Result;
import play.test.*;
import controllers.routes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class ProgramingPackageTest extends TestHelper{

    public static FakeApplication app;

    public static Product product;
    public static Project project;
    //public static Private_Homer_Server homer;

    public static String adminToken;

    public static Person person;
    public static String userToken;

    public static Person secondPerson;

    public static Person randomPerson;
    public static String randomUserToken;

    @BeforeClass
    public static void startApp() throws Exception{

        app = Helpers.fakeApplication();
        Helpers.start(app);

        adminToken = person_login(Person.find.byId("1"));

        person = person_create();
        person_authenticate(person);
        userToken = person_login(person);

        product = product_create(person);
        project = project_create(product);
        //homer = homer_create(project);

        secondPerson = person_create();
        person_authenticate(secondPerson);

        randomPerson = person_create();
        person_authenticate(randomPerson);
        randomUserToken = person_login(randomPerson);
    }

    @AfterClass
    public static void stopApp() throws Exception{
        //homer_delete(homer);
        project_delete(project);
        product_delete(product);
        person_delete(person);
        person_delete(secondPerson);
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
    public void create_new_project() {

        ObjectNode body = Json.newObject();

        body.put("project_name", UUID.randomUUID().toString());
        body.put("project_description", UUID.randomUUID().toString());
        body.put("product_id", product.id);

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.ProgramingPackageController.project_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_projects_by_user_account() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.ProgramingPackageController.project_getByUser().toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void user_get_project() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.ProgramingPackageController.project_get(project.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_get_project() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.ProgramingPackageController.project_get(project.id).toString())
                .header("X-AUTH-TOKEN", randomUserToken);

        Result result = route(request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void user_delete_project() {

        Project p = project_create(product);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.ProgramingPackageController.project_delete(p.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_delete_project() {

        Project p = project_create(product);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.ProgramingPackageController.project_delete(p.id).toString())
                .header("X-AUTH-TOKEN", randomUserToken);

        Result result = route(request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void user_edit_project() {

        ObjectNode body = Json.newObject();

        body.put("project_name", UUID.randomUUID().toString());
        body.put("project_description", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.ProgramingPackageController.project_update(project.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_edit_project() {

        ObjectNode body = Json.newObject();

        body.put("project_name", UUID.randomUUID().toString());
        body.put("project_description", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.ProgramingPackageController.project_update(project.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", randomUserToken);

        Result result = route(request);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void share_project() {

        ObjectNode body = Json.newObject();

        List<String> persons_mail = new ArrayList<>();
        persons_mail.add(UUID.randomUUID().toString() + "@mail.com");
        persons_mail.add(secondPerson.mail);

        body.set("persons_mail", Json.toJson(persons_mail));

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.ProgramingPackageController.project_invite(project.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void add_participant() {

        Invitation i = project_share(project, person, randomPerson);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.ProgramingPackageController.project_addParticipant(i.id, false).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void unshare_project() {

        project_add_participant(project, secondPerson);

        ObjectNode body = Json.newObject();

        List<String> persons_mail = new ArrayList<>();
        persons_mail.add(secondPerson.mail);

        body.set("persons_mail", Json.toJson(persons_mail));

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.ProgramingPackageController.project_removeParticipant(project.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_invitation() {

        Invitation i = project_share(project, person, randomPerson);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.ProgramingPackageController.project_deleteInvitation(i.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }
/*
    @Test
    public void new_homer_in_project() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);
        body.put("mac_address", UUID.randomUUID().toString());
        body.put("type_of_device", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.ProgramingPackageController.newHomer().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void new_homer() {

        ObjectNode body = Json.newObject();

        body.put("mac_address", UUID.randomUUID().toString());
        body.put("type_of_device", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.ProgramingPackageController.newHomer().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void user_get_homer() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.ProgramingPackageController.getHomer(homer.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_get_homer() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.ProgramingPackageController.getHomer(homer.id).toString())
                .header("X-AUTH-TOKEN", randomUserToken);

        Result result = route(request);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void user_delete_homer() {

        Private_Homer_Server h = homer_create(project);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.ProgramingPackageController.removeHomer(h.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_delete_homer() {

        Private_Homer_Server h = homer_create(project);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.ProgramingPackageController.removeHomer(h.id).toString())
                .header("X-AUTH-TOKEN", randomUserToken);

        Result result = route(request);

        assertEquals(FORBIDDEN, result.status());
    }
    */
}
