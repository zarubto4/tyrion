package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PropertyOfPost extends Model {


    @Id   public String propertyOfPostId;
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)  public List<Post> posts = new ArrayList<>();

    public PropertyOfPost(){}
    public PropertyOfPost(String postHashTagId){
        this.propertyOfPostId = postHashTagId;
    }
    public static Finder<String,PropertyOfPost> find = new Finder<>(PropertyOfPost.class);
}
