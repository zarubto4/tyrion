package utilities.authentication;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseController;
import controllers._BaseFormFactory;
import exceptions.NotFoundException;
import io.swagger.annotations.*;
import models.*;
import play.libs.ws.WSClient;
import play.mvc.*;
import responses.*;
import utilities.enums.TokenType;
import utilities.enums.PlatformAccess;
import utilities.financial.FinancialPermission;
import utilities.model.EchoService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.swagger.input.Swagger_EmailAndPassword;
import utilities.swagger.output.Swagger_Blocko_Token_validation_result;
import utilities.swagger.output.Swagger_Login_Token;
import utilities.swagger.output.Swagger_Person_All_Details;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_Blocko_Token_validation_request;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

/**
 * Třída Controller_Security slouží k ověřování uživatelů pro přihlášení i odhlášení a to jak pro Becki, tak i Administraci Tyriona.
 *
 * Dále ověřuje validitu tokenů na Homer serveru, na Compilačním serveru, platnost Rest-API reqest tokenů (a jejich počet)
 */
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Security extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Security.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @Inject
    public Controller_Security(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService, NotificationService notificationService, EchoService echoService) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);
    }

// CLASIC LOGIN ########################################################################################################

    @ApiOperation(value = "check Request Token",
            tags = {"Blocko"},
            notes = "",     //TODO
            produces = "application/json",
            protocols = "https"
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Blocko_Token_validation_result.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result get_status_request_token() {
        try {

            // Get and Validate Object
            Swagger_Blocko_Token_validation_request help  = formFromRequestWithValidation(Swagger_Blocko_Token_validation_request.class);

            TokenType token_type = TokenType.getType(help.type_of_token);
            if (token_type == null) return badRequest("Wrong type of token");

            Swagger_Blocko_Token_validation_result result = new Swagger_Blocko_Token_validation_result();

            if (token_type == TokenType.PERSON_TOKEN) {

                Model_Person person = Model_Person.getByAuthToken(help.token);

                result.token = help.token;
                result.available_requests = 50L;
            }

            if (token_type == TokenType.INSTANCE_TOKEN) {

                Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.find.byId(help.token);

                result.token = help.token;
                result.available_requests = FinancialPermission.checkRestApiRequest(snapshot.getProduct(), snapshot.id);
            }

            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "login",
            tags = {"Access", "Person", "APP-Api"},
            notes = "Get access Token",
            produces = "application/json",
            protocols = "https"
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
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully logged",       response = Swagger_Login_Token.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",   response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class),
            @ApiResponse(code = 705, message = "Account not validated",     response = Result_NotValidated.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result login() {
        try {

            // Get and Validate Object
            Swagger_EmailAndPassword help  = formFromRequestWithValidation(Swagger_EmailAndPassword.class);

            // Ověření Person - Heslo a email

            Model_Person person;

            try {

                 person = Model_Person.getByEmail(help.email.toLowerCase());

            } catch (NotFoundException e) {
                logger.trace("Email {} not found", help.email.toLowerCase());
                return forbidden("Email or password is wrong");
            }

            if (person == null || !person.checkPassword(help.password)) {
                logger.trace("Email {} -> password are wrong", help.email.toLowerCase());
                return forbidden("Email or password is wrong");
            }

            // Kontrola validity - jestli byl ověřen přes email
            // Jestli není účet blokován
            if (!person.validated) return notValidated();
            if (person.frozen) return badRequest("Your account has been temporarily suspended");

            // Vytvářim objekt tokenu pro přihlášení (na něj jsou vázány co uživatel kde a jak dělá) - Historie pro využití v MongoDB widgety atd..
            Model_AuthorizationToken token = new Model_AuthorizationToken();
            token.person = person;
            token.where_logged  = PlatformAccess.BECKI_WEBSITE;

            // Zjistím kde je přihlášení (user-Agent je třeba "Safari v1.30" nebo "Chrome 12.43" atd..)
            token.user_agent = Http.Context.current().request().getHeaders().get("User-Agent").orElse("Unknown browser");

            token.setDate();

            // Ukládám do databáze
            token.save();

            // Ukládám do Cahce pamětí pro další operace
            Model_Person.token_cache.put(token.token, person.id);

            // Chache Update
            Model_Person.find.byId(person.id);


            // Vytvářím objekt, který zasílám zpět frontendu
            Swagger_Login_Token swagger_login_token = new Swagger_Login_Token();
            swagger_login_token.auth_token = token.token;

            // Odesílám odpověď
            return ok(swagger_login_token);

        } catch (Exception e) {
            return controllerServerError(e);
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
    @ApiResponses({
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


            // Create Hmac
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(config.getString("Intercom.hmacToken").getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hash = (sha256_HMAC.doFinal( person.id.toString().getBytes() ));
            StringBuffer result_hash = new StringBuffer();
            for (byte b : hash) {
                result_hash.append(String.format("%02x", b)); // thanks sachins! https://gist.github.com/thewheat/7342c76ade46e7322c3e#gistcomment-1863031
            }

            // System.out.println("HMAC " + result_hash.toString());
            result.hmac = result_hash.toString();

            return ok(result);


        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "logout",
            tags = {"Access", "Person", "APP-Api"},
            notes = "for logout person - that's deactivate person token ",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully logged out",   response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Wrong Email or Password",   response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result logout() {
        try {

            Optional<String> optional = Controller.request().header("X-AUTH-TOKEN");
            if (optional.isPresent()) {

                UUID token = UUID.fromString(optional.get());
                Model_Person.token_cache.remove(token);

                Model_AuthorizationToken token_model = Model_AuthorizationToken.getByToken(token);

                token_model.delete();

            }

            return ok();

        } catch (Exception e) {
            logger.internalServerError(e);
            return ok();
        }
    }

    @ApiOperation(value = "add apikey",
            tags = {"Access", "Person", "APP-Api"},
            notes = "creates permanent api key",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",             response = Swagger_Login_Token.class),
            @ApiResponse(code = 401, message = "Unauthorized",          response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result apikey() {
        try {

            Model_AuthorizationToken token = new Model_AuthorizationToken();
            token.person = person();
            token.where_logged  = PlatformAccess.BECKI_WEBSITE;

            token.user_agent = "Various -> permanent token";

            this.checkCreatePermission(token);

            // Ukládám do databáze
            token.save();

            // Vytvářím objekt, který zasílám zpět frontendu
            Swagger_Login_Token swagger_login_token = new Swagger_Login_Token();
            swagger_login_token.auth_token = token.token;

            return ok(swagger_login_token);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


///###### Option########################################################################################################

    @ApiOperation( value = "option", hidden = true)
    public Result option() {
        return ok();
    }

    @ApiOperation( value = "optionLink", hidden = true)
    public Result optionLink(String url) {
        return Controller.ok();
    }
}
