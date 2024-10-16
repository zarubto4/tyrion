package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.ebean.Model;
import io.swagger.annotations.ApiModel;
import utilities.logger.Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Tag", description = "Model of Tag")
@Table(name = "Tag")
public class Model_Tag extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Tag.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id public UUID id;
    @Column(unique = true) public String value;

    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Project> projects = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Block> blocks = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Widget> widgets = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Library> libraries = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_CProgram> c_programs = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_BProgram> b_programs = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_GridProgram> m_programs = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_GridProject> m_projects = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Instance> instances = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Hardware> hardware = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_GSM> gsms = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Article> articles = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Tag getByValue(String value) {
        return find.query().where().eq("value", value).findOne();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    private static Finder<UUID, Model_Tag> find = new Finder<>(Model_Tag.class);
}
