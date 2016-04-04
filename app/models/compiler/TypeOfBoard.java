package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class TypeOfBoard extends Model {

     @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)    public String id;
                                                                public String name;
                       @Column(columnDefinition = "TEXT")       public String description;
                       @JsonIgnore  @ManyToOne                  public Producer producer;
                       @JsonIgnore  @ManyToOne                  public Processor processor;

   @JsonIgnore @OneToMany(targetEntity= Board.class, mappedBy="type_of_board", cascade = CascadeType.ALL) public List<Board> boards = new ArrayList<>();


  // @JsonProperty public String description   (){return "http://localhost:9000/compilation/TypeOfBoard/generalDescription/"    +  this.id;}


    @ApiModelProperty(value = "Proxy address to get Objects [LibraryGroup]", readOnly =true, allowableValues = "http://server_url/{id}")
    @JsonProperty public String libraryGroups (){return Server.tyrion_serverAddress + "/compilation/libraryGroups/" +  this.id;}

    @ApiModelProperty(value = "Proxy address to get Object [Libraries]", readOnly =true, allowableValues = "http://server_url/{id}")
    @JsonProperty public String libraries     (){return Server.tyrion_serverAddress + "/compilation/libraries/files/" +  this.id;}

    @ApiModelProperty(value = "Proxy address to get Object \"Processor\"", readOnly =true, allowableValues = "http://server_url/{id}")
    @JsonProperty public String processor      (){return Server.tyrion_serverAddress + "/compilation/processor/" +  this.id;}

    @ApiModelProperty(value = "Proxy address to get all registered objects [Board] with this TypeOfPost property",  readOnly =true, allowableValues = "http://server_url/{id}")
    @JsonProperty public String boards        (){return Server.tyrion_serverAddress + "/compilation/TypeOfBoard/boards/" +  this.id;}

    @ApiModelProperty(value = "Proxy address to get object \"Producer\" who made this TypeOfPost", readOnly =true, allowableValues = "http://server_url/{id}")
    @JsonProperty
    public String producer      (){return Server.tyrion_serverAddress + "/compilation/producer/" +  this.id;}


    public static Finder<String, TypeOfBoard> find = new Finder<>(TypeOfBoard.class);


}
