package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TypeOfConfirms extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)   public String id;
    @Constraints.MaxLength(value = 30) @Constraints.Required  public String type;
                                                              public String color;
                                                              public Integer size;

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)  public List<Post> posts = new ArrayList<>();

    //******************************************************************************************************************
    public TypeOfConfirms(){}
    public static Model.Finder<String,TypeOfConfirms> find = new Model.Finder<>(TypeOfConfirms.class);
}
