package utilities.notifications;

import models.*;
import utilities.enums.Enum_Notification_action;
import utilities.enums.Enum_Participant_status;
import utilities.logger.Class_Logger;

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

        Model_Person person = Model_Person.find.where().eq("mail", invitation.mail).findUnique();
        if (person == null) throw new Exception("Person does not exist.");

        Model_Project project = invitation.project;
        if (project == null) throw new IllegalArgumentException("Failed to add you to the project. Project no longer exists.");

        if (Model_ProjectParticipant.find.where().eq("person.id", person.id).eq("project.id", project.id).findUnique() == null) {

            Model_ProjectParticipant participant = new Model_ProjectParticipant();
            participant.person = person;
            participant.project = project;
            participant.state = Enum_Participant_status.member;

            participant.save();
        }

        project.notification_project_invitation_accepted(person, invitation.owner);

        invitation.delete();
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