package controllers;

import com.google.inject.Inject;
import io.ebean.Ebean;
import io.ebean.ExpressionList;
import io.ebean.Junction;
import io.ebean.Query;
import io.swagger.annotations.*;
import models.Model_Article;
import models.Model_BProgram;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import responses.*;
import utilities.authentication.Authentication;
import utilities.logger.Logger;
import utilities.scheduler.SchedulerController;
import utilities.swagger.input.Swagger_Article_CreateUpdate;
import utilities.swagger.input.Swagger_Article_Filter;
import utilities.swagger.output.filter_results.Swagger_Article_List;
import java.util.UUID;

@Security.Authenticated(Authentication.class)
@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Article extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_Blocko.class);

// CONTROLLER CONFIGURATION ############################################################################################

    private _BaseFormFactory baseFormFactory;
    private SchedulerController scheduler;


    @Inject
    public Controller_Article(_BaseFormFactory formFactory, SchedulerController scheduler) {
        this.baseFormFactory = formFactory;
        this.scheduler = scheduler;
    }


    @ApiOperation(value = "create Article",
            tags = {"B_ArticleProgram"},
            notes = "create new Article",
            produces = "application/json",
            consumes = "application/json",
            protocols = "https",
            code = 201
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Article_CreateUpdate",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Successfully created",      response = Model_Article.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result article_create() {
        try {

            // Get and Validate Object
            Swagger_Article_CreateUpdate help  = baseFormFactory.formFromRequestWithValidation(Swagger_Article_CreateUpdate.class);

            // Kontrola objektu


            // Tvorba article
            Model_Article article = new Model_Article();
            article.description           = help.description;
            article.name                  = help.name;
            article.mark_down_text        = help.mark_down_text;

            // Uložení objektu
            article.save();

            article.setTags(help.tags);

            // Vrácení objektu
            return created(article);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


    @ApiOperation(value = "edit Article",
            tags = {"Article"},
            notes = "edit Article object",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input. Swagger_Article_CreateUpdate",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Model_BProgram.class),
            @ApiResponse(code = 400, message = "Invalid body",              response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    @BodyParser.Of(BodyParser.Json.class)
    public Result article_edit(UUID article_id) {
        try {

            // Get and Validate Object
            Swagger_Article_CreateUpdate help  = baseFormFactory.formFromRequestWithValidation(Swagger_Article_CreateUpdate.class);

            // Kontrola objektu
            Model_Article article = Model_Article.getById(article_id);

            // Úprava objektu
            article.description         = help.description;
            article.name                = help.name;
            article.mark_down_text      = help.mark_down_text;

            // Uložení objektu
            article.update();

            // Vrácení objektu
            return ok(article);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }

    @ApiOperation(value = "delete Article",
            tags = {"Article"},
            notes = "remove Article object",
            produces = "application/json",
            consumes = "text/html",
            protocols = "https"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",                 response = Result_Ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",      response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",  response = Result_Forbidden.class),
            @ApiResponse(code = 404, message = "Object not found",          response = Result_NotFound.class),
            @ApiResponse(code = 500, message = "Server side Error",         response = Result_InternalServerError.class)
    })
    public Result article_delete(UUID article_id) {
        try {

            // Kontrola objektu
            Model_Article article = Model_Article.getById(article_id);

            // Smazání objektu
            article.delete();

            // Vrácení potvrzení
            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


    @ApiOperation(value = "get Article List by Filter",
            tags = {"Article"},
            notes = "get Article List",
            produces = "application/json",
            protocols = "https"
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.input.Swagger_Article_Filter",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Ok Result",             response = Swagger_Article_List.class),
            @ApiResponse(code = 400, message = "Invalid body",          response = Result_InvalidBody.class),
            @ApiResponse(code = 401, message = "Unauthorized request",  response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error",     response = Result_InternalServerError.class)
    })
    public Result article_getByFilter(@ApiParam(value = "page_number is Integer. 1,2,3...n" + "For first call, use 1 (first page of list)", required = true) int page_number) {
        try {

            // Get and Validate Object
            Swagger_Article_Filter help  = baseFormFactory.formFromRequestWithValidation(Swagger_Article_Filter.class);

            // Získání všech objektů a následné filtrování podle vlastníka
            Query<Model_Article> query = Ebean.find(Model_Article.class);
            query.where().eq("deleted", false);


            if ((help.tags != null && !help.tags.isEmpty())) {

                ExpressionList<Model_Article> list = query.where();
                Junction<Model_Article> disjunction = list.disjunction();

                // Pokud JSON obsahuje project_id filtruji podle projektu
                if (help.tags != null && !help.tags.isEmpty()) {
                    disjunction
                            .conjunction()
                                .in("tags.value", help.tags)
                            .endJunction();
                }

                disjunction.endJunction();
            }

                // Vytvoření odchozího JSON
            Swagger_Article_List result = new Swagger_Article_List(query, page_number, help);

            // Vrácení výsledku
            return ok(result);

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }


}
