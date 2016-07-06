package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.person.Person;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BlockoBlock extends Model {


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)    public String id;
                                                               public String name;
                         @Column(columnDefinition = "TEXT")    public String general_description;
                                    @JsonIgnore @ManyToOne     public Person author;
                                    @JsonIgnore @ManyToOne     public TypeOfBlock type_of_block;

    @JsonIgnore @OneToMany(mappedBy="blocko_block", cascade = CascadeType.ALL) @OrderBy("date_of_create desc") public List<BlockoBlockVersion> blocko_versions = new ArrayList<>();


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Transient  @JsonProperty public List<String>    versions()             { List<String> l = new ArrayList<>();  for( BlockoBlockVersion m : blocko_versions)  l.add(m.id); return l; }
    @Transient  @JsonProperty public String         author_id()             { return author.id;}
    @Transient  @JsonProperty public String  type_of_block_id()             { return type_of_block.id; }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read TypeOfBlock, than can read all BlockoBlocks from list of TypeOfBlock ( You get ids of list of BlockoBlocks in object \"BlockoBlocks\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have TypeOfBlock.update_permission = true, you can create new BlockoBlocks on this TypeOfBlock - Or you need static/dynamic permission key if user want create BlockoBlock in public TypeOfBlock";

    @JsonIgnore  @Transient  public Boolean create_permission()  {return  type_of_block.update_permission();}
    @JsonProperty @Transient public Boolean edit_permission()    {return  type_of_block.update_permission();}
    @JsonProperty @Transient public Boolean update_permission()  {return  type_of_block.update_permission();}
    @JsonIgnore  @Transient  public Boolean read_permission()    {return  type_of_block.read_permission();}
    @JsonProperty @Transient  public Boolean delete_permission() {return  type_of_block.delete_permission();}



    public enum permissions{BlockoBlock_create, BlockoBlock_read, BlockoBlock_edit, BlockoBlock_delete}

/* FINDER -------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,BlockoBlock> find = new Finder<>(BlockoBlock.class);

}
