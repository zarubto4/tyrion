package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Post extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

@Id  @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)          public String id;
@ApiModelProperty(required = false, value = "Only if Post is Main (not answers or comments)")
@JsonInclude(JsonInclude.Include.NON_EMPTY)                                                          public String name;
@ApiModelProperty(required = true)                                                                   public int likes;
@ApiModelProperty(required = true, dataType = "integer", readOnly = true,
        value = "UNIX time stamp", example = "1460126537")                                           public Date date_of_create;
@JsonIgnore                                                                                          public boolean deleted;
@ApiModelProperty(required = true)                                                                   public boolean updated;

@JsonIgnore                                                                                          public int views;
@ApiModelProperty(required = true) @Column(columnDefinition = "TEXT")                                public String text_of_post;

                                                                                     @JsonIgnore @ManyToOne     public Post postParentComment;
                                                                                     @JsonIgnore @ManyToOne     public Post postParentAnswer;
                                                                                     @JsonIgnore @ManyToOne     public TypeOfPost type;
    @ApiModelProperty(required = true)                                                           @ManyToOne     public Person author;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "posts")      @JoinTable(name = "hashTagsTable")      public List<HashTag>            hashTagsList = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "posts")      @JoinTable(name = "typePostsTable")     public List<PropertyOfPost>     propertyOfPostList = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "postLiker")  @JoinTable(name = "postLikerTable")     public List<Person>             listOfLikers = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "posts")      @JoinTable(name = "postConfirmsTable")  public List<TypeOfConfirms>     typeOfConfirms = new ArrayList<>();


    @JsonIgnore @OneToMany(mappedBy="question", cascade = CascadeType.ALL)         public List<LinkedPost> linkedQuestions = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="answer",   cascade = CascadeType.ALL)         public List<LinkedPost> linkedAnswers = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="postParentAnswer", cascade=CascadeType.ALL)   public List<Post> answers = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="postParentComment", cascade=CascadeType.ALL)  public List<Post>  comments = new ArrayList<>();

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = false, value = "Only if Post is Main")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)  @JsonProperty  public TypeOfPost           type()                  { return type == null ? null : type;}

    @ApiModelProperty(required = false, value = "Only if Post is Main")
    @JsonInclude(JsonInclude.Include.NON_NULL)   @JsonProperty  public List<TypeOfConfirms> type_of_confirms()      { return name == null  ? null : typeOfConfirms;}

    @ApiModelProperty(required = false, value = "Only if Post is Main")
    @JsonInclude(JsonInclude.Include.NON_NULL)   @JsonProperty  public Integer              views()                 { return name == null ? null : views; }

    @ApiModelProperty(required = false, value = "Only if Post is Main")
    @JsonInclude(JsonInclude.Include.NON_NULL)   @JsonProperty  public List<Post>           answers()               { return name == null ? null : answers; }

    @ApiModelProperty(required = false, value = "Only if Post is Main")
    @JsonInclude(JsonInclude.Include.NON_NULL)   @JsonProperty  public List<LinkedPost>     linked_answers()        { return name == null ? null : linkedQuestions; }

    @ApiModelProperty(required = false, value = "Only if Post is Main or Answare")
    @JsonInclude(JsonInclude.Include.NON_NULL)   @JsonProperty  public List<Post>           comments()              { return name == null && postParentAnswer == null ? null : comments;}

    @ApiModelProperty(required = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)   @JsonProperty  public List<String>         hashTags()              { return hashTagsList.stream().map(tag -> tag.postHashTagId).collect(Collectors.toList());}

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   public Boolean create_permission(){  return true;    }
    @JsonProperty public Boolean read_permission()  {  return true;    }
    @JsonProperty public Boolean edit_permission()  {  return ( Post.find.where().eq("person.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Post_edit"); }

    @JsonProperty public Boolean answer_permission(){  return ( this.postParentComment == null && this.postParentAnswer  == null );}
    @JsonProperty public Boolean comment_permission(){ return ( name != null || postParentAnswer != null );}

    @JsonProperty public Boolean edit_confirms_permission() { return  SecurityController.getPerson().has_permission("Post_edit"); }

    @JsonProperty public Boolean delete_permission(){  return ( Post.find.where().eq("person.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("Post_delete"); }

    public enum permissions{ Post_edit, Post_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Post> find = new Finder<>(Post.class);


}
