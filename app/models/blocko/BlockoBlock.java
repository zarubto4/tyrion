package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.persons.Person;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BlockoBlock extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)    public String id;
                                                               public String name;
                         @Column(columnDefinition = "TEXT")    public String general_description;
                                    @JsonIgnore @ManyToOne     public Person author;
                                    @JsonIgnore @ManyToOne     public TypeOfBlock type_of_block;


    @JsonIgnore @OneToMany(mappedBy="blocko_block", cascade = CascadeType.ALL) @OrderBy("dateOfCreate desc") public List<BlockoBlockVersion> blocko_versions = new ArrayList<>();



    @JsonProperty public String  versions()           { return Server.serverAddress + "/project/blocko_block/version/all/"  + this.id;}
    @JsonProperty public Integer countOfversions()    { return blocko_versions.size(); }
    @JsonProperty public String  author()             { return Server.serverAddress + "/coreClient/person/person/"  + this.id;}




    // JsonIgnore Methods **********************************************************************************************



    // Finder **********************************************************************************************************
    public static Finder<String,BlockoBlock> find = new Finder<>(BlockoBlock.class);
}
