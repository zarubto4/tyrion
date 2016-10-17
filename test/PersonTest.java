import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.TestCase;
import models.person.FloatingPersonToken;
import models.person.Person;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Http.RequestBuilder;
import org.junit.Test;
import play.mvc.Result;
import play.test.*;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class PersonTest extends WithApplication{

    Logger logger = LoggerFactory.getLogger(TestCase.class);
    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            logger.info("Test {} is running.", method.getName());
        }
        public void succeeded(FrameworkMethod method) {
            logger.info("Test {} successfully run.", method.getName());
        }
        public void failed(Throwable e, FrameworkMethod method) { logger.error("Test {} failed! Reason: {} a.", method.getName(), e.getMessage()); }
    };

    @Test
    public void register_person() {

        ObjectNode body = Json.newObject();

        body.put("mail", "test@mail.com");
        body.put("nick_name", "Test_user_nickname");
        body.put("password", "password123");

        RequestBuilder request = new RequestBuilder()
                .method(POST)
                .uri("/coreClient/person/person")
                .bodyJson(body);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void edit_person_information() {

        String id = Person.find.where().eq("mail", "test@mail.com").findUnique().id;

        ObjectNode body = Json.newObject();

        body.put("nick_name", "Test_user_nickname_change");
        body.put("full_name", "Test Byzance User");

        RequestBuilder request = new RequestBuilder()
                .method(PUT)
                .uri("/coreClient/person/person/" + id)
                .bodyJson(body)
                .header("X-AUTH-TOKEN", FloatingPersonToken.find.where().eq("person.mail", "admin@byzance.cz").findUnique().authToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void get_person() {

        String id = Person.find.where().eq("mail", "test@mail.com").findUnique().id;

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri("/coreClient/person/person/" + id)
                .header("X-AUTH-TOKEN", FloatingPersonToken.find.where().eq("person.mail", "admin@byzance.cz").findUnique().authToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void get_person_all() {

        RequestBuilder request = new RequestBuilder()
                .method(GET)
                .uri("/coreClient/person/person/all")
                .header("X-AUTH-TOKEN", FloatingPersonToken.find.where().eq("person.mail", "admin@byzance.cz").findUnique().authToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }

    @Test
    public void delete_person() {

        String id = Person.find.where().eq("mail", "test@mail.com").findUnique().id;

        RequestBuilder request = new RequestBuilder()
                .method(DELETE)
                .uri("/coreClient/person/person/remove/" + id)
                .header("X-AUTH-TOKEN", FloatingPersonToken.find.where().eq("person.mail", "admin@byzance.cz").findUnique().authToken);

        Result result = route(request);
        assertEquals(OK, result.status());
    }
}