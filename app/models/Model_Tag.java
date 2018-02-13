package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.beans.Transient;
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
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_GridProgram> m_programs = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_GridProject> m_projects = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Instance> instances = new ArrayList<>();
    @JsonIgnore @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY) public List<Model_Hardware> hardware = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Tag getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_Tag getById(UUID id) throws _Base_Result_Exception {
        return find.byId(id);
    }

    public static Model_Tag getByValue(String value) throws _Base_Result_Exception {
        return find.query().where().eq("value", value).eq("person.id", _BaseController.personId()).findOne();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    private static Finder<UUID, Model_Tag> find = new Finder<>(Model_Tag.class);
}
