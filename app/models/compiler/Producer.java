package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Producer extends Model {

                                   @Id  public String id;
                                        public String name;
    @Column(columnDefinition = "TEXT")  public String description;

    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<TypeOfBoard> typeOfBoards = new ArrayList<>();

    @JsonProperty public String description()  { return "http://localhost:9000/compilation/producer/description/"  +id;}
    @JsonProperty public String typeOfBoards() { return "http://localhost:9000/compilation/producer/typeOfBoards/" +id;}


    public static Finder<String, Producer> find = new Finder<>(Producer.class);

}
