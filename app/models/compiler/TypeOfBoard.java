package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class TypeOfBoard extends Model {

     @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)    public String id;
                                                                public String name;
                       @Column(columnDefinition = "TEXT")       public String description;
                                   @ManyToOne @JsonIgnore       public Producer producer;
                                   @ManyToOne @JsonIgnore       public Processor processor;

    @JsonIgnore @OneToMany(mappedBy="typeOfBoard", cascade = CascadeType.ALL) public List<Board> boards = new ArrayList<>();



    @JsonProperty public String description   (){return "http://localhost:9000/compilation/TypeOfBoard/generalDescription/"    +  this.id;}
    @JsonProperty public String libraryGroups (){return "http://localhost:9000/compilation/processor/libraryGroups/" +  this.id;}
    @JsonProperty public String libraries     (){return "http://localhost:9000/compilation/processor/records/" +  this.id;}
    @JsonProperty public String procesor      (){return "http://localhost:9000/compilation/processor/" +  this.id;}
    @JsonProperty public String boards        (){return "http://localhost:9000/compilation/TypeOfBoard/boards/" +  this.id;}

    public static Finder<String, TypeOfBoard> find = new Finder<>(TypeOfBoard.class);


}
