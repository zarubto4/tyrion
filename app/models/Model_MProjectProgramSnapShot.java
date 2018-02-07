package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "M_Project_SnapShot_Detail",  description = "Model of Snapshot of versions of M_Project Snapshots")
@Table(name="MProjectProgramSnapShot")
public class Model_MProjectProgramSnapShot extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_MProjectProgramSnapShot.class);

/* DATABASE VALUE  ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)      public Model_MProject m_project;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY) @JoinTable(name = "b_program_version_snapshots") public List<Model_Version> instance_versions = new ArrayList<>(); // Vazba na version Blocka (zatím je využívaná jen jako M:1

    @JsonIgnore @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "m_project_program_snapshot") public List<Model_MProgramInstanceParameter> m_program_snapshots = new ArrayList<>();    // Verze M_Programu // TODO CACHE

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public UUID m_project_id()          { return m_project.id;}
    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public String m_project_name()        { return m_project.name;}
    @JsonProperty @Transient  @ApiModelProperty(required = true, readOnly = true) public String m_project_description() { return m_project.description;}

    @JsonProperty @ApiModelProperty(required = true) public List<Model_MProgramInstanceParameter> m_program_snapshots() {
        try {

            return Model_MProgramInstanceParameter.find.query().where().eq("m_project_program_snapshot.id", id).order().asc("m_program_version.m_program.name").findList();

        } catch (Exception e) {

            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/




/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("update :: Update object Id: {}",  this.id);
        super.save();
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);
        super.update();
    }

    @JsonIgnore @Override
    public boolean delete() {
        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore                                      public boolean create_permission()      {return true;}
    @JsonIgnore                                      public boolean read_permission()      {return true;}
    @JsonProperty @ApiModelProperty(required = true) public boolean unshare_permission()   {return true;}
    @JsonProperty @ApiModelProperty(required = true) public boolean share_permission ()    {return true;}
    @JsonProperty @ApiModelProperty(required = true) public boolean admin_permission ()    {return true;}
    @JsonIgnore                                      public boolean update_permission()      {return true;}
    @JsonIgnore                                      public boolean delete_permission()      {return true;}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<UUID, Model_MProjectProgramSnapShot> find = new Finder<>(Model_MProjectProgramSnapShot.class);

}
