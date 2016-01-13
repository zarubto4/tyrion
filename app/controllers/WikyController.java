package controllers;


import play.mvc.Controller;
import play.mvc.Security;
import utilities.Secured;

@Security.Authenticated(Secured.class)
public class WikyController extends Controller {





}
