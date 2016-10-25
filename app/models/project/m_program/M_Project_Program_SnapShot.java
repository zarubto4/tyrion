package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Version_Object;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@ApiModel(value = "M_Project_SnapShot_Detail")
public class M_Project_Program_SnapShot extends Model {

/* DATABASE VALUE  ----------------------------------------------------------------------------------------------------*/
    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;


    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public M_Project m_project;
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER) @JoinTable(name = "m_version_snapShots")  public List<Version_Object> version_objects = new ArrayList<>();    // Verze M_Programu




/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public String m_project_id() { return m_project.id;}

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public List<M_Program_SnapShot_Detail> m_program_snapshots() {
        List<M_Program_SnapShot_Detail> list = new ArrayList<>();
        try {

            if (version_objects != null)
                for (Version_Object version_object : version_objects) {
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

/* Private Documentation Class -----------------------------------------------------------------------------------------*/

    public class M_Program_SnapShot_Detail {

        public M_Program_SnapShot_Detail(){}

        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String m_program_id;
        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String m_program_name;
        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String m_program_description;
        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String version_object_id;
        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String version_object_name;
        @Constraints.Required @ApiModelProperty(required = true, readOnly = true) public String version_object_description;
    }


/* FINDER -------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,M_Project_Program_SnapShot> find = new Finder<>(M_Project_Program_SnapShot.class);

}
