package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TypeOfPost extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
    @Constraints.MaxLength(value = 30) @Constraints.Required public String type;

    @JsonIgnore
    @OneToMany(mappedBy="type", cascade = CascadeType.ALL)     public List<Post> posts = new ArrayList<>();

    //******************************************************************************************************************
    public TypeOfPost(){}
    public static Finder<String,TypeOfPost> find = new Finder<>(TypeOfPost.class);


}

