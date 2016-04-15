package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Producer extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String name;
                     @Column(columnDefinition = "TEXT")      public String description;

    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<TypeOfBoard> type_of_boards = new ArrayList<>();

    @JsonProperty @Transient public List<String>  type_of_boards_id() { List<String> l = new ArrayList<>();  for( TypeOfBoard m : type_of_boards)  l.add(m.id); return l;  }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/




/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Producer> find = new Model.Finder<>(Producer.class);

}
