package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuthService;
import io.swagger.annotations.*;
import models.persons.LinkedAccount;
import models.persons.Person;
import models.persons.PersonPermission;
import play.Configuration;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.*;
import utilities.loginEntities.Secured;
import utilities.loginEntities.Socials;
import utilities.response.CoreResponse;
import utilities.response.GlobalResult;
import utilities.response.response_objects.JsonValueMissing;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.documentationClass.Login_Social_Network;
import utilities.swagger.documentationClass.Login_return_object;

import javax.inject.Inject;
import javax.websocket.server.PathParam;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Api(value = "Ještě neroztříděné a neupravené")
public class SecurityController extends Controller {

    // Úvodní metoda - // TODO - nasadit základní zobrazovací šablonu o stavu serveru
    public Result index() {
        String link = "";
        link += "Server version: " + Configuration.root().getString("api.version") + "\n";
        link += "Developer mode = " + Configuration.root().getString("Server.developerMode") +"\n";
        link += "Connection to blocko server = " + Configuration.root().getString("Servers.blocko.server1.run") +"\n";
        return ok( link );
    }

    public static Person getPerson() {
        return (Person) Http.Context.current().args.get("person");
    }
    public static Person getPerson(Http.Context context) {
        return (Person) context.current().args.get("person");
    }

//######################################################################################################################


    @ApiOperation(value = "login",
            tags = {"Access", "Person", "APP-Api"},
            notes = "For login",
            produces = "application/json",
            response =  Login_return_object.class,
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
            @ApiResponse(code = 200, message = "Successful logged",      response = Login_return_object.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
        try {

            // There is Login_return_object for swagger //

            JsonNode json = request().body().asJson();

            Person person = Person.findByEmailAddressAndPassword(json.get("mail").asText(), json.get("password").asText());
            if (person == null) return GlobalResult.forbidden_Global("Email or password are wrong");


            if (!person.mailValidated) return GlobalResult.forbidden_Global("Account is not authorized");

            String authToken = person.createToken();

            ObjectNode result = Json.newObject();
            result.set("person", Json.toJson(person));
            result.put("authToken", authToken);
            result.set("roles", Json.toJson(person.roles));


            List<PersonPermission> list = PersonPermission.find.where().eq("roles.persons.id", person.id).findList();

            result.set("permission", Json.toJson(list));

            return GlobalResult.okResult(result);

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "mail - String", "password - String");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - login ERROR");
            Logger.error(request().body().asJson().toString());
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Person by token (after Oauth2 Login -> Facebook, GitHub, Twitter)",
            tags = {"Access", "Person", "Social-GitHub", "Social-Facebook"},
            notes = "If you want login to system with social networks - you can used facebook, github or twitter api " +
                    "just ask for token, server responds with object where is token and redirection link. Redirect user " +
                    "to this link and after returning to success page that you filled in ask for token, ask again to this api " +
                    "and server respond with Person Object and with Roles and Permissions lists",
            produces = "application/json",
            response =  Login_return_object.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful logged",      response = Login_return_object.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public  Result getPersonByToken(){
        try{

            String token = request().getHeader("X-AUTH-TOKEN");

            Person person = Person.findByAuthToken(token);
            if(person == null) return GlobalResult.forbidden_Global("Account is not authorized");

            ObjectNode result = Json.newObject();
            result.set("person", Json.toJson(person));
            result.put("authToken", token);
            result.set("roles", Json.toJson(person.roles));
            result.set("permission", Json.toJson(person.permissions));


            return GlobalResult.okResult(result);

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("PersonController - getPerson ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "logout",
            tags = {"Access", "Person", "APP-Api"},
            notes = "for logout person - that's deactivate person token ",
            produces = "application/json",
            response =  Result_ok.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "Logged in system", description = "Person must be logged in server")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful logged",      response = Result_ok.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result logout() {
        try {

            getPerson().deleteAuthToken();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - logout ERROR");
            return GlobalResult.internalServerError();
        }
    }

//#######################################################################################################################


//#### Oaut pro příjem Requestů zvenčí ##################################################################################

    @Inject WSClient ws;
    // Metoda slouží pro příjem autentifikačních klíču ze sociálních sítí když se přihlásí uživatel.
    // Taktéž spojuje přihlášené účty pod jednu cvirtuální Person - aby v systému bylo jendotné rozpoznávání.
    // V nějaké fázy je nutné mít mail - pokud ho nedostaneme od sociální služby - mělo by někde v kodu být upozornění pro fontEnd
    // Aby doplnil uživatel svůj mail - hlavní identifikátor!
    @ApiOperation( value = "GET_github_oauth", hidden = true)
    public Result GET_github_oauth(String url) {
        try {
            String state = "";
            String code = "";

            // Z důvodů nemožnosti nastavit tandartně matici query v hlavičce metody v routes (když se to povede vrací to jiné
            // parametry než když se to nepovede - bylo nutné přistoupit k trochu hloupému řešení, které však funguje za všech předpokladů

            final Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
            for (Map.Entry<String,String[]> entry : entries) {

                final String key = entry.getKey();
                final String value = Arrays.toString(entry.getValue());

                // Kontrola obsahu
                if(key.equals("code")) code = value.replace("[", "").replace("]", "");
                if(key.equals("state")) state = value.replace("[", "").replace("]", "");

                if(key.equals("error")){
                    if(state.length() > 1) LinkedAccount.find.where().eq("providerKey", state).findUnique().delete();
                    return redirect(Configuration.root().getString("Becki.redirectFail"));
                }

            }

            LinkedAccount linkedAccount = LinkedAccount.find.where().eq("providerKey", state).findUnique();
            if (linkedAccount == null) return redirect(Configuration.root().getString("Becki.redirectFail"));
            linkedAccount.tokenVerified = true;

            OAuthService service = Socials.GitHub(state);

            Verifier verifier = new Verifier(code);
            Token accessToken = service.getAccessToken(null, verifier);


            OAuthRequest request = new OAuthRequest(Verb.GET, Configuration.root().getString(linkedAccount.typeOfConnection + ".url"), service);
            service.signRequest(accessToken, request);

            Response response = request.send();
            if (!response.isSuccessful()) {
                Logger.error("Incoming state: " + state + " not found in database");
                redirect(Configuration.root().getString("Becki.redirectFail"));
            }

            System.out.println("Body = " + response.getBody());

            JsonNode jsonNode = Json.parse(response.getBody());


                // Zkontroluji zda už takové id nemám náhodou v databázi a pokud ano, zamezuji duplicitě
                LinkedAccount usedAccount = LinkedAccount.find.where().eq("providerUserId", jsonNode.get("id").asText()).findUnique();

                // Ovařím zda už v databázi není - pokud ano provedu následující metodu, ve které smažu poslední LinkedAccount
                // a používám už ten předchozí, jen prohodím a aktualizuji token! Poté přesměruji
                if (usedAccount != null) {

                    usedAccount.authToken = linkedAccount.authToken;
                    linkedAccount.delete();
                    usedAccount.update();
                    return redirect( Configuration.root().getString("Becki.mainUrl") + linkedAccount.returnUrl);

                }

                // Pokud se uživatel přihlásí poprvé
                else {

                    // Přiřadím do záznamu id uživatele ze sociální sítě
                    linkedAccount.providerUserId = jsonNode.get("id").asText();
                    linkedAccount.update();



                        Person person = new Person();

                        if (jsonNode.has("mail")) {
                            person.mail = jsonNode.get("mail").asText();
                        }

                        if (jsonNode.has("login")) {
                            person.nick_name = jsonNode.get("login").asText();
                        }

                        if (jsonNode.has("name")) {
                            try {
                                System.out.println("name: " + jsonNode.get("login").asText());
                                String[] parts = jsonNode.get("name").asText().split("\\s+");
                                person.first_name = parts[0];
                                person.last_name = parts[1];
                            }catch (ArrayIndexOutOfBoundsException e){
                                System.out.println("Uživatel nemá vyplněné jméno a příjmení s mezerou .. nebo jiná TODO aktivita");
                            }
                        }


                        person.setToken(linkedAccount.authToken); // update je v metodě už zahrnut!
                        person.save();

                        linkedAccount.person = person;
                        linkedAccount.update();

                        return redirect(Configuration.root().getString("Becki.mainUrl") + linkedAccount.returnUrl);


                }

        } catch (Exception e) {

            Logger.error("Error ");
            Logger.error("Error - Příchozí JSON v metodě GEToauth_callback neobsahuje identifikačnmí ID!");
            Logger.error("Error \n");
            Logger.error("Error", e);
            Logger.error("SecurityController - GEToauth_callback ERROR");
            return GlobalResult.internalServerError();
        }


    }


    @ApiOperation( value = "GET_github_oauth", hidden = true)
    public Result GET_facebook_oauth(String url) {
        try {

            String code = "";
            String state = "";

            // Z důvodů nemožnosti nastavit standartně matici query v hlavičce metody v routes (když se to povede vrací to jiné
            // parametry než když se to nepovede - bylo nutné přistoupit k trochu hloupému řešení, které však funguje za všech předpokladů
            final Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
            for (Map.Entry<String,String[]> entry : entries) {

                final String key = entry.getKey();
                final String value = Arrays.toString(entry.getValue());


                if(key.equals("code")) code = value.replace("[", "").replace("]", "");
                if(key.equals("state")) state = value.replace("[", "").replace("]", "");

                if(key.equals("error")){
                    if(state.length() > 1) LinkedAccount.find.where().eq("providerKey", state).findUnique().delete();
                    return redirect(Configuration.root().getString("Becki.redirectFail"));
                }

            }

            LinkedAccount linkedAccount = LinkedAccount.find.where().eq("providerKey", state).findUnique();
            if (linkedAccount == null) return redirect(Configuration.root().getString("Becki.redirectFail"));
            linkedAccount.tokenVerified = true;

            OAuthService  service = Socials.Facebook(state);

            Verifier verifier = new Verifier(code);
            Token accessToken = service.getAccessToken(null, verifier);

            OAuthRequest request = new OAuthRequest(Verb.GET, Configuration.root().getString(linkedAccount.typeOfConnection + ".url"), service);
            service.signRequest(accessToken, request);

            Response response = request.send();
            if (!response.isSuccessful()) {
                Logger.error("Incoming state: " + state + " not found in database");
                redirect(Configuration.root().getString("Becki.redirectFail"));
            }

            System.out.println("Body = " + response.getBody());

            JsonNode jsonNode = Json.parse(response.getBody());

           // System.out.println("Acces token je: " + accessToken.getToken());

            WSRequest wsrequest = ws.url("https://graph.facebook.com/v2.5/"+ jsonNode.get("id").asText());
            WSRequest complexRequest = wsrequest.setQueryParameter("access_token", accessToken.getToken())
                                                .setQueryParameter("fields", "id,name,first_name,last_name,email,birthday,languages");

            F.Promise<JsonNode> jsonPromise = wsrequest.get().map(rsp -> { return rsp.asJson();});
            JsonNode jsonRequest = jsonPromise.get(10000);

            System.out.println("Příchozí JSON: " + jsonRequest.toString());

            // Zkontroluji zda už takové id nemám náhodou v databázi a pokud ano, zamezuji duplicitě
           LinkedAccount usedAccount = LinkedAccount.find.where().eq("providerUserId", jsonRequest.get("id").asText()).findUnique();

           // Ověřím zda už v databázi není - pokud ano provedu následující metodu, ve které smažu poslední LinkedAccount
           // a používám už ten předchozí, jen prohodím a aktualizuji token! Poté přesměruji
           if (usedAccount != null) {

                usedAccount.authToken = linkedAccount.authToken;
                linkedAccount.delete();
                usedAccount.update();
                usedAccount.person.setToken(usedAccount.authToken);
                usedAccount.person.update();
                return redirect( Configuration.root().getString("Becki.mainUrl") + linkedAccount.returnUrl);

           }

           // Pokud se uživatel přihlásí poprvé
           else {

                // Přiřadím do záznamu id uživatele ze sociální sítě
                linkedAccount.providerUserId = jsonRequest.get("id").asText();
                linkedAccount.update();

                // A vytvářím osobu - Je nutné se podívat, zda náhodou už neexistuje daná osoba v systému
                // - pak bych tento LinkedAccount napojil na ní.


                // V ostatních případech - kdy mail nemám a tedy nemám šanci najít osobu pro marge

                        Person person = new Person();
                        if (jsonRequest.has("mail")) {
                            person.mail = jsonRequest.get("mail").asText();
                        }

                        if (jsonRequest.has("first_name")) {
                            person.first_name = jsonRequest.get("first_name").asText();
                        }
                        if (jsonRequest.has("last_name")) {
                            person.last_name = jsonRequest.get("last_name").asText();
                        }
                        if (jsonRequest.has("birthday")) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY");
                                person.date_of_birth = sdf.parse(jsonRequest.get("birthday").asText());
                            }catch (Exception e){
                                Logger.error("Error FACEBOOK");
                                Logger.error("Error - Příchozí JSON Obsahoval narozeniny - ale něco se pokazilo! " + jsonRequest.toString());
                                Logger.error("Error '");
                            }
                        }

                        person.setToken(linkedAccount.authToken);
                        person.save();

                        linkedAccount.person = person;
                        linkedAccount.update();

                        return redirect(Configuration.root().getString("Becki.mainUrl") + linkedAccount.returnUrl);


           }



        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - GET_facebook_oauth ERROR");
            return GlobalResult.internalServerError();
        }
    }


//###### Socilání sítě - a generátory přístupů ########################################################################*/

    @ApiOperation(value = "login with GitHub",
            tags = {"Access", "Social-GitHub"},
            notes = "For login via GitHub \n\n "+
                    "If you want login to system with social networks - you can used Facebook, GitHub, Twitter... api " +
                    "just ask via this Api and server responds with object where is token and redirection link. After that redirect user " +
                    "to this link and after returning to your success page you have to ask again (api - get Person by token ) for information about logged Person",
            produces = "application/json",
            response =  Login_Social_Network.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful created",      response = Login_Social_Network.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result GitHub(@ApiParam(value = "this is return url address in format  /link/link   ", required = true) @PathParam("return_link")  String return_link){
        try {
            LinkedAccount linkedAccount = LinkedAccount.setProviderKey("GitHub");

            linkedAccount.returnUrl = return_link;
            linkedAccount.update();

            OAuthService service = Socials.GitHub(linkedAccount.providerKey);

            Login_Social_Network result = new Login_Social_Network();
            result.type = "GitHub";
            result.redirect_url = service.getAuthorizationUrl(null);
            result.authToken = linkedAccount.authToken;

            return GlobalResult.okResult(Json.toJson(result));

        }catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - GitHub ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "login with Facebook",
            tags = {"Access", "Social-Facebook"},
            notes = "For login via Facebook \n\n "+
                    "If you want login to system with social networks - you can used Facebook, GitHub, Twitter... api " +
                    "just ask via this Api and server responds with object where is token and redirection link. After that redirect user " +
                    "to this link and after returning to your success page you have to ask again (api - get Person by token ) for information about logged Person",
            produces = "application/json",
            response =  Login_Social_Network.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful created",      response = Login_Social_Network.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result Facebook(@ApiParam(value = "this is return url address in format  ?return_link=/link/link ", required = true) @PathParam("return_link") String return_link){
        try {
            LinkedAccount linkedAccount = LinkedAccount.setProviderKey("Facebook");

            linkedAccount.returnUrl = return_link;
            linkedAccount.update();

            OAuthService service = Socials.Facebook(linkedAccount.providerKey);

            Login_Social_Network result = new Login_Social_Network();
            result.type = "Facebook";
            result.redirect_url = service.getAuthorizationUrl(null);
            result.authToken = linkedAccount.authToken;

            return GlobalResult.okResult(Json.toJson(result));

        }catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - facebook ERROR");

            return GlobalResult.internalServerError();
        }
    }

    // TODO implementovat twitter http://youtrack.byzance.cz/youtrack/issue/TYRION-49
    @ApiOperation( value = "login via Twitter", hidden = true)
    public Result Twitter(String returnLink){
        try {
            LinkedAccount linkedAccount = LinkedAccount.setProviderKey("Twitter");

            OAuthService service = Socials.Twitter(linkedAccount.providerKey);

        // Kraviny
            Token requestToken = service.getRequestToken();

            Login_Social_Network result = new Login_Social_Network();
            result.type = "GitHub";
            result.redirect_url = service.getAuthorizationUrl(null);
            result.authToken = linkedAccount.authToken;

            return GlobalResult.okResult(Json.toJson(result));

        }catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - Twitter ERROR");
            return GlobalResult.internalServerError();
        }
    }

    // TODO implementovat Vkontakte http://youtrack.byzance.cz/youtrack/issue/TYRION-49
    @ApiOperation( value = "login via Vkontakte", hidden = true)
    public Result Vkontakte(String returnLink){
        try {
            LinkedAccount linkedAccount = LinkedAccount.setProviderKey("Vkontakte");

            OAuthService service = Socials.Vkontakte(linkedAccount.providerKey);

            ObjectNode result = Json.newObject();
            result.put("type", "Vkontakte");
            result.put("url", service.getAuthorizationUrl(null));
            result.put("authToken", linkedAccount.authToken);

            return GlobalResult.okResult(result);

        }catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("SecurityController - facebook ERROR");
            return GlobalResult.internalServerError();
        }
    }


//###### Option########################################################################*/

    @ApiOperation( value = "option", hidden = true)
    public Result option(){
        return GlobalResult.okResult();
    }

    @ApiOperation( value = "optionLink", hidden = true)
    public Result optionLink(String url){
        CoreResponse.cors();
        response().setHeader("Access-Control-Link", url);
        return ok();
    }
}