package controllers;

import play.mvc.Controller;
import play.mvc.Security;
import utilities.loginEntities.Secured;


@Security.Authenticated(Secured.class)
public class MonitoringController  extends Controller {

}
