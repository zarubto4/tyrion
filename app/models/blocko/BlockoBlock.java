package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.persons.Person;
import utilities.a_main_utils.GlobalValue;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BlockoBlock extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)    public String id;
                                                               public String name;
                         @Column(columnDefinition = "TEXT")    public String generalDescription;
                                    @JsonIgnore @ManyToOne     public Person author;
                                    @JsonIgnore @ManyToOne     public TypeOfBlock typeOfBlock;


    @JsonIgnore @OneToMany(mappedBy="blockoBlock", cascade = CascadeType.ALL) @OrderBy("dateOfCreate desc") public List<BlockoContentBlock> contentBlocks = new ArrayList<>();




    @JsonProperty public String  versions()           { return GlobalValue.serverAddress + "/project/blockoBlock/versions/"  + this.id;}
    @JsonProperty public Integer countOfversions()    { return contentBlocks.size(); }
    @JsonProperty public String  author()             { return GlobalValue.serverAddress + "/coreClient/person/person/"  + this.id;}




    // JsonIgnore Methods **********************************************************************************************



    // Finder **********************************************************************************************************
    public static Finder<String,BlockoBlock> find = new Finder<>(BlockoBlock.class);
}
