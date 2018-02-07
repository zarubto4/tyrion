package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.storage.StorageException;
import controllers.BaseController;
import controllers.Controller_WebSocket;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.apache.commons.io.FileExistsException;
import org.ehcache.Cache;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import responses.*;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.Approval;
import utilities.enums.CompilationStatus;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.swagger.input.Swagger_C_Program_Version_Update;
import utilities.swagger.input.Swagger_Library_File_Load;
import utilities.swagger.input.Swagger_Library_Record;
import websocket.messages.compilator_with_tyrion.WS_Message_Make_compilation;

import javax.persistence.*;
import java.net.ConnectException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Entity
@ApiModel( value = "Version", description = "Model of Version")
@Table(name="Version")
public class Model_Version extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Version.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                       @JsonIgnore public boolean public_version;  // Používá se u Gridu, u C_programů atd..

    @ManyToOne(fetch = FetchType.LAZY) @JsonIgnore public Model_Person author;

    @JsonIgnore @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, fetch = FetchType.EAGER) public List<Model_Blob> files = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached private UUID cache_b_program_id;
    @JsonIgnore @Transient @Cached private UUID cache_c_program_id;
    @JsonIgnore @Transient @Cached private UUID cache_m_program_id;

    // Libraries ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @JsonIgnore @ManyToOne                                  public Model_Library library;

    @JsonIgnore @OneToMany(mappedBy = "example_library",
            cascade = CascadeType.ALL)                      public List<Model_CProgram> examples = new ArrayList<>();

    // C_Programs --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                                                   public Model_CProgram c_program;  // TODO - tady je hooodně návazností
    @JsonIgnore @OneToOne(mappedBy="version", cascade = CascadeType.ALL)                                             public Model_Compilation compilation;

    @JsonIgnore @OneToMany(mappedBy="actual_c_program_version", fetch = FetchType.LAZY)                              public List<Model_Hardware>  c_program_version_boards  = new ArrayList<>(); // Používám pro zachycení, která verze C_programu na desce běží
    @JsonIgnore @OneToMany(mappedBy="actual_backup_c_program_version", fetch = FetchType.LAZY)                       public List<Model_Hardware>  c_program_version_backup_boards  = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="c_program_version_for_update",cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_CProgramUpdatePlan> c_program_update_plans = new ArrayList<>();
                                                                                                   @JsonIgnore  public Approval approval_state; // Zda je program schválený veřejný program
                                                                                         @OneToOne @JsonIgnore  public Model_CProgram default_program;

    // B_Programs --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)   public Model_BProgram b_program;

                                                                        @JsonIgnore  public String additional_configuration;
    // TODO vazby na grid
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, mappedBy = "instance_versions") public List<Model_MProjectProgramSnapShot> b_program_version_snapshots = new ArrayList<>();    // Vazba kvůli puštěným B_programům

    // B_Program - Instance
    @JsonIgnore  @OneToMany(mappedBy="b_version", fetch = FetchType.LAZY) public List<Model_InstanceSnapshot> instances = new ArrayList<>();

    // M_Program --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_MProgram m_program;
                                    @JsonIgnore @Column(columnDefinition = "TEXT")  public String m_program_virtual_input_output;

    @JsonIgnore @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "m_program_version") public List<Model_MProgramInstanceParameter> m_program_instance_parameters = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    public Model_Person author() {
        if (this.author == null) {

            this.author = Model_Person.find.query().where().eq("version_objects.id", this.id).findOne();
            if (this.author == null) return null;
        }
        return this.author;
    }

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_BProgram get_b_program() {

        if (cache_b_program_id == null) {
            Model_BProgram bProgram = Model_BProgram.find.query().where().eq("version_objects.id", id).select("id").findOne();
            if (bProgram == null) return null;
            cache_b_program_id = bProgram.id;
        }
        if (cache_b_program_id == null) return null;
        return Model_BProgram.getById(cache_b_program_id);
    }

    @JsonIgnore
    public Model_CProgram get_c_program() {

        if (cache_c_program_id == null) {
            Model_CProgram cProgram = Model_CProgram.find.query().where().eq("version_objects.id", id).select("id").findOne();
            cache_c_program_id = cProgram.id;
        }

        if (cache_c_program_id == null) {
            return null;
        }

        return Model_CProgram.getById(cache_c_program_id);
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void compile_program_thread(String library_compilation_version) {

        Model_Version version = this;

        if (this.compilation == null) {

            Model_Compilation cCompilation = new Model_Compilation();
            cCompilation.status = CompilationStatus.IN_PROGRESS;
            cCompilation.firmware_version_lib = library_compilation_version;
            cCompilation.version = this;
            cCompilation.save();

            this.compilation = cCompilation;
            this.update();
        }

        Thread compile_that = new Thread() {

            @Override
            public void run() {
                try {
                    version.compile_program_procedure();
                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        };

        compile_that.start();
    }

    @JsonIgnore
    public Response_Interface compile_program_procedure() {

        Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.query().where().eq("c_programs.id", this.get_c_program().id).findOne();
        if (typeOfBoard == null) {

            logger.internalServerError(new Exception("compile_program_procedure:: Type_of_Board not found! Not found way how to compile version."));

            Result_BadRequest result = new Result_BadRequest();
            result.message = "Version is not version of C_Program";
            return result;
        }

        // Něco se kriticky posralo a soubor nebyl vytvořen, kde byl pozanmenán tag kompilace
        if (this.compilation == null) {

            logger.internalServerError(new Exception("compile_program_procedure:: this.compilation == null"));

            Model_Compilation cCompilation = new Model_Compilation();
            cCompilation.version = this;

            if (!typeOfBoard.cache_library_list.isEmpty()) {
                cCompilation.firmware_version_lib = typeOfBoard.cache_library_list.get(typeOfBoard.cache_library_list.size()).tag_name;
            }

            cCompilation.save();

            this.compilation = cCompilation;
            this.update();
        }

        compilation.status = CompilationStatus.IN_PROGRESS;
        compilation.update();

        Model_Blob file_record = Model_Blob.find.query().where().eq("file_name", "code.json").eq("version.id", id).findOne();
        if (file_record == null) {

            logger.internalServerError(new Exception("File not found! Version is not compilable!"));

            compilation.status = CompilationStatus.FILE_NOT_FOUND;
            compilation.update();

            Result_BadRequest result = new Result_BadRequest();
            result.message = "Server has no content from version";
            return result;
        }

        // Zpracování Json
        JsonNode json = Json.parse( file_record.get_fileRecord_from_Azure_inString() );

        Swagger_C_Program_Version_Update code_file;

        try {

            code_file = Json.fromJson(json, Swagger_C_Program_Version_Update.class);

        } catch (Exception e) {
            logger.internalServerError(e);
            compilation.status = CompilationStatus.BROKEN_JSON;
            compilation.update();
            Result_BadRequest result = new Result_BadRequest();
            result.message = "Json code is broken - contact tech support!";
            return result;
        }

        List<Swagger_Library_Record> library_files = new ArrayList<>();

        for (String lib_id : code_file.imported_libraries) {

            logger.trace("compile_C_Program_code:: Looking for library Version Id " + lib_id);
            Model_Version lib_version = Model_Version.getById(lib_id);

            if (lib_version == null) {
                logger.error("compile_C_Program_code:: lib_version is null ");
                Result_BadRequest result = new Result_BadRequest();
                result.message = "Error getting libraries - library version not found";
                return result;
            }

            if (lib_version.library == null) {
                logger.error("compile_C_Program_code:: library is null ");
                Result_BadRequest result = new Result_BadRequest();
                result.message = "Error getting libraries - some file is not a library";
                return result;
            }

            if (!lib_version.files.isEmpty()) {

                logger.trace("compile_program_procedure:: Library contains files");

                for (Model_Blob f : lib_version.files) {

                    JsonNode j = Json.parse(f.get_fileRecord_from_Azure_inString());

                    Swagger_Library_File_Load lib_file;

                    try {

                        lib_file = Json.fromJson(j, Swagger_Library_File_Load.class);

                    } catch (Exception e) {
                        logger.internalServerError(e);
                        Result_BadRequest result = new Result_BadRequest();
                        result.message = "Error importing libraries";
                        return result;
                    }

                    library_files.addAll(lib_file.files);
                }
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

            Result_ServerOffline result = new Result_ServerOffline("server was offline");
            result.message = "Compilation cloud_compilation_server is offline! It will be compiled as soon as possible!";
            return result;
        }

        WS_Message_Make_compilation compilation = Model_CompilationServer.make_Compilation( new WS_Message_Make_compilation().make_request(typeOfBoard, this.compilation.firmware_version_lib, this.id, code_file.main, includes   ));

        // Když obsahuje chyby - vrátím rovnou Becki - Toto je regulérní správná odpověd - chyby způsobil v c++ kodu uživatel
        if (!compilation.build_errors.isEmpty()) {

            logger.trace("compile_program_procedure:: compilation contains user Errors");

            this.compilation.status = CompilationStatus.FAILED;
            this.compilation.update();

            Result_CompilationListError result_compilationListError = new Result_CompilationListError();
            result_compilationListError.errors = compilation.build_errors;
            return result_compilationListError;
        }

        // Toto už regulérní zpráva není  - něco se posralo!
        if (!compilation.status.equals("success") && compilation.error_message != null) {
            this.compilation.status = CompilationStatus.SERVER_ERROR;
            this.compilation.update();

            Result_BadRequest result = new Result_BadRequest();
            result.message = "Error on Server Side";
            return result;
        }

        if (compilation.interface_code == null || compilation.build_url == null) {

            logger.internalServerError(new Exception("Missing fields ('interface_code' or 'build_url') in result from Code Server. Result: " + Json.toJson(compilation).toString()));

            this.compilation.status = CompilationStatus.BROKEN_JSON;
            this.compilation.update();

            Result_BadRequest result = new Result_BadRequest();
            result.message = "Json code is broken - contact tech support!";
            return result;
        }

        if (compilation.error_message != null || !compilation.status.equals("success")) {

            logger.internalServerError(new Exception("Error is empty, but status is not 'success' in result from Code Server. Result: " + Json.toJson(compilation).toString()));

            this.compilation.status = CompilationStatus.SERVER_ERROR;
            this.compilation.update();

            Result_ExternalServerSideError result = new Result_ExternalServerSideError();
            result.message = "Server side Error!";
            return result;
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
                this.compilation.blob = Model_Blob.create_Binary_file(this.compilation.get_path(), body, "firmware.bin");

                logger.trace("compile_program_procedure:: Body is ok - uploading to Azure was successful");
                this.compilation.status = CompilationStatus.SUCCESS;
                this.compilation.build_url = compilation.build_url;
                this.compilation.firmware_build_id = compilation.build_id;
                this.compilation.virtual_input_output = compilation.interface_code;
                this.compilation.firmware_build_datetime = new Date();
                this.compilation.update();

                return new Result_Ok();

             //   return (ObjectNode) Json.toJson(new Swagger_Compilation_Ok());

            } catch (StorageException e) {

                logger.internalServerError(new Exception("StorageException" + compilation.build_url, e));
                this.compilation.status = CompilationStatus.SERVER_ERROR;
                this.compilation.update();

                Result_ExternalServerSideError result = new Result_ExternalServerSideError();
                result.message = "Server side Error!";
                return result;

            } catch (ConnectException e) {

                logger.internalServerError(new Exception("Compilation Server is probably offline on URL: " + compilation.build_url, e));
                this.compilation.status = CompilationStatus.SUCCESS_DOWNLOAD_FAILED;
                this.compilation.update();

                Result_ExternalServerSideError result = new Result_ExternalServerSideError();
                result.message = "Server side Error!";
                return result;

            } catch (FileExistsException e) {

                logger.internalServerError(new Exception("Compilation body is empty.", e));

                this.compilation.status = CompilationStatus.SUCCESS_DOWNLOAD_FAILED;
                this.compilation.update();

                Result_ExternalServerSideError result = new Result_ExternalServerSideError();
                result.message = "Server side Error!";
                return result;

            } catch (Exception e) {

                logger.internalServerError(e);

                this.compilation.status = CompilationStatus.SERVER_ERROR;
                this.compilation.update();

                Result_ExternalServerSideError result = new Result_ExternalServerSideError();
                result.message = "Server side Error!";
                return result;
            }
        }

        this.compilation.status = CompilationStatus.UNDEFINED;
        this.compilation.update();

        Result_ExternalServerSideError result = new Result_ExternalServerSideError();
        result.message = "Server side Error!";
        return result;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public String blob_version_link;

    @JsonIgnore @Transient public String get_path() {
        return  blob_version_link;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        this.blob_version_link = "/versions/" + UUID.randomUUID().toString();

        try {
            if (this.author == null) this.author = BaseController.person();
        } catch (Exception e) {
            // this.author = null;
        }


        super.save();

        if (library != null) {
            library.cache_version_ids.add(0, id);
        }

        if (c_program != null) {
            c_program.cache_version_ids.add(0, id);
        }

        if (get_b_program() != null) {
            get_b_program().cache_version_ids.add(0, id);
        }

        if (m_program != null) {
            m_program.cache_version_ids.add(0, id);
        }

        cache.put(id, this);
    }

    @JsonIgnore @Transient @Override
    public void update() {

        // TODO informace o změně směr Becki!
        super.update();

        cache_refresh();
    }

    @JsonIgnore @Override
    public boolean delete() {

        if (get_c_program() != null) {
            get_c_program().cache_version_ids.remove(id);
        }

        if (get_b_program() != null) {
            get_b_program().cache_version_ids.remove(id);
        }

        if (m_program != null) {
            m_program.cache_version_ids.remove(id);
        }

        this.deleted = true;
        super.update();
        cache_refresh();
        return false;
    }

    @JsonIgnore @Transient
    public Model_Version cache_refresh() {
        return getById(id);
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have \"Object\".read_permission = true, you can read / get version on this Object - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have \"Object\".update_permission = true, you can create / update on this Object - Or you need static/dynamic permission key";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_Version.class, timeToIdle = 600, maxElements = 300)
    public static Cache<UUID, Model_Version> cache;

    public static Model_Version getById(String id) {
    return getById(UUID.fromString(id));
    }

    public static Model_Version getById(UUID id) {

        Model_Version version = cache.get(id);
        if (version == null) {

            version = Model_Version.find.byId(id);
            if (version == null) return null;

            cache.put(id, version);
        }

        return version;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Version> find = new Finder<>(Model_Version.class);
}