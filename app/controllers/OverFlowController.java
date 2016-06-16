package controllers;


import com.avaje.ebean.Ebean;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;
import io.swagger.annotations.*;
import models.overflow.*;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.UtilTools;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.Result_JsonValueMissing;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.documentationClass.*;

import javax.websocket.server.PathParam;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Api(value = "Not Documented API - InProgress or Stuck")
public class OverFlowController  extends Controller {


// PUBLIC ##############################################################################################################

    @ApiOperation(value = "get Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "create new Project",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response =  Post.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    public Result get_Post(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id){
        try{

            Post post = Post.find.byId(post_id);
            if(post == null) return GlobalResult.notFoundObject("Post post_id not found");

            post.views++;
            post.update();

            return GlobalResult.result_ok( Json.toJson(post) );

        } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "get Posts by Filter",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "get Post by Filter parameters in JSON. ",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Post_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Post.class, responseContainer =  "List"),
            @ApiResponse(code = 500, message = "Server side Error")
    })

    @BodyParser.Of(BodyParser.Json.class)
    public Result get_Post_ByFilter(){
        try {

            final Form<Swagger_Post_Filter> form = Form.form(Swagger_Post_Filter.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Post_Filter help = form.get();

            Query<Post> query = Ebean.find(Post.class);
            query.where().eq("postParentAnswer", null);
            query.where().eq("postParentComment", null);


            // If contains HashTags
            if(help.hash_tags != null ){
                List<String> hashTags =help.hash_tags;
                Set<String> HashTagset = new HashSet<>(hashTags);
                query.where().in("hashTagsList.postHashTagId", HashTagset);

            }

            // If contains confirms
            if(help.confirms != null){
                List<String> confirms = help.confirms;
                Set<String> confirmsSet = new HashSet<>(confirms);
                 query.where().in("hashTagsList.postHashTagId", confirmsSet);
            }

            // From date
            if(help.date_from != null){
                query.where().ge("date_of_create", help.date_from );
            }

            // To date
            if(help.date_to != null ){
                query.where().le("date_of_create", help.date_to);
            }

            if(help.types != null){
                query.where().in("type.id", help.types );
            }

            if(help.nick_name != null){
                query.where().ieq("author.nick_name", help.nick_name);
            }


            if(help.count_from != null){
                query.setFirstRow(help.count_from);
            }

            if(help.count_to != null){
                query.setMaxRows(help.count_to);
            }

            if(help.order != null){

               String order = help.order;
               String value = help.value;

                OrderBy<Post> orderBy = new OrderBy<>();

                if(order.equals("asc")) orderBy.asc(value);
                else if (order.equals("desc")) orderBy.desc(value);

                query.setOrder(orderBy);
            }

            // TODO TOM - FILTER PATTERN
            List<Post> list = query.findList();


            return GlobalResult.result_ok(Json.toJson(list));

        } catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }

    }


// SECURED **********************************************************************************************************************

    @ApiOperation(value = "Create new Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "Create new Post. ",
            produces = "application/json",
            protocols = "https",
            code = 201,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Post_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = Post.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_Post(){
        try {

            final Form<Swagger_Post_New> form = Form.form(Swagger_Post_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Post_New help = form.get();

            TypeOfPost typeOfPost = TypeOfPost.find.byId(help.type_of_post_id);
            if (typeOfPost == null) return GlobalResult.notFoundObject("TypeOfPost type_of_post_id not found");

            Post post = new Post();
            post.author = SecurityController.getPerson();

            post.name = help.name;
            post.type = typeOfPost;
            post.views = 0;
            post.likes = 0;
            post.text_of_post = help.text_of_post;
            post.date_of_create = new Date();

            if (help.hash_tags != null) UtilTools.add_hashTags_to_Post(form.get().hash_tags, post);

            post.save();


            return GlobalResult.created(Json.toJson(post));

          } catch (Exception e) {
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "delete Post",
            tags = {"Blocko-OverFlow"},
            notes = "You can delete Main Post, Answers to post and comments.. ",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                   @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Post.delete_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public  Result delete_Post(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id){
        try {

            Post post = Post.find.byId(post_id);
            if (post == null ) return GlobalResult.notFoundObject("Post post_id not found");
            if (!post.author.id.equals( SecurityController.getPerson().id) ) return GlobalResult.forbidden_Permission();


            if (!post.delete_permission())  return GlobalResult.forbidden_Permission();
            post.delete();

            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    @ApiOperation(value = "edit Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "You can edit main post",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Post.edit_permission", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Post_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Post.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Post(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id){
        try {
            final Form<Swagger_Post_New> form = Form.form(Swagger_Post_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Post_New help = form.get();

            Post post = Post.find.byId(post_id);
            if (post == null) return GlobalResult.notFoundObject("Post post_id not found");

            if (!post.edit_permission())  return GlobalResult.forbidden_Permission();

            TypeOfPost typeOfPost = TypeOfPost.find.byId( help.type_of_post_id);
            if(typeOfPost == null) return GlobalResult.notFoundObject("TypeOfPost type_of_post_id not found");



            post.name = help.name;
            post.type = typeOfPost;
            post.text_of_post = help.text_of_post;
            post.updated = true;

            post.hashTagsList.clear();

            if( help.hash_tags != null ) UtilTools.add_hashTags_to_Post( help.hash_tags, post );

            post.update();

           return GlobalResult.result_ok(Json.toJson(post));


        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "add comment to the Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "You can comment Main Post and all answers in Main Post. But you cannot comment another comment!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Post.comment_permission", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Post_Comment",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Post.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result addComment(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id){
       try {

           final Form<Swagger_Post_Comment> form = Form.form(Swagger_Post_Comment.class).bindFromRequest();
           if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
           Swagger_Post_Comment help = form.get();

           Post parentPost = Post.find.byId(post_id);
           if (parentPost == null) return GlobalResult.notFoundObject("Post post_id not found");

           if (!parentPost.comment_permission())  return GlobalResult.forbidden_Permission();

           Post post = new Post();
           post.author = SecurityController.getPerson();
           post.likes = 0;
           post.text_of_post = help.text_of_post;
           post.date_of_create = new Date();

           if (help.hash_tags != null) UtilTools.add_hashTags_to_Post(form.get().hash_tags, post);

           parentPost.comments.add(post);
           post.postParentComment = parentPost;

           parentPost.save();
           post.save();

           return GlobalResult.result_ok( Json.toJson(post) );

       } catch (Exception e) {
           Logger.error("Error", e);
           Logger.error("OverFlowController - newPost ERROR");
           return GlobalResult.internalServerError();
       }
    }

    @ApiOperation(value = "add Answer to the Post",
            tags = {"Blocko-OverFlow"},
            notes = "You can  answer to Main Post. But you cannot answer another answer!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Post.answer_permission", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Post_Answer",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Post.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result addAnswer(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id){
        try {
            final Form<Swagger_Post_Answer> form = Form.form(Swagger_Post_Answer.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Post_Answer help = form.get();


            Post parentPost = Post.find.byId(post_id);
            if (parentPost == null) throw new Exception("Post not Exist");

            if (!parentPost.answer_permission())  return GlobalResult.forbidden_Permission();

            Post post = new Post();
            post.author = SecurityController.getPerson();
            post.likes = 0;
            post.text_of_post = help.text_of_post;
            post.date_of_create = new Date();

            if (help.hash_tags != null) UtilTools.add_hashTags_to_Post(form.get().hash_tags, post);

            parentPost.answers.add(post);
            post.postParentAnswer = parentPost;

            parentPost.save();
            post.save();

            return GlobalResult.result_ok(Json.toJson(post));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit Comment or Answer Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "You can update Comment post",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Post.edit_permission", value = "true"),
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_Post_Comment",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Post.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Comment_or_Answer(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id){
        try {

            final Form<Swagger_Post_Comment> form = Form.form(Swagger_Post_Comment.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_Post_Comment help = form.get();

            Post post = Post.find.byId(post_id);
            if (post == null) throw new Exception("Comment not Exist");

            if (!post.edit_permission()) return GlobalResult.forbidden_Permission();

            post.text_of_post = help.text_of_post;

            post.hashTags().clear();

            if (help.hash_tags != null) UtilTools.add_hashTags_to_Post(form.get().hash_tags, post);

            post.update();
            return GlobalResult.result_ok(Json.toJson(post));


        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "answer to Post with link",
            tags = {"Blocko-OverFlow"},
            notes = "You can connect question (main Post) with previous version",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = LinkedPost.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result linkWithPreviousAnswer(@ApiParam(value = "question_post_id String path", required = true) @PathParam("question_post_id") String question_post_id, @ApiParam(value = "This is Answer Id (main post)", required = true) @PathParam("post_id") String answer_post_id){
        try {

            Post question = Post.find.byId(question_post_id);
            Post answer = Post.find.byId(answer_post_id);

            if (question == null)   return GlobalResult.notFoundObject("Post question_post_id not found");
            if (answer == null)     return GlobalResult.notFoundObject("Post answer_post_id not found");
            if (question.postParentComment != null)   return GlobalResult.result_BadRequest("You can link only main post");
            if (answer.postParentComment != null)     return GlobalResult.result_BadRequest("You can link only main post");

            LinkedPost linkedPost = new LinkedPost();
            linkedPost.answer = answer;
            linkedPost.question = question;
            linkedPost.author = SecurityController.getPerson();
            linkedPost.save();

            return GlobalResult.result_ok(Json.toJson(linkedPost));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "remove link to Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "Remove connection (Link) between Posts",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "LinkedPost.delete_permission", value = "true"),
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result unlinkWithPreviousAnswer(String linked_post_id){
        try {
            LinkedPost post = LinkedPost.find.byId(linked_post_id);

            if (post == null ) throw new Exception("Linked connection not Exist");
            if (!post.delete_permission()) return GlobalResult.forbidden_Permission();

            post.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }




/// TYPE OF POST #######################################################################################################

    @ApiOperation(value = "new Type of Post",
            tags = {"Blocko-OverFlow", "Type-Of-Post"},
            notes = "Create new type of post. Its only for Blocko-OverFlow Administrators!",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_description", properties = {
                            @ExtensionProperty(name = "TypeOfPost.create_permission", value = TypeOfPost.create_permission_docs),
                    }),
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfPost.create_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfPost_create" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfPost_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = TypeOfPost.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_TypeOfPost(){
        try{

            final Form<Swagger_TypeOfPost_New> form = Form.form(Swagger_TypeOfPost_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfPost_New help = form.get();

            if( TypeOfPost.find.where().ieq("type", help.type ).findUnique() != null) return GlobalResult.result_BadRequest("Duplicate Value");

            TypeOfPost typeOfPost = new TypeOfPost();
            typeOfPost.type =  help.type;

            if (!typeOfPost.create_permission())  return GlobalResult.forbidden_Permission();

            typeOfPost.save();

            return GlobalResult.created( Json.toJson(typeOfPost) );

        }catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get all Type of Post",
            tags = {"Blocko-OverFlow", "Type-Of-Post"},
            notes = "get All Type of Post. (Its for all logged users)",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = TypeOfPost.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_TypeOfPost_all(){
        try{
            return GlobalResult.result_ok(Json.toJson( TypeOfPost.find.all() ));
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }


    @ApiOperation(value = "get Type of Post",
            tags = {"Blocko-OverFlow", "Type-Of-Post"},
            notes = "get Type of Post by path id. (Its for all logged users)",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = TypeOfPost.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_TypeOfPost(@ApiParam(value = "type_of_post_id String path", required = true) @PathParam("type_of_post_id") String type_of_post_id){
        try{

            TypeOfPost typeOfPost = TypeOfPost.find.byId(type_of_post_id);
            if(typeOfPost == null) return GlobalResult.notFoundObject("TypeOfPost type_of_post_id not found");

            return GlobalResult.result_ok( Json.toJson(typeOfPost) );

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }


    @ApiOperation(value = "edit Type of Post",
                tags = {"Blocko-OverFlow", "Type-Of-Post"},
                notes = "edit type of post. Its required special permission!",
                produces = "application/json",
                protocols = "https",
                code = 200,
                extensions = {
                        @Extension( name = "permission_required", properties = {
                                @ExtensionProperty(name = "TypeOfPost.edit_permission", value = "true"),
                                @ExtensionProperty(name = "Static Permission key", value =  "TypeOfPost_edit" )
                        })
                }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfPost_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = TypeOfPost.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result edit_TypeOfPost(@ApiParam(value = "type_of_post_id String path", required = true) @PathParam("type_of_post_id") String type_of_post_id){
        try{

            final Form<Swagger_TypeOfPost_New> form = Form.form(Swagger_TypeOfPost_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfPost_New help = form.get();


            TypeOfPost typeOfPost = TypeOfPost.find.byId(type_of_post_id);
            if(typeOfPost == null) return GlobalResult.notFoundObject("TypeOfPost type_of_post_id not found");

            if (!typeOfPost.edit_permission())  return GlobalResult.forbidden_Permission();

            if ( TypeOfPost.find.where().ieq("type", help.type ).where().ne("id", type_of_post_id).findRowCount() > 0) return GlobalResult.result_BadRequest("Name is used already");




            typeOfPost.type = help.type;
            typeOfPost.update();

            return GlobalResult.result_ok( Json.toJson(typeOfPost) );

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete Type of Post",
            tags = {"Blocko-OverFlow", "Type-Of-Post"},
            notes = "delete type of post. Its required special permission!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfPost.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfPost_delete" )
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Result_ok.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result delete_TypeOfPost(@ApiParam(value = "type_of_post_id String path", required = true) @PathParam("type_of_post_id") String type_of_post_id){
        try{

            TypeOfPost typeOfPost = TypeOfPost.find.byId(type_of_post_id);
            if(typeOfPost == null) return GlobalResult.notFoundObject("TypeOfPost type_of_post_id not found");

            if (!typeOfPost.delete_permission())  return GlobalResult.forbidden_Permission();

            typeOfPost.delete();

            return GlobalResult.result_ok();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }


/// TYPE OF CONFIRMS ###################################################################################################

    @ApiOperation(value = "new Type of Confirms",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms"},
            notes = "Create new type of Confirms. Its only for Blocko-OverFlow Administrators!",
            produces = "application/json",
            protocols = "https",
            code = 201,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfConfirm.create_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfConfirm_create" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfConfirms_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = TypeOfConfirms.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result new_TypeOfConfirms(){
        try{
            final Form<Swagger_TypeOfConfirms_New> form = Form.form(Swagger_TypeOfConfirms_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfConfirms_New help = form.get();

            TypeOfConfirms typeOfConfirms = new TypeOfConfirms();
            typeOfConfirms.type = help.type;
            typeOfConfirms.color = help.color;
            typeOfConfirms.size =  help.size;

            if (!typeOfConfirms.create_permission())  return GlobalResult.forbidden_Permission();
            typeOfConfirms.save();

            return GlobalResult.created(Json.toJson( typeOfConfirms) );

        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit Type of Confirms",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms"},
            notes = "edit  type of Confirms. Its only for Blocko-OverFlow Administrators!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfConfirm.edit_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfConfirm_edit" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfConfirms_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = TypeOfConfirms.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result edit_TypeOfConfirms(@ApiParam(value = "type_of_confirm_id String path", required = true) @PathParam("type_of_confirm_id") String  type_of_confirm_id){
        try{

            final Form<Swagger_TypeOfConfirms_New> form = Form.form(Swagger_TypeOfConfirms_New.class).bindFromRequest();
            if(form.hasErrors()) {return GlobalResult.formExcepting(form.errorsAsJson());}
            Swagger_TypeOfConfirms_New help = form.get();

            TypeOfConfirms typeOfConfirms = TypeOfConfirms.find.byId(type_of_confirm_id);
            if(typeOfConfirms == null) return GlobalResult.notFoundObject("TypeOfConfirms type_of_confirm_id not found");

            if (!typeOfConfirms.edit_permission())  return GlobalResult.forbidden_Permission();

            typeOfConfirms.type = help.type;
            typeOfConfirms.color = help.color;
            typeOfConfirms.size =  help.size;

            typeOfConfirms.save();

            return GlobalResult.result_ok(Json.toJson( typeOfConfirms) );

        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "edit Type of Confirms",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms"},
            notes = "edit  type of Confirms. Its only for Blocko-OverFlow Administrators!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "TypeOfConfirm.delete_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "TypeOfConfirm_delete" )
                    })
            }
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfConfirms_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = TypeOfConfirms.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = Result_JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result delete_TypeOfConfirms(@ApiParam(value = "type_of_confirm_id String path", required = true) @PathParam("type_of_confirm_id") String  type_of_confirm_id){
        try{

            TypeOfConfirms typeOfConfirms = TypeOfConfirms.find.byId(type_of_confirm_id);
            if(typeOfConfirms == null) return GlobalResult.notFoundObject("TypeOfConfirms type_of_confirm_id not found");

            if (!typeOfConfirms.delete_permission())  return GlobalResult.forbidden_Permission();

            typeOfConfirms.delete();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Type of Confirms",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms"},
            notes = "get  type of Confirms. Its only for Blocko-OverFlow Administrators!",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = TypeOfConfirms.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_TypeOfConfirms(@ApiParam(value = "type_of_confirm_id String path", required = true) @PathParam("type_of_confirm_id") String  type_of_confirm_id){
        try{

            TypeOfConfirms typeOfConfirms = TypeOfConfirms.find.byId(type_of_confirm_id);
            if(typeOfConfirms == null) return GlobalResult.notFoundObject("TypeOfConfirms type_of_confirm_id not found");

            return GlobalResult.result_ok(Json.toJson( typeOfConfirms) );
        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "get Type of Confirms",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms"},
            notes = "get  type of Confirms. Its only for Blocko-OverFlow Administrators!",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = TypeOfConfirms.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_TypeOfConfirms_all(){
        try{
            return GlobalResult.result_ok(Json.toJson( TypeOfConfirms.find.all() ));
        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "set Type of Confirms to Post",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms", "Post"},
            notes = "set type of Confirms to post. Its allowed only for system or Blocko-OverFlow Administrators",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Post.edit_confirms_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Post_edit" )
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Post.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result set_TypeOfConfirm_to_Post(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id, @ApiParam(value = "type_of_confirm_id String path", required = true) @PathParam("type_of_confirm_id") String  type_of_confirm_id){
        try{
            TypeOfConfirms typeOfConfirms = TypeOfConfirms.find.byId(type_of_confirm_id);
            if(typeOfConfirms == null) return GlobalResult.notFoundObject("TypeOfConfirms type_of_confirm_id not found");

            Post post = Post.find.byId(post_id);
            if(post == null)  return GlobalResult.notFoundObject("Post post_id not found");

            if (!post.edit_confirms_permission())  return GlobalResult.forbidden_Permission();

            if(!post.typeOfConfirms.contains(typeOfConfirms)) post.typeOfConfirms.add(typeOfConfirms);

            post.update();

            return GlobalResult.result_ok(Json.toJson(post));
        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "remove Type of Confirms from Post",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms", "Post"},
            notes = "set type of Confirms to post. Its allowed only for system or Blocko-OverFlow Administrators",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Post.edit_confirms_permission", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Post_edit" )
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Post.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result remove_TypeOfConfirm_to_Post(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id,@ApiParam(value = "type_of_confirm_id String path", required = true) @PathParam("type_of_confirm_id") String  type_of_confirm_id){
        try{

            TypeOfConfirms typeOfConfirms = TypeOfConfirms.find.byId(type_of_confirm_id);
            if(typeOfConfirms == null)  return GlobalResult.notFoundObject("TypeOfConfirms type_of_confirm_id not found");

            Post post = Post.find.byId(post_id);
            if(post == null)  return GlobalResult.notFoundObject("Post post_id not found");

            if (!post.edit_confirms_permission())  return GlobalResult.forbidden_Permission();

            if(post.typeOfConfirms.contains(typeOfConfirms)) post.typeOfConfirms.remove(typeOfConfirms);

            post.update();

            return GlobalResult.result_ok(Json.toJson(post));
        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }


/// OTHER ##############################################################################################################

    @ApiOperation(value = "add HashTag to Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "add HashTag to post",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Post.edit", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Post_edit" )
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Post.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result add_HashTag_to_Post(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id, @ApiParam(value = "hash_tag String path", required = true) @PathParam("hash_tag")String hash_tag){
        try{

            Post post = Post.find.byId(post_id);
            if(post == null) return GlobalResult.notFoundObject("Post post_id not found");

            if (!post.edit_permission())  return GlobalResult.forbidden_Permission();

            HashTag postHashTag = HashTag.find.byId(hash_tag);

            if(postHashTag == null) {
                 postHashTag = new HashTag(hash_tag);
                 postHashTag.save();
            }

            if(!post.hashTagsList.contains(postHashTag)) post.hashTagsList.add(postHashTag);

            post.update();

            return GlobalResult.result_ok(Json.toJson(post));

        }catch (Exception e){
            return GlobalResult.internalServerError();
        }

    }

    @ApiOperation(value = "remove HashTag from Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "remove HashTag to post",
            produces = "application/json",
            protocols = "https",
            code = 200,
            extensions = {
                    @Extension( name = "permission_required", properties = {
                            @ExtensionProperty(name = "Post.edit", value = "true"),
                            @ExtensionProperty(name = "Static Permission key", value =  "Post_edit" )
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = Post.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result remove_HashTag_from_Post(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id, @ApiParam(value = "hash_tag String path", required = true) @PathParam("hash_tag")String hash_tag){
        try{

            Post post = Post.find.byId(post_id);
            if(post == null) return GlobalResult.notFoundObject("Post post_id not found");

            HashTag postHashTag = HashTag.find.byId(hash_tag);
            if(postHashTag == null) return GlobalResult.notFoundObject("HashTag hash tag not found");

            if (!post.edit_permission())  return GlobalResult.forbidden_Permission();

            if(post.hashTagsList.contains(postHashTag))post.hashTagsList.remove(postHashTag);

            post.update();

            return GlobalResult.result_ok(Json.toJson(post));
        }catch (Exception e){
            return GlobalResult.internalServerError();
        }

    }

    @ApiOperation(value = "like plus on Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "touch like plus - And user can do that only once! ",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = TypeOfConfirms.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result likePlus(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id){
        try {
            Post post = Post.find.where().eq("id", post_id).findUnique();

            if(post.listOfLikers != null &&  post.listOfLikers.contains(  SecurityController.getPerson()  ) ) throw new Exception("You have decided");


            post.listOfLikers.add(SecurityController.getPerson());
            post.likes++;
            post.update();

            return GlobalResult.result_ok();
        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }


    @ApiOperation(value = "like minus on Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "touch like minus - And user can do that only once! ",
            produces = "application/json",
            protocols = "https",
            code = 200
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = TypeOfConfirms.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result likeMinus(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id){
        try {
            Post post = Post.find.where().eq("id", post_id).findUnique();

            if(post.listOfLikers != null &&  post.listOfLikers.contains(  SecurityController.getPerson()  ) ) return GlobalResult.forbidden_Permission();

            post.listOfLikers.add(SecurityController.getPerson());
            post.likes--;
            post.update();

            return GlobalResult.result_ok();
        }catch (Exception e){
            return GlobalResult.internalServerError();
        }
    }






}
