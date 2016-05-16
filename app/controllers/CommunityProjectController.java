package controllers;

import io.swagger.annotations.*;
import models.project.community.Article;
import models.project.community.Documentation;
import models.project.global.Project;
import models.project.m_program.M_Project;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_JsonValueMissing;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.swagger.documentationClass.Swagger_M_Project_New;
import utilities.swagger.documentationClass.Swagger_Project_Documentation;
import utilities.swagger.documentationClass.Swagger_Project_Documentation_Article;

import javax.websocket.server.PathParam;
import java.util.Date;

@Api(value = "Not Documented API - InProgress or Stuck")
@Security.Authenticated(Secured.class)
public class CommunityProjectController extends Controller {

    @ApiOperation(value = "Create new documentation",
            tags = {"Documentation"},
            notes = "Documentation contains text and images for community projects",
            produces = "application/json",
            response =  Documentation.class,
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Project_Documentation",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Documentation.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Documentation(String project_id) {
        try{
            final Form<Swagger_Project_Documentation> form = Form.form(Swagger_Project_Documentation.class).bindFromRequest();
            if (form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Project_Documentation help = form.get();

            Project project = Project.find.byId( project_id );
            if(project == null) return GlobalResult.notFoundObject("Project project_id not found");

            Documentation documentation = new Documentation();
            documentation.date_of_create = new Date();
            documentation.project = project;
            documentation.save();

            for (Swagger_Project_Documentation_Article swaggerArticle : help.articles) {
                Article article = new Article();
                article.documentation = documentation;
                article.name = swaggerArticle.name;
                article.text = swaggerArticle.text;
                article.save();
                documentation.articles.add(article);
            }

            Documentation oldDocumentation = project.documentation;

            project.documentation = documentation;
            project.views++;
            project.update();

            if (oldDocumentation != null) oldDocumentation.delete();

            return GlobalResult.created( Json.toJson(documentation));
        }
        catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Documentation",
            tags = {"Documentation"},
            notes = "get Documentation by query = project_id",
            produces = "application/json",
            response =  Documentation.class,
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "Person need this value of permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Documentation.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_Documentation(@ApiParam(value = "project_id String query", required = true) @PathParam("project_id") String project_id){
        try {
            Project project = Project.find.byId(project_id);
            if (project == null) return GlobalResult.notFoundObject("Project project_id not found");

            Documentation documentation = project.documentation;
            if (documentation == null) return GlobalResult.notFoundObject("Project project_id does not have documentation");

            if (!project.read_permission())  return GlobalResult.forbidden_Permission();
            return GlobalResult.result_ok(Json.toJson(documentation));

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

}
