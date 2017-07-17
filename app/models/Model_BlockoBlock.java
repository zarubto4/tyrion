package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.enums.Enum_Approval_state;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.outboundClass.Swagger_BlockoBlock_Version_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Blocko_Block_Short_Detail;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "BlockoBlock", description = "Model of BlockoBlock")
public class Model_BlockoBlock extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_BlockoBlock.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)   public String id;
                                                            @ApiModelProperty(required = true)   public String name;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)   public String description;

                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Person author;
                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_TypeOfBlock type_of_block;
                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Producer producer;

    @JsonIgnore @OneToMany(mappedBy="blocko_block", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_BlockoBlockVersion> blocko_versions = new ArrayList<>();

    @JsonIgnore  public Integer order_position;
    @JsonIgnore  public boolean removed_by_user;



/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList private String cache_value_type_of_block_id;
    @JsonIgnore @Transient @TyrionCachedList private List<String> cache_value_blocko_versions_id = new ArrayList<>();
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_author_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_value_producer_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty  public String    author_id()         { return cache_value_author_id != null ? cache_value_author_id : get_author().id;}

    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by Byzance or Other Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty  public String    author_nick_name()  { return get_author().nick_name; }



    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty  public String    producer_id()       { return cache_value_producer_id != null ? cache_value_producer_id : get_producer().id;}

    @ApiModelProperty(readOnly = true, value = "can be hidden, if BlockoBlock is created by User not by Company")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Transient  @JsonProperty  public String    producer_name()     { return get_producer().name;}


    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_block_id()   { return cache_value_type_of_block_id != null ? cache_value_type_of_block_id : get_type_of_block().id; }
    @Transient  @JsonProperty @ApiModelProperty(required = true, readOnly = true)  public String  type_of_block_name() { return get_type_of_block().name; }

    @Transient  @JsonProperty @ApiModelProperty(required = true) public  List<Swagger_BlockoBlock_Version_Short_Detail> versions(){

        List<Swagger_BlockoBlock_Version_Short_Detail> list = new ArrayList<>();

        for( Model_BlockoBlockVersion v : get_blocko_block_versions()){

            // TODO Tohle je hrozně komplikovanej shit - nešlo by to jednoduššeji? Víme přece co je private a co je public ne???
            if((v.approval_state == Enum_Approval_state.approved)||(v.approval_state == Enum_Approval_state.edited)||((this.get_author() != null)&&(this.get_author().id.equals(Controller_Security.get_person().id)))) {
                list.add(v.get_short_blockoblock_version());
            }
        }

        return list;
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @Transient @JsonIgnore @TyrionCachedList
    public Model_TypeOfBlock get_type_of_block() {
        if(cache_value_type_of_block_id == null){
            Model_TypeOfBlock type_of_block = Model_TypeOfBlock.find.where().eq("blocko_blocks.id", id).select("id").findUnique();
            cache_value_type_of_block_id = type_of_block.id;
        }

        return Model_TypeOfBlock.get_byId(cache_value_type_of_block_id);
    }

    @Transient @JsonIgnore @TyrionCachedList
    public List<Model_BlockoBlockVersion> get_blocko_block_versions(){
        try{

            if(cache_value_blocko_versions_id.isEmpty()){

                List<Model_BlockoBlockVersion> blocko_versions =  Model_BlockoBlockVersion.find.where().eq("blocko_block.id", id).eq("removed_by_user", false).order().desc("date_of_create").select("id").findList();

                // Získání seznamu
                for (Model_BlockoBlockVersion blocko_version : blocko_versions) {
                    cache_value_blocko_versions_id.add(blocko_version.id);
                }

            }

            List<Model_BlockoBlockVersion> blocko_versions  = new ArrayList<>();

            for(String version_id : cache_value_blocko_versions_id){
                blocko_versions.add(Model_BlockoBlockVersion.get_byId(version_id));
            }

            return blocko_versions;

        }catch (Exception e){
            terminal_logger.internalServerError("getVersion_objects", e);
            return new ArrayList<Model_BlockoBlockVersion>();
        }

    }

    @JsonIgnore @TyrionCachedList
    public Model_Person get_author(){

        if(cache_value_author_id == null){
            Model_Person person = Model_Person.find.where().eq("blocksAuthor.id", id).select("id").findUnique();
            cache_value_author_id = person.id;
        }

        return Model_Person.get_byId(cache_value_author_id);
    }

    @JsonIgnore @TyrionCachedList
    public Model_Producer get_producer(){

        if(cache_value_producer_id == null){
            Model_Producer producer = Model_Producer.find.where().eq("blocko_blocks.id", id).select("id").findUnique();
            cache_value_producer_id = producer.id;
        }

        return Model_Producer.get_byId(cache_value_producer_id);
    }

    @Transient @JsonIgnore
    public Swagger_Blocko_Block_Short_Detail get_blocko_block_short_detail(){
        try {

            Swagger_Blocko_Block_Short_Detail help = new Swagger_Blocko_Block_Short_Detail();
            help.id = id;
            help.name = name;
            help.description = description;
            help.versions = versions();
            help.edit_permission = edit_permission();
            help.delete_permission = delete_permission();
            help.update_permission = update_permission();

            return help;

        }catch (Exception e){
            terminal_logger.internalServerError("get_blocko_block_short_detail:", e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        order_position = Model_BlockoBlock.find.where().eq("type_of_block.id", type_of_block.id).findRowCount() + 1;

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (get_byId(this.id) == null) break;
        }
        super.save();

        if(type_of_block.project_id() != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, type_of_block.project_id(), type_of_block.project_id()))).start();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: " + this.id);

        super.update();

        if(type_of_block.project_id() != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_BlockoBlock.class, type_of_block.project_id(), id))).start();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete :: Delete object Id: " + this.id);

        removed_by_user = true;
        super.update();

        if(type_of_block.project_id() != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, type_of_block.project_id(), type_of_block.project_id()))).start();
    }

/* ORDER ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void up(){

        terminal_logger.trace("up :: Change Order Position! UP ");

        Model_BlockoBlock up = Model_BlockoBlock.find.where().eq("order_position", (order_position-1) ).eq("type_of_block.id", type_of_block.id).findUnique();
        if(up == null){
            terminal_logger.warn("up :: illegal operation (out of index)! ");
            return;
        }

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        terminal_logger.trace("down :: Change Order Position! DOWN ");

        Model_BlockoBlock down = Model_BlockoBlock.find.where().eq("order_position", (order_position+1) ).eq("type_of_block.id", type_of_block.id).findUnique();
        if(down == null){
            terminal_logger.warn("down :: illegal operation (out of index)! ");
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

    @JsonIgnore  @Transient                                     public boolean create_permission() {return  get_type_of_block().update_permission();}
    @JsonIgnore  @Transient                                     public boolean read_permission()   {return  get_type_of_block().read_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()   {return  get_type_of_block().update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission() {return  get_type_of_block().update_permission();}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission() {return  get_type_of_block().delete_permission();}

    public enum permissions{BlockoBlock_create, BlockoBlock_read, BlockoBlock_edit, BlockoBlock_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_BlockoBlock.class.getSimpleName();
    public static Cache<String, Model_BlockoBlock> cache = null;               // < String id, Model_BlockoBlock>

    @JsonIgnore
    public static Model_BlockoBlock get_byId(String id) {

        Model_BlockoBlock blocko_block = cache.get(id);
        if (blocko_block == null){

            blocko_block = find.where().eq("id", id).eq("removed_by_user", false).findUnique();
            if (blocko_block == null){
                terminal_logger.warn("get_byId :: This object id:: " + id + " wasn't found.");
            }

            cache.put(id, blocko_block);
        }

        return blocko_block;

    }



    @JsonIgnore
    public static Model_BlockoBlock get_publicByName(String name) {
        return find.where().isNull("type_of_block.project").eq("removed_by_user", false).eq("name", name).findUnique();
    }

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_BlockoBlock> find = new Finder<>(Model_BlockoBlock.class);
}