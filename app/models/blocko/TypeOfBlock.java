package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Project;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TypeOfBlock extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
                                                            public String name;
                         @Column(columnDefinition = "TEXT") public String generalDescription;


                                    @JsonIgnore @ManyToOne  public Project project;

    @JsonIgnore @OneToMany(mappedBy="type_of_block", cascade = CascadeType.ALL) public List<BlockoBlock> blockoBlocks = new ArrayList<>();


    @ApiModelProperty(value = "This value will be in Json only if TypeOfBlock is private! And its also only proxy address to get Objects \"Project\"\" ", readOnly =true, required = false, allowableValues = "http://server_url/{id}")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) @JsonProperty @Transient public String project() {  return Server.serverAddress + "/project/project/" + this.project.id; }


    public static Finder<String,TypeOfBlock> find = new Finder<>(TypeOfBlock.class);
}
