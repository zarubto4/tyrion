package controllers;

import com.google.inject.Inject;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.scope.FacebookPermissions;
import com.restfb.scope.ScopeBuilder;
import com.restfb.types.User;
import com.typesafe.config.Config;
import io.swagger.annotations.*;
import mongo.ModelMongo_FacebookLoginRelation;
import mongo.ModelMongo_FacebookProfile;
import models.Model_Person;
import play.Environment;
import play.libs.ws.WSClient;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerService;
import utilities.swagger.input.Swagger_Facebook_LoginRedirect;
import utilities.swagger.output.Swagger_Facebook_Login;

import javax.security.auth.login.LoginException;
import java.util.UUID;

import static play.mvc.Controller.request;

/**
 * Průvodní komentář:
 * <p>
 * Trida realizuje login pomoci Facebook Graph API prostrednictvim knihovny fbrest. Access token spolu s dalsim infem
 * je ulozen do profilu uzivatele
 */

/*
 * Annotation "Not Documented API - InProgress or Stuck" is for collection not branded API points (methods
 * in this controller. We use that for filtering all not properly set methods for Swagger or Postman
 */
@Api(
        value = "Not Documented API - InProgress or Stuck",
        protocols = "https",
        produces = "application/json"
)

/*
 * Security Annotation - if its allowed only registred and validated HTTP request will be proceed in assigned methods
 */
public class Controller_FacebookLogin extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_FacebookLogin.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @Inject
    public Controller_FacebookLogin(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerService scheduler, PermissionService permissionService) {
        super(environment, ws, formFactory, youTrack, config, scheduler, permissionService);
    }


// CONTROLLER CONTENT ##################################################################################################

    private static final String FB_APP_ID = "939700616095852";
    private static final String FB_APP_SECRET = "2134b00e463225e1e7500a0a15fe93c0";
    private final Version graphApiVersion = Version.VERSION_3_1;

    @ApiOperation(value = "Get FB login URL for person",
            tags = {"Facebook"},
            notes = "Facebook Login with OAuth Authentication for current person relation"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Facebook_LoginRedirect", // Class that describes what I consume
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values" // Description about body in http request
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Swagger_Facebook_Login.class),
            @ApiResponse(code = 400, message = "Something is wrong",        response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @Security.Authenticated(Authentication.class)
    public Result login_url_person() {
        try {

            Swagger_Facebook_LoginRedirect help = formFromRequestWithValidation(Swagger_Facebook_LoginRedirect.class);

            ModelMongo_FacebookLoginRelation relation = new ModelMongo_FacebookLoginRelation();
            relation.person_id = personId();
            relation.hash = UUID.randomUUID();
            relation.save();
            FacebookClient client = new DefaultFacebookClient(graphApiVersion);

            // Save to cache
            ModelMongo_FacebookProfile.redirect_link_cache.put(relation.hash, help.redirect_link);

            Swagger_Facebook_Login login = new Swagger_Facebook_Login();

            login.link = client.getLoginDialogUrl(
                    FB_APP_ID,
                    routes.Controller_FacebookLogin.login_redirect_person().absoluteURL(play.mvc.Http.Context.current().request()),
                    getScope(),
                    Parameter.with("state", relation.hash.toString())
            );

            return ok(login);


        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Get FB login redirect for logged person",
            tags = {"Facebook"},
            notes = "Facebook login redirect callback",
            hidden = true
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong", response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 478, message = "External server error", response = Result_ExternalServerSideError.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result login_redirect_person() {
        try {
            String code = play.mvc.Http.Context.current().request().getQueryString("code");
            if (code == null) {
                throw new LoginException("Login error, code query string is not present in the redirect query");
            }

            String state = play.mvc.Http.Context.current().request().getQueryString("state");
            if (state == null) {
                throw new LoginException("Login error, state query string is not present in the redirect query");
            }

            ModelMongo_FacebookLoginRelation relation = ModelMongo_FacebookLoginRelation.find.bySingleArgument("hash", UUID.fromString(state));
            if(relation == null) {
                return badRequest("Relation not found");
            }

            Model_Person person = Model_Person.find.byId(relation.person_id);
            if (person == null) {
                return badRequest("User not found");
            }

            FacebookClient.AccessToken accessToken = new DefaultFacebookClient(graphApiVersion).obtainUserAccessToken(
                    FB_APP_ID,
                    FB_APP_SECRET,
                    routes.Controller_FacebookLogin.login_redirect_person().absoluteURL(play.mvc.Http.Context.current().request()),
                    code
            );

            User fbUser = new DefaultFacebookClient(accessToken.getAccessToken(), graphApiVersion)
                    .fetchObject("me", User.class, Parameter.with("fields", "id,email,first_name,last_name,gender"));

            ModelMongo_FacebookProfile facebook_profile = ModelMongo_FacebookProfile.find.bySingleArgument("facebook_id", fbUser.getId());
            if (facebook_profile == null) {
                facebook_profile = new ModelMongo_FacebookProfile();
            }

            facebook_profile.person_id = person.id;
            facebook_profile.access_token = accessToken.getAccessToken();
            facebook_profile.first_name = fbUser.getFirstName();
            facebook_profile.last_name = fbUser.getLastName();
            facebook_profile.email = fbUser.getEmail();
            facebook_profile.facebook_id = fbUser.getId();
            facebook_profile.save();

            // TODO person.facebook_profile = facebook_profile;
            person.update();

            if(request().getQueryString("state") != null) {
                String redirect_url = ModelMongo_FacebookProfile.redirect_link_cache.get(UUID.fromString(request().getQueryString("state")));
                if(redirect_url != null) return redirect(redirect_url);
            }

            return ok();

        } catch (FacebookOAuthException e) {
            return externalServerError(e.getMessage());
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "Get FB login URL",
            tags = {"Facebook"},
            notes = "Facebook Login with OAuth Authentication, this is a first API point for get URL for redirection, you have to set URL the backend server at the end of process redirects your person."
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "body",
                    dataType = "utilities.swagger.input.Swagger_Facebook_LoginRedirect", // Class that describes what I consume
                    required = true,
                    paramType = "body",
                    value = "Contains Json with values" // Description about body in http request
            )
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Swagger_Facebook_Login.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result login_url() {
        try {


            Swagger_Facebook_LoginRedirect help = formFromRequestWithValidation(Swagger_Facebook_LoginRedirect.class);

            FacebookClient client = new DefaultFacebookClient(graphApiVersion);

            // create UUID for cache response redirection
            UUID identification = UUID.randomUUID();

            // Save to cache
            ModelMongo_FacebookProfile.redirect_link_cache.put(identification, help.redirect_link);

            // Create Response
            Swagger_Facebook_Login login = new Swagger_Facebook_Login();

            // Set Value
            login.link =  client.getLoginDialogUrl(
                    FB_APP_ID,
                    routes.Controller_FacebookLogin.login_redirect().absoluteURL(play.mvc.Http.Context.current().request()),
                    getScope(),
                    Parameter.with("state", identification.toString())
            );

            login.auth_token = identification;

            System.out.println("login_url:: redirectIdentificator" + identification.toString());


            // Response
            return ok(login);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    /**
     * Private URL for Facebook!
     * @return
     */
    @ApiOperation(value = "Get FB login redirect",
            tags = {"Facebook"},
            notes = "Facebook login redirect callback",
            hidden = true
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result", response = Result_Ok.class),
            @ApiResponse(code = 400, message = "Something is wrong", response = Result_BadRequest.class),
            @ApiResponse(code = 401, message = "Unauthorized request", response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission", response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found", response = Result_NotFound.class),
            @ApiResponse(code = 478, message = "External server error", response = Result_ExternalServerSideError.class),
            @ApiResponse(code = 500, message = "Server side Error", response = Result_InternalServerError.class)
    })
    public Result login_redirect() {
        try {

            String code = request().getQueryString("code");

            System.out.println("login_redirect");
            System.out.println("login_redirect: path: " + request().path());
            System.out.println("login_redirect: code: " + request().getQueryString("code"));
            System.out.println("login_redirect: state: " + request().getQueryString("state"));

            if (code == null) {
                throw new LoginException("Login error, code query string is not present in the redirect query");
            }

            FacebookClient.AccessToken accessToken = new DefaultFacebookClient(graphApiVersion).obtainUserAccessToken(
                    FB_APP_ID,
                    FB_APP_SECRET,
                    routes.Controller_FacebookLogin.login_redirect().absoluteURL(play.mvc.Http.Context.current().request()),
                    code
            );


            User fbUser = new DefaultFacebookClient(accessToken.getAccessToken(), graphApiVersion)
                    .fetchObject("me", User.class, Parameter.with("fields", "id,email,first_name,last_name,gender"));

            ModelMongo_FacebookProfile facebook_profile = ModelMongo_FacebookProfile.find.bySingleArgument("facebook_id", fbUser.getId());
            if (facebook_profile == null) {
                facebook_profile = new ModelMongo_FacebookProfile();
            }


            facebook_profile.access_token = accessToken.getAccessToken();
            facebook_profile.first_name = fbUser.getFirstName();
            facebook_profile.last_name = fbUser.getLastName();
            facebook_profile.email = fbUser.getEmail();
            facebook_profile.facebook_id = fbUser.getId();
            facebook_profile.save();


            if (facebook_profile.person_id != null) {
                //uzivatel se prihlasil a uz je registrovan

                Model_Person person = Model_Person.find.byId(facebook_profile.person_id);
                if (person == null) {
                    return badRequest("User not found");
                }

                facebook_profile.person_id = person.id;
                facebook_profile.update();

                // TODO person.facebook_profile = facebook_profile;
                person.update();

            } else {

                //uzivatel neni registrovan
                Model_Person person = new Model_Person();
                person.first_name = fbUser.getFirstName();
                person.last_name = fbUser.getLastName();
                person.email = fbUser.getEmail();
                person.frozen = false;
                person.save();

                facebook_profile.person_id = person.id;
                facebook_profile.update();;

                // TODO person.facebook_profile = facebook_profile;
                person.update();

            }

            if(request().getQueryString("state") != null) {
               String redirect_url = ModelMongo_FacebookProfile.redirect_link_cache.get(UUID.fromString(request().getQueryString("state")));
               if(redirect_url != null) return redirect(redirect_url);
            }

            return ok();

        } catch (FacebookOAuthException e) {
            return externalServerError(e.getMessage());
        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    private ScopeBuilder getScope() {
        ScopeBuilder scopeBuilder = new ScopeBuilder();

        return scopeBuilder.addPermission(FacebookPermissions.ADS_MANAGEMENT)
                .addPermission(FacebookPermissions.ADS_READ)
                .addPermission(FacebookPermissions.READ_INSIGHTS)
                .addPermission(FacebookPermissions.MANAGE_PAGES)
                .addPermission(FacebookPermissions.PUBLIC_PROFILE)
                .addPermission(FacebookPermissions.EMAIL);
    }
}