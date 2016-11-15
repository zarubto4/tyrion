import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.TestCase;
import models.compiler.Board;
import models.compiler.Processor;
import models.compiler.Producer;
import models.compiler.TypeOfBoard;
import models.person.Person;
import models.project.c_program.C_Program;
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

import java.util.UUID;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class CompilationLibrariesTest extends TestHelper {

    public static FakeApplication app;

    public static Product product;
    public static Project project;
    public static Producer producer;
    public static Processor processor;
    public static TypeOfBoard typeOfBoard;
    public static Board board;
    public static C_Program c_program;

    public static String adminToken;

    public static Person person;
    public static String userToken;

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
        producer = producer_create();
        processor = processor_create();
        typeOfBoard = type_of_board_create(producer, processor);

        randomPerson = person_create();
        person_authenticate(randomPerson);
        randomUserToken = person_login(randomPerson);
    }

    @AfterClass
    public static void stopApp() throws Exception{

        type_of_board_delete(typeOfBoard);
        project_delete(project);
        processor_delete(processor);
        producer_delete(producer);
        product_delete(product);
        person_delete(person);
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
    public void create_c_program() {

        ObjectNode body = Json.newObject();

        body.put("project_id", project.id);
        body.put("name", UUID.randomUUID().toString());
        body.put("description", UUID.randomUUID().toString());
        body.put("type_of_board_id", typeOfBoard.id);

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.CompilationLibrariesController.create_C_Program().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);

        assertEquals(CREATED, result.status());
    }
}
