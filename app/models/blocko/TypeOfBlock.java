package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TypeOfBlock extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
                                                            public String name;
                         @Column(columnDefinition = "TEXT") public String general_description;


                                    @JsonIgnore @ManyToOne  public Project project;

    @OneToMany(mappedBy="type_of_block", cascade = CascadeType.ALL) public List<BlockoBlock> blockoBlocks = new ArrayList<>();


    @ApiModelProperty(value = "This value will be in Json only if TypeOfBlock is private!",readOnly =true, required = false)
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient public String project_id() {  return project == null ? null : this.project.id; }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public Boolean create_permission(){
        if(project == null) {
            return SecurityController.getPerson().has_permission("TypeOfBlock_create");
        }
        else return project.update_permission();
    }

    @JsonIgnore @Transient public Boolean read_permission() {
        return project == null || (project.read_permission() || SecurityController.getPerson().has_permission("TypeOfBlock_read"));

    }

    @JsonIgnore @Transient  public Boolean update_permission(){
        return (project.update_permission() || SecurityController.getPerson().has_permission("TypeOfBlock_update"));
    }
    @JsonProperty @Transient  public Boolean edit_permission(){
        if(project != null) return  ( project.edit_permission() || SecurityController.getPerson().has_permission("TypeOfBlock_edit") );
        return SecurityController.getPerson().has_permission("TypeOfBlock_edit");
    }
    @JsonProperty @Transient public Boolean delete_permission(){
        if(project != null) return  ( project.update_permission() || SecurityController.getPerson().has_permission("TypeOfBlock_delete") );
        return SecurityController.getPerson().has_permission("TypeOfBlock_delete");
    }

    public enum permissions{TypeOfBlock_create, TypeOfBlock_read, TypeOfBlock_edit , TypeOfBlock_delete, TypeOfBlock_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,TypeOfBlock> find = new Finder<>(TypeOfBlock.class);
}
