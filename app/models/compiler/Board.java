package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.project.global.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Board extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id; // Vlastní projectId je přidělováno
         @JsonIgnore @Column(columnDefinition = "TEXT")      public String userDescription;
                                 @ManyToOne @JsonIgnore      public TypeOfBoard typeOfBoard;  // Typ desky
                                                             public boolean isActive;

    @JsonIgnore   @ManyToMany(cascade = CascadeType.ALL)     public List<Project> projects = new ArrayList<>(); // Uživatelovi projekty


    @JsonProperty public String projects()          { return "http://localhost:9000/compilation/board/projects/" + id; }
    @JsonProperty public String typeOfBoard()       { return "http://localhost:9000/compilation/typeOfBoard/" +  typeOfBoard.id; }

    @JsonProperty public String userDescription()   { return userDescription == null ? null : "http://localhost:9000/compilation/board/userDescription/"    +  this.id; }


    public static Finder<String, Board> find = new Finder<>(Board.class);


}
