package _projects.cez;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.swagger.annotations.Api;
import mongo.mongo_services._MongoNativeConnector;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import utilities.logger.Logger;
import utilities.model.EchoService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;


// @Security.Authenticated(Authentication.class)
@Api(value = "CEZ")
@SuppressWarnings({"rawtypes", "unchecked"})
public class Controller_CEZ extends _BaseController {

    private HttpExecutionContext httpExecutionContext;

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(_projects.eon.Controller_EON.class);

    @Inject
    public Controller_CEZ(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService,
                          NotificationService notificationService, EchoService echoService, _MongoNativeConnector mongoNativConnector, HttpExecutionContext httpExecutionContext) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);
        this.httpExecutionContext = httpExecutionContext;
    }


}
