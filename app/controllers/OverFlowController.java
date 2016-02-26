package controllers;


import com.avaje.ebean.Ebean;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
import models.overflow.*;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.a_main_utils.UtilTools;
import utilities.loginEntities.Secured;
import utilities.response.GlobalResult;

import java.util.*;

@Api(value = "Ještě neroztříděné a neupravené",
        description = "Compilation operation (C_Program, Processor, Libraries, TypeOfBoard...",
        authorizations = { @Authorization(value="logged_in", scopes = {} )}
)
public class OverFlowController  extends Controller {

// PUBLIC **********************************************************************************************************************


    public Result getPost(String id){
        try{
            Post post = Post.find.byId(id);
            if(post == null) return GlobalResult.notFoundObject();

            post.views++;
            post.update();


            return GlobalResult.okResult( Json.toJson(post) );

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("ProgramingPackageController - newBlock ERROR");
            return GlobalResult.internalServerError();
        }
    }


   public Result hashTagsListOnPost(String id){
        try{
            Post post = Post.find.byId(id);
            if(post == null) return GlobalResult.notFoundObject();
            return GlobalResult.okResult(Json.toJson(post.hashTagsList));
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    public Result commentsListOnPost(String id){
        try{
            Post post = Post.find.byId(id);
            if(post == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(post.comments));

        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    public Result answereListOnPost(String id){
        try{
            Post post = Post.find.byId(id);
            if(post == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson(post.answers));
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    public Result textOfPost(String id){
        try{
            Post post = Post.find.byId(id);
            if(post == null) return GlobalResult.notFoundObject();
            return GlobalResult.okResult(Json.toJson(post.textOfPost));
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }


    /**
     Example Predicates

     eq(...) = equals
     ne(...) = not equals
     ieq(...) = case insensitve equals
     between(...) = between
     gt(...) = greater than
     ge(...) = greater than or equals
     lt(...) = less than or equals
     le(...) = less than or equals
     isNull(...) = is null
     isNotNull(...) = is not null
     like(...) = like
     startsWith(...) = string starts with
     endswith(...) = string ends with
     contains(...) = string conains
     in(...) = in a subquery, collection or array
     exists(...) = at least one row exists in a subquery
     notExists(...) = no row exists in a subquery
     more...
     */

    @BodyParser.Of(BodyParser.Json.class)
    public Result getPostByFilter(){
        try {
            JsonNode json = request().body().asJson();

            Query<Post> query = Ebean.find(Post.class);
            query.where().eq("postParentAnswer", null);
            query.where().eq("postParentComment", null);


            // If contains HashTags
            if(json.has("hash_tags") ){
                List<String> hashTags = UtilTools.getListFromJson( json, "hash_tags" );
                Set<String> HashTagset = new HashSet<>(hashTags);
                query.where().in("hashTagsList.postHashTagId", HashTagset);

            }

            // If contains confirms
            if(json.has("confirms") ){
                List<String> confirms = UtilTools.getListFromJson( json, "confirms" );
                Set<String> confirmsSet = new HashSet<>(confirms);
                 query.where().in("hashTagsList.postHashTagId", confirmsSet);
            }

            // From date
            if(json.has("date_from")){
                Date dateFrom = UtilTools.returnDateFromMillis( json.get("date_from").asText());
                query.where().ge("dateOfCreate", dateFrom);
            }

            // To date
            if(json.has("date_to")){
                Date dateTo = UtilTools.returnDateFromMillis( json.get("date_to").asText() );
                query.where().le("dateOfCreate", dateTo);
            }

            if(json.has("type")){
                List<String> type=  UtilTools.getListFromJson( json, "type" );
                query.where().in("type.id", type);
            }

            if(json.has("nick_name")){
                String authorId = json.get("nick_name").asText();
                query.where().ieq("author.nick_name", authorId);
            }


            if(json.has("count_from")){
                Integer countFrom = json.get("count_from").asInt();
                query.setFirstRow(countFrom);
            }

            if(json.has("count_to")){
                Integer countTo = json.get("count_to").asInt();
                query.setMaxRows(countTo);
            }

            if(json.has("order_by")){
                JsonNode rdb = json.get("order_by");

               String order = rdb.get("order").asText();
               String value = rdb.get("value").asText();

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


    public Result getPostLinkedAnswers(String id){
        try {
            Post postMain = Post.find.byId(id);
            if(postMain == null) return GlobalResult.notFoundObject();

            List<ObjectNode> list = new ArrayList<>();


                for(LinkedPost linkedPost : postMain.linkedQuestions){

                    Post post = linkedPost.answer;
                    ObjectNode json = Json.newObject();
                    json.put("linkId", linkedPost.linkId);

                    json.put("post", "http://localhost:9000/overflow/post/"  +  post.postId);
                    json.put("name", post.name);
                    json.put("question", post.textOfPost);

                    List<ObjectNode> answerJson = new ArrayList<>();

                    for(Post answer : post.answers){
                        ObjectNode j = Json.newObject();
                        j.put("textOfAnswer", answer.textOfPost);
                        answerJson.add(j);
                    }

                    json.replace("answers", Json.toJson(answerJson));
                    list.add(json);
                }
                return GlobalResult.okResult(Json.toJson(list));

        } catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }


// SECURED **********************************************************************************************************************
    @Security.Authenticated(Secured.class)
    public Result getAllPersonPost(){
        try {

          return GlobalResult.okResult( Json.toJson(SecurityController.getPerson().personPosts)  );

        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result newPost(){
        try {
            JsonNode json = request().body().asJson();

            TypeOfPost typeOfPost = TypeOfPost.find.byId(json.get("type").asText());
            if(typeOfPost == null) return GlobalResult.notFoundObject();

            Post post = new Post();
            post.author = SecurityController.getPerson();
            post.name = json.get("name").asText();
            post.type = typeOfPost;
            post.views = 0;
            post.likes = 0;
            post.textOfPost = json.get("comment").asText();
            post.dateOfCreate = new Date();


           for (final JsonNode objNode : json.get("hashTags")) {

                HashTag postHashTag = HashTag.find.byId(objNode.asText());

                if(postHashTag == null) {
                    postHashTag = new HashTag(objNode.asText());
                    postHashTag.save();
                }

                if(!post.hashTagsList.contains(postHashTag)) post.hashTagsList.add(postHashTag);

            }

            post.save();

            SecurityController.getPerson().personPosts.add(post);
            SecurityController.getPerson().update();

            return GlobalResult.created( Json.toJson(post) );
        } catch (NullPointerException e) {
            return GlobalResult.nullPointerResult(e, "name - String", "comment - TEXT", "hashTags - [String, String..]");
        } catch (Exception e) {
            Logger.error("Error", e);
            Logger.error("OverFlowController - newPost ERROR");
            return GlobalResult.internalServerError();
        }
    }

    @Security.Authenticated(Secured.class)
    public  Result deletePost(String postId){
        try {

            Post post = Post.find.byId(postId);
            if (post == null ) return GlobalResult.notFoundObject();
            if (!post.author.id.equals( SecurityController.getPerson().id) ) return GlobalResult.forbidden_Global();

            post.delete();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result editPost(String id){
        try {
            JsonNode json = request().body().asJson();

            System.out.println("Jsem zde");

            Post post = Post.find.byId(id);
            if (post == null) return GlobalResult.notFoundObject();

            if (!post.author.id.equals( SecurityController.getPerson().id) ) return GlobalResult.forbidden_Global();

            TypeOfPost typeOfPost = TypeOfPost.find.byId(json.get("type").asText());
            if(typeOfPost == null) return GlobalResult.notFoundObject();

            post.name = json.get("name").asText();
            post.type = typeOfPost;
            post.textOfPost = json.get("comment").asText();
            post.updated = true;

            post.hashTagsList.clear();

            for (final JsonNode objNode : json.get("hashTags")) {

                HashTag postHashTag = HashTag.find.byId(objNode.asText());
                System.out.println("3");

                if(postHashTag == null) {
                    postHashTag = new HashTag(objNode.asText());
                    postHashTag.save();
                }

                if(!post.hashTagsList.contains(postHashTag)) post.hashTagsList.add(postHashTag);

            }

            post.update();

           return GlobalResult.okResult();

        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }

    }

    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result addComment(){
       try {
           JsonNode json = request().body().asJson();

           Post parentPost = Post.find.byId(json.get("postId").asText());
           if (parentPost == null) return GlobalResult.notFoundObject();

           if( parentPost.postParentComment != null)  return GlobalResult.nullPointerResult("You cannot comment another comment");

           Post post = new Post();
           post.author = SecurityController.getPerson();
           post.likes = 0;
           post.textOfPost = json.get("comment").asText();
           post.dateOfCreate = new Date();


           for (final JsonNode objNode : json.get("hashTags")) {

               HashTag postHashTag = HashTag.find.byId(objNode.asText());

               if(postHashTag == null) {
                   postHashTag = new HashTag(objNode.asText());
                   postHashTag.save();
               }

               if(!post.hashTagsList.contains(postHashTag)) post.hashTagsList.add(postHashTag);

           }

           parentPost.comments.add(post);
           post.postParentComment = parentPost;

           parentPost.save();
           post.save();

           return GlobalResult.okResult( Json.newObject().put( "postId", post.postId ));
       }catch (Exception e){
           return GlobalResult.nullPointerResult(e);
       }
    }

    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public Result addAnswer(){
        try {
            JsonNode json = request().body().asJson();

            Post parentPost = Post.find.byId(json.get("postId").asText());
            if (parentPost == null) throw new Exception("Post not Exist");

            if( parentPost.postParentComment != null)  return GlobalResult.nullPointerResult("You cannot answer to comment");
            if( parentPost.postParentAnswer != null)   return GlobalResult.nullPointerResult("You cannot answer to another  answer");

            Post post = new Post();
            post.author = SecurityController.getPerson();
            post.likes = 0;
            post.textOfPost = json.get("comment").asText();
            post.dateOfCreate = new Date();


            for (final JsonNode objNode : json.get("hashTags")) {

                HashTag postHashTag = HashTag.find.byId(objNode.asText());

                if(postHashTag == null) {
                    postHashTag = new HashTag(objNode.asText());
                    postHashTag.save();
                }

                if(!post.hashTagsList.contains(postHashTag)) post.hashTagsList.add(postHashTag);

            }

            parentPost.answers.add(post);
            post.postParentAnswer = parentPost;

            parentPost.save();
            post.save();

            return GlobalResult.okResult( Json.newObject().put( "postId", post.postId ));

        }catch (Exception e){

            return GlobalResult.nullPointerResult(e);

        }
    }

    @Security.Authenticated(Secured.class)
    public Result updateComment(String id){
        try {
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Post comment = Post.find.byId(id);
            if (comment == null) throw new Exception("Comment not Exist");

            comment.textOfPost = json.get("comment").asText();

            comment.hashTags().clear();

            for (final JsonNode objNode : json.get("hashTags")) {

                HashTag postHashTag = HashTag.find.byId(objNode.asText());

                if(postHashTag == null) {
                    postHashTag = new HashTag(objNode.asText());
                    postHashTag.save();
                }

                if(!comment.hashTagsList.contains(postHashTag)) comment.hashTagsList.add(postHashTag);

            }

            comment.save();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public Result linkWithPreviousAnswer(){
        try {
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Post question = Post.find.byId(json.get("postId").asText());
            Post answer = Post.find.byId(json.get("linkId").asText());

            if (question == null)   return GlobalResult.nullPointerResult(new Exception(" Overflow post not Exist"));
            if (answer == null)     return GlobalResult.nullPointerResult(new Exception(" Overflow link post not Exist"));
            if (question.postParentComment != null)     throw new Exception("You can link only main post");
            if (answer.postParentComment != null)       throw new Exception("You can link only main post");

            LinkedPost linkedPost = new LinkedPost();
            linkedPost.answer = answer;
            linkedPost.question = question;
            linkedPost.author = SecurityController.getPerson();

            linkedPost.save();

            ObjectNode result = Json.newObject();
            result.put("connectionId", linkedPost.linkId);

            return GlobalResult.okResult(result);
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e, "postId", "linkId");
        }
    }

    @Security.Authenticated(Secured.class)
    public Result unlinkWithPreviousAnswer(String id){
        try {
            LinkedPost post = LinkedPost.find.byId(id);

            if (post == null ) throw new Exception("Linked connection not Exist");
            if (!post.author.id.equals( SecurityController.getPerson().id) ) return GlobalResult.forbidden_Global();

            post.delete();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

//------------------------------------------------------------------------------------------------------------------------

    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result new_TypeOfPost(){
        try{
            JsonNode json = request().body().asJson();

            if( TypeOfPost.find.where().ieq("type", json.get("type").asText() ).findUnique() != null) throw new Exception("Duplicate value");

            TypeOfPost typeOfPost = new TypeOfPost();
            typeOfPost.type = json.get("type").asText();

            typeOfPost.save();

            return GlobalResult.okResult( Json.toJson(typeOfPost) );
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public Result get_TypeOfPost_all(){
        try{
            return GlobalResult.okResult(Json.toJson( TypeOfPost.find.all() ));
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public Result get_TypeOfPost(String type_of_post_id){
        try{

            TypeOfPost typeOfPost = TypeOfPost.find.byId(type_of_post_id);
            if(typeOfPost == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult( Json.toJson(typeOfPost) );

        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }


    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result edit_TypeOfPost(String type_of_post_id){
        try{
            JsonNode json = request().body().asJson();

            TypeOfPost typeOfPost = TypeOfPost.find.byId(type_of_post_id);
            if(typeOfPost == null) return GlobalResult.notFoundObject();

            List<TypeOfPost> list = TypeOfPost.find.where().ieq("type",json.get("type").asText()).where().ne("id", type_of_post_id).findList();
            if(list.size()>0) return GlobalResult.badRequest("Name is used already");


            typeOfPost.type = json.get("type").asText();
            typeOfPost.update();

            return GlobalResult.okResult( Json.toJson(typeOfPost) );
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public Result delete_TypeOfPost(String type_of_post_id){
        try{

            TypeOfPost typeOfPost = TypeOfPost.find.byId(type_of_post_id);
            if(typeOfPost == null) return GlobalResult.notFoundObject();

            typeOfPost.delete();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

//------------------------------------------------------------------------------------------------------------------------
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result new_TypeOfConfirms(){
        try{
            JsonNode json = request().body().asJson();

            TypeOfConfirms typeOfConfirms = new TypeOfConfirms();
            typeOfConfirms.type = json.get("type").asText();
            typeOfConfirms.color = json.get("color").asText();
            typeOfConfirms.size = json.get("size").asInt();

            typeOfConfirms.save();

            return GlobalResult.okResult(Json.toJson( typeOfConfirms) );

        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public Result edit_TypeOfConfirms(String type_of_confirm_id){
        try{
            JsonNode json = request().body().asJson();

            TypeOfConfirms typeOfConfirms = TypeOfConfirms.find.byId(type_of_confirm_id);
            if(typeOfConfirms == null) return GlobalResult.notFoundObject();

            typeOfConfirms.type = json.get("type").asText();
            typeOfConfirms.color = json.get("color").asText();
            typeOfConfirms.size = json.get("size").asInt();

            typeOfConfirms.save();

            return GlobalResult.okResult(Json.toJson( typeOfConfirms) );

        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public Result delete_TypeOfConfirms(String type_of_confirm_id){
        try{

            TypeOfConfirms typeOfConfirms = TypeOfConfirms.find.byId(type_of_confirm_id);
            if(typeOfConfirms == null) return GlobalResult.notFoundObject();

            typeOfConfirms.delete();

            return GlobalResult.okResult();

        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }



    @Security.Authenticated(Secured.class)
    public Result get_TypeOfConfirms(String type_of_confirm_id){
        try{

            TypeOfConfirms typeOfConfirms = TypeOfConfirms.find.byId(type_of_confirm_id);
            if(typeOfConfirms == null) return GlobalResult.notFoundObject();

            return GlobalResult.okResult(Json.toJson( typeOfConfirms) );
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }


    @Security.Authenticated(Secured.class)
    public Result get_TypeOfConfirms_all(){
        try{
            return GlobalResult.okResult(Json.toJson( TypeOfConfirms.find.all() ));
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }


    @Security.Authenticated(Secured.class)
    public Result set_TypeOfConfirm_to_Post(String post_id, String type_of_confirm_id){
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

    @Security.Authenticated(Secured.class)
    public Result remove_TypeOfConfirm_to_Post(String post_id, String type_of_confirm_id){
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

//------------------------------------------------------------------------------------------------------------------------

    @Security.Authenticated(Secured.class)
    public Result add_HashTag_to_Post(String post_id, String hashTag){
        try{

            Post post = Post.find.byId(post_id);
            if(post == null) return GlobalResult.notFoundObject();


            HashTag postHashTag = HashTag.find.byId(hashTag);

            if(postHashTag == null) {
                 postHashTag = new HashTag(hashTag);
                 postHashTag.save();
            }

            if(!post.hashTagsList.contains(postHashTag)) post.hashTagsList.add(postHashTag);

            post.update();

            return GlobalResult.okResult();

        }catch (Exception e){
            return GlobalResult.internalServerError();
        }

    }

    @Security.Authenticated(Secured.class)
    public Result remove_HashTag_from_Post(String post_id, String hashTag){
        try{

            Post post = Post.find.byId(post_id);
            if(post == null) return GlobalResult.notFoundObject();

            HashTag postHashTag = HashTag.find.byId(hashTag);
            if(postHashTag == null) return GlobalResult.notFoundObject();


            if(post.hashTagsList.contains(postHashTag))post.hashTagsList.remove(postHashTag);

            post.update();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.internalServerError();
        }

    }

        @Security.Authenticated(Secured.class)
    public Result likePlus(String postId){
        try {
            Post post = Post.find.where().eq("postId", postId).findUnique();

            if(post.listOfLikers != null &&  post.listOfLikers.contains(  SecurityController.getPerson()  ) ) throw new Exception("You have decided");


            post.listOfLikers.add(SecurityController.getPerson());
            post.likes++;
            post.update();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.nullPointerResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public Result likeMinus(String postId){
        try {
            Post post = Post.find.where().eq("postId", postId).findUnique();

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
