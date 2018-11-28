package utilities.notifications;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.*;
import play.Environment;
import play.data.Form;
import play.data.FormFactory;
import play.libs.ws.WSClient;
import play.mvc.Result;
import play.mvc.Security;
import utilities.authentication.Authentication;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Button;
import utilities.notifications.helps_objects.Notification_Link;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.Swagger_C_Program_Version_Update;
import utilities.swagger.input.Swagger_Notification_Test;

import java.util.Date;
import java.util.Random;
import java.util.UUID;


@Api(value = "Not Documented API - InProgress or Stuck")
public class NotificationTester extends _BaseController {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger terminal_logger = new Logger(NotificationTester.class);

    @Inject
    public static _BaseFormFactory formFactory;

// CONTROLLER CONFIGURATION ############################################################################################

    @javax.inject.Inject
    public NotificationTester(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler, PermissionService permissionService) {
        super(environment, ws, formFactory, youTrack, config, scheduler, permissionService);
    }

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    @ApiOperation(value = "test_notifications", hidden = true)
    @Security.Authenticated(Authentication.class)
    public Result test_chain_notifications(String mail) {
        try {

            if(!isAdmin()) {
                return forbidden();
            }

            terminal_logger.debug("test_chain_notifications - email: {}", mail);

            Model_Person person = Model_Person.getByEmail(mail);
            if (person == null) return notFound("Person not found");

            Thread notification_test_thread = new Thread() {

                @Override
                public void run() {
                    try {

                        UUID id = UUID.randomUUID();

                        Random rand = new Random();

                        if ( rand.nextInt(10) > 5) {

                            Model_Notification notification_start = new Model_Notification();

                            notification_start.setNotificationId(id)
                                    .setChainType(NotificationType.CHAIN_START)
                                    .setImportance(NotificationImportance.LOW)
                                    .setLevel(NotificationLevel.INFO);

                            notification_start.setText(new Notification_Text().setText("CHAIN TEST:: Yes thats all!!!!"))
                                    .send(person);

                        } else {

                            Model_Notification notification_start = new Model_Notification();

                            notification_start.setNotificationId(id)
                                    .setChainType(NotificationType.CHAIN_UPDATE)
                                    .setImportance(NotificationImportance.LOW)
                                    .setLevel(NotificationLevel.INFO);

                            notification_start.setText(new Notification_Text().setText("CHAIN TEST:: Shit! This Test not send CHAIN_START notification parameter but first message is CHAIN_UPDATE !!!!"))
                                    .send(person);
                        }

                        sleep(4000);
                        
                        for (int i = 0; i <= 100; i = i+8) {

                            Model_Notification notification_progress = new Model_Notification();

                            notification_progress.setNotificationId(id)
                                    .setChainType(NotificationType.CHAIN_UPDATE)
                                    .setImportance( NotificationImportance.LOW)
                                    .setLevel( NotificationLevel.INFO);

                            notification_progress
                                    .setText(new Notification_Text().setText("CHAIN TEST:: This is message about progress on Board " + i + "%" + " Actual time is:: "))
                                    .setDate( new Date())
                                    .setText(new Notification_Text().setText(". Thanks Pepa!"))
                                    .send(person);

                            sleep(400);
                        }

                        sleep(4000);

                        if (rand.nextInt(10) > 5) {

                            Model_Notification notification_finish = new Model_Notification();

                            notification_finish.setNotificationId(id)
                                    .setChainType(NotificationType.CHAIN_UPDATE)
                                    .setImportance( NotificationImportance.LOW)
                                    .setLevel( NotificationLevel.INFO);

                            notification_finish.setText(new Notification_Text().setText("CHAIN TEST:: Shit... This test not send CHAIN_END parameter - Do you know what to do? " ))
                                    .send(person);

                            return;
                        }

                        Model_Notification notification_finish = new Model_Notification();

                        notification_finish.setNotificationId(id)
                                .setChainType(NotificationType.CHAIN_END)
                                .setImportance( NotificationImportance.LOW)
                                .setLevel( NotificationLevel.INFO);

                        notification_finish.setText(new Notification_Text().setText("CHAIN TEST:: Yes thats all!!!!" ))
                                .send(person);

                    } catch (Exception e) {
                        terminal_logger.internalServerError(e);
                    }
                }
            };

            notification_test_thread.start();

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "test_notifications", hidden = true)
    @Security.Authenticated(Authentication.class)
    public Result test_notifications() {
        try {

            if(!isAdmin()) {
                return forbidden();
            }

            terminal_logger.debug("test_notifications - test");

            // Get and Validate Object
            Swagger_Notification_Test help  = formFromRequestWithValidation(Swagger_Notification_Test.class);


            Model_Person person = Model_Person.getByEmail(help.mail);
            if (person == null) return notFound("Person not found");

            test_notification(person, help.level, help.importance, help.type, help.buttons);
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    public static void test_notification(Model_Person person, String level, String importance, String type, String buttons) {

        NotificationLevel lvl;

        NotificationImportance imp;

        switch (importance) {
            case "low": imp = NotificationImportance.LOW; break;
            case "normal": imp = NotificationImportance.NORMAL; break;
            case "high": imp = NotificationImportance.HIGH; break;
            default: imp = NotificationImportance.NORMAL; break;
        }

        switch (level) {
            case "info": lvl = NotificationLevel.INFO;break;
            case "success": lvl = NotificationLevel.SUCCESS;break;
            case "warning": lvl = NotificationLevel.WARNING;break;
            case "error": lvl = NotificationLevel.ERROR;break;
            default: lvl = NotificationLevel.INFO;break;
        }

        Model_Notification notification;

        switch (type) {
            case "1":{
                notification = new Model_Notification()
                        .setImportance(imp)
                        .setLevel(lvl)
                        .setText( new Notification_Text().setText("Test object: "))
                        .setObject(person)
                        .setText(new Notification_Text().setText(" test text, "))
                        .setText(new Notification_Text().setText(" test bold text, ").setBoldText())
                        .setText(new Notification_Text().setText(" test italic text, ").setItalicText())
                        .setText(new Notification_Text().setText(" test underline text, ").setUnderlineText())
                        .setText(new Notification_Text().setText(" test red color text, ").setColor(Becki_color.byzance_red))
                        .setLink(new Notification_Link().setUrl("Text linku na google ", "http://google.com"));

                Model_Project project = Model_Project.find.query().nullable().where().eq("persons.id", person.id).eq("name", "První velkolepý projekt").findOne();
                if (project != null) {
                    notification.setObject(project);

                    if (!project.getHardware().isEmpty()) {
                        Model_Hardware board = project.getHardware().get(0);
                        notification.setObject(board);
                    }

                    Model_CProgram cProgram;
                    if (!project.c_programs.isEmpty()) {

                        cProgram = project.c_programs.get(0);

                    } else {

                        cProgram = new Model_CProgram();
                        cProgram.name                  = "Test notification c program";
                        cProgram.description           = "random text sd asds dasda ";
                        cProgram.project               = project;
                        cProgram.save();
                        cProgram.refresh();

                        terminal_logger.info("Setting new C Program");
                    }

                    notification.setObject(cProgram);

                    Model_CProgramVersion version;
                    if (cProgram.getVersions().isEmpty()) {

                        version = new Model_CProgramVersion();
                        version.name        = "Test notification c version";
                        version.description = "random text sd asds dasda";
                        version.author_id              = person.id;
                        version.c_program           = cProgram;
                        version.publish_type        = ProgramType.PRIVATE;
                        version.save();
                        version.refresh();

                        terminal_logger.info("Setting new C Program Version");

                    } else {
                        version = cProgram.getVersions().get(0);
                    }

                    notification.setObject(version);

                    Model_BProgram bProgram;
                    if (!project.b_programs.isEmpty()) {
                        bProgram = project.b_programs.get(0);
                    } else {

                        bProgram = new Model_BProgram();
                        bProgram.name                  = "Test notification b program";
                        bProgram.description           = "random text sd asds dasda ";
                        bProgram.project = project;
                        bProgram.save();
                        bProgram.refresh();

                        terminal_logger.info("Setting new B Program");
                    }

                    notification.setObject(bProgram);

                    Model_BProgramVersion b_version;
                    if (bProgram.getVersions().isEmpty()) {

                        b_version = new Model_BProgramVersion();
                        b_version.name        = "Test notification b version";
                        b_version.description = "random text sd asds dasda";
                        b_version.author_id         = person.id;
                        b_version.b_program         = bProgram;
                        version.publish_type        = ProgramType.PRIVATE;
                        b_version.save();
                        b_version.refresh();

                        terminal_logger.info("Setting new B Program Version");

                    } else {
                        b_version = bProgram.getVersions().get(0);
                    }

                    notification.setObject(b_version);
                }



                break;}
            case "2":{
                notification = new Model_Notification()
                        .setImportance(imp)
                        .setLevel(lvl)
                        .setText(new Notification_Text().setText("Test object and long text: "))
                        .setObject(person)
                        .setText(new Notification_Text().setText(" test text: Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "));
                break;}
            case "3":{
                notification = new Model_Notification()
                        .setImportance(imp)
                        .setLevel(lvl)
                        .setText(new Notification_Text().setText("Test short text with link: "))
                        .setLink(new Notification_Link().setUrl("Text linku na google ", "http://google.com"));
                break;}
            case "4": {
                notification = new Model_Notification()
                        .setImportance(imp)
                        .setLevel(lvl)
                        .setText( new Notification_Text().setText("Test object and link: "))
                        .setObject(person)
                        .setText( new Notification_Text().setText(" test link: "))
                        .setLink(new Notification_Link().setUrl("Yes", "http://google.com"));
                break;}
            default:{
                notification = new Model_Notification()
                        .setImportance(imp)
                        .setLevel(lvl)
                        .setText( new Notification_Text().setText("Test object: "))
                        .setObject(person)
                        .setText( new Notification_Text().setText(" test bold text: ").setBoldText())
                        .setText( new Notification_Text().setText( "test link: "))
                        .setLink( new Notification_Link().setUrl("Yes", "http://google.com"));
                break;}
        }
        switch (buttons) {
            case "0": break;
            case "1":{
                notification.setButton( new Notification_Button().setAction(NotificationAction.CONFIRM_NOTIFICATION).setPayload("test").setColor(Becki_color.byzance_blue).setText("OK") );
                break;}
            case "2":{
                notification.setButton( new Notification_Button().setAction(NotificationAction.CONFIRM_NOTIFICATION).setPayload("test").setColor(Becki_color.byzance_green).setText("YES") );
                notification.setButton( new Notification_Button().setAction(NotificationAction.CONFIRM_NOTIFICATION).setPayload("test").setColor(Becki_color.byzance_red).setText("NO").setUnderLine() );
                break;}
            case "3":{
                notification.setButton( new Notification_Button().setAction(NotificationAction.CONFIRM_NOTIFICATION).setPayload("test").setColor(Becki_color.white).setText("NO").setBold() );
                notification.setButton( new Notification_Button().setAction(NotificationAction.CONFIRM_NOTIFICATION).setPayload("test").setColor(Becki_color.byzance_green).setText("NO").setItalic() );
                notification.setButton( new Notification_Button().setAction(NotificationAction.CONFIRM_NOTIFICATION).setPayload("test").setColor(Becki_color.byzance_blue).setText("NO").setItalic() );
                break;}
            default:{
                notification.setButton( new Notification_Button().setAction(NotificationAction.CONFIRM_NOTIFICATION).setPayload("test").setColor(Becki_color.byzance_blue).setText("NO").setItalic() );
                break;}
        }

        notification.send(person);
    }
}
