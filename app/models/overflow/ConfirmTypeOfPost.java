package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ConfirmTypeOfPost extends Model {

    @Id  public String id;
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)  public List<Post> posts = new ArrayList<>();

    public ConfirmTypeOfPost(){}
    public ConfirmTypeOfPost(String postHashTagId){
        this.id = postHashTagId;
    }
    public static Finder<String,ConfirmTypeOfPost> find = new Finder<>(ConfirmTypeOfPost.class);
}
