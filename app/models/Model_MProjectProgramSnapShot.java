package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "M_Project_SnapShot_Detail",  description = "Model of Snapshot of versions of M_Project Snapshots")
public class Model_MProjectProgramSnapShot extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_MProjectProgramSnapShot.class);

/* DATABASE VALUE  ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public UUID id;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)      public Model_MProject m_project;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER) @JoinTable(name = "b_program_version_snapshots") public List<Model_VersionObject> instance_versions = new ArrayList<>(); // Vazba přes version_objet na Homer_Instance_record

    @JsonProperty @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER) @JoinTable(name = "m_project_program_snapshot") public List<Model_MProgramInstanceParameter> m_program_snapshots = new ArrayList<>();    // Verze M_Programu

//REMOVED(1.09.04) @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER) @JoinTable(name = "m_project_program_snapshots") public List<Model_VersionObject> version_objects_program = new ArrayList<>();    // Verze M_Programu


/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public String m_project_id()          { return m_project.id;}
    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public String m_project_name()        { return m_project.name;}
    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public String m_project_description() { return m_project.description;}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore                                      public boolean read_permission()      {return true;}
    @JsonProperty @ApiModelProperty(required = true) public boolean unshare_permission()   {return true;}
    @JsonProperty @ApiModelProperty(required = true) public boolean share_permission ()    {return true;}
    @JsonProperty @ApiModelProperty(required = true) public boolean admin_permission ()    {return true;}


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);
        super.save();
    }

    @JsonIgnore @Override
    public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);
        super.update();
    }

    @JsonIgnore @Override
    public void delete() {
        terminal_logger.error("update :: This Object is not allow to remove");
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_MProjectProgramSnapShot> find = new Finder<>(Model_MProjectProgramSnapShot.class);

}
