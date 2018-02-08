package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.*;
import play.mvc.Result;
import utilities.logger.Logger;
import websocket.messages.tyrion_with_becki.WS_Message_Online_Change_status;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ZZZ_Tester extends BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_ZZZ_Tester.class);

    @ApiOperation(value = "Hidden test Method", hidden = true)
     public Result test1() {
         try {

             JsonNode json = request().body().asJson();

             String tag = json.get("tag").asText();

             switch (json.get("mode").asText()) {
                 case "developer": {
                     Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)(-(.)*)?$");
                     Matcher matcher = pattern.matcher(tag);
                     if (!matcher.find()) {
                         return badRequest("Invalid version");
                     }
                     break;
                 }
                 case "stage": {
                     Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)(-beta(.)*)?$");
                     Matcher matcher = pattern.matcher(tag);
                     if (!matcher.find()) {
                         return badRequest("Invalid version");
                     }
                     break;
                 }
                 case "production": {
                     Pattern pattern = Pattern.compile("^(v)(\\d+\\.)(\\d+\\.)(\\d+)$");
                     Matcher matcher = pattern.matcher(tag);
                     if (!matcher.find()) {
                         return badRequest("Invalid version");
                     }
                     break;
                 }
                 default: // empty
             }

             return ok("Valid version for mode");

         } catch (Exception e) {
             logger.internalServerError(e);
             return badRequest();
         }
     }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test2() {
        try {

            // logger.error(BCrypt.hashpw("password", BCrypt.gensalt(12)));
            // Test online change stavů
            JsonNode json = request().body().asJson();

            String type = json.get("type").asText();
            UUID id = UUID.fromString(json.get("id").asText());
            UUID project_id = UUID.fromString(json.get("project_id").asText());
            boolean online_state = json.get("online_state").asBoolean();

            if (type.equals("board")) {
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Hardware.class, id, online_state, project_id);
            }

            if (type.equals("instance")) {
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Instance.class, id, online_state, project_id);
            }

            if (type.equals("server")) {
                WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_HomerServer.class, id, online_state, project_id);
            }

            return ok(json);

        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3() {
        try {

            return okEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test4() {
        try {

            return okEmpty();

        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(e);
        }
    }




}