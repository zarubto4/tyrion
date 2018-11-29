package utilities.notifications;

import exceptions.NotFoundException;
import models.*;
import utilities.enums.NotificationAction;
import utilities.enums.ParticipantStatus;
import utilities.logger.Logger;
import utilities.models_update_echo.RefreshTouch_echo_handler;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WS_Message_RefreshTouch;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import java.util.UUID;

/**
 * Class is used to handle every action, that can happen in notification confirmation.
 */
public class NotificationActionHandler {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(NotificationActionHandler.class);

    /**
     * Decides how to handle the given action from notification confirmation.
     * @param action Enumerated action from notification.
     * @param payload String payload from notification.
     * @throws IllegalArgumentException Exception is thrown when the action is unknown.
     */
    public static void perform(NotificationAction action, String payload) throws Exception{

        logger.debug("perform: Performing new notification action {}", action.name());

        switch (action) {

            case CONFIRM_NOTIFICATION: break;

            case ACCEPT_PROJECT_INVITATION: acceptProjectInvitation(payload); break;

            case REJECT_PROJECT_INVITATION: rejectProjectInvitation(payload); break;

            default: throw new Exception("Unknown notification action");
        }
    }

    /**
     * If the action was "accept_project_invitation", the method creates new project participant with status "MEMBER"
     * and sends a notification to inform the invitation owner.
     * @param invitation_id String id of related invitation.
     */
    private static void acceptProjectInvitation(String invitation_id) throws Exception{

        try {
            Model_Invitation invitation = Model_Invitation.find.byId(UUID.fromString(invitation_id));
            if (invitation == null) // TODO replace with not found exception
                throw new IllegalArgumentException("Failed to add you to the project. Invitation no longer exists, it might have been drawn back.");

            Model_Person person = Model_Person.find.query().where().eq("email", invitation.email).findOne();

            Model_Project project_not_cached = invitation.project;
            if (project_not_cached == null)
                throw new IllegalArgumentException("Failed to add you to the project. Project no longer exists.");

            Model_Project project = Model_Project.find.byId(project_not_cached.id);

            if (!project.getPersons().contains(person)) {
                project.persons.add(person);
                project.update();

                try {
                    Model_Role role = Model_Role.find.query().where().eq("project.id", project.id).eq("default_role", true).findOne();
                    if (!role.persons.contains(person)) {
                        role.persons.add(person);
                        role.update();
                    }
                } catch (NotFoundException e) {
                    logger.warn("acceptProjectInvitation - unable to find default role for project, id {}", project.id);
                }
            }

            person.get_user_access_projects();
            person.idCache().add(Model_Project.class, project_not_cached.id);
            project.notification_project_invitation_accepted(person, invitation.owner);

            new Thread(() -> RefreshTouch_echo_handler.addToQueue(new WS_Message_RefreshTouch("ProjectsRefreshAfterInvite", person.id))).start();
            new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Project.class, project_not_cached.id, project_not_cached.id))).start();

            invitation.delete();
            project.refresh();

        } catch (Exception e){
            logger.internalServerError(e);
        }
    }

    /**
     * Deletes invitation and sends a notification to inform the invitation owner.
     * @param invitation_id String id of related invitation.
     */
    private static void rejectProjectInvitation(String invitation_id) throws Exception{

        // Kontroly objektů
        Model_Invitation invitation = Model_Invitation.find.byId(UUID.fromString(invitation_id));
        if (invitation == null) throw new IllegalArgumentException("Invitation no longer exists.");

        Model_Person person = Model_Person.getByEmail(invitation.email);
        if (person == null) throw new Exception("Person does not exist.");

        Model_Project project = invitation.project;
        if (project == null) throw new IllegalArgumentException("Project no longer exists.");

        project.notification_project_invitation_rejected(invitation.owner);

        invitation.delete();
    }
}