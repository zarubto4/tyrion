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
public class HashTag extends Model {

    @Id public String  postHashTagId;
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)  public List<Post> posts = new ArrayList<>();

    public HashTag(){}
    public HashTag(String postHashTagId){
        this.postHashTagId = postHashTagId;
    }
    public static Finder<String,HashTag> find = new Finder<>(HashTag.class);


}
