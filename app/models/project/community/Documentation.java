package models.project.community;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Documentation extends Model {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)         public String           id;
    @OneToMany(mappedBy="documentation", cascade = CascadeType.ALL) public List<Article>    articles = new ArrayList<>();

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,
            value = "UNIX time stamp", example = "1460126537")      public Date             date_of_create;
    @JsonIgnore                                                     public Project          project;


    public static Finder<String, Documentation> find = new Finder<>(Documentation.class);
}
