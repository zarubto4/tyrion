import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Project;
import controllers.routes;
import junit.framework.TestCase;
import models.Model_BlockoBlock;
import models.Model_BlockoBlockVersion;
import models.Model_TypeOfBlock;
import models.Model_Invitation;
import models.Model_Person;
import models.Model_Product;
import models.Model_Project;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class ProjectTest extends TestHelper{

    public static FakeApplication app;

    public static Model_Product product;
    public static Model_Project project;
    //public static Private_Homer_Server homer;

    public static Model_TypeOfBlock type_of_block;
    public static Model_BlockoBlock blocko_block;
    public static Model_BlockoBlockVersion blocko_block_version;

    public static String adminToken;

    public static Model_Person person;
    public static String userToken;

    public static Model_Person secondPerson;

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

        product = product_create(person);
        project = project_create(product);
        //homer = homer_create(project);

        type_of_block = type_of_block_create(project);
        blocko_block = blocko_block_create(type_of_block);
        blocko_block_version = blocko_block_version_create(blocko_block);

        secondPerson = person_create();
        person_authenticate(secondPerson);

        randomPerson = person_create();
        person_authenticate(randomPerson);
        randomUserToken = person_login(randomPerson);
    }

    @AfterClass
    public static void stopApp() throws Exception{
        blocko_block_version_delete(blocko_block_version);
        blocko_block_delete(blocko_block);
        type_of_block_delete(type_of_block);
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

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("product_id", product.id);

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.Controller_Project.project_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_projects_by_user_account() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Project.project_getByUser().toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void user_get_project() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Project.project_get(project.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_get_project() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Project.project_get(project.id).toString())
                .header("X-AUTH-TOKEN", randomUserToken);

        Result result = route(request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void user_delete_project() {

        Model_Project p = project_create(product);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Project.project_delete(p.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_delete_project() {

        Model_Project p = project_create(product);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Project.project_delete(p.id).toString())
                .header("X-AUTH-TOKEN", randomUserToken);

        Result result = route(request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void user_edit_project() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Project.project_update(project.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_edit_project() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Project.project_update(project.id).toString())
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
                .uri(routes.Controller_Project.project_invite(project.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    /*@Test
    public void add_participant() {

        Model_Invitation i = project_share(project, person, randomPerson);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Project.project_addParticipant(i.id, false).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }*/

    @Test
    public void unshare_project() {

        project_add_participant(project, secondPerson);

        ObjectNode body = Json.newObject();

        List<String> persons_mail = new ArrayList<>();
        persons_mail.add(secondPerson.mail);

        body.set("persons_mail", Json.toJson(persons_mail));

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Project.project_removeParticipant(project.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_invitation() {

        Model_Invitation i = project_share(project, person, randomPerson);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Project.project_removeParticipant(i.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }
}