package models.overflow;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;


@Entity
public class HashTag extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id          @ApiModelProperty(required = true)      public String  postHashTagId;
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)  public List<Post> posts = new ArrayList<>();

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore
    public HashTag(String postHashTagId){
        this.postHashTagId = postHashTagId;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,HashTag> find = new Finder<>(HashTag.class);
}
