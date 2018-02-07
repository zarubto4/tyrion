package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Tag", description = "Model of Tag")
@Table(name = "Tag")
public class Model_Tag extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Tag.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(unique = true)
    public String value;

    @ManyToOne public Model_Person person;

    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Project> projects = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Block> blocks = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Widget> widgets = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Library> libraries = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_CProgram> c_programs = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_BProgram> b_programs = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_MProgram> m_programs = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_MProject> m_projects = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Instance> instances = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_HardwareRegistration> hardware = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Create Permission is always JsonIgnore
    @JsonIgnore   public boolean create_permission() { return true; }
    @JsonProperty public boolean update_permission() { return false; }
    @JsonProperty public boolean edit_permission()   { return false; }
    @JsonProperty public boolean delete_permission() { return false; }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Tag getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_Tag getById(UUID id) {
        return find.byId(id);
    }

    public static Model_Tag getByValue(String value) {
        return find.query().where().eq("value", value).eq("person.id", BaseController.personId()).findOne();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    private static Finder<UUID, Model_Tag> find = new Finder<>(Model_Tag.class);
}
