package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Producer extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String name;
                     @Column(columnDefinition = "TEXT")      public String description;

    @JsonIgnore @OneToMany(mappedBy="producer", cascade = CascadeType.ALL) public List<TypeOfBoard> type_of_boards = new ArrayList<>();

    @JsonProperty public String description()  { return Server.serverAddress + "/compilation/producer/description/"  +id;}
    @JsonProperty @Transient public String type_of_boards() { return Server.serverAddress + "/compilation/producer/typeOfBoards/" +id;}


    public static Finder<String, Producer> find = new Model.Finder<>(Producer.class);

}
