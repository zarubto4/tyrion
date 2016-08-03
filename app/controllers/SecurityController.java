package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuthService;
import com.google.inject.Inject;
import io.swagger.annotations.*;
import models.person.FloatingPersonToken;
import models.person.Person;
import models.person.PersonPermission;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.*;
import utilities.Server;
import utilities.UtilTools;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_API;
import utilities.loginEntities.Socials;
import utilities.response.CoreResponse;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_JsonValueMissing;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.documentationClass.Login_IncomingLogin;
import utilities.swagger.outboundClass.Login_Social_Network;
import utilities.swagger.outboundClass.Swagger_Login_Token;
import utilities.swagger.outboundClass.Swagger_Person_All_Details;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api(value = "Not Documented API - InProgress or Stuck")
public class SecurityController extends Controller {

    // Rest Api call client
    @Inject WSClient ws;

//######################################################################################################################

    public static Person getPerson() {
        return (Person) Http.Context.current().args.get("person");
    }
    public static Person getPerson(Http.Context context) {
        return (Person) Http.Context.current().args.get("person");
    }

//######################################################################################################################

    @ApiOperation(value = "login",
            tags = {"Access", "Person", "APP-Api"},
            notes = "Get access Token",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Login_IncomingLogin",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully logged",       response = Swagger_Login_Token.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",   response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
        try {

            final Form<Login_IncomingLogin> form = Form.form(Login_IncomingLogin.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Login_IncomingLogin help = form.get();

            Person person = Person.findByEmailAddressAndPassword(help.mail, help.password);
            if (person == null) return GlobalResult.forbidden_Permission("Email or password are wrong");


            if (!person.mailValidated) return GlobalResult.result_BadRequest("Your account is not validated");


            FloatingPersonToken floatingPersonToken = new FloatingPersonToken();
            floatingPersonToken.set_basic_values();
            floatingPersonToken.person = person;

            if( Http.Context.current().request().headers().get("User-Agent")[0] != null) floatingPersonToken.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
            else  floatingPersonToken.user_agent = "Unknown browser";

            floatingPersonToken.save();

            Swagger_Login_Token swagger_login_token = new Swagger_Login_Token();
            swagger_login_token.authToken = floatingPersonToken.authToken;


            return GlobalResult.result_ok( Json.toJson( swagger_login_token ) );

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Person by token (after Oauth2 Login -> Facebook, GitHub, Twitter)",
            tags = {"Access", "Person", "Social-GitHub", "Social-Facebook"},
            notes = "If you want login to system with social networks - you can used facebook, github or twitter api " +
                    "just ask for token, server responds with object where is token and redirection link. Redirect user " +
                    "to this link and after returning to success page that you filled in ask for token, ask again to this api " +
                    "and server respond with Person Object and with Roles and Permissions lists",
            produces = "application/json",
            response =  Swagger_Person_All_Details.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully logged",     response = Swagger_Person_All_Details.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password", response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured_API.class)
    public  Result getPersonByToken(){
        try{

            String token = request().getHeader("X-AUTH-TOKEN");

            Person person = Person.findByAuthToken(token);
            if(person == null) return GlobalResult.forbidden_Permission("Account is not authorized");

            Swagger_Person_All_Details result = new Swagger_Person_All_Details();
            result.person = person;

            result.roles = person.roles;

            List<String> permissions = new ArrayList<>();
            for( PersonPermission m :  PersonPermission.find.where().eq("roles.persons.id", person.id).findList() ) permissions.add(m.value);

            result.permissions = permissions;

            return GlobalResult.result_ok( Json.toJson( result ) );


        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "logout",
            tags = {"Access", "Person", "APP-Api"},
            notes = "for logout person - that's deactivate person token ",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully logged out",   response = Result_ok.class),
            @ApiResponse(code = 400, message = "Some Json value Missing",   response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",   response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result logout() {
        try {

            try {
                String token = request().getHeader("X-AUTH-TOKEN");
                FloatingPersonToken.find.where().eq("authToken", token).findUnique().deleteAuthToken();

            }catch (Exception e){
                e.printStackTrace();
            }

            // JE nutné garantovat vždy odpověď ok za všech situací kromě kritického selhální
            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

//#### Oaut pro příjem Requestů zvenčí ################################################################################


    // Metoda slouží pro příjem autentifikačních klíču ze sociálních sítí když se přihlásí uživatel.
    // Taktéž spojuje přihlášené účty pod jednu cvirtuální Person - aby v systému bylo jendotné rozpoznávání.
    // V nějaké fázy je nutné mít mail - pokud ho nedostaneme od sociální služby - mělo by někde v kodu být upozornění pro frontEnd
    // Aby doplnil uživatel svůj mail - hlavní identifikátor!
    @ApiOperation( value = "GET_github_oauth", hidden = true)
    public Result GET_github_oauth(String url) {
        try {

            Map<String, String> map = UtilTools.getMap_From_querry(request().queryString().entrySet());

            if (map.containsKey("error")) {
                if (map.containsKey("state"))
                    FloatingPersonToken.find.where().eq("providerKey", map.get("state")).findUnique().delete();
                return redirect(Server.becki_redirectFail);
            }

            String state = map.get("state").replace("[", "").replace("]", "");
            String code = map.get("code").replace("[", "").replace("]", "");


            FloatingPersonToken floatingPersonToken = FloatingPersonToken.find.where().eq("providerKey", state).findUnique();
            if (floatingPersonToken == null) return redirect(Server.becki_redirectFail);
            floatingPersonToken.social_tokenVerified = true;

            OAuthService service = Socials.GitHub(state);
            Token accessToken = service.getAccessToken(null, new Verifier(code));

            OAuthRequest request = new OAuthRequest(Verb.GET, Server.GitHub_url, service);
            service.signRequest(accessToken, request);

            Response response = request.send();


            if (!response.isSuccessful()) redirect(Server.becki_redirectFail);


            JsonNode jsonNode = Json.parse(response.getBody());

            floatingPersonToken.providerUserId = jsonNode.get("id").asText();
            floatingPersonToken.update();


            List<FloatingPersonToken> before_registred = FloatingPersonToken.find.where().eq("providerUserId", floatingPersonToken.providerUserId).where().ne("connection_id", floatingPersonToken.connection_id).findList();
            if (!before_registred.isEmpty()) {
                System.out.println("Tento uživatel se nepřihlašuje poprvné");
                floatingPersonToken.person = before_registred.get(0).person;
                floatingPersonToken.update();

            } else {

                Person person = new Person();

                if (jsonNode.has("mail")) person.mail = jsonNode.get("mail").asText();
                if (jsonNode.has("login")) person.nick_name = jsonNode.get("login").asText();
                // TODO  + další info co lze z JSONu dostat


                if (jsonNode.has("name")) {
                    try {
                        System.out.println("name: " + jsonNode.get("login").asText());
                       person.full_name = jsonNode.get("name").asText();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Uživatel nemá vyplněné jméno a příjmení s mezerou .. nebo jiná TODO aktivita");
                    }
                }

                person.save();
                floatingPersonToken.person = person;
                floatingPersonToken.update();

            }

            return redirect(Server.becki_mainUrl + floatingPersonToken.returnUrl);


        } catch (Exception e) {
            return redirect( Server.becki_redirectFail );
        }


    }


    @ApiOperation( value = "GET_github_oauth", hidden = true)
    public Result GET_facebook_oauth(String url) {
        try {

            Map<String, String> map = UtilTools.getMap_From_querry(request().queryString().entrySet());

            if (map.containsKey("error")) {
                if (map.containsKey("state"))
                FloatingPersonToken.find.where().eq("providerKey", map.get("state")).findUnique().delete();
                return redirect(Server.becki_redirectFail);
            }

            String state = map.get("state").replace("[", "").replace("]", "");
            String code  = map.get("code").replace("[", "").replace("]", "");

            System.out.println("State: " + state);
            System.out.println("Code: " + code);


            FloatingPersonToken floatingPersonToken = FloatingPersonToken.find.where().eq("providerKey", state).findUnique();
            if (floatingPersonToken == null) return redirect(Server.becki_redirectFail);
            floatingPersonToken.social_tokenVerified = true;
            floatingPersonToken.setDate();

            OAuthService  service = Socials.Facebook(state);

            Token accessToken = service.getAccessToken(null, new Verifier(code) );

            OAuthRequest request = new OAuthRequest(Verb.GET, Server.Facebook_url, service);
            service.signRequest(accessToken, request);

            Response response = request.send();
            if (!response.isSuccessful()) redirect(Server.becki_redirectFail );


            JsonNode jsonNode = Json.parse(response.getBody());

            WSRequest wsrequest = ws.url("https://graph.facebook.com/v2.5/"+ jsonNode.get("id").asText());
            WSRequest complexRequest = wsrequest.setQueryParameter("access_token", accessToken.getToken())
                                                .setQueryParameter("fields", "id,name,first_name,last_name,email,birthday,languages");

            F.Promise<JsonNode> jsonPromise = wsrequest.get().map(rsp -> { return rsp.asJson();});
            JsonNode jsonRequest = jsonPromise.get(10000);

            List<FloatingPersonToken> before_registred = FloatingPersonToken.find.where().eq("providerUserId", jsonRequest.get("id").asText() ).where().ne("connection_id", floatingPersonToken.connection_id).findList();
            if (!before_registred.isEmpty()){
                floatingPersonToken.person = before_registred.get(0).person;
                floatingPersonToken.providerUserId = jsonRequest.get("id").asText();
                floatingPersonToken.update();
            }
            else{

                Person person = new Person();
                if (jsonRequest.has("mail")) person.mail = jsonRequest.get("mail").asText();
                if (jsonRequest.has("first_name")) person.full_name = jsonRequest.get("first_name").asText();

                if (jsonRequest.has("last_name")) person.full_name += " " +jsonRequest.get("last_name").asText();

                person.save();
                floatingPersonToken.person = person;
                floatingPersonToken.update();
            }

            return redirect(Server.becki_mainUrl + floatingPersonToken.returnUrl);


        } catch (Exception e) {
            return redirect( Server.becki_redirectFail );
        }
    }


//###### Socilání sítě - a generátory přístupů ########################################################################

    @ApiOperation(value = "login with GitHub",
            tags = {"Access", "Social-GitHub"},
            notes = "For login via GitHub \n\n "+
                    "If you want login to system with social networks - you can used Facebook, GitHub, Twitter... api " +
                    "just ask via this Api and cloud_blocko_server responds with object where is token and redirection link. After that redirect user " +
                    "to this link and after returning to your success page you have to ask again (api - get Person by token ) for information about logged Person",
            produces = "application/json",
            response =  Login_Social_Network.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created",    response = Login_Social_Network.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password", response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result GitHub( @ApiParam(value = "this is return url address in format  /link/link", required = true)  String return_link){
        try {

            FloatingPersonToken floatingPersonToken = FloatingPersonToken.setProviderKey("GitHub");

            floatingPersonToken.returnUrl = return_link;

            if( Http.Context.current().request().headers().get("User-Agent")[0] != null) floatingPersonToken.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
            else  floatingPersonToken.user_agent = "Unknown browser";

            floatingPersonToken.update();

            OAuthService service = Socials.GitHub( floatingPersonToken.providerKey);

            Login_Social_Network result = new Login_Social_Network();
            result.type = "GitHub";
            result.redirect_url = service.getAuthorizationUrl(null);
            result.authToken = floatingPersonToken.authToken;

            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "login with Facebook",
            tags = {"Access", "Social-Facebook"},
            notes = "For login via Facebook \n\n "+
                    "If you want login to system with social networks - you can used Facebook, GitHub, Twitter... api " +
                    "just ask via this Api and cloud_blocko_server responds with object where is token and redirection link. After that redirect user " +
                    "to this link and after returning to your success page you have to ask again (api - get Person by token ) for information about logged Person",
            produces = "application/json",
            response =  Login_Social_Network.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created",    response = Login_Social_Network.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password", response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result Facebook(@ApiParam(value = "this is return url address in format  ?return_link=/link/link", required = true)  String return_link){
        try {
            FloatingPersonToken floatingPersonToken = FloatingPersonToken.setProviderKey("Facebook");

            floatingPersonToken.returnUrl = return_link;

            if( Http.Context.current().request().headers().get("User-Agent")[0] != null) floatingPersonToken.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
            else  floatingPersonToken.user_agent = "Unknown browser";

            floatingPersonToken.update();

            OAuthService service = Socials.Facebook(floatingPersonToken.providerKey);

            Login_Social_Network result = new Login_Social_Network();
            result.type = "Facebook";
            result.redirect_url = service.getAuthorizationUrl(null);
            result.authToken = floatingPersonToken.authToken;

            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    // TODO implementovat twitter http://youtrack.byzance.cz/youtrack/issue/TYRION-49
    @ApiOperation( value = "login via Twitter", hidden = true)
    public Result Twitter(String returnLink){
        try {
            FloatingPersonToken floatingPersonToken = FloatingPersonToken.setProviderKey("Twitter");

            OAuthService service = Socials.Twitter(floatingPersonToken.providerKey);

            Token requestToken = service.getRequestToken();

            Login_Social_Network result = new Login_Social_Network();
            result.type = "GitHub";
            result.redirect_url = service.getAuthorizationUrl(null);
            result.authToken = floatingPersonToken.authToken;

            return GlobalResult.result_ok(Json.toJson(result));

        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    // TODO implementovat Vkontakte http://youtrack.byzance.cz/youtrack/issue/TYRION-49
    @ApiOperation( value = "login via Vkontakte", hidden = true)
    public Result Vkontakte(String returnLink){
        try {
            FloatingPersonToken floatingPersonToken = FloatingPersonToken.setProviderKey("Vkontakte");

            OAuthService service = Socials.Vkontakte(floatingPersonToken.providerKey);

            ObjectNode result = Json.newObject();
            result.put("type", "Vkontakte");
            result.put("url", service.getAuthorizationUrl(null));
            result.put("authToken", floatingPersonToken.authToken);

            return GlobalResult.result_ok(result);

        }catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }


///###### Option########################################################################################################

    @ApiOperation( value = "option", hidden = true)
    public Result option(){

        return GlobalResult.result_ok();
    }

    @ApiOperation( value = "optionLink", hidden = true)
    public Result optionLink(String url){

        System.out.print("Opetion!!! " + request().toString() );
        CoreResponse.cors(url);
        return ok();

    }
}

// curl -X PUT --header 'Content-Type: application/json' --header 'Accept: application/json' 'http://127.0.0.1:9000/project/b_program/uploadToCloud/1'
// curl -X PUT -H "http://localhost:9000/project/b_program/uploadToCloud/21"