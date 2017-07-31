package utilities.test.tests;

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
import play.api.Play;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.test.TestHelper;

import java.util.UUID;

public class Code extends TestHelper {

    public static Model_Product product;
    public static Model_Project project;
    public static Model_Producer producer;
    public static Model_Processor processor;
    public static Model_TypeOfBoard typeOfBoard;
    public static Model_Board board;
    public static Model_CProgram private_c_program;
    public static Model_VersionObject private_c_program_version;

    public static String adminToken;

    public static Model_Person person;
    public static String userToken;

    public static Model_Person randomPerson;
    public static String randomUserToken;

    @BeforeClass
    public static void startApp() throws Exception{

        adminToken = person_login(Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique());

        person = person_create();
        person_authenticate(person);
        userToken = person_login(person);

        product = product_create(person);
        project = project_create(product);
        producer = producer_create();
        processor = processor_create();
        typeOfBoard = type_of_board_create(producer, processor);
        private_c_program = private_c_program_create(typeOfBoard, project);
        private_c_program_version = c_program_version_create(private_c_program);

        randomPerson = person_create();
        person_authenticate(randomPerson);
        randomUserToken = person_login(randomPerson);
    }

    @AfterClass
    public static void stopApp() throws Exception{

        c_program_version_delete(private_c_program_version);
        c_program_delete(private_c_program);
        type_of_board_delete(typeOfBoard);
        project_delete(project);
        processor_delete(processor);
        producer_delete(producer);
        product_delete(product);
        person_delete(person);
        person_delete(randomPerson);
        person_token_delete(adminToken);
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
    public void create_c_program() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);
        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("type_of_board_id", typeOfBoard.id);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Code.c_program_create().toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .post(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.putNull("id");
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("description").asText());
        expected.put("project_id", project.id);
        expected.put("type_of_board_id", typeOfBoard.id);
        expected.put("type_of_board_id", typeOfBoard.name);
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, CREATED, expected);
    }

    @Test
    public void update_c_program() {

        Model_CProgram c = private_c_program_create(typeOfBoard, project);

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("type_of_board_id", typeOfBoard.id);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Code.c_program_update(c.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", c.id);
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("description").asText());
        expected.put("project_id", project.id);
        expected.putNull("widgets");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, CREATED, expected);
    }
/*
    @Test
    public void get_c_program() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Code.c_program_get(private_c_program.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_c_program_by_filter() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Code.c_program_getByFilter(1).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_c_program() {

        Model_CProgram c = private_c_program_create(typeOfBoard, project);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Code.c_program_delete(c.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void create_c_program_version() {

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.Controller_Code.c_programVersion_create(private_c_program.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_c_program_version() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Code.c_programVersion_get(private_c_program_version.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void edit_c_program_version() {

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Code.c_programVersion_update(private_c_program_version.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_c_program_version() {

        Model_VersionObject v = c_program_version_create(private_c_program);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Code.c_programVersion_delete(v.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void make_c_program_version_public() {

        Model_VersionObject v = c_program_version_create(private_c_program);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Code.c_programVersion_makePublic(v.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void change_approval_state() {

        Model_VersionObject v = c_program_version_create(private_c_program);

        ObjectNode body = Json.newObject();

        body.put("id", v.id);
        body.put("decision", true);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_CompilationLibraries.c_programVersion_changeApprovalState().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_public_c_program() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Code.c_program_getPublicList(1).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }
    */
}
