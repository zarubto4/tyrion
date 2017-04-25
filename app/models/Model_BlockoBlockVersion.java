package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Enum_Approval_state;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.outboundClass.Swagger_BlockoBlock_Version_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Person_Short_Detail;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "BlockoBlockVersion", description = "Model of BlockoBlockVersion")
public class Model_BlockoBlockVersion extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_BlockoBlockVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)    public String id;
                                                            @ApiModelProperty(required = true)    public String version_name;
                                                            @ApiModelProperty(required = true)    public String version_description;
    @Enumerated(EnumType.STRING)                            @ApiModelProperty(required = true)    public Enum_Approval_state approval_state;

                                                                        @JsonIgnore @ManyToOne    public Model_Person author;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,
            value = "UNIX time in ms", example = "1466163478925")                                 public Date date_of_create;

                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)    public String design_json;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)    public String logic_json;
                                                                        @JsonIgnore @ManyToOne    public Model_BlockoBlock blocko_block;

    @JsonIgnore              public boolean removed_by_user;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty()
    public Swagger_Person_Short_Detail author(){
        try{

            return this.author.get_short_person();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


    @JsonIgnore
    public Swagger_BlockoBlock_Version_Short_Detail get_short_blockoblock_version(){
        try {

            Swagger_BlockoBlock_Version_Short_Detail help = new Swagger_BlockoBlock_Version_Short_Detail();
            help.id = this.id;
            help.name = this.version_name;
            help.description = this.version_description;
            help.date_of_create = this.date_of_create;
            help.design_json = this.design_json;
            help.author = this.author.get_short_person();

            return help;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/
    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (get_byId(this.id) == null) break;
        }
        super.save();

        if(blocko_block.type_of_block.project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_BlockoBlock.class, blocko_block.type_of_block.project_id(), blocko_block.id))).start();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: " + this.id);

        super.update();

        if(blocko_block.type_of_block.project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_BlockoBlockVersion.class, blocko_block.type_of_block.project_id(), id))).start();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete :: Delete object Id: " + this.id);

        removed_by_user = true;
        super.update();

        if(blocko_block.type_of_block.project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_BlockoBlock.class, blocko_block.type_of_block.project_id(), blocko_block.id))).start();
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read BlockoBlock, than can read all Versions from list of BlockoBlock ( You get ids of list of version in object \"BlockoBlocks\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have BlockoBlock.update_permission = true, you can create new version of BlockoBlocks on this BlockoBlock - Or you need static/dynamic permission key if user want create version of BlockoBlock in public BlockoBlock in public TypeOfBlock";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean create_permission()  {  return  blocko_block.update_permission() ||  Controller_Security.get_person().has_permission("BlockoBlock_create"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean read_permission()    {  return  blocko_block.read_permission()   ||  Controller_Security.get_person().has_permission("BlockoBlock_read");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()    {  return  blocko_block.update_permission() || Controller_Security.get_person().has_permission("BlockoBlock_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()  {  return  blocko_block.update_permission() ||  Controller_Security.get_person().has_permission("BlockoBlock_delete"); }

    public enum permissions{BlockoBlock_create, BlockoBlock_read, BlockoBlock_edit, BlockoBlock_delete}

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    private static Model.Finder<String,Model_BlockoBlockVersion> find = new Finder<>(Model_BlockoBlockVersion.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_BlockoBlockVersion get_byId(String id) {
        return find.byId(id);
    }

    @JsonIgnore
    public static Model_BlockoBlockVersion get_scheme() {
        return find.where().eq("version_name", "version_scheme").findUnique();
    }

    @JsonIgnore
    public static List<Model_BlockoBlockVersion> get_pending() {
        return find.where().eq("approval_state", Enum_Approval_state.pending).findList();
    }
}
