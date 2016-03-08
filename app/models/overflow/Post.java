package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.persons.Person;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Post extends Model {

                                                   @Id  @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String postId;
       @Constraints.Required @Constraints.MinLength(value = 12) @JsonInclude(JsonInclude.Include.NON_EMPTY)     public String name;
                                                                                                                public int likes;
                                @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")    public Date date_of_create;
                                                                                                @JsonIgnore     public boolean deleted;
                                                                                                                public boolean updated;

                                                                                                @JsonIgnore     public int views;
                @Constraints.Required @Constraints.MinLength(value = 30) @Column(columnDefinition = "TEXT")     public String text_of_post;

                                                                                     @JsonIgnore @ManyToOne     public Post postParentComment;
                                                                                     @JsonIgnore @ManyToOne     public Post postParentAnswer;
                                                                                     @JsonIgnore @ManyToOne     public TypeOfPost type;
                                                                                                 @ManyToOne     public Person author;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "posts")      @JoinTable(name = "hashTagsTable")      public List<HashTag>            hashTagsList = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "posts")      @JoinTable(name = "typePostsTable")     public List<PropertyOfPost>     propertyOfPostList = new ArrayList<>();
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "postLiker")  @JoinTable(name = "postLikerTable")     public List<Person>             listOfLikers = new ArrayList<>();
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, mappedBy = "posts")      @JoinTable(name = "postConfirmsTable")  public List<TypeOfConfirms>     typeOfConfirms = new ArrayList<>();


    @JsonIgnore @OneToMany(mappedBy="question", cascade = CascadeType.ALL)         public List<LinkedPost> linkedQuestions = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="answer",   cascade = CascadeType.ALL)         public List<LinkedPost> linkedAnswers = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="postParentAnswer", cascade=CascadeType.ALL)   public List<Post> answers = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="postParentComment", cascade=CascadeType.ALL)  public List<Post>  comments = new ArrayList<>();



        @JsonInclude(JsonInclude.Include.NON_EMPTY)  @JsonProperty  public TypeOfPost           type()     { return type == null ? null : type;}
        @JsonInclude(JsonInclude.Include.NON_NULL)   @JsonProperty  public List<TypeOfConfirms> type_of_confirms()     { return name == null  ? null : typeOfConfirms;}
        @JsonInclude(JsonInclude.Include.NON_NULL)   @JsonProperty  public Integer              views()     { return name == null ? null : views; }
        @JsonInclude(JsonInclude.Include.NON_NULL)   @JsonProperty  public List<Post>           answers()   { return name == null ? null : answers; }
        @JsonInclude(JsonInclude.Include.NON_NULL)   @JsonProperty  public List<Post>           comments()  { return name == null ? null : comments;}
        @JsonInclude(JsonInclude.Include.NON_NULL)   @JsonProperty  public List<String>         hashTags(){ return hashTagsList.stream().map(tag -> tag.postHashTagId).collect(Collectors.toList());}







//******************************************************************************************************************
    public Post(){}
    public static Finder<String,Post> find = new Finder<>(Post.class);

//******************************************************************************************************************

    // Pro zjednodušení čtení ze strany front-end se linkované odpovědi profiltrují (jednak kvuli zac
    // a je zasílán jen přehled (Pole linkovaných odpovědí) tedy
    // Jména Main postu s ID a jeho otázkou a pak následně pouze odpovědi na kontrkétní otázku odfiltrováno naprosto od všeho

    @JsonInclude(JsonInclude.Include.NON_EMPTY) @JsonProperty public String linkedAnswers   (){return linkedQuestions.isEmpty()  ? null : "http://localhost:9000/overflow/linkedAnswers/" + postId;}



}
