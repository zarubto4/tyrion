import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.TestCase;
import models.person.FloatingPersonToken;
import models.person.Person;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;


import java.util.UUID;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class FinanceTest extends TestHelper{

    public static FakeApplication app;

    public static String adminToken;

    public static Person person;
    public static String userToken;

    public static Person randomPerson;
    public static String randomUserToken;

    @BeforeClass
    public static void startApp() throws Exception{

        app = Helpers.fakeApplication();
        Helpers.start(app);

        adminToken = FloatingPersonToken.find.where().eq("person.mail", "admin@byzance.cz").findList().get(0).authToken;

        person = person_create();
        person_authenticate(person);
        userToken = person_login(person);

        randomPerson = person_create();
        person_authenticate(randomPerson);
        randomUserToken = person_login(randomPerson);
    }

    @AfterClass
    public static void stopApp() throws Exception{
        person_delete(person);
        person_delete(randomPerson);
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
            logger.error("Test {} failed! Reason: {} a.", description.getMethodName(), e.getMessage());
        }
    };

    @Test
    public void tariff_general_create() {

        ObjectNode body = Json.newObject();

        body.put("tariff_name", UUID.randomUUID().toString());
        body.put("identificator", UUID.randomUUID().toString());
        body.put("color", "blue");
        body.put("number_of_free_months", 3);
        body.put("cze", 3);
        body.put("usd", 3);
        body.put("eur", 3);
        body.put("company_details_required", false);
        body.put("required_payment_mode", false);
        body.put("required_payment_method", false);
        body.put("credit_card_support", true);
        body.put("bank_transfer_support", true);
        body.put("mode_annually", true);
        body.put("mode_credit", true);
        body.put("free", true);

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri("/admin/tariff")
                .bodyJson(body)
                .header("X-AUTH-TOKEN", adminToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }
}
