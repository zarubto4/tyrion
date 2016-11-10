import junit.framework.TestCase;
import models.compiler.Processor;
import models.compiler.Producer;
import models.compiler.TypeOfBoard;
import models.compiler.Version_Object;
import models.person.FloatingPersonToken;
import models.person.Invitation;
import models.person.Person;
import models.person.ValidationToken;
import models.project.b_program.servers.Private_Homer_Server;
import models.project.c_program.C_Program;
import models.project.global.Product;
import models.project.global.Project;
import models.project.global.financial.GeneralTariff;
import models.project.global.financial.Payment_Details;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import utilities.enums.Currency;
import utilities.enums.Payment_mode;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class TestHelper extends Controller{

    static Logger logger = LoggerFactory.getLogger(TestCase.class);

    // PERSON ##########################################################################################################

    public static Person person_create(){

        try{

            Person person = new Person();

            person.nick_name = UUID.randomUUID().toString();
            person.mail = UUID.randomUUID().toString() + "@mail.com";
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
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static void person_token_delete(String token){
        try {

            FloatingPersonToken.find.where().eq("authToken", token).findUnique().delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // PRODUCT #########################################################################################################

    public static Product product_create(Person person){
        try {

            Product product = new Product();
            product.general_tariff = GeneralTariff.find.byId("2");
            product.product_individual_name = UUID.randomUUID().toString();
            product.active = true;
            product.mode = Payment_mode.free;
            product.paid_until_the_day = new GregorianCalendar(2016, 12, 30).getTime();
            product.currency = Currency.CZK;

            product.save();
            product.refresh();

            Payment_Details payment_details = new Payment_Details();
            payment_details.person = person;
            payment_details.company_account = false;

            payment_details.street = UUID.randomUUID().toString();
            payment_details.street_number = UUID.randomUUID().toString();
            payment_details.city =UUID.randomUUID().toString();
            payment_details.zip_code = UUID.randomUUID().toString();
            payment_details.country = UUID.randomUUID().toString();
            payment_details.product = product;

            payment_details.save();
            payment_details.refresh();

            product.update();
            product.refresh();

            return product;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void product_delete(Product product){
        try {

            product.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // PROJECT #########################################################################################################

    public static Project project_create(Product product){
        try {

            Project project  = new Project();
            project.name = UUID.randomUUID().toString();
            project.description = UUID.randomUUID().toString();
            project.product = product;
            project.ownersOfProject.add(product.payment_details.person);

            project.save();
            project.refresh();

            return project;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void project_delete(Project project){
        try {

            project.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Invitation project_share(Project project, Person owner, Person invited_person){
        try {

            Invitation invitation = Invitation.find.where().eq("mail", invited_person.mail).findUnique();
            if(invitation == null) {
                invitation = new Invitation();
                invitation.mail = invited_person.mail;
                invitation.date_of_creation = new Date();
                invitation.owner = owner;
                invitation.project = project;

                invitation.save();
                invitation.refresh();

                project.invitations.add(invitation);
            }

            return invitation;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void project_add_participant(Project project, Person person){
        try {

            project.ownersOfProject.add(person);
            project.update();

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // HOMER ###########################################################################################################

    public static Private_Homer_Server homer_create(Project project){
        try {

            Private_Homer_Server privateHomerServer = new Private_Homer_Server();
            privateHomerServer.mac_address = UUID.randomUUID().toString();
            privateHomerServer.type_of_device = UUID.randomUUID().toString();
            privateHomerServer.project = project;

            privateHomerServer.save();
            privateHomerServer.refresh();

            return privateHomerServer;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void homer_delete(Private_Homer_Server homer){
        try {

            homer.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // PRODUCER ########################################################################################################

    public static Producer producer_create(){
        try {

            Producer producer = new Producer();
            producer.name = UUID.randomUUID().toString();
            producer.description = UUID.randomUUID().toString();

            producer.save();
            producer.refresh();

            return producer;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void producer_delete(Producer producer){
        try {

            producer.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // PROCESSOR #######################################################################################################

    public static Processor processor_create(){
        try {

            Processor processor = new Processor();
            processor.description    = UUID.randomUUID().toString();
            processor.processor_code = UUID.randomUUID().toString();
            processor.processor_name = UUID.randomUUID().toString();
            processor.speed          = 3000;

            processor.save();
            processor.refresh();

            return processor;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void processor_delete(Processor processor){
        try {

            processor.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // TYPE_OF_BOARD ###################################################################################################

    public static TypeOfBoard type_of_board_create(Producer producer, Processor processor){
        try {

            TypeOfBoard typeOfBoard = new TypeOfBoard();
            typeOfBoard.name = UUID.randomUUID().toString();
            typeOfBoard.description = UUID.randomUUID().toString();
            typeOfBoard.compiler_target_name = UUID.randomUUID().toString();
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;
            typeOfBoard.connectible_to_internet = true;

            typeOfBoard.save();
            typeOfBoard.refresh();

            C_Program default_program = new C_Program();
            default_program.name = UUID.randomUUID().toString();
            default_program.description = UUID.randomUUID().toString();
            default_program.default_program_type_of_board = typeOfBoard;

            default_program.save();
            default_program.refresh();

            Version_Object default_version = new Version_Object();
            default_version.version_name = UUID.randomUUID().toString();
            default_version.version_description = UUID.randomUUID().toString();
            default_version.default_version_program = default_program;

            default_version.save();
            default_program.refresh();
            typeOfBoard.refresh();

            return typeOfBoard;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void type_of_board_delete(TypeOfBoard typeOfBoard){
        try {

            typeOfBoard.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // BOARD ###########################################################################################################

    public static void method7(){
        try {



        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static void method8(){
        try {



        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }
}
