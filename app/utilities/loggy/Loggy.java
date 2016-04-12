package utilities.loggy;

import com.google.inject.Inject;
import controllers.SecurityController;
import play.libs.ws.WSClient;
import play.mvc.Http;
import play.mvc.Result;
import utilities.Server;
import utilities.response.GlobalResult;

import java.net.NetworkInterface;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Loggy{

    @Inject WSClient ws;

    // Vlastní Loggy objekt definovaný konfigurací
    static play.Logger.ALogger logger = play.Logger.of("Loggy");


    public static Result result_internalServerError(Exception exception, Http.Request request) {
        StringBuilder builder = new StringBuilder();

        builder.append("Internal Server Error");
        builder.append("\n");
        builder.append("    Time: "+ new Date().toString());
        builder.append("\n");
        builder.append("    Request Type: " + request.method());
        builder.append("\n");
        builder.append("    Request Path: " + request.path());
        builder.append("\n");
        builder.append("    Unique Identificator: "+ UUID.randomUUID().toString());
        builder.append("\n");
        builder.append("    Tyrion version: "+ Server.server_version);
        builder.append("\n");
        builder.append("    Tyrion mode: "+ Server.server_mode);
        builder.append("\n");
        builder.append("    Server MAC address: "+ getMac());
        builder.append("\n");
        builder.append("    User: "+ (SecurityController.getPerson()!= null?SecurityController.getPerson().mail:"null"));
        builder.append("\n");

        builder.append("    Stack trace: \n");
        for (StackTraceElement element : exception.getStackTrace()) {
            builder.append("        " + element);
            builder.append("\n");
        }

        builder.append("\n");
        builder.append("\n");

        logger.error(builder.toString());
        return GlobalResult.internalServerError();


    }

    public static Result result_internalServerError(String problem, Http.Request request) {

        StringBuilder builder = new StringBuilder();
        builder.append("Internal Server Error");
        builder.append("\n");
        builder.append("    Time: "+ new Date().toString());
        builder.append("\n");
        builder.append("    Individual description: "+ problem);
        builder.append("\n");
        builder.append("    Request Type: " + request.method());
        builder.append("\n");
        builder.append("    Request Path: " + request.path());
        builder.append("\n");
        builder.append("    Unique Identificator: "+ UUID.randomUUID().toString());
        builder.append("\n");
        builder.append("    Tyrion version: "+ Server.server_version);
        builder.append("\n");
        builder.append("    Tyrion mode: "+ Server.server_mode);
        builder.append("\n");
        builder.append("    Server MAC address: "+ getMac());
        builder.append("\n");
        builder.append("    User: "+ (SecurityController.getPerson()!= null?SecurityController.getPerson().mail:"null"));
        builder.append("\n");

        builder.append("    Stack trace: \n");
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            builder.append("        " + element);
            builder.append("\n");
        }

        builder.append("\n");
        builder.append("\n");

        logger.error(builder.toString());
        return GlobalResult.internalServerError();
    }

    // Vracím počet zaznamenaných bugů v souboru
    public static Integer number_of_reported_bugs(){
        return 25; // TODO - dodělat načítání počtu chyb
    }

    // Vracím počet zaznamenaných bugů v souboru
    public static void remove_bug_from_file(String bug_identificator){
        // TODO - smazat bug ze souboru
    }

    public static void remove_all_bugs(){
        // TODO - vyprázdnit soubor
    }

    public static void upload_to_youtrack(String bug_id) {}

    // Vracím v poli všechny chyby ze souboru (kde zaznamenávám chyby
    public static List<LoggyError> getErrors(Integer a){
        // TODO - dodělat načítání a separování chyb v souboru
        return null;
    }



    private static String getMac() {
        StringBuilder builder = new StringBuilder();
        try {
            byte[] mac = NetworkInterface
                    .getNetworkInterfaces()
                    .nextElement()
                    .getHardwareAddress();

            for (int i = 0; i < mac.length; i++) {
                builder.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
        }
        catch (Exception e) {
            play.Logger.error("network problem", e);
        }
        return builder.toString();
    }



}


