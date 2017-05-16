package utilities.test;

import controllers.Controller_Board;
import controllers.Controller_Dashboard;
import io.swagger.annotations.Api;
import play.Application;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.twirl.api.Html;
import utilities.logger.Class_Logger;
import utilities.login_entities.Secured_Admin;
import views.html.tyrion_developers.test;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Api(value = "Private Admin Api", hidden = true)
@Security.Authenticated(Secured_Admin.class)
public class Controller_Test extends Controller {


    @Inject
    Application application;

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Dashboard.class);


    public Result test(){
        try {


            List<String> fileNames = new ArrayList<>();
            File[] files = new File(application.path() + "/test").listFiles();

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
            terminal_logger.internalServerError(e);
            return ok();
        }
    }
}
