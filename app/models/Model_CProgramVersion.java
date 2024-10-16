package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.libs.Json;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.CompilationStatus;
import utilities.enums.EntityType;
import exceptions.NotFoundException;
import utilities.hardware.update.Updatable;
import utilities.logger.Logger;
import utilities.model.UnderProject;
import utilities.model.VersionModel;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_C_Program_Version;
import utilities.swagger.output.Swagger_Short_Reference;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel( value = "CProgramVersion", description = "Model of CProgramVersion")
@Table(name="CProgramVersion")
public class Model_CProgramVersion extends VersionModel implements Permissible, UnderProject, Updatable {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_CProgramVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                                                   public Model_CProgram c_program;
    @JsonIgnore @OneToOne(mappedBy="version", cascade = CascadeType.ALL, fetch = FetchType.LAZY)                     public Model_Compilation compilation;

    @JsonIgnore @OneToMany(mappedBy="actual_c_program_version", fetch = FetchType.LAZY)                              public List<Model_Hardware> c_program_version_boards  = new ArrayList<>(); // Používám pro zachycení, která verze C_programu na desce běží
    @JsonIgnore @OneToMany(mappedBy="actual_backup_c_program_version", fetch = FetchType.LAZY)                       public List<Model_Hardware>  c_program_version_backup_boards  = new ArrayList<>(); // Nikdy z této ztrany nepoužívat!
    @JsonIgnore @OneToMany(mappedBy="c_program_version_for_update",cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_HardwareUpdate> updates = new ArrayList<>();

    @OneToOne @JsonIgnore  public Model_CProgram default_program;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public CompilationStatus status() {
        try {
            return this.getCompilation().status;
        } catch (Exception e) {
            return CompilationStatus.UNDEFINED;
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public String compilation_version() {
        try {
            return this.getCompilation().firmware_version_lib;
        } catch (Exception e) {
            return CompilationStatus.UNDEFINED.name();
        }
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true, value = "Value can be empty, Server cannot guarantee that.")
    public String virtual_input_output(){
        try {
            return this.getCompilation().virtual_input_output;
        } catch (Exception e) {
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(required = false, readOnly = true, value = "Link for download file in Binary (Not in Base64). Its ready to manual Upload. Only if \"status\" == \"SUCCESS\"")
    public String download_link_bin_file() {
        try {
            if (status() == CompilationStatus.SUCCESS) {
                return getCompilation().file_path();
            } else {
                return null;
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonProperty @ApiModelProperty(value = "Visible only for Administrator with Special Permission", required = false) @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean main_mark(){
        return default_program != null ? true : null;
    }

    @JsonProperty @ApiModelProperty(value = "Program", required = false)
    public Swagger_C_Program_Version program() {
        try {

            Swagger_C_Program_Version c_program_versions = new Swagger_C_Program_Version();

            if (file != null) {

                JsonNode json = Json.parse(file.downloadString());

                Swagger_C_Program_Version_Refresh version_new = formFromJsonWithValidation(Swagger_C_Program_Version_Refresh.class, json);

                c_program_versions.main = version_new.main;
                c_program_versions.files = version_new.files;

                for (UUID imported_library_version_id : version_new.imported_libraries) {

                    try {
                        Model_LibraryVersion library_version = Model_LibraryVersion.find.byId(imported_library_version_id);

                        Swagger_Library_Library_Version_pair pair = new Swagger_Library_Library_Version_pair();
                        pair.library         =  new Swagger_Short_Reference(library_version.library.id, library_version.library.name, library_version.library.description);
                        pair.library_version = new Swagger_Short_Reference(library_version.id, library_version.name, library_version.description);

                        c_program_versions.imported_libraries.add(pair);
                    } catch (NotFoundException e) {
                        // Nothing
                    }
                }
            } else {
                logger.error("File Record is null!");
            }

            return c_program_versions;

        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_c_program_id() {

        if (idCache().get(Model_CProgram.class) == null) {
            idCache().add(Model_CProgram.class, (UUID) Model_CProgram.find.query().where().eq("deleted", false).eq("versions.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_CProgram.class);
    }

    @JsonIgnore
    public Model_CProgram getProgram() {
        return isLoaded("c_program") ? c_program : Model_CProgram.find.query().where().eq("versions.id", id).findOne();
    }

    @JsonIgnore
    public Model_Compilation getCompilation() {
        return isLoaded("compilation") ? compilation : Model_Compilation.find.query().nullable().where().eq("version.id", id).findOne();
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return this.getProgram().getProject();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        super.save();
        this.getProgram().refresh();
    }

    @JsonIgnore @Override
    public boolean delete() {
        boolean delete = super.delete();
        this.getProgram().refresh();
        return delete;
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public String get_path() {
        return  getProgram().get_path() + "/version/" + this.id;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.FIRMWARE_VERSION;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.PUBLISH);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER -------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_CProgramVersion.class)
    public static CacheFinder<Model_CProgramVersion> find = new CacheFinder<>(Model_CProgramVersion.class);
}
