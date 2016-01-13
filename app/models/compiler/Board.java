package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.blocko.Project;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


@Entity
public class Board extends Model {

                                              @Id   public String id; // Vlastní projectId je přidělováno
    @JsonIgnore @Column(columnDefinition = "TEXT")  public String generalDescription;
    @JsonIgnore @Column(columnDefinition = "TEXT")  public String userDescription;
                            @ManyToOne @JsonIgnore  public Project project;          // Uživatelův projekt
                            @ManyToOne @JsonIgnore  public TypeOfBoard typeOfBoard;  // Typ desky



    @JsonProperty public String project()           {  return "http://localhost:9000/project/project/" + project.projectId; }
    @JsonProperty public String typeOfBoard()       {  return "http://localhost:9000/compilation/typeOfBoard/" +  typeOfBoard.id; }
    @JsonProperty public String generalDescription(){  return "http://localhost:9000/compilation/board/generalDescription/" +  this.id; }
    @JsonProperty public String userDescription()   {  return "http://localhost:9000/compilation/board/userDescription/"    +  this.id; }


    public static Finder<String, Board> find = new Finder<>(Board.class);


}
