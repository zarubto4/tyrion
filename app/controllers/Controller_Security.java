package controllers;

import com.google.inject.Inject;
import io.swagger.annotations.*;
import models.*;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.*;
import responses.*;
import utilities.authentication.Authentication;
import utilities.enums.TokenType;
import utilities.enums.PlatformAccess;
import utilities.financial.FinancialPermission;
import utilities.swagger.input.Swagger_EmailAndPassword;
import utilities.swagger.output.Swagger_Blocko_Token_validation_result;
import utilities.swagger.output.Swagger_Login_Token;
import utilities.swagger.output.Swagger_Person_All_Details;
import utilities.threads.Check_Online_Status_after_user_login;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_Blocko_Token_validation_request;
import websocket.interfaces.WS_Portal;

import java.util.*;

/**
 * Třída Controller_Security slouží k ověřování uživatelů pro přihlášení i odhlášení a to jak pro Becki, tak i Administraci Tyriona.
 *
 * Dále ověřuje validitu tokenů na Homer serveru, na Compilačním serveru, platnost Rest-API reqest tokenů (a jejich počet)
 */
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Security extends BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Security.class);

//######################################################################################################################

    private FormFactory formFactory;
    private WSClient ws;

    @Inject
    public Controller_Security(FormFactory formFactory, WSClient ws) {
        this.formFactory = formFactory;
        this.ws = ws;
    }

//######################################################################################################################

    @ApiOperation(value = "check Request Token",
            tags = {"Blocko"},
            notes = "",
            produces = "application/json",
            response =  Swagger_Blocko_Token_validation_result.class,
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Blocko_Token_validation_request",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Blocko_Token_validation_result.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_status_request_token() {
        try {

            // Zpracování Json
            final Form<Swagger_Blocko_Token_validation_request> form = formFactory.form(Swagger_Blocko_Token_validation_request.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_Blocko_Token_validation_request help = form.get();

            TokenType token_type = TokenType.getType(help.type_of_token);
            if (token_type == null) return badRequest("Wrong type of token");

            Swagger_Blocko_Token_validation_result result = new Swagger_Blocko_Token_validation_result();

            if (token_type == TokenType.PERSON_TOKEN) {

                Model_Person person = Model_Person.getByAuthToken(UUID.fromString(help.token));
                if (person == null) return notFound("Token not found");

                result.token = help.token;
                result.available_requests = 50L;
            }

            if (token_type == TokenType.INSTANCE_TOKEN) {

                Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.getById(help.token);
                if (snapshot == null) return notFound("Token not found");

                result.token = help.token;
                result.available_requests = FinancialPermission.checkRestApiRequest(snapshot.getProduct(), snapshot.id);
            }

            return ok(Json.toJson(result));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

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
                            dataType = "utilities.swagger.input.Swagger_EmailAndPassword",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully logged",       response = Swagger_Login_Token.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",   response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class),
            @ApiResponse(code = 705, message = "Account not validated",     response = Result_NotValidated.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
        try {

            // Kontrola JSON
            final Form<Swagger_EmailAndPassword> form = formFactory.form(Swagger_EmailAndPassword.class).bindFromRequest();
            if (form.hasErrors()) {return invalidBody(form.errorsAsJson());}
            Swagger_EmailAndPassword help = form.get();

            // Ověření Person - Heslo a email
            Model_Person person = Model_Person.getByEmail(help.email);
            if (person == null || !person.checkPassword(help.password)) {
                logger.trace("Email {} or password are wrong", help.email);
                return forbidden("Email or password is wrong");
            }

            // Kontrola validity - jestli byl ověřen přes email
            // Jestli není účet blokován
            if (!person.validated) return notValidated();
            if (person.frozen) return badRequest("Your account has been temporarily suspended");

            // Volání Cache
            new Check_Online_Status_after_user_login(person.id).run();

            // Vytvářim objekt tokenu pro přihlášení (na něj jsou vázány co uživatel kde a jak dělá) - Historie pro využití v MongoDB widgety atd..
            Model_AuthorizationToken token = new Model_AuthorizationToken();
            token.person = person;
            token.where_logged  = PlatformAccess.BECKI_WEBSITE;

            // Zjistím kde je přihlášení (user-Agent je třeba "Safari v1.30" nebo "Chrome 12.43" atd..)
            if ( Http.Context.current().request().headers().get("User-Agent")[0] != null) token.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
            else  token.user_agent = "Unknown browser";

            // Ukládám do databáze
            token.save();

            // Ukládám do Cahce pamětí pro další operace
            Model_Person.token_cache.put(token.token, person.id);

            // Chache Update
            Model_Person.getById(person.id);


            // Vytvářím objekt, který zasílám zpět frontendu
            Swagger_Login_Token swagger_login_token = new Swagger_Login_Token();
            swagger_login_token.authToken = token.token;

            // Odesílám odpověď
            return ok(Json.toJson(swagger_login_token));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    // Když se uživatel přihlásí, dostane pouze Token. Ale aby mohl načíst základní projekty, atd. Tato metoda
    // mu výměnou za Token vrátí celkový přehled (práva atd.)
    @ApiOperation(value = "get Person by token",
            tags = {"Access", "Person", "Social-GitHub", "Social-Facebook"},
            notes = "If you want login to system with social networks - you can used facebook, github or twitter api " +
                    "just ask for token, server responds with object where is token and redirection link. Redirect user " +
                    "to this link and after returning to success page that you filled in ask for token, ask again to this api " +
                    "and server respond with Person Object and with Roles and Permissions lists",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully logged",       response = Swagger_Person_All_Details.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",   response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public  Result person_get_by_token() {
        try {

            Model_Person person = person();
            if (person == null) return forbidden("Account is not authorized");

            Swagger_Person_All_Details result = new Swagger_Person_All_Details();
            result.person = person;

            result.roles = person.roles;

            List<String> permissions = new ArrayList<>();
            for ( Model_Permission m :  Model_Permission.find.query().where().eq("roles.persons.id", person.id).findList() ) permissions.add(m.name);

            result.permissions = permissions;

            return ok(Json.toJson(result));


        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "logout",
            tags = {"Access", "Person", "APP-Api"},
            notes = "for logout person - that's deactivate person token ",
            produces = "application/json",
            consumes = "text/html",
            response =  Result_Ok.class,
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully logged out",   response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",   response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result logout() {
        try {

            Optional<String> optional = request().header("X-AUTH-TOKEN");
            if (optional.isPresent()) {

                UUID token = UUID.fromString(optional.get());
                Model_Person.token_cache.remove(token);

                Model_AuthorizationToken token_model = Model_AuthorizationToken.getByToken(token);
                if (token_model != null) {

                    // Úklid přihlášených websocketů
                    WS_Portal portal = Controller_WebSocket.portals.get(personId());
                    if (portal != null) portal.close(token);

                    token_model.delete();
                }
            }

            return okEmpty();

        } catch (Exception e) {
            logger.internalServerError(e);
            return okEmpty();
        }
    }

//#### Oaut pro příjem Requestů zvenčí ################################################################################
/* TODO

    // Metoda slouží pro příjem autentifikačních klíču ze sociálních sítí když se přihlásí uživatel.
    // Taktéž spojuje přihlášené účty pod jednu cvirtuální Person - aby v systému bylo jendotné rozpoznávání.
    // V nějaké fázy je nutné mít mail - pokud ho nedostaneme od sociální služby - mělo by někde v kodu být upozornění pro frontEnd
    // Aby doplnil uživatel svůj mail - hlavní identifikátor!
    @ApiOperation( value = "GET_github_oauth", hidden = true)
    public Result github_oauth_get(String url) {
        try {

            logger.debug("GET_github_oauth:: " + url);

            Map<String, String> map = UtilTools.getMap_From_query(request().queryString().entrySet());

            if (map.containsKey("error")) {

                logger.warn("GET_github_oauth::  contains Error: {} " , map.get("error"));

                if (map.containsKey("state")) {

                    Model_AuthorizationToken floatingPersonToken = Model_AuthorizationToken.find.query().where().eq("provider_key", map.get("state")).findOne();
                    floatingPersonToken.delete();

                    return redirect(floatingPersonToken.return_url.replace("[_status_]", "fail"));

                }
            }

            String state = map.get("state").replace("[", "").replace("]", "");
            String code = map.get("code").replace("[", "").replace("]", "");


            logger.debug("GET_github_oauth:: state" + state);
            logger.debug("GET_github_oauth:: code" + code);

            Model_AuthorizationToken floatingPersonToken = Model_AuthorizationToken.find.query().where().eq("provider_key", state).findOne();
            if (floatingPersonToken == null) {

               logger.internalServerError(new Exception("GET_github_oauth:: Not recognize URL fragment!"));

                return redirect(floatingPersonToken.return_url.replace("[_status_]", "fail")); // TODO TOM musí vyhodit NullPointerException
            }

            floatingPersonToken.social_token_verified = true;

            OAuth20Service service = Socials.GitHub(state);
            OAuth2AccessToken accessToken = service.getAccessToken(code);


            OAuthRequest request = new OAuthRequest(Verb.GET,Server.GitHub_url);
            service.signRequest(accessToken, request);

            Response response = service.execute(request);


            if (!response.isSuccessful()) redirect(Server.becki_mainUrl + "/" + Server.becki_redirectFail);


            JsonNode json_response_from_github = Json.parse(response.getBody());

            floatingPersonToken.provider_user_id = json_response_from_github.get("id").asText();
            floatingPersonToken.update();


            Model_Person person = Model_Person.find.query().where().eq("github_oauth_id",json_response_from_github.get("id").asText() ).findOne();
            if (person != null) {

                System.out.println("Tento uživatel se nepřihlašuje poprvné");

                // Zrevidovat stav??
                if (json_response_from_github.has("mail"))   person.mail = json_response_from_github.get("mail").asText();
                if (json_response_from_github.has("login"))  person.nick_name = json_response_from_github.get("login").asText();
                if (json_response_from_github.has("name") && json_response_from_github.get("name") != null &&  !json_response_from_github.get("name").equals("") && !json_response_from_github.get("name").equals("null"))   person.full_name = json_response_from_github.get("name").asText();
                if (json_response_from_github.has("avatar_url")) person.alternative_picture_link = json_response_from_github.get("avatar_url").asText();
                person.update();

                floatingPersonToken.person = person;
                floatingPersonToken.update();

            } else {

                System.out.println("Tento uživatel se přihlašuje poprvé");
                System.out.println("Json::" + json_response_from_github.toString());

                if (json_response_from_github.has("mail")) person = Model_Person.find.query().where().eq("mail", json_response_from_github.get("mail").asText()).findOne();


                if (person != null) {

                    System.out.println("13. Uživatel existuje s emailem ale bez github tokenu - a tak jen doplním token");

                    person = Model_Person.find.query().where().eq("mail", json_response_from_github.get("mail").asText()).findOne();
                    person.github_oauth_id = json_response_from_github.get("id").asText();
                    if (json_response_from_github.has("name") && json_response_from_github.get("name") != null &&  !json_response_from_github.get("name").equals("") && !json_response_from_github.get("name").equals("null")) person.full_name = json_response_from_github.get("name").asText();
                    if (person.picture == null && json_response_from_github.has("avatar_url")) person.alternative_picture_link = json_response_from_github.get("avatar_url").asText();
                    person.update();

                } else {

                    System.out.println("13. Uživatel neexistuje - tvořím nového ");

                    person = new Model_Person();
                    person.github_oauth_id = json_response_from_github.get("id").asText();
                    if (json_response_from_github.has("mail"))   person.mail = json_response_from_github.get("mail").asText();


                    if (json_response_from_github.has("login") && Model_Person.find.query().where().eq("nick_name", json_response_from_github.get("login").asText()).findOne() == null) person.nick_name = json_response_from_github.get("login").asText();
                    if (json_response_from_github.has("name") && json_response_from_github.get("name") != null &&  !json_response_from_github.get("name").equals("") && !json_response_from_github.get("name").equals("null"))  person.full_name = json_response_from_github.get("name").asText();
                    if (json_response_from_github.has("avatar_url")) person.alternative_picture_link = json_response_from_github.get("avatar_url").asText();
                    person.save();
                }

                floatingPersonToken.person = person;
                floatingPersonToken.update();

            }

            logger.debug("GET_github_oauth:: Return URL:: " + floatingPersonToken.return_url);

            new Check_Online_Status_after_user_login(person.id).run();
            return redirect(floatingPersonToken.return_url.replace("[_status_]", "success"));


        } catch (Exception e) {
            logger.internalServerError(e);
            return internalServerError(e);
        }

    }


    @ApiOperation( value = "GET_facebook_oauth", hidden = true)
    public Result facebook_oauth_get(String url) {
        try {

            logger.debug("GET_facebook_oauth:: URL:: {} ", url);
            Map<String, String> map = UtilTools.getMap_From_query(request().queryString().entrySet());

            if (map.containsKey("error")) {

                logger.warn("GET_facebook_oauth:: Map Contains Error");

                if (map.containsKey("state"))
                Model_AuthorizationToken.find.query().where().eq("provider_key", map.get("state")).findOne().delete();
                return redirect(url.replace("[_status_]", "fail"));
            }


            for (String parameter : map.keySet()) {

                System.out.println("Facebook parameter: " + parameter);
                System.out.println("Facebook contains: "  + map.get(parameter));

            }

            String state = map.get("state").replace("[", "").replace("]", "");
            String code  = map.get("code").replace("[", "").replace("]", "");

            System.out.println("State: " + state);
            System.out.println("Code: " + code);


            Model_AuthorizationToken floatingPersonToken = Model_AuthorizationToken.find.query().where().eq("provider_key", state).findOne();
            if (floatingPersonToken == null) {

                logger.warn("GET_facebook_oauth:: floatingPersonToken not found! ");
                return redirect(url.replace("[_status_]", "fail"));
            }

            logger.debug("GET_facebook_oauth:: floatingPersonToken set as verified! ");

            floatingPersonToken.social_token_verified = true;
            floatingPersonToken.setDate();

            OAuth20Service service = Socials.Facebook(state);

            System.out.println("0. Trading the Request Token for an Access Token...");
            OAuth2AccessToken accessToken = service.getAccessToken(code);

            System.out.println("1. Got the Access Token!");
            System.out.println("2. (if your curious it looks like this: " + accessToken + ", 'rawResponse'='" + accessToken.getRawResponse() + "')");
            System.out.println();

            // Now let's go and ask for a protected resource!
            System.out.println("3. Now we're going to access a protected resource...");

            System.out.println("4. Facebook URL: " + Server.Facebook_url);

            OAuthRequest request = new OAuthRequest(Verb.GET, Server.Facebook_url);
            service.signRequest(accessToken, request);

            final Response response = service.execute(request);

            System.out.println("5. Got it! Lets see what we found...");
            System.out.println("6. Code: " + response.getCode());


            if (!response.isSuccessful()) {

                logger.warn("GET_facebook_oauth:: Get Response wasnt succesfull :(  ");

                return redirect(url.replace("[_status_]", "fail"));
            }


            System.out.println("7. GET_facebook_oauth:: Get Response was successful");

            JsonNode jsonNode = Json.parse(response.getBody());
            System.out.println("8. Převedl jsem body na JSON:: " + jsonNode.toString());


            System.out.println("9. Chystám se udělat request");


            WSRequest complexRequest = ws.url("https://graph.facebook.com/v2.8/" + jsonNode.get("id").asText())
                                            .setQueryParameter("access_token", accessToken.getAccessToken())
                                            .setQueryParameter("fields", "id,name,first_name,last_name,email");

            F.Promise<WSResponse> responsePromise = complexRequest.get();
            JsonNode json_response_from_facebook = responsePromise.get(5000).asJson();

            System.out.println("10. JsonRequest: " + json_response_from_facebook.toString());



            Model_Person person = Model_Person.find.query().where().eq("facebook_oauth_id",json_response_from_facebook.get("id").asText() ).findOne();
            if (person != null) {

                System.out.println("Tento uživatel se nepřihlašuje poprvné - pouze updajtuji jeho informace");

                System.out.println("13. Seznam není prázdný - uživatel se už někdy registroval skrze facebook");

                if (json_response_from_facebook.has("email")) person.mail = json_response_from_facebook.get("email").asText();
                if (json_response_from_facebook.has("name")) person.full_name = json_response_from_facebook.get("name").asText();
                person.update();

                floatingPersonToken.person = person;
                floatingPersonToken.provider_user_id = jsonNode.get("id").asText();
                floatingPersonToken.update();
            }
            else {

                System.out.println("13. Uživatel neexistuje s tímto id tvořím nového ale ještě před tím zkontroluji zda už nění registrovaný klasicky přes email");

                if (json_response_from_facebook.has("email")) person = Model_Person.find.query().where().eq("mail", json_response_from_facebook.get("email").asText()).findOne();

                if (person != null) {

                    System.out.println("13. Uživatel existuje s emailem ale bez facebook tokenu - a tak jen doplním token");

                    person = Model_Person.find.query().where().eq("mail", json_response_from_facebook.get("email").asText()).findOne();
                    person.facebook_oauth_id = jsonNode.get("id").asText();
                    if (json_response_from_facebook.has("name")) person.full_name = json_response_from_facebook.get("name").asText();
                    person.update();

                } else {

                    person = new Model_Person();
                    person.facebook_oauth_id = jsonNode.get("id").asText();
                    if (json_response_from_facebook.has("email")) person.mail = json_response_from_facebook.get("email").asText();
                    if (json_response_from_facebook.has("name")) person.full_name = json_response_from_facebook.get("name").asText();

                    person.save();
                }


                floatingPersonToken.person = person;
                floatingPersonToken.update();
            }


            System.out.println("16. floatingPersonToken.return_url " + floatingPersonToken.return_url);
            new Check_Online_Status_after_user_login(person.id).run();
            return redirect(floatingPersonToken.return_url.replace("[_status_]", "success"));


        } catch (Exception e) {
            logger.internalServerError(e);
            return internalServerError(e);
        }
    }


//###### Socilání sítě - a generátory přístupů ########################################################################

    @ApiOperation(value = "login GitHub",
            tags = {"Access", "Social-GitHub"},
            notes = "For login via GitHub \n\n "+
                    "If you want login to system with social networks - you can used Facebook, GitHub, Twitter... api " +
                    "just ask via this Api and cloud_blocko_server responds with object where is token and redirection link. After that redirect user " +
                    "to this link and after returning to your success page you have to ask again (api - get Person by token ) for information about logged Person",
            produces = "application/json",
            response =  Swagger_SocialNetwork_Result.class,
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_SocialNetwork_Login",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created",      response = Swagger_SocialNetwork_Result.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",   response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result GitHub() {
        try {


            // Zpracování Json
            final Form<Swagger_SocialNetwork_Login> form = formFactory.form(Swagger_SocialNetwork_Login.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_SocialNetwork_Login help = form.get();


            System.out.print("Link k přesměrování při přihlášení přes github:: " + help.redirect_url);

            logger.debug("GitHub  request for login:: return link:: {}", help.redirect_url);


            Model_AuthorizationToken floatingPersonToken = Model_AuthorizationToken.setProviderKey("GitHub");

            floatingPersonToken.return_url = help.redirect_url;
            floatingPersonToken.where_logged = PlatformAccess.BECKI_WEBSITE;


            if ( Http.Context.current().request().headers().get("User-Agent")[0] != null) floatingPersonToken.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
            else  floatingPersonToken.user_agent = "Unknown browser";

            floatingPersonToken.update();

            System.out.println("GitHub_callBack ::" + Server.GitHub_callBack);
            System.out.println("GitHub_url ::" + Server.GitHub_url);


            OAuth20Service service = Socials.GitHub( floatingPersonToken.provider_key);

            Swagger_SocialNetwork_Result result = new Swagger_SocialNetwork_Result();
            result.type = "GitHub";
            result.redirect_url = service.getAuthorizationUrl(null);
            result.authToken = floatingPersonToken.token;

            logger.debug("GitHub  request for login:: response:: {}", Json.toJson(result));

            return result_ok(Json.toJson(result));

        } catch (Exception e) {
            logger.internalServerError(e);
            return internalServerError(e);
        }
    }

    @ApiOperation(value = "login Facebook",
            tags = {"Access", "Social-Facebook"},
            notes = "For login via Facebook \n\n "+
                    "If you want login to system with social networks - you can used Facebook, GitHub, Twitter... api " +
                    "just ask via this Api and cloud_blocko_server responds with object where is token and redirection link. After that redirect user " +
                    "to this link and after returning to your success page you have to ask again (api - get Person by token ) for information about logged Person",
            produces = "application/json",
            response =  Swagger_SocialNetwork_Result.class,
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_SocialNetwork_Login",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created",    response = Swagger_SocialNetwork_Result.class),
            @ApiResponse(code = 400, message = "Invalid body",            response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password", response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",       response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result Facebook() {
        try {


            // Zpracování Json
            final Form<Swagger_SocialNetwork_Login> form = formFactory.form(Swagger_SocialNetwork_Login.class).bindFromRequest();
            if (form.hasErrors()) return invalidBody(form.errorsAsJson());
            Swagger_SocialNetwork_Login help = form.get();
            
            System.out.println("Link k přesměrování při přihlášení přes facebook:: " + help.redirect_url);

            logger.debug("Facebook request for login:: return link:: {}", help.redirect_url);

            Model_AuthorizationToken floatingPersonToken = Model_AuthorizationToken.setProviderKey("Facebook");

            if (help.redirect_url.contains("/login-failed")) {
                help.redirect_url = "https://portal.stage.byzance.cz/dashboard";
                logger.warn("Na Becki jsou líní to fixnout už měsíc!");
            }

            floatingPersonToken.return_url = help.redirect_url;

            if ( Http.Context.current().request().headers().get("User-Agent")[0] != null) floatingPersonToken.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
            else  floatingPersonToken.user_agent = "Unknown browser";

            floatingPersonToken.update();

            OAuth20Service service = Socials.Facebook(floatingPersonToken.provider_key);

            Swagger_SocialNetwork_Result result = new Swagger_SocialNetwork_Result();
            result.type = "Facebook";
            result.redirect_url = service.getAuthorizationUrl(null);
            result.authToken = floatingPersonToken.token;

            return result_ok(Json.toJson(result));

        } catch (Exception e) {
            return internalServerError(e);
        }
    }*/


///###### Option########################################################################################################

    @ApiOperation( value = "option", hidden = true)
    public Result option() {

        return okEmpty();
    }

    @ApiOperation( value = "optionLink", hidden = true)
    public Result optionLink(String url) {
        return okEmpty();
    }
}
