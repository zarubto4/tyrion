package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.mindrot.jbcrypt.BCrypt;
import play.mvc.Result;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.logger.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ZZZ_Tester extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_ZZZ_Tester.class);

// CONTROLLER CONFIGURATION ############################################################################################

    // Nothing
    private Config config;

// CONTROLLER CONTENT ##################################################################################################
    @ApiOperation(value = "Hidden test Method", hidden = true)
     public Result test1() {
         try {


             return ok("Valid version for mode");

         } catch (Exception e) {
             logger.internalServerError(e);
             return badRequest();
         }
     }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test2() {
        try {

            // Device ID:  0049002E3136510236363332  With UserName:: 7dd9820d-4beb-4c8c-b019-d74ba53b8a4a  and Password:: 381fbc82-cddb-4648-a6b2-747507683d08
            // Device ID:  003100363136510236363332  With UserName:: 8194aeed-af63-405e-86eb-3c70f0f7c130  and Password:: 2deae8c8-58ec-4396-a29b-798415596808

            // Device ID:  003100363136510236363332  With UserName:: 8194aeed-af63-405e-86eb-3c70f0f7c130  and Password:: 2deae8c8-58ec-4396-a29b-798415596808

            String name = "8194aeed-af63-405e-86eb-3c70f0f7c130";
            String pass = "2deae8c8-58ec-4396-a29b-798415596808";

            String name_ss = BCrypt.hashpw(name, BCrypt.gensalt());
            String pass_ss = BCrypt.hashpw(pass, BCrypt.gensalt());

            System.err.println("UserName:: " + name_ss);
            System.err.println("Password:: " + pass_ss);


            if (BCrypt.checkpw(pass, pass_ss) && BCrypt.checkpw(name, name_ss)) {
                System.err.println("Prošlo to");
            } else {
                System.err.println("Ne-Prošlo to");
            }


           return ok();


        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3() {
        try {

            new Controller_Things_Mobile().test_of_all_apis();

            return ok();
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test4() {
        try {

            return ok();

        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(e);
        }
    }




}