package utilities.test;

import play.Application;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.logger.Server_Logger;
import utilities.response.GlobalResult;

import javax.inject.Inject;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestLog extends Controller {

    @Inject
    Application application;

    public Result get_test_log() {

        try {

            String content =  new String(Files.readAllBytes(Paths.get(application.path() + "/logs/test.log")), StandardCharsets.UTF_8);

            return GlobalResult.result_ok(content);

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result clear_test_log() {

        try {

            PrintWriter writer = new PrintWriter(new File(application.path() + "/logs/test.log"));
            writer.close();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}
