package utilities.test;


import com.google.inject.Inject;
import play.Application;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.loggy.Loggy;
import utilities.response.GlobalResult;

import java.io.*;

class StreamGobbler extends Thread
{
    InputStream is;
    String type;

    StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ( (line = br.readLine()) != null)
                System.out.println(type + ">" + line);
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}


public class TestLauncher extends Controller{

    @Inject
    Application application;

    public Result launch_test(String cmd) {

        try {

            PrintWriter writer = new PrintWriter(new File(application.path() + "/logs/test.log"));
            writer.close();

            String os = System.getProperty("os.name").toLowerCase();

            Runtime rt = Runtime.getRuntime();
            Process pr = null;

            if(cmd.contains("_")) cmd = cmd.replace("_"," ");

            if (os.contains("win")) {

                pr = rt.exec("cmd /c activator \"" + cmd + "\"");
                System.out.println("Running test on Windows");

            } else if (os.contains("mac")) {

                pr = rt.exec("activator \"" + cmd + "\"");
                System.out.println("Running test on Mac");

            } else if ((os.contains("nix"))||(os.contains("nux"))||(os.contains("aix"))) {

                pr = rt.exec("activator \"" + cmd + "\"");
                System.out.println("Running test on Linux");

            } else {
                return GlobalResult.badRequest("This OS is not supported.");
            }


            StreamGobbler errorGobbler = new
                    StreamGobbler(pr.getErrorStream(), "ERROR");

            StreamGobbler outputGobbler = new
                    StreamGobbler(pr.getInputStream(), "TEST - OUTPUT");

            errorGobbler.start();
            outputGobbler.start();

            int exitVal = pr.waitFor();
            System.out.println("Exited with error code " + exitVal);

            return GlobalResult.result_ok("Test was successfully run. See the test log for errors.");

        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }
}
