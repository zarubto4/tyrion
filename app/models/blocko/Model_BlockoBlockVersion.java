package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.person.Model_Person;
import utilities.enums.Approval_state;
import utilities.swagger.outboundClass.Swagger_BlockoBlock_Version_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Person_Short_Detail;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of BlockoBlockVersion",
        value = "BlockoBlockVersion")
public class Model_BlockoBlockVersion extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)    public String id;
                                                            @ApiModelProperty(required = true)    public String version_name;
                                                            @ApiModelProperty(required = true)    public String version_description;
    @Enumerated(EnumType.STRING)                            @ApiModelProperty(required = true)    public Approval_state approval_state;

                                                                        @JsonIgnore @ManyToOne    public Model_Person author;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,
            value = "UNIX time in ms", example = "1466163478925")                                 public Date date_of_create;

                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)    public String design_json;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true)    public String logic_json;
                                                                        @JsonIgnore @ManyToOne    public Model_BlockoBlock blocko_block;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    public Swagger_Person_Short_Detail author(){
        return this.author.get_short_person();
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (get_byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore
    public Swagger_BlockoBlock_Version_Short_Detail get_short_blockoblock_version(){

        Swagger_BlockoBlock_Version_Short_Detail help = new Swagger_BlockoBlock_Version_Short_Detail();
        help.id = this.id;
        help.name = this.version_name;
        help.description = this.version_description;
        help.date_of_create = this.date_of_create;
        help.design_json = this.design_json;
        help.author = this.author.get_short_person();

        return help;
    }

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
        return find.where().eq("approval_state",Approval_state.pending).findList();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user can read BlockoBlock, than can read all Versions from list of BlockoBlock ( You get ids of list of version in object \"BlockoBlocks\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have BlockoBlock.update_permission = true, you can create new version of BlockoBlocks on this BlockoBlock - Or you need static/dynamic permission key if user want create version of BlockoBlock in public BlockoBlock in public TypeOfBlock";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean create_permission()  {  return  blocko_block.update_permission() ||  Controller_Security.getPerson().has_permission("BlockoBlock_create"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean read_permission()    {  return  blocko_block.read_permission()   ||  Controller_Security.getPerson().has_permission("BlockoBlock_read");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()    {  return  blocko_block.update_permission() || Controller_Security.getPerson().has_permission("BlockoBlock_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()  {  return  blocko_block.update_permission() ||  Controller_Security.getPerson().has_permission("BlockoBlock_delete"); }

    public enum permissions{BlockoBlock_create, BlockoBlock_read, BlockoBlock_edit, BlockoBlock_delete}

/* FINDER -------------------------------------------------------------------------------------------------------------*/
    private static Model.Finder<String,Model_BlockoBlockVersion> find = new Finder<>(Model_BlockoBlockVersion.class);

}
