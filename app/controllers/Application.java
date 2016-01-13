package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    public Result index(){
        String accept_language = request().getHeader(ACCEPT_LANGUAGE);

        return ok(" Vše je ok  \n Jazyková mutace je " + accept_language);
    }



}


