package utilities.notifications;

import models.*;
import utilities.enums.Enum_Notification_action;
import utilities.enums.Enum_Participant_status;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.RefresTuch_echo_handler;
import utilities.models_update_echo.Update_echo_handler;
import web_socket.message_objects.tyrion_with_becki.WS_Message_RefreshTuch;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

/**
 * Class is used to handle every action, that can happen in notification confirmation.
 */
public class NotificationActionHandler {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(NotificationActionHandler.class);

    /**
     * Decides how to handle the given action from notification confirmation.
     * @param action Enumerated action from notification.
     * @param payload String payload from notification.
     * @throws IllegalArgumentException Exception is thrown when the action is unknown.
     */
    public static void perform(Enum_Notification_action action, String payload) throws Exception{

        terminal_logger.debug("perform: Performing new notification action {}", action.name());

        switch (action){

            case confirm_notification: break;

            case accept_project_invitation: acceptProjectInvitation(payload); break;

            case reject_project_invitation: rejectProjectInvitation(payload); break;

            default: throw new Exception("Unknown notification action");
        }
    }

    /**
     * If the action was "accept_project_invitation", the method creates new project participant with status "member"
     * and sends a notification to inform the invitation owner.
     * @param invitation_id String id of related invitation.
     */
    private static void acceptProjectInvitation(String invitation_id) throws Exception{

        Model_Invitation invitation = Model_Invitation.find.byId(invitation_id);
        if (invitation == null) throw new IllegalArgumentException("Failed to add you to the project. Invitation no longer exists, it might have been drawn back.");

        Model_Person person_not_cached = Model_Person.find.where().eq("mail", invitation.mail).select("id").findUnique();
        if (person_not_cached == null) throw new Exception("Person does not exist.");

        Model_Person person = Model_Person.get_byId(person_not_cached.id);

        Model_Project project_not_cached = invitation.project;
        if (project_not_cached == null) throw new IllegalArgumentException("Failed to add you to the project. Project no longer exists.");

        Model_Project project = Model_Project.get_byId(project_not_cached.id);

        if (Model_ProjectParticipant.find.where().eq("person.id", person.id).eq("project.id", project.id).findUnique() == null) {

            Model_ProjectParticipant participant = new Model_ProjectParticipant();
            participant.person = person;
            participant.project = project;
            participant.state = Enum_Participant_status.member;

            participant.save();
        }

        person.cache_value_project_ids.add(project_not_cached.id);
        project.notification_project_invitation_accepted(person, invitation.owner);

        new Thread(() -> RefresTuch_echo_handler.addToQueue(new WS_Message_RefreshTuch( "ProjectsRefreshAfterInvite", person_not_cached.id))).start();
        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, project_not_cached.id, project_not_cached.id))).start();

        invitation.delete();

        project.cache_refresh();
    }

    /**
     * Deletes invitation and sends a notification to inform the invitation owner.
     * @param invitation_id String id of related invitation.
     */
    private static void rejectProjectInvitation(String invitation_id) throws Exception{

        // Kontroly objektů
        Model_Invitation invitation = Model_Invitation.find.byId(invitation_id);
        if(invitation == null) throw new IllegalArgumentException("Invitation no longer exists.");

        Model_Person person = Model_Person.find.where().eq("mail", invitation.mail).findUnique();
        if(person == null) throw new Exception("Person does not exist.");

        Model_Project project = invitation.project;
        if(project == null) throw new IllegalArgumentException("Project no longer exists.");

        project.notification_project_invitation_rejected(invitation.owner);

        invitation.delete();
    }
}