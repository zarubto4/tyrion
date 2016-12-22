import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.routes;
import junit.framework.TestCase;
import models.person.Model_Person;
import models.project.global.Model_Product;
import models.project.global.financial.Model_GeneralTariff;
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

    public static Model_GeneralTariff tariff_personal;
    public static Model_GeneralTariff tariff_company;

    public static Model_Product product;

    public static String adminToken;

    public static Model_Person person;
    public static String userToken;

    public static Model_Person randomPerson;
    public static String randomUserToken;

    @BeforeClass
    public static void startApp() throws Exception{

        app = Helpers.fakeApplication();
        Helpers.start(app);

        adminToken = person_login(Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique());

        tariff_personal = Model_GeneralTariff.find.where().eq("identificator", "geek").findUnique();
        tariff_company = Model_GeneralTariff.find.where().eq("identificator", "business_1").findUnique();

        person = person_create();
        person_authenticate(person);
        userToken = person_login(person);

        product = product_create(person);

        randomPerson = person_create();
        person_authenticate(randomPerson);
        randomUserToken = person_login(randomPerson);
    }

    @AfterClass
    public static void stopApp() throws Exception{
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
    public void get_products_tariffs() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Finance.get_products_tariffs().toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void get_applicable_products_for_creating_new_project() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Finance.get_applicable_products_for_creating_new_project().toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void personal_product_create() {

        ObjectNode body = Json.newObject();

        body.put("tariff_id", tariff_personal.id);
        body.put("product_individual_name", UUID.randomUUID().toString());
        body.put("currency_type", "CZK");
        body.put("payment_mode", "monthly");
        body.put("city", UUID.randomUUID().toString());
        body.put("country", UUID.randomUUID().toString());
        body.put("street_number", UUID.randomUUID().toString());
        body.put("street", UUID.randomUUID().toString());
        body.put("zip_code", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.Controller_Finance.product_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(CREATED, result.status());
    }

    @Test
    public void company_product_create() {

        ObjectNode body = Json.newObject();

        body.put("tariff_id", tariff_company.id);
        body.put("product_individual_name", UUID.randomUUID().toString());
        body.put("currency_type", "CZK");
        body.put("city", UUID.randomUUID().toString());
        body.put("country", UUID.randomUUID().toString());
        body.put("street_number", UUID.randomUUID().toString());
        body.put("street", UUID.randomUUID().toString());
        body.put("zip_code", UUID.randomUUID().toString());

        body.put("payment_mode", "monthly");
        body.put("payment_method", "credit_card");

        body.put("registration_no", UUID.randomUUID().toString());
        body.put("vat_number", "CZ" + UUID.randomUUID().toString());
        body.put("company_name", UUID.randomUUID().toString());
        body.put("company_authorized_email", UUID.randomUUID().toString());
        body.put("company_authorized_phone", UUID.randomUUID().toString());
        body.put("company_web", UUID.randomUUID().toString());
        body.put("company_invoice_email", UUID.randomUUID().toString() + "@mail.com");

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri(routes.Controller_Finance.product_create().toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(CREATED, result.status());
    }

    @Test
    public void edit_payment_details() {

        ObjectNode body = Json.newObject();

        body.put("city", UUID.randomUUID().toString());
        body.put("country", UUID.randomUUID().toString());
        body.put("street_number", UUID.randomUUID().toString());
        body.put("street", UUID.randomUUID().toString());
        body.put("zip_code", UUID.randomUUID().toString());

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Finance.edit_payment_details(product.payment_details.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void edit_general_product_details() {

        ObjectNode body = Json.newObject();

        body.put("product_individual_name", UUID.randomUUID().toString());
        body.put("currency_type", "EUR");

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Finance.edit_general_product_details(product.id).toString())
                .bodyJson(body)
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }


    @Test
    public void deactivate_product() {

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri(routes.Controller_Finance.product_deactivate(product.id).toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void product_get_all() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri(routes.Controller_Finance.product_get_all().toString())
                .header("X-AUTH-TOKEN", userToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }
}
