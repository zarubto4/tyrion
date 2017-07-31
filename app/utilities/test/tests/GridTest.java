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

public class GridTest extends TestHelper {

    public static Model_Product product;
    public static Model_Project project;

    public static Model_TypeOfWidget type_of_widget;
    public static Model_GridWidget grid_widget;
    public static Model_GridWidgetVersion grid_widget_version;

    public static String adminToken;

    public static Model_Person person;
    public static String userToken;

    public static Model_Person secondPerson;

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

        type_of_widget = type_of_widget_create(project);
        grid_widget = grid_widget_create(type_of_widget);
        grid_widget_version = grid_widget_version_create(grid_widget);

        secondPerson = person_create();
        person_authenticate(secondPerson);

        randomPerson = person_create();
        person_authenticate(randomPerson);
        randomUserToken = person_login(randomPerson);
    }

    @AfterClass
    public static void stopApp() throws Exception{

        grid_widget_version_delete(grid_widget_version);
        grid_widget_delete(grid_widget);
        type_of_widget_delete(type_of_widget);
        project_delete(project);
        product_delete(product);
        person_delete(person);
        person_delete(secondPerson);
        person_delete(randomPerson);
        person_token_delete(adminToken);
    }

    Logger logger = LoggerFactory.getLogger(TestCase.class);

    @Rule
    public TestRule watchman = new TestWatcher() {
        public void starting(Description description) {
            logger.debug("Test \t<{}>\t is running.", description.getMethodName());
        }
        public void succeeded(Description description) {
            logger.info("Test: \t<{}>\t completed successfully.", description.getMethodName());
        }
        public void failed(Throwable e, Description description) {
            logger.error("Test \t<{}>\t failed! Reason: {}", description.getMethodName(), e.getMessage());
        }
    };

    @Test
    public void create_type_of_widget() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("project_id", project.id);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.typeOfWidget_create().toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .post(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", "id can have any value");
        expected.put("name", body.get("name").asText() + "ggg");
        expected.put("description", body.get("description").asText());
        expected.put("project_id", project.id);
        expected.putNull("blocks");
        expected.put("edit_permission", false);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, CREATED, expected);
    }
/*
    @Test
    public void get_type_of_widget() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Grid.typeOfWidget_get(type_of_widget.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_all_type_of_widget() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Grid.typeOfWidget_getAll().toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_by_filter_type_of_widget() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);
        body.put("private_type", true);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Grid.typeOfWidget_getByFilter(1).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void update_type_of_widget() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Grid.typeOfWidget_update(type_of_widget.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_type_of_widget() {

        Model_TypeOfWidget t = type_of_widget_create(project);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Grid.typeOfWidget_delete(t.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void create_grid_widget() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("type_of_widget_id", type_of_widget.id);

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.Controller_Grid.gridWidget_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_grid_widget() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Grid.gridWidget_get(grid_widget.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_by_filter_grid_widget() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Grid.gridWidget_getByFilter(1).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void update_grid_widget() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("type_of_widget_id", type_of_widget.id);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Grid.gridWidget_update(grid_widget.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_grid_widget() {

        Model_GridWidget b = grid_widget_create(type_of_widget);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Grid.gridWidget_delete(b.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void create_grid_widget_version() {

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());
        body.put("logic_json", UUID.randomUUID().toString());
        body.put("design_json", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.Controller_Grid.gridWidgetVersion_create(grid_widget.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_grid_widget_version() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Grid.gridWidgetVersion_get(grid_widget_version.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_grid_widget_grid_widget_version() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Grid.gridWidgetVersion_getAll(grid_widget.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void make_public_grid_widget_version() {

        Model_GridWidgetVersion b = grid_widget_version_create(grid_widget);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Grid.gridWidgetVersion_makePublic(b.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void update_grid_widget_version() {

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Grid.gridWidgetVersion_update(grid_widget_version.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_grid_widget_version() {

        Model_GridWidgetVersion b = grid_widget_version_create(grid_widget);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.Controller_Grid.gridWidgetVersion_delete(b.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }*/
}
