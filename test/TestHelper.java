import junit.framework.TestCase;
import models.blocko.Model_BlockoBlock;
import models.blocko.Model_BlockoBlockVersion;
import models.blocko.Model_TypeOfBlock;
import models.compiler.Model_Processor;
import models.compiler.Model_Producer;
import models.compiler.Model_TypeOfBoard;
import models.compiler.Model_VersionObject;
import models.grid.Model_GridWidget;
import models.grid.Model_GridWidgetVersion;
import models.grid.Model_TypeOfWidget;
import models.person.Model_FloatingPersonToken;
import models.person.Model_Invitation;
import models.person.Model_Person;
import models.person.Model_ValidationToken;
import models.project.c_program.Model_CProgram;
import models.project.global.Model_Product;
import models.project.global.Model_Project;
import models.project.global.Model_ProjectParticipant;
import models.project.global.financial.Model_GeneralTariff;
import models.project.global.financial.Model_PaymentDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import utilities.enums.Participant_status;
import utilities.enums.Payment_mode;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class TestHelper extends Controller{

    static Logger logger = LoggerFactory.getLogger(TestCase.class);

    // PERSON ##########################################################################################################

    public static Model_Person person_create(){

        try{

            Model_Person person = new Model_Person();

            person.nick_name = UUID.randomUUID().toString();
            person.mail = UUID.randomUUID().toString() + "@mail.com";
            person.mailValidated = false;
            person.full_name = UUID.randomUUID().toString();

            person.setSha("password123");
            person.save();
            person.refresh();

            new Model_ValidationToken().setValidation(person.mail);

            return person;
        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
            return null;
        }
    }

    public static void person_authenticate(Model_Person person) {

        try {

            person.mailValidated = true;
            person.update();

        }catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
        }
    }

    public static String person_login(Model_Person person) {

        try {

            Model_FloatingPersonToken floatingPersonToken = new Model_FloatingPersonToken();
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

    public static void person_delete(Model_Person person){
        try {

            person.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static void person_token_delete(String token){
        try {

            Model_FloatingPersonToken.find.where().eq("authToken", token).findUnique().delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // PRODUCT #########################################################################################################

    public static Model_Product product_create(Model_Person person){
        try {

            Model_Product product = new Model_Product();
            product.general_tariff = Model_GeneralTariff.find.where().eq("identificator", "geek").findUnique();
            product.product_individual_name = UUID.randomUUID().toString();
            product.active = true;
            product.mode = Payment_mode.free;
            product.paid_until_the_day = new GregorianCalendar(2016, 12, 30).getTime();

            product.save();
            product.refresh();

            Model_PaymentDetails payment_details = new Model_PaymentDetails();
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

    public static void product_delete(Model_Product product){
        try {

            product.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // PROJECT #########################################################################################################

    public static Model_Project project_create(Model_Product product){
        try {

            Model_Project project  = new Model_Project();
            project.name = UUID.randomUUID().toString();
            project.description = UUID.randomUUID().toString();
            project.product = product;

            project.save();
            project.refresh();

            Model_ProjectParticipant participant = new Model_ProjectParticipant();
            participant.person = product.payment_details.person;
            participant.project = project;
            participant.state = Participant_status.owner;

            participant.save();

            project.refresh();

            return project;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void project_delete(Model_Project project){
        try {

            project.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_Invitation project_share(Model_Project project, Model_Person owner, Model_Person invited_person){
        try {

            Model_Invitation invitation = Model_Invitation.find.where().eq("mail", invited_person.mail).findUnique();
            if(invitation == null) {
                invitation = new Model_Invitation();
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

    public static void project_add_participant(Model_Project project, Model_Person person){
        try {

            Model_ProjectParticipant participant = new Model_ProjectParticipant();
            participant.person = person;
            participant.project = project;
            participant.state = Participant_status.member;

            participant.save();
            project.refresh();

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // HOMER ###########################################################################################################
/*
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
*/
    // PRODUCER ########################################################################################################

    public static Model_Producer producer_create(){
        try {

            Model_Producer producer = new Model_Producer();
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

    public static void producer_delete(Model_Producer producer){
        try {

            producer.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // PROCESSOR #######################################################################################################

    public static Model_Processor processor_create(){
        try {

            Model_Processor processor = new Model_Processor();
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

    public static void processor_delete(Model_Processor processor){
        try {

            processor.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // TYPE_OF_BOARD ###################################################################################################

    public static Model_TypeOfBoard type_of_board_create(Model_Producer producer, Model_Processor processor){
        try {

            Model_TypeOfBoard typeOfBoard = new Model_TypeOfBoard();
            typeOfBoard.name = UUID.randomUUID().toString();
            typeOfBoard.description = UUID.randomUUID().toString();
            typeOfBoard.compiler_target_name = UUID.randomUUID().toString();
            typeOfBoard.processor = processor;
            typeOfBoard.producer = producer;
            typeOfBoard.connectible_to_internet = true;

            typeOfBoard.save();
            typeOfBoard.refresh();

            Model_CProgram default_program = new Model_CProgram();
            default_program.name = UUID.randomUUID().toString();
            default_program.description = UUID.randomUUID().toString();
            default_program.default_program_type_of_board = typeOfBoard;

            default_program.save();
            default_program.refresh();

            Model_VersionObject default_version = new Model_VersionObject();
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

    public static void type_of_board_delete(Model_TypeOfBoard typeOfBoard){
        try {

            typeOfBoard.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // BOARD ###########################################################################################################

    // C_PROGRAM #######################################################################################################

    public static Model_CProgram private_c_program_create(Model_TypeOfBoard typeOfBoard, Model_Project project){
        try {

            Model_CProgram c_program             = new Model_CProgram();
            c_program.name                  = UUID.randomUUID().toString();
            c_program.description           = UUID.randomUUID().toString();
            c_program.date_of_create        = new Date();
            c_program.type_of_board         = typeOfBoard;
            c_program.project               = project;

            c_program.save();
            c_program.refresh();

            return c_program;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void c_program_delete(Model_CProgram c_program){
        try {

            c_program.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_VersionObject c_program_version_create(Model_CProgram c_program){
        try {

            Model_VersionObject version_object      = new Model_VersionObject();
            version_object.version_name        = UUID.randomUUID().toString();
            version_object.version_description = UUID.randomUUID().toString();
            version_object.author              = c_program.project.product.payment_details.person;
            version_object.date_of_create      = new Date();
            version_object.c_program           = c_program;
            version_object.public_version      = false;

            version_object.save();
            version_object.refresh();

            return version_object;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void c_program_version_delete(Model_VersionObject version_object){
        try {

            version_object.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // BLOCKO ##########################################################################################################

    public static Model_TypeOfBlock type_of_block_create(Model_Project project){
        try {

            Model_TypeOfBlock typeOfBlock = new Model_TypeOfBlock();
            typeOfBlock.name = UUID.randomUUID().toString();
            typeOfBlock.description = UUID.randomUUID().toString();
            typeOfBlock.project = project;

            typeOfBlock.save();
            typeOfBlock.refresh();

            return typeOfBlock;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void type_of_block_delete(Model_TypeOfBlock typeOfBlock){
        try {

            typeOfBlock.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_BlockoBlock blocko_block_create(Model_TypeOfBlock typeOfBlock){
        try {

            Model_BlockoBlock blockoBlock = new Model_BlockoBlock();
            blockoBlock.name = UUID.randomUUID().toString();
            blockoBlock.description = UUID.randomUUID().toString();
            blockoBlock.type_of_block = typeOfBlock;

            blockoBlock.save();
            blockoBlock.refresh();

            return blockoBlock;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void blocko_block_delete(Model_BlockoBlock blockoBlock){
        try {

            blockoBlock.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_BlockoBlockVersion blocko_block_version_create(Model_BlockoBlock blockoBlock){
        try {

            Model_BlockoBlockVersion blockoBlockVersion = new Model_BlockoBlockVersion();
            blockoBlockVersion.version_name = UUID.randomUUID().toString();
            blockoBlockVersion.version_description = UUID.randomUUID().toString();
            blockoBlockVersion.design_json = UUID.randomUUID().toString();
            blockoBlockVersion.logic_json = UUID.randomUUID().toString();
            blockoBlockVersion.blocko_block = blockoBlock;

            blockoBlockVersion.save();
            blockoBlockVersion.refresh();

            return blockoBlockVersion;
        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void blocko_block_version_delete(Model_BlockoBlockVersion blockoBlockVersion){
        try {

            blockoBlockVersion.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // GRID ############################################################################################################

    public static Model_TypeOfWidget type_of_widget_create(Model_Project project){
        try {

            Model_TypeOfWidget typeOfWidget = new Model_TypeOfWidget();
            typeOfWidget.name = UUID.randomUUID().toString();
            typeOfWidget.description = UUID.randomUUID().toString();
            typeOfWidget.project = project;

            typeOfWidget.save();
            typeOfWidget.refresh();

            return typeOfWidget;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void type_of_widget_delete(Model_TypeOfWidget typeOfWidget){
        try {

            typeOfWidget.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_GridWidget grid_widget_create(Model_TypeOfWidget typeOfWidget){
        try {

            Model_GridWidget gridWidget = new Model_GridWidget();
            gridWidget.name = UUID.randomUUID().toString();
            gridWidget.description = UUID.randomUUID().toString();
            gridWidget.type_of_widget = typeOfWidget;

            gridWidget.save();
            gridWidget.refresh();

            return gridWidget;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void grid_widget_delete(Model_GridWidget gridWidget){
        try {

            gridWidget.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_GridWidgetVersion grid_widget_version_create(Model_GridWidget gridWidget){
        try {

            Model_GridWidgetVersion gridWidgetVersion = new Model_GridWidgetVersion();
            gridWidgetVersion.version_name = UUID.randomUUID().toString();
            gridWidgetVersion.version_description = UUID.randomUUID().toString();
            gridWidgetVersion.design_json = UUID.randomUUID().toString();
            gridWidgetVersion.logic_json = UUID.randomUUID().toString();
            gridWidgetVersion.grid_widget = gridWidget;

            gridWidgetVersion.save();
            gridWidgetVersion.refresh();

            return gridWidgetVersion;
        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void grid_widget_version_delete(Model_GridWidgetVersion gridWidgetVersion){
        try {

            gridWidgetVersion.delete();

        }catch (Exception e){
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static void method4(){
        try {



        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static void method1(){
        try {



        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }
}
