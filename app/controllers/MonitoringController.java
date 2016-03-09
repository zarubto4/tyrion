package controllers;

import io.swagger.annotations.Api;
import play.mvc.Controller;
import play.mvc.Security;
import utilities.loginEntities.Secured;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured.class)
public class MonitoringController  extends Controller {

}
