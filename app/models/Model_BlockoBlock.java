package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Approval_state;
import utilities.loggy.Loggy;
import utilities.swagger.outboundClass.Swagger_BlockoBlock_Version_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Blocko_Block_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of BlockoBlock",
        value = "BlockoBlock")
public class Model_BlockoBlock extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)   public String id;
                                                            @ApiModelProperty(required = true)   public String name;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)   public String description;

                                    @JsonIgnore @ManyToOne                                       public Model_Person author;
                                    @JsonIgnore @ManyToOne                                       public Model_TypeOfBlock type_of_block;
                                    @JsonIgnore @ManyToOne                                       public Model_Producer producer;

    @JsonIgnore @OneToMany(mappedBy="blocko_block", cascade = CascadeType.ALL) @OrderBy("date_of_create desc") public List<Model_BlockoBlockVersion> blocko_versions = new ArrayList<>();

    @JsonIgnore  public Integer order_position;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = false, readOnly = true, value = "can be hidden, if BlockoBlock is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty                                               public String    author_id()         { return author != null ? author.id : null;}

    @ApiModelProperty(required = false, readOnly = true, value = "can be hidden, if BlockoBlock is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty                                               public String    author_nick_name()  { return  author != null ? author.nick_name : null;}


    @ApiModelProperty(required = false, readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty                                               public String    producer_id()       { return producer != null ? producer.id : null;}

    @ApiModelProperty(required = false, readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty                                               public String    producer_name()     { return producer != null ? producer.name : null;}


    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_block_id()             { return type_of_block.id; }
    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_block_name()           { return type_of_block.name; }



    @Transient  @JsonProperty @ApiModelProperty(required = true) public  List<Swagger_BlockoBlock_Version_Short_Detail> versions(){

        List<Swagger_BlockoBlock_Version_Short_Detail> list = new ArrayList<>();

        for( Model_BlockoBlockVersion v : blocko_versions){

            if((v.approval_state == Enum_Approval_state.approved)||(v.approval_state == Enum_Approval_state.edited)||((this.author != null)&&(this.author.id.equals(Controller_Security.get_person().id)))) {

                list.add(v.get_short_blockoblock_version());
            }
        }

        return list;
    }

    @Transient @JsonIgnore
    public Swagger_Blocko_Block_Short_Detail get_blocko_block_short_detail(){
        try {

            Swagger_Blocko_Block_Short_Detail help = new Swagger_Blocko_Block_Short_Detail();
            help.id = id;
            help.name = name;
            help.description = description;

            help.edit_permission = edit_permission();
            help.delete_permission = delete_permission();
            help.update_permission = update_permission();

            return help;

        }catch (Exception e){
            Loggy.internalServerError("Model_BlockoBlock:: get_blocko_block_short_detail", e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        order_position = Model_BlockoBlock.find.where().eq("type_of_block.id", type_of_block.id).findRowCount() + 1;

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (get_byId(this.id) == null) break;
        }
        super.save();
    }


    @JsonIgnore @Transient
    public void up(){

        System.out.println("Změna Order Possition! UP");

        Model_BlockoBlock up = Model_BlockoBlock.find.where().eq("order_position", (order_position-1) ).eq("type_of_block.id", type_of_block.id).findUnique();
        if(up == null){
            System.out.println("UP - Nejde :(");
            return;
        }

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        System.out.println("Změna Order Possition! DOWN");

        Model_BlockoBlock down = Model_BlockoBlock.find.where().eq("order_position", (order_position+1) ).eq("type_of_block.id", type_of_block.id).findUnique();
        if(down == null){
            System.out.println("DOWN - Nejde :(");
            return;
        }

        down.order_position -= 1;
        down.update();

        this.order_position += 1;
        this.update();

    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read TypeOfBlock, than can read all BlockoBlocks from list of TypeOfBlock ( You get ids of list of BlockoBlocks in object \"BlockoBlocks\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have TypeOfBlock.update_permission = true, you can create new BlockoBlocks on this TypeOfBlock - Or you need static/dynamic permission key if user want create BlockoBlock in public TypeOfBlock";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore  @Transient                                     public boolean create_permission() {return  type_of_block.update_permission();}
    @JsonIgnore  @Transient                                     public boolean read_permission()   {return  type_of_block.read_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()   {return  type_of_block.update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission() {return  type_of_block.update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission() {return  type_of_block.delete_permission();}


    public enum permissions{BlockoBlock_create, BlockoBlock_read, BlockoBlock_edit, BlockoBlock_delete}

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_BlockoBlock> find = new Finder<>(Model_BlockoBlock.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_BlockoBlock get_byId(String id) {
        return find.byId(id);
    }

    @JsonIgnore
    public static Model_BlockoBlock get_publicByName(String name) {
        return find.where().isNull("type_of_block.project").eq("name", name).findUnique();
    }
}
