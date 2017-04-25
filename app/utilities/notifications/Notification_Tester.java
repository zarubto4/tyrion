package utilities.notifications;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.*;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.Enum_Notification_action;
import utilities.enums.Enum_Notification_importance;
import utilities.enums.Enum_Notification_level;
import utilities.enums.Enum_Notification_type;
import utilities.independent_threads.Check_Update_for_hw_on_homer;
import utilities.independent_threads.Security_WS_token_confirm_procedure;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Button;
import utilities.notifications.helps_objects.Notification_Link;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.response.GlobalResult;
import utilities.swagger.documentationClass.Swagger_Notification_Test;

import java.util.Date;
import java.util.Random;
import java.util.UUID;


@Api(value = "Not Documented API - InProgress or Stuck")
public class Notification_Tester extends Controller {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Notification_Tester.class);


/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    @ApiOperation(value = "test_notifications", hidden = true)
    @Security.Authenticated(Secured_API.class)
    public Result test_chain_notifications(String mail){
        try {

            terminal_logger.debug("Notification_Tester:: test_chain_notifications:: Email:: " + mail);

            Model_Person person = Model_Person.find.where().eq("mail", mail).findUnique();
            if (person == null) return GlobalResult.notFoundObject("Person not found");


            Thread notification_test_thread = new Thread() {

                @Override
                public void run() {

                    terminal_logger.trace("Check_update_for_hw_under_homer_ws:: add_new_Procedure:: Independent Thread in Check_update_for_hw_under_homer_ws now working"); ;

                    try{



                        String id = UUID.randomUUID().toString();

                        Random rand = new Random();

                        if( rand.nextInt(10) > 5) {

                            Model_Notification notification_start = new Model_Notification();

                            notification_start.setId(id)
                                    .setChainType(Enum_Notification_type.CHAIN_START)
                                    .setImportance(Enum_Notification_importance.low)
                                    .setLevel(Enum_Notification_level.info);

                            notification_start.setText(new Notification_Text().setText("CHAIN TEST:: Yes thats all!!!!"))
                                    .send(person);

                        }else {

                            Model_Notification notification_start = new Model_Notification();

                            notification_start.setId(id)
                                    .setChainType(Enum_Notification_type.CHAIN_UPDATE)
                                    .setImportance(Enum_Notification_importance.low)
                                    .setLevel(Enum_Notification_level.info);

                            notification_start.setText(new Notification_Text().setText("CHAIN TEST:: Shit! This Test not send CHAIN_START notification parameter but first message is CHAIN_UPDATE !!!!"))
                                    .send(person);

                        }

                        sleep(4000);
                        
                        for(int i = 0; i <= 100; i = i+8){

                            Model_Notification notification_progress = new Model_Notification();

                            notification_progress.setId(id)
                                    .setChainType(Enum_Notification_type.CHAIN_UPDATE)
                                    .setImportance( Enum_Notification_importance.low)
                                    .setLevel( Enum_Notification_level.info);

                            notification_progress
                                    .setText(new Notification_Text().setText("CHAIN TEST:: This is message about progress on Board " + i + "%" + " Actual time is:: "))
                                    .setDate( new Date())
                                    .setText(new Notification_Text().setText(". Thanks Pepa!"))
                                    .send(person);

                            sleep(400);
                        }


                        sleep(4000);


                        if(rand.nextInt(10) > 5){

                            Model_Notification notification_finish = new Model_Notification();

                            notification_finish.setId(id)
                                    .setChainType(Enum_Notification_type.CHAIN_UPDATE)
                                    .setImportance( Enum_Notification_importance.low)
                                    .setLevel( Enum_Notification_level.info);

                            notification_finish.setText(new Notification_Text().setText("CHAIN TEST:: Shit... This test not send CHAIN_END parameter - Do you know what to do? " ))
                                    .send(person);

                            return;
                        }




                        Model_Notification notification_finish = new Model_Notification();

                        notification_finish.setId(id)
                                .setChainType(Enum_Notification_type.CHAIN_END)
                                .setImportance( Enum_Notification_importance.low)
                                .setLevel( Enum_Notification_level.info);

                        notification_finish.setText(new Notification_Text().setText("CHAIN TEST:: Yes thats all!!!!" ))
                                .send(person);



                    }catch (Exception e){
                        terminal_logger.error("Check_update_for_hw_under_homer_ws:: Error", e);
                    }
                }
            };

            notification_test_thread.start();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }



    @ApiOperation(value = "test_notifications", hidden = true)
    @Security.Authenticated(Secured_API.class)
    public Result test_notifications(){
        try {

            terminal_logger.debug("Notification_Tester:: test_notifications:: ");
            final Form<Swagger_Notification_Test> form = Form.form(Swagger_Notification_Test.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Notification_Test help = form.get();

            Model_Person person = Model_Person.find.where().eq("mail", help.mail).findUnique();
            if (person == null) return GlobalResult.notFoundObject("Person not found");

            test_notification(person, help.level, help.importance, help.type, help.buttons);
            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }






    public static void test_notification(Model_Person person, String level, String importance, String type, String buttons){

        Enum_Notification_level lvl;

        Enum_Notification_importance imp;

        switch (importance){
            case "low": imp = Enum_Notification_importance.low; break;
            case "normal": imp = Enum_Notification_importance.normal; break;
            case "high": imp = Enum_Notification_importance.high; break;
            default: imp = Enum_Notification_importance.normal; break;
        }

        switch (level){
            case "info": lvl = Enum_Notification_level.info;break;
            case "success": lvl = Enum_Notification_level.success;break;
            case "warning": lvl = Enum_Notification_level.warning;break;
            case "error": lvl = Enum_Notification_level.error;break;
            default: lvl = Enum_Notification_level.info;break;
        }

        Model_Notification notification;

        switch (type){
            case "1":{
                notification = new Model_Notification()
                        .setImportance(imp)
                        .setLevel(lvl)
                        .setText( new Notification_Text().setText("Test object: "))
                        .setObject(person)
                        .setText(new Notification_Text().setText(" test text, "))
                        .setText(new Notification_Text().setText(" test bold text, ").setBoltText())
                        .setText(new Notification_Text().setText(" test italic text, ").setItalicText())
                        .setText(new Notification_Text().setText(" test underline text, ").setUnderlineText())
                        .setText(new Notification_Text().setText(" test red color text, ").setColor(Becki_color.byzance_red))
                        .setLink(new Notification_Link().setUrl("Text linku na google ", "http://google.com"));

                Model_Project project = Model_Project.find.where().eq("participants.person.id", person.id).eq("name", "První velkolepý projekt").findUnique();
                if (project != null) {
                    notification.setObject(project);

                    if (!project.boards.isEmpty()){
                        Model_Board board = project.boards.get(0);
                        notification.setObject(board);
                    }

                    Model_CProgram cProgram;
                    if (!project.c_programs.isEmpty()){

                        cProgram = project.c_programs.get(0);

                    } else {

                        cProgram = new Model_CProgram();
                        cProgram.name                  = "Test notification c program";
                        cProgram.description           = "random text sd asds dasda ";
                        cProgram.date_of_create        = new Date();
                        cProgram.project               = project;
                        cProgram.save();
                        cProgram.refresh();

                        terminal_logger.info("Setting new C Program");
                    }

                    notification.setObject(cProgram);

                    Model_VersionObject version_object;
                    if (cProgram.getVersion_objects().isEmpty()){

                        version_object = new Model_VersionObject();
                        version_object.version_name        = "Test notification c version";
                        version_object.version_description = "random text sd asds dasda";
                        version_object.author              = person;
                        version_object.date_of_create      = new Date();
                        version_object.c_program           = cProgram;
                        version_object.public_version      = false;
                        version_object.save();
                        version_object.refresh();

                        terminal_logger.info("Setting new C Program Version");

                    } else {
                        version_object = cProgram.getVersion_objects().get(0);
                    }

                    notification.setObject(version_object);

                    Model_BProgram bProgram;
                    if (!project.b_programs.isEmpty()){
                        bProgram = project.b_programs.get(0);
                    } else {

                        bProgram = new Model_BProgram();
                        bProgram.name                  = "Test notification b program";
                        bProgram.description           = "random text sd asds dasda ";
                        bProgram.date_of_create        = new Date();
                        bProgram.project = project;
                        bProgram.save();
                        bProgram.refresh();

                        terminal_logger.info("Setting new B Program");
                    }

                    notification.setObject(bProgram);

                    Model_VersionObject b_version_object;
                    if (bProgram.getVersion_objects().isEmpty()){

                        b_version_object = new Model_VersionObject();
                        b_version_object.version_name        = "Test notification b version";
                        b_version_object.version_description = "random text sd asds dasda";
                        b_version_object.author              = person;
                        b_version_object.date_of_create      = new Date();
                        b_version_object.b_program           = bProgram;
                        b_version_object.save();
                        b_version_object.refresh();

                        terminal_logger.info("Setting new B Program Version");

                    } else {
                        b_version_object = bProgram.getVersion_objects().get(0);
                    }

                    notification.setObject(b_version_object);
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
                        .setText( new Notification_Text().setText(" test bold text: ").setBoltText())
                        .setText( new Notification_Text().setText( "test link: "))
                        .setLink( new Notification_Link().setUrl("Yes", "http://google.com"));
                break;}
        }
        switch (buttons){
            case "0": break;
            case "1":{
                notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_blue).setText("OK") );
                break;}
            case "2":{
                notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_green).setText("YES") );
                notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_red).setText("NO").setUnderLine() );
                break;}
            case "3":{
                notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.white).setText("NO").setBold() );
                notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_green).setText("NO").setItalic() );
                notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_blue).setText("NO").setItalic() );
                break;}
            default:{
                notification.setButton( new Notification_Button().setAction(Enum_Notification_action.confirm_notification).setPayload("test").setColor(Becki_color.byzance_blue).setText("NO").setItalic() );
                break;}
        }

        notification.send(person);
    }
}
