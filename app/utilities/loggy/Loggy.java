package utilities.loggy;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.SecurityController;
import models.persons.Person;
import play.Configuration;
import play.Logger;
import play.Play;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Result;
import play.mvc.Results;
import utilities.response.GlobalResult;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Loggy {

    @Inject static WSClient ws;
    static Logger.ALogger logger = Logger.of("Loggy");
    static LinkedList<LoggyError> fastErrors = new LinkedList<>();
    static Configuration conf = Play.application().configuration();
    static long tokenExpire = 0;
    static String token = "";




    public static void debug(String content) {
        logger.debug(content);
    }

    public static void info(String content) {
        logger.info(content);
    }

    public static void warn(String content) {
        logger.warn(content);
    }

    public static void trace(String content) {
        logger.trace(content);
    }

    public static Result internalServerError(String url_request, String class_name, String method_name, Person person, Exception e) {


        error("Internal Server Error - Path: " + url_request + " Class: "+ class_name + "." + method_name + ((SecurityController.getPerson() != null ) ? " Person id: " + SecurityController.getPerson().id :  "" ), e.toString());
        return GlobalResult.internalServerError();
    }

    public static List<LoggyError> getErrors(int count) {
        return getErrors(0, count);
    }

    public static List<LoggyError> getErrors(int start, int count) {
        if(count == 0) {
            count = Integer.MAX_VALUE;
        }
        List<LoggyError> l = fastErrors.stream().skip(start).limit(count).collect(Collectors.toList());
        if (l.size()<count) {
            l.addAll(loadErrors(start+l.size(), count-l.size()));
        }

        return l;
    }

    public static Promise<Result> upload(int id) {
        if (System.currentTimeMillis() > tokenExpire-10000) {
            return login().flatMap((result) -> upload(id));
        }

        List<LoggyError> eL = getErrors(id, 1);
        if (fastErrors.size() == 0) {
            return Promise.promise(Results::badRequest);
        }
        LoggyError e = eL.get(0);

        WSRequest request = ws.url(conf.getString("Loggy.youtrackUrl") + "/youtrack/rest/issue");
        request.setQueryParameter("project", conf.getString("Loggy.youtrackProject"));
        request.setQueryParameter("summary", e.getSummmary());
        request.setQueryParameter("description", e.getDescription());
        request.setHeader("Authorization", "Bearer "+token);

        Promise<WSResponse> promise = request.put("");
        return promise.map(response -> checkUploadResponse(response, e));
    }

    public static void deleteFast() {
        fastErrors.clear();
    }

    public static void deleteFile() {
        File errors =Play.application().getFile("logs/loggyErrors.log");
        File all =Play.application().getFile("logs/loggyAll.log");

        try {
            new PrintWriter(errors).close();
            new PrintWriter(all).close();
        } catch (Exception e) {}
    }

    public static void error(String content) {
        error("Unnamed Error", content);
    }

    public static void error(String summary, String content) {
        logger.error(summary+"%%%"+content);

        if (fastErrors.size() >= conf.getInt("Loggy.fastCapacity")) {
            fastErrors.remove(fastErrors.size()-1);
        }
        fastErrors.push(new LoggyError(summary, content));
    }

    public static void start() {
        ws = Play.application().injector().instanceOf(WSClient.class);

        loadFastErrors();
    }

    public static void loadFastErrors() {
        fastErrors.addAll(loadErrors(
                fastErrors.size(),
                conf.getInt("Loggy.fastCapacity")-fastErrors.size()
        ));
    }

    public static Promise<Result> login() {
        WSRequest request = ws.url(conf.getString("Loggy.youtrackUrl") + "/hub/rest/oauth2/token");
        request.setContentType("application/x-www-form-urlencoded");
        request.setHeader("Authorization",
                "Basic "+ new String(Base64.getEncoder().encode(
                        (conf.getString("Loggy.youtrackId") + ":" + conf.getString("Loggy.youtrackSecret"))
                                .getBytes())));
        Promise<WSResponse> promise = request.post(
                "grant_type=password"
                +"&scope="+conf.getString("Loggy.youtrackScopeId")
                +"&username="+conf.getString("Loggy.youtrackUsername")
                +"&password="+conf.getString("Loggy.youtrackPassword")
        );

        return promise.map(Loggy::checkLoginResponse);
    }

    private static Result checkUploadResponse(WSResponse response, LoggyError error) {
        if (response.getStatus() == 201) {
            error.url = response.getHeader("Location").replace("/rest", "");
            return Results.redirect("/loggy");
        }

        return Results.status(response.getStatus(), response.getBody());
    }

    private static Result checkLoginResponse(WSResponse response) {
        if (response.getStatus() == 200) {
            JsonNode content = response.asJson();
            token = content.get("access_token").asText();
            tokenExpire = System.currentTimeMillis() + content.get("expires_in").asLong()*1000;
            return Results.ok("login successful");
        }

        else return Results.status(response.getStatus(), response.getBody());
    }

    private static List<LoggyError> loadErrors(int start, int count) {
        LinkedList<LoggyError> l = null;
        try {
            File f =Play.application().getFile("logs/loggyErrors.log");
            l = new LinkedList<>();

            LineNumberReader linesReader = new LineNumberReader(new FileReader(f));
            linesReader.skip(Long.MAX_VALUE);
            int lines = linesReader.getLineNumber();
            linesReader.close();

            LineNumberReader reader = new LineNumberReader(new FileReader(f));
            String line;
            while(!(line = reader.readLine()).equals("") && reader.getLineNumber() <= lines-start) {
                if (reader.getLineNumber() <= lines-(count+start)) {
                    continue;
                }
                String[] splitLine = line.split("%%%");
                l.push(new LoggyError(splitLine[0], splitLine[1]));
            }
            reader.close();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        return l;
    }
}


