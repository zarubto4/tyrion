package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "HardwareRegistration", description = "Model of HardwareRegistration")
@Table(name="HardwareRegistration")
public class Model_HardwareRegistration extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HardwareRegistration.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @OneToOne public Model_Hardware hardware;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;

    @JsonIgnore @ManyToMany(mappedBy = "hardware", fetch = FetchType.LAZY) public List<Model_InstanceSnapshot> instances = new ArrayList<>();

    @ManyToMany public List<Model_Tag> tags = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    public Model_Hardware getUnderlayingHardware() {
        return this.hardware;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

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

    private static Finder<UUID, Model_HardwareRegistration> find = new Finder<>(Model_HardwareRegistration.class);
}
