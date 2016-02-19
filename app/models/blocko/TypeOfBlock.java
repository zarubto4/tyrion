package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TypeOfBlock extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
                                                            public String name;
                         @Column(columnDefinition = "TEXT") public String generalDescription;

    @JsonIgnore @OneToMany(mappedBy="typeOfBlock", cascade = CascadeType.ALL) public List<BlockoBlock> blockoBlocks = new ArrayList<>();




    public static Finder<String,TypeOfBlock> find = new Finder<>(TypeOfBlock.class);
}
