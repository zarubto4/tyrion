package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_Blocko_Block_Short_Detail;
import utilities.swagger.outboundClass.Swagger_GridWidget_Short_Detail;
import utilities.swagger.outboundClass.Swagger_TypeOfBlock_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of TypeOfBlock",
        value = "TypeOfBlock")
public class Model_TypeOfBlock extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_SecurityRole.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true) public String id;
                                                            @ApiModelProperty(required = true) public String name;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true) public String description;

                                                                        @JsonIgnore @ManyToOne public Model_Project project;

    @JsonIgnore @OneToMany(mappedBy="type_of_block", cascade=CascadeType.ALL, fetch = FetchType.LAZY) @ApiModelProperty(required = true) public List<Model_BlockoBlock> blocko_blocks = new ArrayList<>();

    @JsonIgnore  public Integer order_position;

    @JsonIgnore              public boolean removed_by_user;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(value = "This value will be in Json only if TypeOfBlock is private!", readOnly = true, required = false)
    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @Transient                        public String project_id() {  return project == null ? null : this.project.id; }


    @JsonProperty @Transient public List<Swagger_Blocko_Block_Short_Detail> blocks() {

        try {

            List<Swagger_Blocko_Block_Short_Detail> short_detail_blocks = new ArrayList<>();

            for (Model_BlockoBlock block :  Model_BlockoBlock.find.where().eq("type_of_block.id", id).eq("removed_by_user", false).order().asc("order_position").findList()) {
                short_detail_blocks.add( block.get_blocko_block_short_detail() ) ;
            }

            return short_detail_blocks;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public Swagger_TypeOfBlock_Short_Detail get_type_of_block_short_detail(){
        Swagger_TypeOfBlock_Short_Detail help = new Swagger_TypeOfBlock_Short_Detail();
        help.id = id;
        help.name = name;
        help.description = description;
        help.blocko_blocks.addAll(blocks());
        help.edit_permission = edit_permission();
        help.delete_permission = delete_permission();
        help.update_permission = update_permission();
        return help;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        if(project == null){
            order_position = Model_TypeOfBlock.find.where().isNull("project").findRowCount() + 1;
        }else {
            order_position = Model_TypeOfBlock.find.where().eq("project.id", project.id).findRowCount() + 1;
        }

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (get_byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object value: {}",  this.id);

        super.update();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);


        removed_by_user = true;
        super.update();

    }


/* ORDER ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void up(){

        // Čísla mohou být shodná!! TODO

        Model_TypeOfBlock up = Model_TypeOfBlock.find.where().eq("order_position", (order_position-1) ).isNull("project").findUnique();
        if(up == null)return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        Model_TypeOfBlock down = Model_TypeOfBlock.find.where().eq("order_position", (order_position+1) ).isNull("project").findUnique();
        if(down == null)return;

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
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read TypeOfBlock on this Project ( You get ids of list of TypeOfBLocks in object \"project\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create TypeOfBlock on this Project - Or you need static/dynamic permission key if user want create public TypeOfBlock";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient                                      public boolean create_permission()  {return                      (project != null && project.update_permission()) || Controller_Security.get_person().has_permission("TypeOfBlock_create");}
    @JsonIgnore @Transient                                      public boolean read_permission()    {return (project == null) || (project != null && project.read_permission())   || Controller_Security.get_person().has_permission("TypeOfBlock_read");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission()  {return                      (project != null && project.update_permission()) || Controller_Security.get_person().has_permission("TypeOfBlock_update");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()    {return                      (project != null && project.edit_permission())   || Controller_Security.get_person().has_permission("TypeOfBlock_edit");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()  {return                      (project != null && project.update_permission()) || Controller_Security.get_person().has_permission("TypeOfBlock_delete");}

    public enum permissions{TypeOfBlock_create, TypeOfBlock_read, TypeOfBlock_edit , TypeOfBlock_delete, TypeOfBlock_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_TypeOfBlock> find = new Finder<>(Model_TypeOfBlock.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_TypeOfBlock get_byId(String id) {
        return find.byId(id);
    }

    @JsonIgnore
    public static List<Model_TypeOfBlock> get_all() {

        List<Model_TypeOfBlock> typeOfBlocks = find.where().isNull("project").findList();
        typeOfBlocks.addAll( find.where().eq("project.participants.person.id", Controller_Security.get_person().id ).eq("removed_by_user", false).order().asc("name").findList() );
        typeOfBlocks.addAll( find.where().isNull("project").eq("removed_by_user", false).order().asc("order_position").findList());
        return typeOfBlocks;
    }

    @JsonIgnore
    public static Model_TypeOfBlock get_publicByName(String name) {
        return find.where().isNull("project").eq("name",name).findUnique();
    }

    @JsonIgnore
    public static List<Model_TypeOfBlock> get_public() {
        return find.where().isNull("project").order().asc("order_position").findList();
    }
}
