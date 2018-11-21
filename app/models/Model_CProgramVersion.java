package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.storage.StorageException;
import controllers._BaseController;
import controllers.Controller_WebSocket;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.io.FileExistsException;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Result;
import responses.*;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.CompilationStatus;
import utilities.enums.EntityType;
import exceptions.NotFoundException;
import utilities.logger.Logger;
import utilities.model.UnderProject;
import utilities.model.VersionModel;
import utilities.models_update_echo.EchoHandler;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.input.*;
import utilities.swagger.output.Swagger_C_Program_Version;
import utilities.swagger.output.Swagger_Short_Reference;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.*;
import java.net.ConnectException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

@Entity
@ApiModel( value = "CProgramVersion", description = "Model of CProgramVersion")
@Table(name="CProgramVersion")
public class Model_CProgramVersion extends VersionModel implements Permissible, UnderProject {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_CProgramVersion.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                                                   public Model_CProgram c_program;
    @JsonIgnore @OneToOne(mappedBy="version", cascade = CascadeType.ALL)                                             public Model_Compilation compilation; // TODO dá se cachovat

    @JsonIgnore @OneToMany(mappedBy="actual_c_program_version", fetch = FetchType.LAZY)                              public List<Model_Hardware> c_program_version_boards  = new ArrayList<>(); // Používám pro zachycení, která verze C_programu na desce běží
    @JsonIgnore @OneToMany(mappedBy="actual_backup_c_program_version", fetch = FetchType.LAZY)                       public List<Model_Hardware>  c_program_version_backup_boards  = new ArrayList<>(); // Nikdy z této ztrany nepoužívat!
    @JsonIgnore @OneToMany(mappedBy="c_program_version_for_update",cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_HardwareUpdate> updates = new ArrayList<>();

    @OneToOne @JsonIgnore  public Model_CProgram default_program;

/* JSON PROPERTY VALUES -------------------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public CompilationStatus status(){
        return compilation != null ? compilation.status : CompilationStatus.UNDEFINED;
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public String compilation_version(){
        return  compilation != null ? compilation.firmware_version_lib : CompilationStatus.UNDEFINED.name();
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true, value = "Value can be empty, Server cannot guarantee that.")
    public String virtual_input_output(){
        return compilation != null ? compilation.virtual_input_output : null;
    }

    @JsonProperty @ApiModelProperty(required = false, readOnly = true, value = "Link for download file in Binary (Not in Base64). Its ready to manual Upload. Only if \"status\" == \"SUCCESS\"")
    public String download_link_bin_file() {
        try {
            if (status() == CompilationStatus.SUCCESS) {
                return compilation.file_path();
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
    public Model_CProgram get_c_program() {
        return isLoaded("c_program") ? c_program : Model_CProgram.find.query().where().eq("versions.id", id).findOne();
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return this.get_c_program().getProject();
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save :: Creating new Object");
        super.save();

        // Add to Cache

        Model_CProgram program = get_c_program();

        if(get_c_program() != null) {
            System.out.println("Add To CProgram by get_c_program()");
            program.getVersionsId();
            program.idCache().add(this.getClass(), id);
            program.sort_Model_Model_CProgramVersion_ids();
        }

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_CProgram.class, program.getProjectId(), program.id))).start();
    }

    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}",  this.id);
        super.update();

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_CProgram.class, get_c_program().getProjectId(), get_c_program_id()))).start();

    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete::Delete object Id: {}",  this.id);

        super.delete();

        get_c_program().idCache().remove(this.getClass(), id);

        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Widget.class, get_c_program().getProjectId(), get_c_program_id()))).start();

        return false;
    }

/* Services --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void compile_program_thread(String library_compilation_version) {

        Model_CProgramVersion version = this;

        if (this.compilation == null) {

            Model_Compilation cCompilation = new Model_Compilation();
            cCompilation.status = CompilationStatus.IN_PROGRESS;
            cCompilation.firmware_version_lib = library_compilation_version;
            cCompilation.version = this;
            cCompilation.save();

            this.compilation = cCompilation;
            this.update();
        }

        new Thread(version::compile_program_procedure).start();
    }

    @JsonIgnore
    public Result compile_program_procedure() {
        try {

            Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("c_programs.id", get_c_program_id()).findOne();
            if (hardwareType == null) {
                logger.internalServerError(new Exception("compile_program_procedure:: HardwareType not found! Not found way how to compile version."));
                return _BaseController.notFound(Model_HardwareType.class);
            }

            // Něco se kriticky posralo a soubor nebyl vytvořen, kde byl pozanmenán tag kompilace
            if (this.compilation == null) {

                logger.internalServerError(new Exception("compile_program_procedure:: this.compilation == null"));

                Model_Compilation cCompilation = new Model_Compilation();
                cCompilation.version = this;

                if (!hardwareType.cache_library_list.isEmpty()) {
                    cCompilation.firmware_version_lib = hardwareType.cache_library_list.get(hardwareType.cache_library_list.size()).tag_name;
                }

                cCompilation.save();

                this.compilation = cCompilation;
                this.update();
            }

            compilation.status = CompilationStatus.IN_PROGRESS;
            compilation.update();

            Model_Blob file_record = file;
            if (file_record == null) {

                logger.internalServerError(new Exception("File not found! Version is not compilable!"));

                compilation.status = CompilationStatus.FILE_NOT_FOUND;
                compilation.update();

                Result_BadRequest result = new Result_BadRequest();
                return _BaseController.badRequest("Server has no content from version");
            }

            // Zpracování Json
            JsonNode json = Json.parse(file_record.downloadString());

            Swagger_C_Program_Version_Update code_file;

            try {

                code_file = formFromJsonWithValidation(Swagger_C_Program_Version_Update.class, json);

            } catch (Exception e) {
                logger.internalServerError(e);
                compilation.status = CompilationStatus.BROKEN_JSON;
                compilation.update();
                return _BaseController.internalServerError(e);
            }

            List<Swagger_Library_Record> library_files = new ArrayList<>();

            for (UUID lib_id : code_file.imported_libraries) {

                logger.trace("compile_C_Program_code:: Looking for library Version Id " + lib_id);
                try {

                    Model_LibraryVersion lib_version = Model_LibraryVersion.find.byId(lib_id);

                    if (lib_version.getLibrary() == null) {
                        logger.error("compile_C_Program_code:: library is null ");
                        return _BaseController.badRequest("Error getting libraries - some file is not a library");
                    }

                    if (lib_version.file != null) {

                        logger.trace("compile_program_procedure:: Library contains files");

                        JsonNode j = Json.parse(lib_version.file.downloadString());

                        Swagger_Library_File_Load lib_file;

                        try {

                            lib_file = formFromJsonWithValidation(Swagger_Library_File_Load.class, j);

                        } catch (Exception e) {
                            logger.internalServerError(e);
                            return _BaseController.internalServerError(e);
                        }

                        library_files.addAll(lib_file.files);

                    }
                } catch (NotFoundException exception) {
                    logger.error("compile_C_Program_code:: lib_version is null ");
                    return _BaseController.notFound("Error getting libraries - library version not found");
                }
            }

            ObjectNode includes = Json.newObject();

            for (Swagger_Library_Record file_lib : library_files) {
                if (file_lib.file_name.equals("README.md") || file_lib.file_name.equals("readme.md")) continue;
                includes.put(file_lib.file_name, file_lib.content);
            }

            if (code_file.files != null) {
                for (Swagger_Library_Record user_file : code_file.files) {
                    includes.put(user_file.file_name, user_file.content);
                }
            }

            // Kontroluji zda je nějaký kompilační cloud_compilation_server připojený
            if (Controller_WebSocket.compilers.isEmpty()) {

                logger.warn("compile_program_procedure:: Server is offline!!!");

                compilation.status = CompilationStatus.SERVER_OFFLINE;
                compilation.update();

                return _BaseController.externalServerOffline("Compilation cloud_compilation_server is offline! It will be compiled as soon as possible!");
            }

            WS_Message_Make_compilation compilation = Model_CompilationServer.make_Compilation(new WS_Message_Make_compilation().make_request(hardwareType, this.compilation.firmware_version_lib, this.id, code_file.main, includes));
            logger.trace("compile_program_procedure:: compilation is Done! - what i Have? {}", Json.toJson(compilation).toString());

            // Když obsahuje chyby - vrátím rovnou Becki - Toto je regulérní správná odpověd - chyby způsobil v c++ kodu uživatel
            if (!compilation.build_errors.isEmpty()) {

                logger.trace("compile_program_procedure:: compilation contains user Errors");

                this.compilation.status = CompilationStatus.FAILED;
                this.compilation.update();

                Result_CompilationListError result_compilationListError = new Result_CompilationListError();
                result_compilationListError.errors = compilation.build_errors;
                return _BaseController.ok(result_compilationListError);
            }

            // Toto už regulérní zpráva není  - něco se posralo!
            if (!compilation.status.equals("success") && compilation.error_message != null) {
                this.compilation.status = CompilationStatus.SERVER_ERROR;
                this.compilation.update();

                return _BaseController.badRequest(Json.toJson(compilation));
            }

            if (compilation.interface_code == null || compilation.build_url == null) {

                logger.internalServerError(new Exception("Missing fields ('interface_code' or 'build_url') in result from Code Server. Result: " + Json.toJson(compilation).toString()));

                this.compilation.status = CompilationStatus.BROKEN_JSON;
                this.compilation.update();

                return _BaseController.badRequest("Json code is broken - contact tech support!");
            }

            if (compilation.error_message != null || !compilation.status.equals("success")) {

                logger.internalServerError(new Exception("Error is empty, but status is not 'success' in result from Code Server. Result: " + Json.toJson(compilation).toString()));

                this.compilation.status = CompilationStatus.SERVER_ERROR;
                this.compilation.update();

                return _BaseController.externalServerError();
            }

            if (compilation.status.equals("success")) {

                logger.trace("compile_program_procedure:: compilation was successful");

                try {

                    logger.trace("compile_program_procedure:: try to download file");

                    WSClient ws = Server.injector.getInstance(WSClient.class);
                    CompletionStage<? extends WSResponse> responsePromise = ws.url(compilation.build_url)
                            .setContentType("undefined")
                            .setRequestTimeout(Duration.ofMillis(7500))
                            .get();

                    byte[] body = responsePromise.toCompletableFuture().get().asByteArray();

                    //(response -> body = response.asByteArray())
                    //      get().asByteArray();

                    if (body == null || body.length == 0) {
                        throw new FileExistsException("Body length is 0");
                    }

                    logger.trace("compile_program_procedure:: Body is ok - uploading to Azure");

                    // Daný soubor potřebuji dostat na Azure a Propojit s verzí
                    this.compilation.blob = Model_Blob.upload(body, "firmware.bin", this.compilation.get_path());

                    logger.trace("compile_program_procedure:: Body is ok - uploading to Azure was successful");
                    this.compilation.status = CompilationStatus.SUCCESS;
                    this.compilation.build_url = compilation.build_url;
                    this.compilation.firmware_build_id = compilation.build_id_in_firmware;
                    this.compilation.virtual_input_output = compilation.interface_code;
                    this.compilation.firmware_build_datetime = new Date();
                    this.compilation.update();

                    return _BaseController.ok();

                } catch (StorageException e) {

                    logger.internalServerError(new Exception("StorageException" + compilation.build_url, e));
                    this.compilation.status = CompilationStatus.SERVER_ERROR;
                    this.compilation.update();

                    return _BaseController.externalServerError();

                } catch (ConnectException e) {

                    logger.internalServerError(new Exception("Compilation Server is probably offline on URL: " + compilation.build_url, e));
                    this.compilation.status = CompilationStatus.SUCCESS_DOWNLOAD_FAILED;
                    this.compilation.update();

                    return _BaseController.externalServerError();

                } catch (FileExistsException e) {

                    logger.internalServerError(new Exception("Compilation body is empty.", e));

                    this.compilation.status = CompilationStatus.SUCCESS_DOWNLOAD_FAILED;
                    this.compilation.update();

                    return _BaseController.externalServerError();

                } catch (Exception e) {

                    logger.internalServerError(e);

                    this.compilation.status = CompilationStatus.SERVER_ERROR;
                    this.compilation.update();

                    return _BaseController.externalServerError();
                }
            }

            this.compilation.status = CompilationStatus.UNDEFINED;
            this.compilation.update();

            return _BaseController.externalServerError();

        } catch (NotFoundException e) {
            return _BaseController.notFound(e.getEntity());
        } catch (Exception e) {
            logger.internalServerError(e);
            return _BaseController.internalServerError(e);
        }
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public String get_path() {
        return  get_c_program().get_path() + "/version/" + this.id;
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
