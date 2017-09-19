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
import play.test.FakeApplication;
import play.test.Helpers;
import utilities.Server;

import javax.inject.Inject;
import java.util.UUID;

public class BlockoTest extends TestHelper{

    @Inject
    WSClient ws;

    public static FakeApplication app;

    public static Model_Product product;
    public static Model_Project project;

    public static Model_TypeOfBlock type_of_block;
    public static Model_BlockoBlock blocko_block;
    public static Model_BlockoBlockVersion blocko_block_version;

    public static Model_BProgram b_program;
    public static Model_VersionObject b_program_version;

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

        type_of_block = type_of_block_create(project);
        blocko_block = blocko_block_create(type_of_block);
        blocko_block_version = blocko_block_version_create(blocko_block);

        b_program = b_program_create(project);
        b_program_version = b_program_version_create(b_program);

        secondPerson = person_create();
        person_authenticate(secondPerson);

        randomPerson = person_create();
        person_authenticate(randomPerson);
        randomUserToken = person_login(randomPerson);
    }

    @AfterClass
    public static void stopApp() throws Exception{
        b_program_version_delete(b_program_version);
        b_program_delete(b_program);
        blocko_block_version_delete(blocko_block_version);
        blocko_block_delete(blocko_block);
        type_of_block_delete(type_of_block);
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
            logger.debug("Test \t<{}>\t is running.", description.getMethodName());
        }
        public void succeeded(Description description) {
            logger.info("Test: \t<{}>\t completed successfully.", description.getMethodName());
        }
        public void failed(Throwable e, Description description) {
            logger.error("Test \t<{}>\t failed! Reason: {}", description.getMethodName(), e.getMessage());
        }
    };

/* TYPE OF BLOCK -------------------------------------------------------------------------------------------------------*/

    @Test
    public void create_type_of_block() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("project_id", project.id);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.typeOfBlock_create().toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .post(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", "id can have any value");
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("description").asText());
        expected.put("project_id", project.id);
        expected.putNull("blocks");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, CREATED, expected);
    }

    @Test
    public void get_type_of_block() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.typeOfBlock_get(type_of_block.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", type_of_block.id);
        expected.put("name", type_of_block.name);
        expected.put("description", type_of_block.description);
        expected.put("project_id", type_of_block.project_id());
        expected.putNull("blocks");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void get_all_type_of_block() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.typeOfBlock_getByFilter(1).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        checkResponse(response, OK, null);
    }

    @Test
    public void get_by_filter_type_of_block() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);
        body.put("private_type", true);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.typeOfBlock_getByFilter(1).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        checkResponse(response, OK, null);
    }

    @Test
    public void update_type_of_block() {

        Model_TypeOfBlock typeOfBlock = type_of_block_create(project);

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.typeOfBlock_edit(typeOfBlock.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", "id can have any value");
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("description").asText());
        expected.put("project_id", project.id);
        expected.putNull("blocks");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        type_of_block_delete(typeOfBlock);

        checkResponse(response, OK, expected);
    }

    @Test
    public void delete_type_of_block() {

        Model_TypeOfBlock t = type_of_block_create(project);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.typeOfBlock_delete(t.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .delete()
                .get(5000);

        checkResponse(response, OK, null);
    }

/* BLOCKO BLOCK --------------------------------------------------------------------------------------------------------*/

    @Test
    public void create_blocko_block() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("general_description", UUID.randomUUID().toString());
        body.put("type_of_block_id", type_of_block.id);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.blockoBlock_create().toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .post(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", "id can have any value");
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("general_description").asText());
        expected.put("type_of_block_id", type_of_block.id);
        expected.put("type_of_block_name", type_of_block.name);
        expected.put("author_id", person.id);
        expected.put("author_nick_name", person.nick_name);
        expected.putNull("versions");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, CREATED, expected);
    }

    @Test
    public void get_blocko_block() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.blockoBlock_get(blocko_block.id.toString()).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", blocko_block.id.toString());
        expected.put("name", blocko_block.name);
        expected.put("description", blocko_block.description);
        expected.put("type_of_block_id", type_of_block.id);
        expected.put("type_of_block_name", type_of_block.name);
        expected.put("author_id", person.id);
        expected.put("author_nick_name", person.nick_name);
        expected.putNull("versions");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void get_by_filter_blocko_block() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.blockoBlock_getByFilter(1).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        checkResponse(response, OK, null);
    }

    @Test
    public void update_blocko_block() {

        Model_BlockoBlock b = blocko_block_create(type_of_block);

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("general_description", UUID.randomUUID().toString());
        body.put("type_of_block_id", type_of_block.id);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.blockoBlock_update(b.id.toString()).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", b.id.toString());
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("general_description").asText());
        expected.put("type_of_block_id", type_of_block.id);
        expected.put("type_of_block_name", type_of_block.name);
        expected.put("author_id", person.id);
        expected.put("author_nick_name", person.nick_name);
        expected.putNull("versions");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, null);
    }

    @Test
    public void delete_blocko_block() {

        Model_BlockoBlock b = blocko_block_create(type_of_block);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.blockoBlock_delete(b.id.toString()).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .delete()
                .get(5000);

        checkResponse(response, OK, null);
    }

/* BLOCKO BLOCK VERSION ------------------------------------------------------------------------------------------------*/

    @Test
    public void create_blocko_block_version() {

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());
        body.put("logic_json", UUID.randomUUID().toString());
        body.put("design_json", UUID.randomUUID().toString());

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.blockoBlockVersion_create(blocko_block.id.toString()).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .post(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", blocko_block.id.toString());
        expected.put("name", blocko_block.name);
        expected.put("description", blocko_block.description);
        expected.put("type_of_block_id", type_of_block.id);
        expected.put("type_of_block_name", type_of_block.name);
        expected.put("author_id", person.id);
        expected.put("author_nick_name", person.nick_name);
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, CREATED, expected);
    }

    @Test
    public void get_blocko_block_version() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.blockoBlockVersion_get(blocko_block_version.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", blocko_block_version.id);
        expected.put("version_name", blocko_block_version.version_name);
        expected.put("version_description", blocko_block_version.version_description);
        expected.set("author", Json.toJson(person.get_short_person()));
        expected.putNull("date_of_create");
        expected.put("design_json", blocko_block_version.design_json);
        expected.put("logic_json", blocko_block_version.logic_json);
        expected.put("create_permission", true);
        expected.put("read_permission", true);
        expected.put("edit_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void get_blocko_block_blocko_block_version() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.typeOfBlock_getByFilter(1).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        checkResponse(response, OK, null);
    }

    @Test
    public void make_public_blocko_block_version() {

        Model_BlockoBlockVersion b = blocko_block_version_create(blocko_block);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.blockoBlockVersion_makePublic(b.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put("")
                .get(5000);

        checkResponse(response, OK, null); // TODO expected
    }

    @Test
    public void update_blocko_block_version() {

        Model_BlockoBlockVersion v = blocko_block_version_create(blocko_block);

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.blockoBlockVersion_update(v.id).toString())
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
    public void delete_blocko_block_version() {

        Model_BlockoBlockVersion b = blocko_block_version_create(blocko_block);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.blockoBlockVersion_delete(b.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .delete()
                .get(5000);

        checkResponse(response, OK, null);
    }

/* B PROGRAM -----------------------------------------------------------------------------------------------------------*/

    @Test
    public void create_b_program() {

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.bProgram_create(project.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .post(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", "id can have any value");
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("description").asText());
        expected.put("project_id", project.id);
        //expected.put("instance_details", "TODO");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, CREATED, expected);
    }

    @Test
    public void get_b_program() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.bProgram_get(b_program.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", b_program.id);
        expected.put("name", b_program.name);
        expected.put("description", b_program.description);
        expected.put("project_id", project.id);
        //expected.put("instance_details", "TODO");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void get_by_filter_b_program() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.bProgram_getByFilter(1).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put("")
                .get(5000);

        checkResponse(response, OK, null);
    }

    @Test
    public void update_b_program() {

        Model_BProgram b = b_program_create(project);

        ObjectNode body = Json.newObject();

        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.bProgram_update(b.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        ObjectNode expected = Json.newObject();
        expected.put("id", "id can have any value");
        expected.put("name", body.get("name").asText());
        expected.put("description", body.get("description").asText());
        expected.put("project_id", project.id);
        //expected.put("instance_details", "TODO");
        expected.put("edit_permission", true);
        expected.put("update_permission", true);
        expected.put("delete_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void delete_b_program() {

        Model_BProgram b = b_program_create(project);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.bProgram_delete(b.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .delete()
                .get(5000);

        checkResponse(response, OK, null);
    }

/* B PROGRAM -----------------------------------------------------------------------------------------------------------*/

    @Test
    public void create_b_program_version() {

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());
        body.put("program", UUID.randomUUID().toString());

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.bProgramVersion_create(b_program.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .post(body)
                .get(5000);

        ObjectNode version_object = Json.newObject();
        version_object.put("id", "id can have any value");
        version_object.put("version_name", body.get("version_name").asText());
        version_object.put("version_description", body.get("version_description").asText());
        version_object.putNull("date_of_create");

        ObjectNode expected = Json.newObject();
        expected.set("version_object", Json.toJson(version_object));
        expected.put("program", body.get("program").asText());
        expected.put("edit_permission", true);
        expected.put("remove_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void get_b_program_version() {

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.bProgramVersion_get(b_program_version.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .get()
                .get(5000);

        ObjectNode version_object = Json.newObject();
        version_object.put("id", b_program_version.id);
        version_object.put("version_name", b_program_version.version_name);
        version_object.put("version_description", b_program_version.version_description);
        version_object.putNull("date_of_create");

        ObjectNode expected = Json.newObject();
        expected.set("version_object", Json.toJson(version_object));
        expected.putNull("program");
        expected.put("edit_permission", true);
        expected.put("remove_permission", true);

        checkResponse(response, OK, expected);
    }

    @Test
    public void update_b_program_version() {

        Model_VersionObject v = b_program_version_create(b_program);

        ObjectNode body = Json.newObject();

        body.put("version_name", UUID.randomUUID().toString());
        body.put("version_description", UUID.randomUUID().toString());

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.bProgramVersion_update(v.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .put(body)
                .get(5000);

        checkResponse(response, OK, null);
    }

    @Test
    public void delete_b_program_version() {

        Model_VersionObject v = b_program_version_create(b_program);

        WSResponse response = Play.current().injector().instanceOf(WSClient.class)
                .url(Server.tyrion_serverAddress + routes.Controller_Blocko.bProgramVersion_delete(v.id).toString())
                .setHeader("X-AUTH-TOKEN", userToken)
                .delete()
                .get(5000);

        checkResponse(response, OK, null);
    }
}