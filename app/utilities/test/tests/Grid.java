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

public class Grid extends TestHelper {

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

/* TYPE OF WIDGET ------------------------------------------------------------------------------------------------------*/

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
        expected.putNull("id");
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("description").asText());
        expected.put("project_id", project.id);
        expected.putNull("widgets");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, CREATED, expected);
    }

    @Test
    public void get_type_of_widget() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.typeOfWidget_get(type_of_widget.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", type_of_widget.id);
        expected.put("name", type_of_widget.name);
        expected.put("description", type_of_widget.description);
        expected.put("project_id", project.id);
        expected.putNull("widgets");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void get_all_type_of_widget() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.typeOfWidget_getAll().toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        checkResponse(response, OK);
    }

    @Test
    public void get_by_filter_type_of_widget() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);
        body.put("private_type", true);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.typeOfWidget_getByFilter(1).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        checkResponse(response, OK);
    }

    @Test
    public void update_type_of_widget() {

        Model_TypeOfWidget t = type_of_widget_create(project);

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.typeOfWidget_update(t.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", t.id);
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("description").asText());
        expected.put("project_id", project.id);
        expected.putNull("widgets");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void delete_type_of_widget() {

        Model_TypeOfWidget t = type_of_widget_create(project);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.typeOfWidget_delete(t.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .delete()
                .get(5000);

        checkResponse(response, OK);
    }

/* GRID WIDGET ---------------------------------------------------------------------------------------------------------*/

    @Test
    public void create_grid_widget() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("type_of_widget_id", type_of_widget.id);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.gridWidget_create().toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .post(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.putNull("id");
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("description").asText());
        expected.put("type_of_widget_id", type_of_widget.id);
        expected.put("type_of_widget_name", type_of_widget.name);
        expected.put("author_id", person.id);
        expected.put("author_nick_name", person.nick_name);
        expected.putNull("versions");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, CREATED, expected);
    }

    @Test
    public void get_grid_widget() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.gridWidget_get(grid_widget.id.toString().toString()).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", grid_widget.id.toString());
        expected.put("name", grid_widget.name);
        expected.put("description",grid_widget.description);
        expected.put("type_of_widget_id", type_of_widget.id);
        expected.put("type_of_widget_name", type_of_widget.name);
        expected.put("author_id", person.id);
        expected.put("author_nick_name", person.nick_name);
        expected.putNull("versions");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void get_by_filter_grid_widget() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.gridWidget_getByFilter(1).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        checkResponse(response, OK);
    }

    @Test
    public void update_grid_widget() {

        Model_GridWidget g = grid_widget_create(type_of_widget);

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("type_of_widget_id", type_of_widget.id);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.gridWidget_update(g.id.toString()).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", g.id.toString());
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("description").asText());
        expected.put("type_of_widget_id", type_of_widget.id);
        expected.put("type_of_widget_name", type_of_widget.name);
        expected.put("author_id", person.id);
        expected.put("author_nick_name", person.nick_name);
        expected.putNull("versions");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void delete_grid_widget() {

        Model_GridWidget g = grid_widget_create(type_of_widget);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.gridWidget_delete(g.id.toString()).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .delete()
                .get(5000);

        checkResponse(response, OK);
    }

/* GRID WIDGET VERSION -------------------------------------------------------------------------------------------------*/

    @Test
    public void create_grid_widget_version() {

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());
        body.put("logic_json", UUID.randomUUID().toString());
        body.put("design_json", UUID.randomUUID().toString());

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.gridWidgetVersion_create(grid_widget.id.toString()).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .post(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", grid_widget.id.toString());
        expected.put("name", grid_widget.name);
        expected.put("description", grid_widget.description);
        expected.put("type_of_widget_id", type_of_widget.id);
        expected.put("type_of_widget_name", type_of_widget.name);
        expected.put("author_id", person.id);
        expected.put("author_nick_name", person.nick_name);
        expected.putNull("versions");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, CREATED, expected);
    }

    @Test
    public void get_grid_widget_version() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.gridWidgetVersion_get(grid_widget_version.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", grid_widget_version.id);
        expected.put("version_name", grid_widget_version.version_name);
        expected.put("version_description", grid_widget_version.version_description);
        expected.set("author", Json.toJson(person.get_short_person()));
        expected.putNull("date_of_create");
        expected.put("design_json", grid_widget_version.design_json);
        expected.put("logic_json", grid_widget_version.logic_json);
        expected.put("create_permission", true);
        expected.put("read_permission", true);
        expected.put("edit_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void get_grid_widget_grid_widget_version() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.gridWidgetVersion_getAll(grid_widget.id.toString()).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        checkResponse(response, OK);
    }

    @Test
    public void make_public_grid_widget_version() {

        Model_GridWidgetVersion v = grid_widget_version_create(grid_widget);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.gridWidgetVersion_makePublic(v.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put("")
                .get(5000);

        checkResponse(response, OK);
    }

    @Test
    public void update_grid_widget_version() {

        Model_GridWidgetVersion v = grid_widget_version_create(grid_widget);

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.gridWidgetVersion_edit(v.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", v.id);
        expected.put("version_name", body.get("version_name").asText());
        expected.put("version_description", body.get("version_description").asText());
        expected.set("author", Json.toJson(person.get_short_person()));
        expected.putNull("date_of_create");
        expected.put("design_json", v.design_json);
        expected.put("logic_json", v.logic_json);
        expected.put("create_permission", true);
        expected.put("read_permission", true);
        expected.put("edit_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void delete_grid_widget_version() {

        Model_GridWidgetVersion v = grid_widget_version_create(grid_widget);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Grid.gridWidgetVersion_delete(v.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .delete()
                .get(5000);

        checkResponse(response, OK);
    }

    // TODO m_program and m_project tests
}