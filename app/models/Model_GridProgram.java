package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.models_update_echo.EchoHandler;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.output.Swagger_M_Program_Version_Interface;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel(value = "GridProgram", description = "Model of GridProgram")
@Table(name = "GridProgram")
public class Model_GridProgram extends TaggedModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GridProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                                      public Model_GridProject grid_project;
    @JsonIgnore @OneToMany(mappedBy="grid_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_GridProgramVersion> versions = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public List<Model_GridProgramVersion> program_versions() {
        try {
            return get_versions().stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList());

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public Model_Project getProject() {
        return this.get_grid_project().getProject();
    }

    @JsonIgnore @Transient public UUID get_grid_project_id() {

        if (idCache().get(Model_GridProject.class) == null) {
            idCache().add(Model_GridProject.class, (UUID) Model_GridProject.find.query().where().eq("grid_programs.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_GridProject.class);
    }

    @JsonIgnore @Transient public Model_GridProject get_grid_project() {
        return isLoaded("grid_project") ? this.grid_project : Model_GridProject.find.query().nullable().where().eq("grid_programs.id", id).findOne();
    }

    @JsonIgnore @Transient public List<UUID> get_versionsId() {
        if (idCache().gets(Model_GridProgramVersion.class) == null) {
            idCache().add(Model_GridProgramVersion.class,  Model_GridProgramVersion.find.query().where().ne("deleted", true).eq("grid_program.id", id).order().desc("created").select("id").findSingleAttributeList());
        }

        return idCache().gets(Model_GridProgramVersion.class) != null ?  idCache().gets(Model_GridProgramVersion.class) : new ArrayList<>();
    }

    @JsonIgnore
    public void sort_Model_Model_GridProgramVersion_ids() {

        List<Model_GridProgramVersion> versions = get_versions();
        this.idCache().removeAll(Model_GridProgramVersion.class);
        versions.stream().sorted((element1, element2) -> element2.created.compareTo(element1.created)).collect(Collectors.toList())
                .forEach(o -> this.idCache().add(Model_GridProgramVersion.class, o.id));

    }

    @JsonIgnore @Transient public List<Model_GridProgramVersion> get_versions() {
        try {

            List<Model_GridProgramVersion> versions  = new ArrayList<>();

            for (UUID version_id : get_versionsId()) {
                versions.add(Model_GridProgramVersion.find.byId(version_id));
            }

            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore @Transient public List<Swagger_M_Program_Version_Interface> program_versions_interface() {
        try {

            List<Swagger_M_Program_Version_Interface> versions = new ArrayList<>();

            for (Model_GridProgramVersion v : get_versions()) {
                Swagger_M_Program_Version_Interface help = new Swagger_M_Program_Version_Interface();
                help.version = v;
                help.virtual_input_output = v.m_program_virtual_input_output;
                versions.add(help);
            }
            return versions;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        super.save();

        Model_GridProject grid_project = get_grid_project();
        if (grid_project != null) {
            grid_project.getGrid_programs_ids();
            grid_project.idCache().add(this.getClass(), id);
        }

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_Project.class, grid_project.get_project_id(), grid_project.id))).start();
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_GridProgram.class, get_grid_project().get_project_id(), id))).start();
    }

    @JsonIgnore @Override
    public boolean delete() {
        logger.debug("update :: Delete object Id: {} ", this.id);

        super.delete();

        get_grid_project().idCache().remove(this.getClass(), id);

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo( Model_GridProject.class, get_grid_project().get_project_id(), get_grid_project_id()))).start();

        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Deprecated
    private String blob_link; // TODO smazat z Databáze

    @JsonIgnore
    public String get_path() {


        // FOR OLD already created objects is still using blob_link, but its @Deprecated
        if(blob_link != null) return blob_link;
        return get_grid_project().get_path() + "/grid-programs/" + this.id;

    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.GRID_PROGRAM;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_GridProgram.class)
    public static CacheFinder<Model_GridProgram> find = new CacheFinder<>(Model_GridProgram.class);
}