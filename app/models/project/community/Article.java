package models.project.community;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.awt.Image;
import java.util.*;

@Entity
public class Article extends Model {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String           id;
                                                            public String           name;
                                                            public String           text;
    @JsonIgnore @ManyToOne                                  public Documentation    documentation;



    public static Finder<String, Article> find = new Finder<>(Article.class);
}
