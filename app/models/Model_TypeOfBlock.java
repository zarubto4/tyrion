package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true) public String id;
                                                            @ApiModelProperty(required = true) public String name;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true) public String description;

                                                                        @JsonIgnore @ManyToOne public Model_Project project;

    @OneToMany(mappedBy="type_of_block", cascade=CascadeType.ALL) @ApiModelProperty(required = true) public List<Model_BlockoBlock> blocko_blocks = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(value = "This value will be in Json only if TypeOfBlock is private!", readOnly = true, required = false)
    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @Transient                        public String project_id() {  return project == null ? null : this.project.id; }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (get_byId(this.id) == null) break;
        }
        super.save();
    }

    /* GET Variable short type of objects ------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public Swagger_TypeOfBlock_Short_Detail get_type_of_block_short_detail(){
        Swagger_TypeOfBlock_Short_Detail help = new Swagger_TypeOfBlock_Short_Detail();
        help.id = id;
        help.name = name;
        help.description = description;

        for (Model_BlockoBlock block : blocko_blocks) help.blocko_blocks.add(block.get_blocko_block_short_detail());

        help.edit_permission = edit_permission();
        help.delete_permission = delete_permission();
        help.update_permission = update_permission();
        return help;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read TypeOfBlock on this Project ( You get ids of list of TypeOfBLocks in object \"project\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create TypeOfBlock on this Project - Or you need static/dynamic permission key if user want create public TypeOfBlock";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient                                      public boolean create_permission()  {return                      (project != null && project.update_permission()) || Controller_Security.getPerson().has_permission("TypeOfBlock_create");}
    @JsonIgnore @Transient                                      public boolean read_permission()    {return (project == null) || (project != null && project.read_permission())   || Controller_Security.getPerson().has_permission("TypeOfBlock_read");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission()  {return                      (project != null && project.update_permission()) || Controller_Security.getPerson().has_permission("TypeOfBlock_update");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()    {return                      (project != null && project.edit_permission())   || Controller_Security.getPerson().has_permission("TypeOfBlock_edit");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()  {return                      (project != null && project.update_permission()) || Controller_Security.getPerson().has_permission("TypeOfBlock_delete");}

    public enum permissions{TypeOfBlock_create, TypeOfBlock_read, TypeOfBlock_edit , TypeOfBlock_delete, TypeOfBlock_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    private static Model.Finder<String,Model_TypeOfBlock> find = new Finder<>(Model_TypeOfBlock.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_TypeOfBlock get_byId(String id) {
        return find.byId(id);
    }

    @JsonIgnore
    public static List<Model_TypeOfBlock> get_all() {

        List<Model_TypeOfBlock> typeOfBlocks = find.where().isNull("project").findList();
        typeOfBlocks.addAll( find.where().eq("project.participants.person.id", Controller_Security.getPerson().id ).findList() );

        return typeOfBlocks;
    }

    @JsonIgnore
    public static Model_TypeOfBlock get_publicByName(String name) {
        return find.where().isNull("project").eq("name",name).findUnique();
    }

    @JsonIgnore
    public static List<Model_TypeOfBlock> get_public() {
        return find.where().isNull("project").findList();
    }
}
