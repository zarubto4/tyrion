import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Project;
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

public class ProgramingPackageTest extends TestHelper{

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
                .uri(Controller_Project.project_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_projects_by_user_account() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(Controller_Project.project_getByUser().toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void user_get_project() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(Controller_Project.project_get(project.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_get_project() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(Controller_Project.project_get(project.id).toString())
                .header("X-AUTH-TOKEN", randomUserToken);

        Result result = route(request);
        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void user_delete_project() {

        Model_Project p = project_create(product);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(Controller_Project.project_delete(p.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_delete_project() {

        Model_Project p = project_create(product);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(Controller_Project.project_delete(p.id).toString())
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
                .uri(Controller_Project.project_update(project.id).toString())
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
                .uri(Controller_Project.project_update(project.id).toString())
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
                .uri(Controller_Project.project_invite(project.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void add_participant() {

        Model_Invitation i = project_share(project, person, randomPerson);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(Controller_Project.project_addParticipant(i.id, false).toString())
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
                .uri(Controller_Project.project_removeParticipant(project.id).toString())
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
                .uri(Controller_Project.project_deleteInvitation(i.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void create_type_of_block() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("project_id", project.id);

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(Controller_Project.typeOfBlock_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_type_of_block() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(Controller_Project.typeOfBlock_get(type_of_block.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_all_type_of_block() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(Controller_Project.typeOfBlock_getAll().toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_by_filter_type_of_block() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);
        body.put("private_type", true);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(Controller_Project.typeOfBlock_getByFilter(1).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void update_type_of_block() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(Controller_Project.typeOfBlock_update(type_of_block.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_type_of_block() {

        Model_TypeOfBlock t = type_of_block_create(project);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(Controller_Project.typeOfBlock_delete(t.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void create_blocko_block() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("general_description", UUID.randomUUID().toString());
        body.put("type_of_block_id", type_of_block.id);

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(Controller_Project.blockoBlock_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_blocko_block() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(Controller_Project.blockoBlock_get(blocko_block.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_by_filter_blocko_block() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(Controller_Project.blockoBlock_getByFilter(1).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void update_blocko_block() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("general_description", UUID.randomUUID().toString());
        body.put("type_of_block_id", type_of_block.id);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(Controller_Project.blockoBlock_update(blocko_block.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_blocko_block() {

        Model_BlockoBlock b = blocko_block_create(type_of_block);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(Controller_Project.blockoBlock_delete(b.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void create_blocko_block_version() {

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());
        body.put("logic_json", UUID.randomUUID().toString());
        body.put("design_json", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(Controller_Project.blockoBlockVersion_create(blocko_block.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_blocko_block_version() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(Controller_Project.blockoBlockVersion_get(blocko_block_version.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_blocko_block_blocko_block_version() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(Controller_Project.blockoBlockVersion_getAll(blocko_block.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void make_public_blocko_block_version() {

        Model_BlockoBlockVersion b = blocko_block_version_create(blocko_block);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(Controller_Project.blockoBlockVersion_makePublic(b.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void update_blocko_block_version() {

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(Controller_Project.blockoBlockVersion_update(blocko_block_version.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_blocko_block_version() {

        Model_BlockoBlockVersion b = blocko_block_version_create(blocko_block);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(Controller_Project.blockoBlockVersion_delete(b.id).toString())
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
                .uri(routes.routes.Controller_ProgramingPackage.newHomer().toString())
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
                .uri(routes.routes.Controller_ProgramingPackage.newHomer().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void user_get_homer() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.routes.Controller_ProgramingPackage.getHomer(homer.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_get_homer() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.routes.Controller_ProgramingPackage.getHomer(homer.id).toString())
                .header("X-AUTH-TOKEN", randomUserToken);

        Result result = route(request);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void user_delete_homer() {

        Private_Homer_Server h = homer_create(project);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.routes.Controller_ProgramingPackage.removeHomer(h.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void random_user_delete_homer() {

        Private_Homer_Server h = homer_create(project);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.routes.Controller_ProgramingPackage.removeHomer(h.id).toString())
                .header("X-AUTH-TOKEN", randomUserToken);

        Result result = route(request);

        assertEquals(FORBIDDEN, result.status());
    }
    */
}
