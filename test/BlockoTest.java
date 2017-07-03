import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.routes;
import junit.framework.TestCase;
import models.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.*;

public class BlockoTest extends TestHelper{

    @Inject
    WSClient ws;

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
    public void create_type_of_block() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("project_id", project.id);

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.Controller_Blocko.typeOfBlock_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_type_of_block() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Blocko.typeOfBlock_get(type_of_block.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_all_type_of_block() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Blocko.typeOfBlock_getAll().toString())
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
                .uri(routes.Controller_Blocko.typeOfBlock_getByFilter(1).toString())
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
                .uri(routes.Controller_Blocko.typeOfBlock_update(type_of_block.id).toString())
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
                .uri(routes.Controller_Blocko.typeOfBlock_delete(t.id).toString())
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
                .uri(routes.Controller_Blocko.blockoBlock_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_blocko_block() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Blocko.blockoBlock_get(blocko_block.id).toString())
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
                .uri(routes.Controller_Blocko.blockoBlock_getByFilter(1).toString())
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
                .uri(routes.Controller_Blocko.blockoBlock_update(blocko_block.id).toString())
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
                .uri(routes.Controller_Blocko.blockoBlock_delete(b.id).toString())
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
                .uri(routes.Controller_Blocko.blockoBlockVersion_create(blocko_block.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_blocko_block_version() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Blocko.blockoBlockVersion_get(blocko_block_version.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_blocko_block_blocko_block_version() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Blocko.blockoBlockVersion_getAll(blocko_block.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void make_public_blocko_block_version() {

        Model_BlockoBlockVersion b = blocko_block_version_create(blocko_block);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Blocko.blockoBlockVersion_makePublic(b.id).toString())
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
                .uri(routes.Controller_Blocko.blockoBlockVersion_update(blocko_block_version.id).toString())
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
                .uri(routes.Controller_Blocko.blockoBlockVersion_delete(b.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }
}
