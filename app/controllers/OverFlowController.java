package controllers;


import com.avaje.ebean.Ebean;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;
import utilities.response.response_objects.JsonValueMissing;
import utilities.response.response_objects.Result_PermissionRequired;
import utilities.response.response_objects.Result_Unauthorized;
import utilities.response.response_objects.Result_ok;
import utilities.swagger.documentationClass.*;

import javax.websocket.server.PathParam;
import java.util.*;

@Api(value = "Not Documented API - InProgress or Stuck")
public class OverFlowController  extends Controller {

// PUBLIC **********************************************************************************************************************


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
            if(post == null) return GlobalResult.notFoundObject();

            post.views++;
            post.update();

            return GlobalResult.okResult( Json.toJson(post) );

        } catch (Exception e) {
            Logger.error("Error", e);
            return GlobalResult.internalServerError();
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


            List<Post> list = query.findList();


            return GlobalResult.okResult(Json.toJson(list));




        } catch (Exception e){
            e.printStackTrace();
            return GlobalResult.nullPointerResult(e);
        }

    }

    public Result get_Post_links(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id){
        try {
            Post postMain = Post.find.byId(post_id);
            if(postMain == null) return GlobalResult.notFoundObject();

            List<ObjectNode> list = new ArrayList<>();


                for(LinkedPost linkedPost : postMain.linkedQuestions){

                    Post post = linkedPost.answer;
                    ObjectNode json = Json.newObject();
                    json.put("linkId", linkedPost.linkId);

                    json.put("post", "http://localhost:9000/overflow/post/"  +  post.postId);
                    json.put("name", post.name);
                    json.put("question", post.text_of_post);

                    List<ObjectNode> answerJson = new ArrayList<>();

                    for(Post answer : post.answers){
                        ObjectNode j = Json.newObject();
                        j.put("textOfAnswer", answer.text_of_post);
                        answerJson.add(j);
                    }

                    json.replace("answers", Json.toJson(answerJson));
                    list.add(json);
                }
                return GlobalResult.okResult(Json.toJson(list));

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
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
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
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
            if (typeOfPost == null) return GlobalResult.notFoundObject();

            Post post = new Post();
            post.author = SecurityController.getPerson();

            post.name = help.name;
            post.type = typeOfPost;
            post.views = 0;
            post.likes = 0;
            post.text_of_post = help.text_of_post;
            post.date_of_create = new Date();

            UtilTools.add_hashTags_to_Post(form.get().hash_tags, post);

            post.save();

            SecurityController.getPerson().personPosts.add(post);
            SecurityController.getPerson().update();

            return GlobalResult.created(Json.toJson(post));


          } catch (Exception e) {
                Logger.error("Error", e);
                return GlobalResult.internalServerError();
        }
    }

    @ApiOperation(value = "delete Post",
            tags = {"Blocko-OverFlow"},
            notes = "You can delete Main Post, Answers to post and comments.. ",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
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
            if (post == null ) return GlobalResult.notFoundObject();
            if (!post.author.id.equals( SecurityController.getPerson().id) ) return GlobalResult.forbidden_Global();

            post.delete();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @ApiOperation(value = "edit Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "You can edit main post",
            produces = "application/json",
            protocols = "https",
            code = 200,
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
            @ApiResponse(code = 200, message = "Ok Result",      response = Post.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result edit_Post(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id){
        try {
            final Form<Swagger_Post_New> form = Form.form(Swagger_Post_New.class).bindFromRequest();
            if(form.hasErrors()) {return badRequest(form.errorsAsJson());}
            Swagger_Post_New help = form.get();

            Post post = Post.find.byId(post_id);
            if (post == null) return GlobalResult.notFoundObject();

            if (!post.author.id.equals( SecurityController.getPerson().id) ) return GlobalResult.forbidden_Global();

            TypeOfPost typeOfPost = TypeOfPost.find.byId( help.type_of_post_id);
            if(typeOfPost == null) return GlobalResult.notFoundObject();

            post.name = help.name;
            post.type = typeOfPost;
            post.text_of_post = help.text_of_post;
            post.updated = true;

            post.hashTagsList.clear();

            UtilTools.add_hashTags_to_Post( help.hash_tags, post );

            post.update();

           return GlobalResult.okResult(Json.toJson(post));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name - String", "comment - TEXT", "hashTags - [String, String..]");
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Post_Comment",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Post.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
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
           if (parentPost == null) return GlobalResult.notFoundObject();

           if( parentPost.postParentComment != null)  return GlobalResult.nullPointerResult("You cannot comment another comment");

           Post post = new Post();
           post.author = SecurityController.getPerson();
           post.likes = 0;
           post.text_of_post = help.text_of_post;
           post.date_of_create = new Date();

           UtilTools.add_hashTags_to_Post(help.hash_tags, post );

           parentPost.comments.add(post);
           post.postParentComment = parentPost;

           parentPost.save();
           post.save();

           return GlobalResult.okResult( Json.newObject().put( "postId", post.postId ));
       } catch (NullPointerException e) {
           return GlobalResult.nullPointerResult(e, "name - String", "comment - TEXT", "hashTags - [String, String..]");
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Post_Answer",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Post.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
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

            if( parentPost.postParentComment != null)  return GlobalResult.nullPointerResult("You cannot answer to comment");
            if( parentPost.postParentAnswer  != null)  return GlobalResult.nullPointerResult("You cannot answer to another answer");

            Post post = new Post();
            post.author = SecurityController.getPerson();
            post.likes = 0;
            post.text_of_post = help.text_of_post;
            post.date_of_create = new Date();

            UtilTools.add_hashTags_to_Post(help.hash_tags, post );

            parentPost.answers.add(post);
            post.postParentAnswer = parentPost;

            parentPost.save();
            post.save();

            return GlobalResult.okResult(Json.toJson(post));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "text_of_post", "[hash_tags]");
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
                            dataType = "utilities.swagger.documentationClass.Swagger_Post_Comment",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = Post.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
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

            post.text_of_post = help.text_of_post;

            post.hashTags().clear();

            UtilTools.add_hashTags_to_Post(help.hash_tags, post );

            post.update();
            return GlobalResult.okResult(Json.toJson(post));

        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name - String", "comment - TEXT", "hashTags - [String, String..]");
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
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
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

            if (question == null)   return GlobalResult.notFoundObject();
            if (answer == null)     return GlobalResult.notFoundObject();
            if (question.postParentComment != null)   return GlobalResult.badRequest("You can link only main post");
            if (answer.postParentComment != null)     return GlobalResult.badRequest("You can link only main post");

            LinkedPost linkedPost = new LinkedPost();
            linkedPost.answer = answer;
            linkedPost.question = question;
            linkedPost.author = SecurityController.getPerson();

            linkedPost.save();

            return GlobalResult.okResult(Json.toJson(linkedPost));
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
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
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
            if (!post.author.id.equals( SecurityController.getPerson().id) ) return GlobalResult.forbidden_Global();

            post.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }

/// TYPE OF POST ###################################################################################################################*/


    @ApiOperation(value = "new Type of Post",
            tags = {"Blocko-OverFlow", "Type-Of-Post"},
            notes = "Create new type of post. Its only for Blocko-OverFlow Administrators!",
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
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfPost_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = TypeOfPost.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
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

            if( TypeOfPost.find.where().ieq("type", help.type ).findUnique() != null) return GlobalResult.badRequest("Duplicate Value");

            TypeOfPost typeOfPost = new TypeOfPost();
            typeOfPost.type =  help.type;

            typeOfPost.save();

            return GlobalResult.okResult( Json.toJson(typeOfPost) );

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
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = TypeOfPost.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result get_TypeOfPost_all(){
        try{
            return GlobalResult.okResult(Json.toJson( TypeOfPost.find.all() ));
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
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
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
            if(typeOfPost == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult( Json.toJson(typeOfPost) );

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
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfPost_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Ok Result",      response = TypeOfPost.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
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
            if(typeOfPost == null) return GlobalResult.notFoundObject();

            List<TypeOfPost> list = TypeOfPost.find.where().ieq("type", help.type ).where().ne("id", type_of_post_id).findList();
            if(list.size()>0) return GlobalResult.badRequest("Name is used already");


            typeOfPost.type = help.type;
            typeOfPost.update();

            return GlobalResult.okResult( Json.toJson(typeOfPost) );

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
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfPost_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
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
            if(typeOfPost == null) return GlobalResult.notFoundObject();

            typeOfPost.delete();

            return GlobalResult.okResult();

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }

/// TYPE OF CONFIRMS ###################################################################################################################*/


    @ApiOperation(value = "new Type of Confirms",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms"},
            notes = "Create new type of Confirms. Its only for Blocko-OverFlow Administrators!",
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
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfConfirms_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful created",      response = TypeOfConfirms.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
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

            typeOfConfirms.save();

            return GlobalResult.okResult(Json.toJson( typeOfConfirms) );

        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @ApiOperation(value = "edit Type of Confirms",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms"},
            notes = "edit  type of Confirms. Its only for Blocko-OverFlow Administrators!",
            produces = "application/json",
            protocols = "https",
            code = 200,
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
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfConfirms_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result",      response = TypeOfConfirms.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
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
            if(typeOfConfirms == null) return GlobalResult.notFoundObject();

            typeOfConfirms.type = help.type;
            typeOfConfirms.color = help.color;
            typeOfConfirms.size =  help.size;

            typeOfConfirms.save();

            return GlobalResult.okResult(Json.toJson( typeOfConfirms) );

        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @ApiOperation(value = "edit Type of Confirms",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms"},
            notes = "edit  type of Confirms. Its only for Blocko-OverFlow Administrators!",
            produces = "application/json",
            protocols = "https",
            code = 200,
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
                            dataType = "utilities.swagger.documentationClass.Swagger_TypeOfConfirms_New",
                            required = true,
                            paramType = "body",
                            value = "Contains Json with values"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = TypeOfConfirms.class),
            @ApiResponse(code = 400, message = "Some Json value Missing", response = JsonValueMissing.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result delete_TypeOfConfirms(@ApiParam(value = "type_of_confirm_id String path", required = true) @PathParam("type_of_confirm_id") String  type_of_confirm_id){
        try{

            TypeOfConfirms typeOfConfirms = TypeOfConfirms.find.byId(type_of_confirm_id);
            if(typeOfConfirms == null) return GlobalResult.notFoundObject();

            typeOfConfirms.delete();

            return GlobalResult.okResult();

        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @ApiOperation(value = "get Type of Confirms",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms"},
            notes = "get  type of Confirms. Its only for Blocko-OverFlow Administrators!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
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
            if(typeOfConfirms == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson( typeOfConfirms) );
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @ApiOperation(value = "get Type of Confirms",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms"},
            notes = "get  type of Confirms. Its only for Blocko-OverFlow Administrators!",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
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
            return GlobalResult.okResult(Json.toJson( TypeOfConfirms.find.all() ));
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @ApiOperation(value = "set Type of Confirms to Post",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms", "Post"},
            notes = "set type of Confirms to post. Its allowed only for system or Blocko-OverFlow Administrators",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
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
            if(typeOfConfirms == null) return GlobalResult.notFoundObject();

            Post post = Post.find.byId(post_id);
            if(post == null)  return GlobalResult.notFoundObject();

            if(!post.typeOfConfirms.contains(typeOfConfirms)) post.typeOfConfirms.add(typeOfConfirms);

            post.update();

            return GlobalResult.okResult(Json.toJson(post));
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @ApiOperation(value = "remove Type of Confirms from Post",
            tags = {"Blocko-OverFlow", "Type-Of-Confirms", "Post"},
            notes = "set type of Confirms to post. Its allowed only for system or Blocko-OverFlow Administrators",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
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
            if(typeOfConfirms == null)  return GlobalResult.notFoundObject();

            Post post = Post.find.byId(post_id);
            if(post == null)  return GlobalResult.notFoundObject();

            if(post.typeOfConfirms.contains(typeOfConfirms)) post.typeOfConfirms.remove(typeOfConfirms);

            post.update();

            return GlobalResult.okResult(Json.toJson(post));
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

/// OTHER ###################################################################################################################*/

    @ApiOperation(value = "add HashTag to Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "add HashTag to post",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = TypeOfConfirms.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result add_HashTag_to_Post(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id, @ApiParam(value = "hash_tag String path", required = true) @PathParam("hash_tag")String hash_tag){
        try{

            Post post = Post.find.byId(post_id);
            if(post == null) return GlobalResult.notFoundObject();


            HashTag postHashTag = HashTag.find.byId(hash_tag);

            if(postHashTag == null) {
                 postHashTag = new HashTag(hash_tag);
                 postHashTag.save();
            }

            if(!post.hashTagsList.contains(postHashTag)) post.hashTagsList.add(postHashTag);

            post.update();

            return GlobalResult.okResult();

        }catch (Exception e){
            return GlobalResult.internalServerError();
        }

    }

    @ApiOperation(value = "remove HashTag from Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "add HashTag to post",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok Result", response = TypeOfConfirms.class),
            @ApiResponse(code = 401, message = "Unauthorized request",    response = Result_Unauthorized.class),
            @ApiResponse(code = 403, message = "Need required permission",response = Result_PermissionRequired.class),
            @ApiResponse(code = 500, message = "Server side Error")
    })
    @Security.Authenticated(Secured.class)
    public Result remove_HashTag_from_Post(@ApiParam(value = "post_id String path", required = true) @PathParam("post_id") String post_id, @ApiParam(value = "hash_tag String path", required = true) @PathParam("hash_tag")String hash_tag){
        try{

            Post post = Post.find.byId(post_id);
            if(post == null) return GlobalResult.notFoundObject();

            HashTag postHashTag = HashTag.find.byId(hash_tag);
            if(postHashTag == null) return GlobalResult.notFoundObject();


            if(post.hashTagsList.contains(postHashTag))post.hashTagsList.remove(postHashTag);

            post.update();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.internalServerError();
        }

    }

    @ApiOperation(value = "like plus on Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "touch like plus - And user can do that only once! ",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner", description = "For create new C_program, you have to own project"),
                                    @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
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
            Post post = Post.find.where().eq("postId", post_id).findUnique();

            if(post.listOfLikers != null &&  post.listOfLikers.contains(  SecurityController.getPerson()  ) ) throw new Exception("You have decided");


            post.listOfLikers.add(SecurityController.getPerson());
            post.likes++;
            post.update();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }


    @ApiOperation(value = "like minus on Post",
            tags = {"Blocko-OverFlow", "Post"},
            notes = "touch like minus - And user can do that only once! ",
            produces = "application/json",
            protocols = "https",
            code = 200,
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "project.owner",  description = "For create new C_program, you have to own project"),
                                       @AuthorizationScope(scope = "Project_Editor", description = "You need Project_Editor permission")}
                    )
            }
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
            Post post = Post.find.where().eq("postId", post_id).findUnique();

            if(post.listOfLikers != null &&  post.listOfLikers.contains(  SecurityController.getPerson()  ) ) return GlobalResult.forbidden_Global();

            post.listOfLikers.add(SecurityController.getPerson());
            post.likes--;
            post.update();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }






}
