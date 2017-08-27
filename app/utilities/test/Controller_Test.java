package utilities.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Dashboard;
import io.swagger.annotations.Api;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import play.Application;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.twirl.api.Html;
import utilities.Server;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;
import views.html.tyrion_developers.test;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Api(value = "Private Admin Api", hidden = true)
@Security.Authenticated(Secured_API.class)
public class Controller_Test extends Controller {


    @Inject
    Application application;

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Dashboard.class);


    public Result test(){
        try {


            List<String> fileNames = new ArrayList<>();
            File[] files = new File(application.path() + "/app/utilities/test/tests").listFiles();

            for (File file : files) {

                if(file.getName().equals(".DS_Store")) continue;
                if(file.getName().equals("resources")) continue;

                fileNames.add((file.getName().substring(0, file.getName().lastIndexOf('.'))));
            }

            Path path;

            try {
                path = Paths.get(application.path() + "/logs/test.log");
            }catch (Exception e){
                File file = new File(application.path() + "/logs/test.log");
                file.getParentFile().mkdirs();
                file.createNewFile();
                path = Paths.get(application.path() + "/logs/test.log");
            }

            String log =  new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            Html test_content = test.render(fileNames, log);
            return new Controller_Dashboard().return_page(test_content);

        }catch (Exception e){
            terminal_logger.internalServerError("test", e);
            return ok();
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result test_run(){
        try {

            if (Server.server_mode != Enum_Tyrion_Server_mode.developer) return GlobalResult.result_badRequest("Tests can be run only in dev mode.");

            terminal_logger.debug("test_run: Retrieving body");

            JsonNode body = request().body().asJson();
            if (!body.has("tests")) return GlobalResult.result_badRequest("Field 'tests' is required");

            List<String> test_names = new ArrayList<>();

            body.get("tests").forEach(node -> test_names.add(node.asText()));

            terminal_logger.debug("test_run: Test names: {}", test_names);

            List<Class> tests = new ArrayList<>();

            for (String name : test_names) {

                Class cls = Class.forName("utilities.test.tests." + name);
                if (cls == null) continue;

                tests.add(cls);
            }

            ObjectNode json = Json.newObject();

            ArrayNode results = Json.newArray();

            for (Class test : tests) {

                terminal_logger.debug("test_run: Beginning test = {}", test.getSimpleName());

                org.junit.runner.Result result = JUnitCore.runClasses(test);

                ArrayNode failures = Json.newArray();

                if (!result.wasSuccessful()) {

                    for (Failure failure : result.getFailures()) {

                        ObjectNode fail = Json.newObject();
                        fail.put("name", failure.getDescription().getMethodName());
                        fail.set("failure", Json.parse(failure.getMessage()));

                        failures.add(fail);
                    }
                }

                results.addObject()
                        .put("name", test.getSimpleName())
                        .put("successful", result.wasSuccessful())
                        .put("time", result.getRunTime())
                        .put("count", result.getRunCount())
                        .put("failed", result.getFailureCount())
                        .putArray("failures").addAll(failures);
            }

            json.set("results", results);

            return GlobalResult.result_ok(json);

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}
