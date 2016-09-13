package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.ProgramingPackageController;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TypeOfBlock extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;
                                       @Column(unique=true) @ApiModelProperty(required = true) public String name;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true) public String general_description;
                                                            @ApiModelProperty(required = true) public ProgramingPackageController.approval_state approval_state;


                                    @JsonIgnore @ManyToOne  public Project project;

    @OneToMany(mappedBy="type_of_block", cascade = CascadeType.ALL)
    @ApiModelProperty(required = true)                                                         public List<BlockoBlock> blockoBlocks = new ArrayList<>();


    @ApiModelProperty(value = "This value will be in Json only if TypeOfBlock is private!",readOnly =true, required = false)
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient public String project_id() {  return project == null ? null : this.project.id; }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read TypeOfBlock on this Project ( You get ids of list of TypeOfBLocks in object \"project\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create TypeOfBlock on this Project - Or you need static/dynamic permission key if user want create public TypeOfBlock";



    @JsonIgnore @Transient                                      public boolean create_permission()  {return                      (project != null && project.update_permission()) || SecurityController.getPerson().has_permission("TypeOfBlock_create");}
    @JsonIgnore @Transient                                      public boolean read_permission()    {return (project == null) || (project != null && project.read_permission())   || SecurityController.getPerson().has_permission("TypeOfBlock_read");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission()  {return                      (project != null && project.update_permission()) || SecurityController.getPerson().has_permission("TypeOfBlock_update");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()    {return                      (project != null && project.edit_permission())   || SecurityController.getPerson().has_permission("TypeOfBlock_edit");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission()  {return                      (project != null && project.update_permission()) || SecurityController.getPerson().has_permission("TypeOfBlock_delete");}

    public enum permissions{TypeOfBlock_create, TypeOfBlock_read, TypeOfBlock_edit , TypeOfBlock_delete, TypeOfBlock_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,TypeOfBlock> find = new Finder<>(TypeOfBlock.class);
}
