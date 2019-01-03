package utilities.model;

import com.google.inject.Inject;
import models.Model_Project;
import utilities.logger.Logger;
import utilities.notifications.NotificationService;

/**
 * This service will inform the portal if any change occurs on some model object.
 * If a model is saved or deleted then the parent model has to be updated in portal.
 */
public class EchoService {

    private static final Logger logger = new Logger(EchoService.class);

    private final NotificationService notificationService;

    @Inject
    public EchoService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void save(Echo model) {
        model.save();
        this.echoParent(model);
    }

    public void update(Echo model) {
        model.update();
        this.echo(model);
    }


    public void delete(Echo model) {
        Echo parent = model.getParent();
        model.delete();
        if (parent != null) {
            this.echo(parent);
        }
    }

    public void onSaved(Echo model) {
        this.echoParent(model);
    }

    public void onUpdated(Echo model) {
        this.echo(model);
    }

    private void echo(Echo echo) {
        if (echo.isPublic()) {
            this.notificationService.modelUpdated(echo.getClass(), echo.getId(), null);
        } else {
            Model_Project project = echo.getProject();
            if (project != null) {
                this.notificationService.modelUpdated(echo.getClass(), echo.getId(), project.getId());
            }
        }
    }

    private void echoParent(Echo echo) {
        Echo parent = echo.getParent();
        if (parent != null) {
            this.echo(parent);
        }
    }
}
