package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.overflow.*;
import models.permission.PermissionKey;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.GlobalResult;
import utilities.Secured;
import utilities.UtilTools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class OverFlowController  extends Controller {

/**
 * Každý uživatel má možnost provádět operace nad vlastními příspěvky. Uložit nový POST, Komentář i Odpověď.
 * Má možnost je smazat a editovat. Pokud nejsem vlastník, tyto operace provádět nemohu.
 *
 * Existuje skupina práv, které dávají uživateli možnost upravovat cizí příspěvky nebo je mazat.
 * Jde především o role moderátorů a administrátorů.
 *
 * Skupina Administrator může vždy vše
 * Skupina Moderator může jen
 *
 * 1. Právo Editovat
 * 2. Právo Mazat
 * 3. Právo Uzamknout
 * 4. Právo Zablokovat uživatele
 * 5. Předávat stejná nebo nižší práva
 */

 // Permission //
 // Jméno které bude používáno - Lze Refaktoringem v budoucnu měnit název controlleru a ničemu to nebude vadit

    /**
     * Ihned po startu serveru je tato metoda zavolána z třídy GLOBAL.onStart() aby zajistila, že budou v
     * databázi vhodně zaregistrovány všechny skupiny. Uživatele do skupin to však v žádném případě nikdy nepřidá.
     *
     * Tato metoda slouží jen k počátečnímu nastavení serveru a databáze. Musí ohlídat, že nevytváří duplicity a další.
     *
     */
    public static void onStartPermission(){
        try {
            final String controllerName =  OverFlowController.class.getSimpleName();

            // ošetříme jednoduchým dotazem zda v databázi už skupina není
            if(PermissionKey.find.byId(controllerName + "_Delete") != null) return;

            new PermissionKey(controllerName + "_Delete",       "Can delete Post in Overflow");
            new PermissionKey(controllerName + "_Lock",         "Can lock Post thread");
            new PermissionKey(controllerName + "_Edit",         "Can edit all Post and Comments");
            new PermissionKey(controllerName + "_PersonBlock",  "Can block Person ");
            new PermissionKey(controllerName + "_Promote",      "Can promote other Person to same Permission");

        }catch (Exception e){
            System.out.println("***********************************************");
            System.out.println("Došlo k chybě v OverFlowController.onStartPermission" );
            e.printStackTrace();
            System.out.println();
            System.out.println("***********************************************");
        }
    }


// PUBLIC
//**********************************************************************************************************************

    public Result getPost(String id){
        try{
            Post post = Post.find.byId(id);
            if(post == null) throw new Exception("Post not Exist");
                post.views++;
            post.update();

            return GlobalResult.okResult(Json.toJson(post));

        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }


   public Result hashTagsListOnPost(String id){
        try{
            Post post = Post.find.byId(id);
            if(post == null) throw new Exception("Id not Exist");
            return GlobalResult.okResult(Json.toJson(post.hashTagsList));
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result commentsListOnPost(String id){
        try{
            Post post = Post.find.byId(id);
            if(post == null) throw new Exception("Id not Exist");



            return GlobalResult.okResult(Json.toJson(post.comments));

        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result answereListOnPost(String id){
        try{
            Post post = Post.find.byId(id);
            if(post == null) throw new Exception("Id not Exist");
            return GlobalResult.okResult(Json.toJson(post.answers));
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    public Result textOfPost(String id){
        try{
            Post post = Post.find.byId(id);
            if(post == null) throw new Exception("Id not Exist");
            return GlobalResult.okResult(Json.toJson(post.textOfPost));
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
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


    public Result getLatestPost(){
        try {
            List<Post> latestPost  = Post.find.
                    where().eq("postParentAnswer", null).eq("postParentComment", null)
                    .orderBy("dateOfCreate")
                    .findPagedList(0, 5).getList();

            return GlobalResult.okResult(Json.toJson(latestPost));

        } catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }

    }


    public Result getPostByFilter(){
        try {
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");


            List<String> confirms =  UtilTools.getListFromJson( json, "confirms" );
            List<String> hashTags =  UtilTools.getListFromJson( json, "hashTags" );

            int countFrom = json.get("countFrom").asInt();
            int countTo   = json.get("countTo").asInt();

            Date dateFrom = UtilTools.returnDateFromMillis( json.get("dateFrom").asText()  );
            Date dateTo   = UtilTools.returnDateFromMillis( json.get("dateTo").asText()  );

            String personId = json.get("personId").asText();
            String type = json.get("type").asText();

            List<Post> latestPost  = Post.find.where()
                    .in("hashTagsList.postHashTagId", hashTags)
                    .icontains("type", type)
                    .eq("postParentAnswer", null)
                    .eq("postParentComment", null)
                    .eq("author.mail", personId)
                    .orderBy("dateOfCreate")
                    .findList().subList( countFrom, countTo);

            return GlobalResult.okResult(Json.toJson(latestPost));

        } catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }

    }

    public Result getPostLinkedAnswers(String id){
        try {
            Post postMain = Post.find.byId(id);
            if(postMain == null) throw new Exception("Post not Exist");

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
            return GlobalResult.badRequestResult(e);
        }
    }


    //**********************************************************************************************************************
    @Security.Authenticated(Secured.class)
    public Result getAllPersonPost(){
        try {
          return GlobalResult.okResult( Json.toJson(SecurityController.getPerson().personPosts)  );
        }catch (Exception e){
            e.printStackTrace();
            return GlobalResult.badRequestResult(e);
        }
    }


    @Security.Authenticated(Secured.class)
    public Result newPost(){
        try {
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            TypeOfPost typeOfPost = TypeOfPost.find.byId(json.get("type").asText());
            if(typeOfPost == null) throw new Exception("Type of post is not in Database");

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

            return GlobalResult.okResult( Json.newObject().put( "postId", post.postId ) );
        }catch (Exception e){
            e.printStackTrace();
            return GlobalResult.badRequestResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public  Result deletePost(String postId){
        try {

            Post post = Post.find.byId(postId);
            if (post == null ) throw new Exception("Overflow post not Exist");
            if (!post.author.mail.equals( SecurityController.getPerson().mail) ) throw new Exception("You are not Author");

            post.delete();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    @Security.Authenticated(Secured.class)

    public Result editPost(){
        try {
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");


            Post post = Post.find.byId(json.get("postId").asText());

            if (post == null) return GlobalResult.badRequestResult(new Exception(" Overflow post not Exist"));

            if( !post.author.mail.equals(SecurityController.getPerson().mail)) return GlobalResult.forbidden();

            TypeOfPost typeOfPost = TypeOfPost.find.byId(json.get("type").asText());
            if(typeOfPost == null) throw new Exception("Type of post is not in Database");

            post.name = json.get("name").asText();
            post.type = typeOfPost;
            post.textOfPost = json.get("comment").asText();

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

            post.save();

           return GlobalResult.okResult();

        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }

    }

    @Security.Authenticated(Secured.class)
    public Result addComment(){
       try {
           JsonNode json = request().body().asJson();
           if (json == null) throw new Exception("Null Json");

           Post parentPost = Post.find.byId(json.get("postId").asText());
           if (parentPost == null) throw new Exception("Post not Exist");

           if( parentPost.postParentComment != null)  throw new Exception("You cannot comment another comment");

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
           return GlobalResult.badRequestResult(e);
       }
    }

    @Security.Authenticated(Secured.class)
    public Result addAnswer(){
        try {
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Post parentPost = Post.find.byId(json.get("postId").asText());
            if (parentPost == null) throw new Exception("Post not Exist");

            if( parentPost.postParentComment != null)  throw new Exception("You cannot answer to comment");
            if( parentPost.postParentAnswer != null)  throw new Exception("You cannot answer to another  answer");

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

            return GlobalResult.badRequestResult(e);

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
            return GlobalResult.badRequestResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public Result linkWithPreviousAnswer(){
        try {
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Post question = Post.find.byId(json.get("postId").asText());
            Post answer = Post.find.byId(json.get("linkId").asText());

            if (question == null)   return GlobalResult.badRequestResult(new Exception(" Overflow post not Exist"));
            if (answer == null)     return GlobalResult.badRequestResult(new Exception(" Overflow link post not Exist"));
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
            return GlobalResult.badRequestResult(e, "postId", "linkId");
        }
    }

    @Security.Authenticated(Secured.class)
    public Result unlinkWithPreviousAnswer(String id){
        try {
            LinkedPost post = LinkedPost.find.byId(id);

            if (post == null ) throw new Exception("Linked connection not Exist");
            if (! post.author.mail.equals(SecurityController.getPerson().mail) ) throw new Exception("You are not Author");

            post.delete();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }




    // TODO
    @Security.Authenticated(Secured.class)
    public Result setProperty(String postId){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Post post = Post.find.byId(json.get("postId").asText());
            if(post == null) throw new Exception("Id not Exist");


            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }



    public Result removeProperty(String postId){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Post post = Post.find.byId(json.get("postId").asText());
            if(post == null) throw new Exception("Id not Exist");



            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public Result newTypeOfPost(){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            if( TypeOfPost.find.where().eq("type", json.get("type").asText() ).findUnique() != null) throw new Exception("Duplicate value");

            TypeOfPost typeOfPost = new TypeOfPost();
            typeOfPost.type = json.get("type").asText();

            typeOfPost.save();

            return GlobalResult.okResult( Json.newObject().put( "postId", typeOfPost.id ) );
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public Result getTypeOfPost(){
        try{
            return GlobalResult.okResult(Json.toJson( TypeOfPost.find.all() ));
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }




    @Security.Authenticated(Secured.class)
    public Result addHashTag(){
        try{

            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Post post = Post.find.byId(json.get("postId").asText());
            if(post == null) throw new Exception("Post not Exist");

            for (final JsonNode objNode : json.get("hashTags")) {

                HashTag postHashTag = HashTag.find.byId(objNode.asText());

                if(postHashTag == null) {
                    postHashTag = new HashTag(objNode.asText());
                    postHashTag.save();
                }

                if(!post.hashTagsList.contains(postHashTag)) post.hashTagsList.add(postHashTag);

            }

            post.save();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.badRequestResult(e, "postId", "[hashTags]");
        }

    }

    @Security.Authenticated(Secured.class)
    public Result removeHashTag(){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            HashTag hashTag = HashTag.find.byId(json.get("postId").asText());
            hashTag.posts.remove(Post.find.byId(json.get("postId").asText()));
            hashTag.update();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }

    }

    @Security.Authenticated(Secured.class)
    public Result addConfirmType(String postId){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Post post = Post.find.byId(postId);
            if(post == null) throw new Exception("Id not Exist");


            for (final JsonNode objNode : json.get("hashTags")) {
                ConfirmTypeOfPost confirmObj = ConfirmTypeOfPost.find.byId(objNode.asText());

                if(confirmObj == null) {
                    confirmObj = new ConfirmTypeOfPost(objNode.asText());
                    confirmObj.save();
                }

                if(!post.confirmTypeOfPostList.contains(confirmObj)) post.confirmTypeOfPostList.add(confirmObj);
            }

            post.save();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public Result removeConfirmType(String postId){
        try{
            JsonNode json = request().body().asJson();
            if (json == null) throw new Exception("Null Json");

            Post post = Post.find.byId(postId);

            for (final JsonNode objNode : json.get("hashTags")) {
                ConfirmTypeOfPost confirmObj = ConfirmTypeOfPost.find.byId(objNode.asText());

                if(confirmObj == null) throw new Exception("ConfirmType not exist");
                confirmObj.posts.remove(post);
                confirmObj.update();
            }

            post.update();

            return GlobalResult.okResult();
        }catch (Exception e){
            e.printStackTrace();
            return GlobalResult.badRequestResult(e);

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
            return GlobalResult.badRequestResult(e);
        }
    }

    @Security.Authenticated(Secured.class)
    public Result likeMinus(String postId){
        try {
            Post post = Post.find.where().eq("postId", postId).findUnique();

            if(post.listOfLikers != null &&  post.listOfLikers.contains(  SecurityController.getPerson()  ) ) throw new Exception("You have decided");

            post.listOfLikers.add(SecurityController.getPerson());
            post.likes--;
            post.update();

            return GlobalResult.okResult();
        }catch (Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }






}
