package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.ProgramingPackageController;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Producer;
import models.person.Person;
import utilities.swagger.outboundClass.Swagger_BlockoBlock_ShortVersion;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class BlockoBlock extends Model {


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)   public String id;
                                                            @ApiModelProperty(required = true)   public String name;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)   public String general_description;
                                                            @ApiModelProperty(required = true)   public ProgramingPackageController.approval_state approval_state;
                                    @JsonIgnore @ManyToOne                                       public Person author;
                                    @JsonIgnore @ManyToOne                                       public TypeOfBlock type_of_block;
                                    @JsonIgnore @ManyToOne()                                     public Producer producer;

    @JsonIgnore @OneToMany(mappedBy="blocko_block", cascade = CascadeType.ALL) @OrderBy("date_of_create desc") public List<BlockoBlockVersion> blocko_versions = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = false, readOnly = true, value = "can be hide, if BlockoBlock is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty                                               public String    author_id()         { return author != null ? author.id : null;}

    @ApiModelProperty(required = false, readOnly = true, value = "can be hide, if BlockoBlock is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty                                               public String    author_nick_name()  { return  author != null ? author.nick_name : null;}


    @ApiModelProperty(required = false, readOnly = true, value = "can be hide, if BlockoBlock is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty                                               public String    producer_id()       { return producer != null ? producer.id : null;}

    @ApiModelProperty(required = false, readOnly = true, value = "can be hide, if BlockoBlock is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty                                               public String    producer_name()     { return producer != null ? producer.name : null;}


    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_block_id()             { return type_of_block.id; }
    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_block_name()           { return type_of_block.name; }


    //@Transient  @JsonProperty @ApiModelProperty(required = true)  public List<String>    versions()             { List<String> l = new ArrayList<>();  for( BlockoBlockVersion m : blocko_versions)  l.add(m.id); return l; }


    @Transient  @JsonProperty @ApiModelProperty(required = true) public  List<Swagger_BlockoBlock_ShortVersion> versions(){

        List<Swagger_BlockoBlock_ShortVersion> list = new ArrayList<>();

        for( BlockoBlockVersion m : blocko_versions){

            Swagger_BlockoBlock_ShortVersion short_version = new Swagger_BlockoBlock_ShortVersion();
            short_version.id = m.id;
            short_version.name = m.version_name;

            list.add(short_version);
        }

        return list;
    }




/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read TypeOfBlock, than can read all BlockoBlocks from list of TypeOfBlock ( You get ids of list of BlockoBlocks in object \"BlockoBlocks\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have TypeOfBlock.update_permission = true, you can create new BlockoBlocks on this TypeOfBlock - Or you need static/dynamic permission key if user want create BlockoBlock in public TypeOfBlock";

    @JsonIgnore  @Transient                                     public boolean create_permission() {return  type_of_block.update_permission();}
    @JsonIgnore  @Transient                                     public boolean read_permission()   {return  type_of_block.read_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()   {return  type_of_block.update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission() {return  type_of_block.update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission() {return  type_of_block.delete_permission();}


    public enum permissions{BlockoBlock_create, BlockoBlock_read, BlockoBlock_edit, BlockoBlock_delete}

/* FINDER -------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,BlockoBlock> find = new Finder<>(BlockoBlock.class);


}
