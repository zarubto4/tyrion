package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class TypeOfBoard extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
     @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)    public String id;
                                                                public String name;
                       @Column(columnDefinition = "TEXT")       public String description;
                       @JsonIgnore  @ManyToOne                  public Producer producer;
                       @JsonIgnore  @ManyToOne                  public Processor processor;

    @JsonIgnore @OneToMany(targetEntity= Board.class, mappedBy="type_of_board", cascade = CascadeType.ALL) public List<Board> boards = new ArrayList<>();


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(readOnly =true)
    @JsonProperty public String processor_id      (){return processor.id;}

    @ApiModelProperty(readOnly =true)
    @JsonProperty public List<String> boards_id() { List<String> l = new ArrayList<>();  for( Board m : boards)  l.add(m.id); return l;  }

    @ApiModelProperty(readOnly =true)
    @JsonProperty public String producer_id      (){return producer.id;}


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, TypeOfBoard> find = new Finder<>(TypeOfBoard.class);

}
