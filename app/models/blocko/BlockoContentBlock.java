package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Date;

@Entity
public class BlockoContentBlock extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
    @JsonIgnore public String versionDescription;
    public Double version;
    public Date dateOfCreate;

    //@JsonIgnore
     @Column(columnDefinition = "TEXT") public String designJson;
    //@JsonIgnore
    @Column(columnDefinition = "TEXT") public String logicJson;

    @JsonIgnore @ManyToOne public BlockoBlock blockoBlock;

    @JsonProperty public String designJson()         { return "http://localhost:9000/project/blockoBlock/designJson/" + blockoBlock.id +"/"+version;}
    @JsonProperty public String logicJson()          { return "http://localhost:9000/project/blockoBlock/logicJson/"  + blockoBlock.id +"/"+version;}
    @JsonProperty public String versionDescription() { return "http://localhost:9000/project/blockoBlock/versionDescription/"  + this.id;}

    //******************************************************************************************************************
    public BlockoContentBlock(){}
    public static Finder<String,BlockoContentBlock> find = new Finder<>(BlockoContentBlock.class);

}
