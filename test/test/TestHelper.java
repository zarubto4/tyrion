package utilities.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.TestCase;
import models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.libs.ws.WSResponse;
import play.mvc.Controller;

import java.util.*;

public class TestHelper extends Controller{

    private static Logger logger = LoggerFactory.getLogger(TestCase.class);

    public static void checkResponse(WSResponse response, int expectedStatus) throws AssertionError{
        checkResponse(response, expectedStatus, null);
    }

    /**
     * Method checks if response status was as expected one.
     * If expected body is provided method checks if response contains all expected values.
     * @param response from tested endpoint.
     * @param expectedStatus which should be returned from the endpoint.
     * @param expectedBody which should be returned from the endpoint. If null, no checks are made.
     * @throws AssertionError if some value(s) was/were different from expected one(s).
     */
    public static void checkResponse(WSResponse response, int expectedStatus, ObjectNode expectedBody) throws AssertionError {

        List<ValidationError> errors = new ArrayList<>();

        if (response.getStatus() != expectedStatus) {
            errors.add(new ValidationError("status", "expected response status was " + expectedStatus + " but got " + response.getStatus()));
        }

        // If there is a body that response should contain.
        if (expectedBody != null) {

            JsonNode result = null;

            try {
                result = response.asJson();
            } catch (Exception e) {
                errors.add(new ValidationError("body", "body is missing"));
            }

            if (result != null) {

                // if response contains JSON, check its fields
                errors.addAll(checkBody(result, expectedBody));
            }
        }

        // Transforming errors into JSON format
        if (!errors.isEmpty()) {

            HashMap<String, String> errorMap = new HashMap<>();

            for (ValidationError error : errors) {

                if (errorMap.containsKey(error.key())) {
                    errorMap.replace(error.key(), errorMap.get(error.key()) + ", " + error.message());
                } else {
                    errorMap.put(error.key(), error.message());
                }
            }

            throw new AssertionError(Json.toJson(errorMap).toString());
        }
    }

    /**
     * Compares two JSONs, the actual response and expected one.
     * @param body from actual response.
     * @param expectedBody that should be returned from the endpoint.
     * @return List of Validation Errors which can be empty or contain errors from fields.
     */
    private static List<ValidationError> checkBody(JsonNode body, JsonNode expectedBody) {

        List<ValidationError> errors = new ArrayList<>();

        Iterator<Map.Entry<String, JsonNode>> iterator = expectedBody.fields();

        // Iterate through all fields of expected JSON
        while (iterator.hasNext()) {

            Map.Entry<String, JsonNode> entry = iterator.next();

            String key = entry.getKey();
            JsonNode value = entry.getValue();

            // If the field is missing
            if (body.has(key)) {

                JsonNode node = body.get(key);

                // If the type of the field is different from the expected one
                if (value.getNodeType() == node.getNodeType()) {

                    switch (node.getNodeType()) {

                        case STRING: {
                            if (!value.asText().equals(node.asText()))
                                errors.add(new ValidationError(key, "expected value '" + value.asText() + "' but got '" + node.asText() + "'"));
                            break;
                        }

                        case OBJECT: errors.addAll(checkBody(node, value)); break;

                        case ARRAY: {

                            if (!value.elements().equals(node.elements()))
                                errors.add(new ValidationError(key, "expected array " + value.toString() + " but got " + node.toString()));
                        }

                        case BOOLEAN: {
                            if (value.asBoolean() != node.asBoolean())
                                errors.add(new ValidationError(key, "expected value '" + value.asBoolean() + "' but got '" + node.asBoolean() + "'"));
                            break;
                        }

                        case NUMBER: {
                            if (node.isLong()) {
                                if (value.isLong()) {
                                    if (node.asLong() != value.asLong())
                                        errors.add(new ValidationError(key, "expected value '" + value.asLong() + "' but got '" + node.asLong() + "'"));
                                } else {
                                    errors.add(new ValidationError(key, "expected type was different from 'Long'"));
                                }
                            }

                            if (node.isDouble()) {
                                if (value.isDouble()) {
                                    if (node.asDouble() != value.asDouble())
                                        errors.add(new ValidationError(key, "expected value '" + value.asDouble() + "' but got '" + node.asDouble() + "'"));
                                } else {
                                    errors.add(new ValidationError(key, "expected type was different from 'Double'"));
                                }
                            }

                            break;
                        }

                        case NULL: {
                            logger.warn("Field '{}' from body: \n{}\n was NULL (could be an expected value, but not necessarily - NULL value is also used, if expected value is unknown, but should be present in the JSON.)", key, Json.prettyPrint(body));
                            break;
                        }

                        default: errors.add(new ValidationError(key, "Unknown type of JsonNode = " + node.getNodeType() + ".")); break;
                    }
                } else if (value.getNodeType() != JsonNodeType.NULL) {

                    errors.add(new ValidationError(key, "expected type '" + value.getNodeType().name() + "' but got '" + node.getNodeType().name() + "'"));
                }
            } else {
                errors.add(new ValidationError(key, "missing from the body"));
            }
        }

        return errors;
    }

    // PERSON ##########################################################################################################

    public static Model_Person person_create() {

        try {

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
        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
            return null;
        }
    }

    public static void person_authenticate(Model_Person person) {

        try {

            person.mailValidated = true;
            person.update();

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
        }
    }

    public static String person_login(Model_Person person) {

        try {

            Model_FloatingPersonToken floatingPersonToken = new Model_FloatingPersonToken();
            floatingPersonToken.person = person;
            floatingPersonToken.user_agent = "Unknown browser";
            floatingPersonToken.save();

            return floatingPersonToken.authToken;
        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
            return null;
        }
    }

    public static void person_delete(Model_Person person) {
        try {

            person.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static void person_token_delete(String token) {
        try {

            Model_FloatingPersonToken.find.where().eq("authToken", token).findOne().delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // PRODUCT #########################################################################################################

    public static Model_Product product_create(Model_Person person) {
        try {

            Model_Customer customer = new Model_Customer();
            customer.save();

            Model_Product product = new Model_Product();
            product.name = UUID.randomUUID().toString();
            product.active = true;
            product.customer = customer;

            product.save();
            product.refresh();

            Model_PaymentDetails payment_details = new Model_PaymentDetails();
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

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void product_delete(Model_Product product) {
        try {

            product.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // PROJECT #########################################################################################################

    public static Model_Project project_create(Model_Product product) {
        try {

            Model_Project project  = new Model_Project();
            project.name = UUID.randomUUID().toString();
            project.description = UUID.randomUUID().toString();
            project.product = product;

            project.save();
            project.refresh();

            Model_ProjectParticipant participant = new Model_ProjectParticipant();
            participant.project = project;
            participant.state = Enum_Participant_status.owner;

            participant.save();

            project.refresh();

            return project;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void project_delete(Model_Project project) {
        try {

            project.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_Invitation project_share(Model_Project project, Model_Person owner, Model_Person invited_person) {
        try {

            Model_Invitation invitation = Model_Invitation.find.query().where().eq("email", invited_person.email).findOne();
            if (invitation == null) {
                invitation = new Model_Invitation();
                invitation.email = invited_person.email;
                invitation.created = new Date();
                invitation.owner = owner;
                invitation.project = project;

                invitation.save();
                invitation.refresh();

                project.invitations.add(invitation);
            }

            return invitation;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void project_add_participant(Model_Project project, Model_Person person) {
        try {

            Model_ProjectParticipant participant = new Model_ProjectParticipant();
            participant.person = person;
            participant.project = project;
            participant.state = Enum_Participant_status.member;

            participant.save();
            project.refresh();

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // PRODUCER ########################################################################################################

    public static Model_Producer producer_create() {
        try {

            Model_Producer producer = new Model_Producer();
            producer.name = UUID.randomUUID().toString();
            producer.description = UUID.randomUUID().toString();

            producer.save();
            producer.refresh();

            return producer;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void producer_delete(Model_Producer producer) {
        try {

            producer.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // PROCESSOR #######################################################################################################

    public static Model_Processor processor_create() {
        try {

            Model_Processor processor = new Model_Processor();
            processor.description    = UUID.randomUUID().toString();
            processor.processor_code = UUID.randomUUID().toString();
            processor.processor_name = UUID.randomUUID().toString();
            processor.speed          = 3000;

            processor.save();
            processor.refresh();

            return processor;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void processor_delete(Model_Processor processor) {
        try {

            processor.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // TYPE_OF_BOARD ###################################################################################################

    public static Model_HardwareType type_of_board_create(Model_Producer producer, Model_Processor processor) {
        try {

            Model_HardwareType typeOfBoard = new Model_HardwareType();
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
            default_program.hardware_type_default = typeOfBoard;

            default_program.save();
            default_program.refresh();

            Model_VersionObject default_version = new Model_VersionObject();
            default_version.version_name = UUID.randomUUID().toString();
            default_version.version_description = UUID.randomUUID().toString();
            default_version.default_program = default_program;

            default_version.save();
            default_program.refresh();
            typeOfBoard.refresh();

            return typeOfBoard;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void type_of_board_delete(Model_HardwareType typeOfBoard) {
        try {

            typeOfBoard.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // BOARD ###########################################################################################################

    // C_PROGRAM #######################################################################################################

    public static Model_CProgram private_c_program_create(Model_HardwareType typeOfBoard, Model_Project project) {
        try {

            Model_CProgram c_program        = new Model_CProgram();
            c_program.name                  = UUID.randomUUID().toString();
            c_program.description           = UUID.randomUUID().toString();
            c_program.date_of_create        = new Date();
            c_program.hardware_type = typeOfBoard;
            c_program.project               = project;

            c_program.save();
            c_program.refresh();

            return c_program;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void c_program_delete(Model_CProgram c_program) {
        try {

            c_program.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_VersionObject c_program_version_create(Model_CProgram c_program) {
        try {

            Model_VersionObject version_object = new Model_VersionObject();
            version_object.version_name        = UUID.randomUUID().toString();
            version_object.version_description = UUID.randomUUID().toString();
            version_object.author              = null;
            version_object.date_of_create      = new Date();
            version_object.c_program           = c_program;
            version_object.public_version      = false;

            version_object.save();
            version_object.refresh();

            return version_object;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void c_program_version_delete(Model_VersionObject version_object) {
        try {

            version_object.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // BLOCKO ##########################################################################################################

    public static Model_TypeOfBlock type_of_block_create(Model_Project project) {
        try {

            Model_TypeOfBlock typeOfBlock = new Model_TypeOfBlock();
            typeOfBlock.name = UUID.randomUUID().toString();
            typeOfBlock.description = UUID.randomUUID().toString();
            typeOfBlock.project = project;

            typeOfBlock.save();

            return typeOfBlock;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void type_of_block_delete(Model_TypeOfBlock typeOfBlock) {
        try {

            typeOfBlock.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_BlockoBlock blocko_block_create(Model_TypeOfBlock typeOfBlock) {
        try {

            Model_BlockoBlock blockoBlock = new Model_BlockoBlock();
            blockoBlock.name = UUID.randomUUID().toString();
            blockoBlock.description = UUID.randomUUID().toString();
            blockoBlock.type_of_block = typeOfBlock;
            blockoBlock.author = null;

            blockoBlock.save();

            return blockoBlock;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void blocko_block_delete(Model_BlockoBlock blockoBlock) {
        try {

            blockoBlock.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_BlockoBlockVersion blocko_block_version_create(Model_BlockoBlock blockoBlock) {
        try {

            Model_BlockoBlockVersion blockoBlockVersion = new Model_BlockoBlockVersion();
            blockoBlockVersion.version_name = UUID.randomUUID().toString();
            blockoBlockVersion.version_description = UUID.randomUUID().toString();
            blockoBlockVersion.date_of_create = new Date();
            blockoBlockVersion.design_json = UUID.randomUUID().toString();
            blockoBlockVersion.logic_json = UUID.randomUUID().toString();
            blockoBlockVersion.blocko_block = blockoBlock;
            blockoBlockVersion.author = blockoBlock.author;

            blockoBlockVersion.save();

            return blockoBlockVersion;
        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void blocko_block_version_delete(Model_BlockoBlockVersion blockoBlockVersion) {
        try {

            blockoBlockVersion.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_BProgram b_program_create(Model_Project project) {
        try {

            Model_BProgram bProgram = new Model_BProgram();
            bProgram.name = UUID.randomUUID().toString();
            bProgram.description = UUID.randomUUID().toString();
            bProgram.date_of_create = new Date();
            bProgram.project = project;

            bProgram.save();

            return bProgram;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void b_program_delete(Model_BProgram bProgram) {
        try {

            bProgram.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_VersionObject b_program_version_create(Model_BProgram bProgram) {
        try {

            Model_VersionObject version = new Model_VersionObject();
            version.version_name = UUID.randomUUID().toString();
            version.version_description = UUID.randomUUID().toString();
            version.b_program = bProgram;
            version.date_of_create = new Date();
            version.author = null;

            version.save();

            return version;
        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void b_program_version_delete(Model_VersionObject version) {
        try {

            version.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    // GRID ############################################################################################################

    public static Model_TypeOfWidget type_of_widget_create(Model_Project project) {
        try {

            Model_TypeOfWidget typeOfWidget = new Model_TypeOfWidget();
            typeOfWidget.name = UUID.randomUUID().toString();
            typeOfWidget.description = UUID.randomUUID().toString();
            typeOfWidget.project = project;

            typeOfWidget.save();

            return typeOfWidget;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void type_of_widget_delete(Model_TypeOfWidget typeOfWidget) {
        try {

            typeOfWidget.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_GridWidget grid_widget_create(Model_TypeOfWidget typeOfWidget) {
        try {

            Model_GridWidget gridWidget = new Model_GridWidget();
            gridWidget.name = UUID.randomUUID().toString();
            gridWidget.description = UUID.randomUUID().toString();
            gridWidget.type_of_widget = typeOfWidget;
            gridWidget.author = null;

            gridWidget.save();

            return gridWidget;

        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void grid_widget_delete(Model_GridWidget gridWidget) {
        try {

            gridWidget.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static Model_WidgetVersion grid_widget_version_create(Model_GridWidget gridWidget) {
        try {

            Model_WidgetVersion gridWidgetVersion = new Model_WidgetVersion();
            gridWidgetVersion.version_name = UUID.randomUUID().toString();
            gridWidgetVersion.version_description = UUID.randomUUID().toString();
            gridWidgetVersion.design_json = UUID.randomUUID().toString();
            gridWidgetVersion.logic_json = UUID.randomUUID().toString();
            gridWidgetVersion.widget = gridWidget;
            gridWidgetVersion.author = gridWidget.author;

            gridWidgetVersion.save();

            return gridWidgetVersion;
        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void grid_widget_version_delete(Model_WidgetVersion gridWidgetVersion) {
        try {

            gridWidgetVersion.delete();

        } catch (Exception e) {
            logger.error("!!!! Error while cleaning up after test. Method {} failed! Reason: {}. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static void method4() {
        try {



        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static void method1() {
        try {



        } catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }
}
