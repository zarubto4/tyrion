import junit.framework.TestCase;
import models.person.FloatingPersonToken;
import models.person.Person;
import models.person.ValidationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TestHelper {

    static Logger logger = LoggerFactory.getLogger(TestCase.class);

    public static Person person_create(){

        try{

            Person person = new Person();

            person.nick_name = UUID.randomUUID().toString();
            person.mail = UUID.randomUUID().toString();
            person.mailValidated = false;
            person.full_name = UUID.randomUUID().toString();

            person.setSha("password123");
            person.save();
            person.refresh();

            new ValidationToken().setValidation(person.mail);

            return person;
        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
            return null;
        }
    }


    public static void person_authenticate(Person person) {

        try {

            person.mailValidated = true;
            person.update();

        }catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
        }
    }

    public static String person_login(Person person) {

        try {

            FloatingPersonToken floatingPersonToken = new FloatingPersonToken();
            floatingPersonToken.set_basic_values();
            floatingPersonToken.person = person;
            floatingPersonToken.user_agent = "Unknown browser";
            floatingPersonToken.save();

            return floatingPersonToken.authToken;
        }catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
            return null;
        }
    }

    public static void person_delete(Person person){
        try {

            person.delete();

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }
}
