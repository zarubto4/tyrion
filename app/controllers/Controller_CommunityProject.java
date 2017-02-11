package controllers;

import io.swagger.annotations.Api;
import play.mvc.Controller;
import play.mvc.Security;
import utilities.login_entities.Secured_API;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured_API.class)
public class Controller_CommunityProject extends Controller {

}
