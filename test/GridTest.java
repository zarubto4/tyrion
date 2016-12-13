import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.routes;
import junit.framework.TestCase;
import models.grid.GridWidget;
import models.grid.GridWidgetVersion;
import models.grid.TypeOfWidget;
import models.person.Person;
import models.project.global.Product;
import models.project.global.Project;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import play.mvc.Http.RequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.*;
import static play.test.Helpers.route;

public class GridTest extends TestHelper{

    public static FakeApplication app;

    public static Product product;
    public static Project project;

    public static TypeOfWidget type_of_widget;
    public static GridWidget grid_widget;
    public static GridWidgetVersion grid_widget_version;

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
    public void create_type_of_widget() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("project_id", project.id);

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.GridController.typeOfWidget_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_type_of_widget() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.GridController.typeOfWidget_get(type_of_widget.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_all_type_of_widget() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.GridController.typeOfWidget_getAll().toString())
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
                .uri(routes.GridController.typeOfWidget_getByFilter(1).toString())
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
                .uri(routes.GridController.typeOfWidget_update(type_of_widget.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_type_of_widget() {

        TypeOfWidget t = type_of_widget_create(project);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.GridController.typeOfWidget_delete(t.id).toString())
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
                .uri(routes.GridController.gridWidget_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_grid_widget() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.GridController.gridWidget_get(grid_widget.id).toString())
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
                .uri(routes.GridController.gridWidget_getByFilter(1).toString())
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
                .uri(routes.GridController.gridWidget_update(grid_widget.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_grid_widget() {

        GridWidget b = grid_widget_create(type_of_widget);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.GridController.gridWidget_delete(b.id).toString())
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
                .uri(routes.GridController.gridWidgetVersion_create(grid_widget.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void get_grid_widget_version() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.GridController.gridWidgetVersion_get(grid_widget_version.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void get_grid_widget_grid_widget_version() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.GridController.gridWidgetVersion_getAll(grid_widget.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void make_public_grid_widget_version() {

        GridWidgetVersion b = grid_widget_version_create(grid_widget);

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.GridController.gridWidgetVersion_makePublic(b.id).toString())
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
                .uri(routes.GridController.gridWidgetVersion_update(grid_widget_version.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }

    @Test
    public void delete_grid_widget_version() {

        GridWidgetVersion b = grid_widget_version_create(grid_widget);

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri(routes.GridController.gridWidgetVersion_delete(b.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(OK, result.status());
    }
}
