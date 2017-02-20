package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Model_VersionObject;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "M_Project_SnapShot_Detail")
public class Model_MProjectProgramSnapShot extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @ApiModelProperty(required = true)  public UUID id;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)      public Model_MProject m_project;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER) @JoinTable(name = "b_program_version_snapshots") public List<Model_VersionObject> instance_versions = new ArrayList<>(); // Vazba přes version_objet na Homer_Instance_record

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER) @JoinTable(name = "m_project_program_snapshots") public List<Model_VersionObject> version_objects_program = new ArrayList<>();    // Verze M_Programu


/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public String m_project_id()          { return m_project.id;}
    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public String m_project_name()        { return m_project.name;}
    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public String m_project_description() { return m_project.description;}

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public List<M_Program_SnapShot_Detail> m_program_snapshots() {
        List<M_Program_SnapShot_Detail> list = new ArrayList<>();
        try {

            if (version_objects_program != null)
                for (Model_VersionObject version_object : version_objects_program) {
                    M_Program_SnapShot_Detail s = new M_Program_SnapShot_Detail();
                    s.m_program_id = version_object.m_program.id;
                    s.m_program_name = version_object.m_program.name;
                    s.m_program_description = version_object.m_program.description;

                    s.version_object_id = version_object.id;
                    s.version_object_name = version_object.version_name;
                    s.version_object_description = version_object.version_description;
                    list.add(s);
                }

            return list;
        }catch (Exception e){

            e.printStackTrace();
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public class M_Program_SnapShot_Detail {

        public M_Program_SnapShot_Detail(){}

        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String m_program_id;
        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String m_program_name;
        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String m_program_description;
        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String version_object_id;
        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String version_object_name;
        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String version_object_description;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_MProjectProgramSnapShot> find = new Finder<>(Model_MProjectProgramSnapShot.class);

}
