package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.Person;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BlockoBlock extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
    public String name;

    @JsonIgnore @ManyToOne public Person author;
    @JsonProperty public String author()   { return "http://localhost:9000/coreClient/person/person/"  + this.id;}


    @JsonIgnore @Column(columnDefinition = "TEXT") public String generalDescription;
    @JsonProperty public String generalDescription(){return  "http://localhost:9000/programing/blockoBlock/description/"+this.id;}

    @JsonIgnore @OneToMany(mappedBy="blockoBlock", cascade = CascadeType.ALL) @OrderBy("version desc") public List<BlockoContentBlock> contentBlocks = new ArrayList<>();
    @JsonProperty public String previousVersions()   { return "http://localhost:9000/project/blockoBlock/allPreviousVersions/"  + this.id;}


    @Transient private Double version = null;
    public void setVersion(Double version) {
        this.version = version;
    }


    @JsonProperty public Double version() {
        if(version == null && !contentBlocks.isEmpty() ) return contentBlocks.get(0).version;
        return version;
    }


    @JsonProperty public String designJson(){ return "http://localhost:9000/project/blockoBlock/designJson/" + this.id + "/"+ version();}
    @JsonProperty public String logicJson() { return "http://localhost:9000/project/blockoBlock/logicJson/"  + this.id + "/"+ version();}

    //******************************************************************************************************************
    public BlockoBlock(){}
    public static Finder<String,BlockoBlock> find = new Finder<>(BlockoBlock.class);

}
