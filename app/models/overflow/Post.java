package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.login.Person;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Post extends Model {

    @Id  @GeneratedValue(strategy = GenerationType.SEQUENCE) public String postId;

    @Constraints.Required @Constraints.MinLength(value = 12) @JsonInclude(JsonInclude.Include.NON_EMPTY) public String name;

    // Počet shlédnutí - chci vracet jen tam kde to má smysl - tedy jen v "otázce" nikoli v odpovědích, kde typ postu není uveden a evidován
    @JsonIgnore @ManyToOne public TypeOfPost type;
    @JsonInclude(JsonInclude.Include.NON_EMPTY) @JsonProperty public String type(){return type == null ? null : type.type;}

    // Počet shlédnutí - chci vracet jen tam kde to má smysl - tedy jen v "otázce" nikoli v odpovědích
    @JsonIgnore   public int views;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)  @JsonProperty public String views(){ return name == null ? null : Integer.toString(views); }

                                public int likes;
                                public Date dateOfCreate;
    @JsonIgnore                 public boolean deleted;
    @JsonIgnore @ManyToOne      public Person author;


    @JsonIgnore @Constraints.Required @Constraints.MinLength(value = 30) @Column(columnDefinition = "TEXT")  public String textOfPost;
    @JsonProperty public String textOfPost(){ return name == null ? textOfPost : "http://localhost:9000/overflow/post/textOfPost/" + this.postId; }


    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "posts")      @JoinTable(name = "hashTagsTable")      public List<HashTag>            hashTagsList = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "posts")      @JoinTable(name = "typePostsTable")     public List<PropertyOfPost>     propertyOfPostList = new ArrayList<>();
     @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "postLiker") @JoinTable(name = "postLikerTable")    public List<Person>             listOfLikers = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "posts")      @JoinTable(name = "postConfirmsTable")  public List<TypeOfConfirms>     typeOfConfirmses = new ArrayList<>();



    // @JsonProperty public String hashTagsList(){  return "http://localhost:9000/overflow/post/hashTags/" + this.postId; } // Není nezbytně vyžadováno
    @JsonProperty public String comments(){ return comments.size() == 0 ? null : "http://localhost:9000/overflow/post/comments/" +      this.postId; }
    @JsonInclude(JsonInclude.Include.NON_EMPTY) @JsonProperty public String answers(){ return name == null ? null : "http://localhost:9000/overflow/post/answers/" +      this.postId; }

    // Vazba M:1 pro nalinkování komentáře na post
    //@JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnore @OneToMany(mappedBy="postParentComment", cascade=CascadeType.ALL)  public List<Post>  comments = new ArrayList<>();
    @JsonIgnore @ManyToOne public Post postParentComment;



    // Vazba M:1 pro nalinkování odpovědi na post
   // @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OneToMany(mappedBy="postParentAnswer", cascade=CascadeType.ALL)  public List<Post> answers = new ArrayList<>();
    @JsonIgnore @ManyToOne public Post postParentAnswer;


    @JsonInclude(JsonInclude.Include.NON_EMPTY)  @JsonProperty
    public List<String> hashTags(){
        List<String> list = new ArrayList<>();
       for(HashTag tag : hashTagsList) list.add(tag.postHashTagId);
       return list;
    }

    @JsonIgnore @OneToMany(mappedBy="question", cascade = CascadeType.ALL)  public List<LinkedPost> linkedQuestions = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="answer", cascade = CascadeType.ALL)    public List<LinkedPost> linkedAnswers = new ArrayList<>();

//******************************************************************************************************************
    public Post(){}
    public static Finder<String,Post> find = new Finder<>(Post.class);

//******************************************************************************************************************

    // Pro zjednodušení čtení ze strany front-end se linkované odpovědi profiltrují (jednak kvuli zac
    // a je zasílán jen přehled (Pole linkovaných odpovědí) tedy
    // Jména Main postu s ID a jeho otázkou a pak následně pouze odpovědi na kontrkétní otázku odfiltrováno naprosto od všeho

    @JsonInclude(JsonInclude.Include.NON_EMPTY) @JsonProperty public String linkedAnswers   (){return linkedQuestions.isEmpty()  ? null : "http://localhost:9000/overflow/linkedAnswers/" + postId;}



}
