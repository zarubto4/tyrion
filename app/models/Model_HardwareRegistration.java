package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.Cached;
import utilities.logger.Logger;
import utilities.model.TaggedModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "HardwareRegistration", description = "Model of HardwareRegistration")
@Table(name="HardwareRegistration")
public class Model_HardwareRegistration extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HardwareRegistration.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @OneToOne public Model_Hardware hardware;
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY)  public Model_Blob picture;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_HardwareGroup group;

    @JsonIgnore @OneToMany(mappedBy = "hardware", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Model_HardwareUpdate> updates = new ArrayList<>();

    @JsonIgnore @ManyToMany(mappedBy = "hardware", fetch = FetchType.LAZY) public List<Model_InstanceSnapshot> instances = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public UUID cache_group_id;
    @JsonIgnore @Transient @Cached public UUID cache_project_id;
    @JsonIgnore @Transient @Cached public UUID cache_picture_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true)
    public Model_HardwareGroup group() {
        return getGroup();
    }

    @JsonProperty @ApiModelProperty(required = true)
    public String picture_link() {
        try {

            if ( cache_picture_id == null) {

                Model_Blob fileRecord = Model_Blob.find.query().where().eq("hardware.id",id).select("id").findOne();
                if (fileRecord != null) {
                    cache_picture_id =  fileRecord.id;
                }
            }

            if (cache_picture_id != null) {
                Model_Blob record = Model_Blob.getById(cache_picture_id);
                if (record != null) {
                    return record.getPublicDownloadLink(300);
                }
            }

            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    public Model_Hardware getUnderlayingHardware() {
        return this.hardware;
    }

    @JsonIgnore
    public Model_HardwareGroup getGroup() {

        if (cache_group_id == null) {
            Model_HardwareGroup group = Model_HardwareGroup.find.query().where().eq("hardware.id", id).findOne();
            if (group == null) return null;

            cache_group_id = group.id;
            group.cache();

            return group;
        }

        return Model_HardwareGroup.getById(cache_group_id);
    }



    @JsonIgnore
    public Model_Project getProject() {

        if (cache_project_id == null) {
            Model_Project project = Model_Project.find.query().where().eq("hardware.id", id).findOne();
            if (project == null) return null;

            cache_project_id = project.id;
            project.cache();

            return project;
        }

        return Model_Project.getById(cache_project_id);
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public String getPath() {
        return getProject().getPath() + "/hardware";
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Create Permission is always JsonIgnore
    @JsonIgnore   public boolean create_permission() { return false; }
    @JsonIgnore   public boolean read_permission()   { return false; }
    @JsonProperty public boolean update_permission() { return false; }
    @JsonProperty public boolean edit_permission()   { return false; }
    @JsonProperty public boolean delete_permission() { return false; }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_HardwareRegistration getById(String id) {
        return getById(UUID.fromString(id));
    }

    @JsonIgnore
    public static Model_HardwareRegistration getById(UUID id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_HardwareRegistration> find = new Finder<>(Model_HardwareRegistration.class);
}
