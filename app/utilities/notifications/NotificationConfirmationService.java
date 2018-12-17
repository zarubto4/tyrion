package utilities.notifications;

import com.google.inject.Inject;
import exceptions.BadRequestException;
import exceptions.NotSupportedException;
import models.*;
import utilities.enums.NotificationAction;
import utilities.enums.NotificationState;
import utilities.hardware.HardwareService;
import utilities.project.ProjectService;

import java.util.UUID;

public class NotificationConfirmationService {

    private final NotificationService notificationService;
    private final ProjectService projectService;
    private final HardwareService hardwareService;

    @Inject
    public NotificationConfirmationService(NotificationService notificationService, ProjectService projectService, HardwareService hardwareService) {
        this.notificationService = notificationService;
        this.projectService = projectService;
        this.hardwareService = hardwareService;
    }

    public void confirm(Model_Notification notification, NotificationAction action, String payload) {
        if (notification.confirmed) {
            throw new BadRequestException("Notification is already confirmed");
        } else {
            notification.confirm();
            this.notificationService.send(notification.getPerson(), notification.setState(NotificationState.UPDATED));
        }

        switch (action) {
            case CONFIRM_NOTIFICATION: break;
            case ACCEPT_PROJECT_INVITATION: this.projectService.acceptInvitation(Model_Invitation.find.byId(UUID.fromString(payload))); break;
            case REJECT_PROJECT_INVITATION: this.projectService.rejectInvitation(Model_Invitation.find.byId(UUID.fromString(payload))); break;
            case ACCEPT_RESTORE_FIRMWARE: this.hardwareService.setDefaultFirmware(Model_Hardware.find.byId(UUID.fromString(payload))); break;
            case REJECT_RESTORE_FIRMWARE: this.hardwareService.rejectDefaultFirmware(Model_Hardware.find.byId(UUID.fromString(payload))); break;
            default: throw new NotSupportedException("Unsupported action: " + action.name());
        }
    }
}
