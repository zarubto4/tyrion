package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Project;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Board extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)      public String id; // Vlastní id je přidělováno
                         @Column(columnDefinition = "TEXT")      public String personal_description;
                                                @ManyToOne       public TypeOfBoard type_of_board;  // Typ desky
                                                                 public boolean isActive;

    @JsonIgnore   @ManyToMany(cascade = CascadeType.ALL)     public List<Project> projects = new ArrayList<>(); // Uživatelovi projekty



    @ApiModelProperty(value = "Proxy address to get Objects \"Project\"", readOnly =true, allowableValues = "http://server_url/{id}")
    @JsonProperty  @Transient public String projects()          { return Server.serverAddress + "/compilation/board/projects/" + id; }

    @ApiModelProperty(value = "Proxy address to get Objects \"TypeOfBoard\"", readOnly =true, allowableValues = "http://server_url/{id}")
    @JsonProperty  @Transient public String type_of_board()       { return Server.serverAddress + "/compilation/type_of_board/" +  type_of_board.id; }




    public static Finder<String, Board> find = new Finder<>(Board.class);


}
