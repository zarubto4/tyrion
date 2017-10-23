package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.io.FileExistsException;
import org.ehcache.Cache;
import play.api.Play;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.enums.Enum_Approval_state;
import utilities.enums.Enum_Compile_status;
import utilities.logger.Class_Logger;
import utilities.response.response_objects.*;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_Update;
import utilities.swagger.documentationClass.Swagger_Library_Record;
import utilities.swagger.documentationClass.Swagger_Library_File_Load;
import utilities.swagger.outboundClass.*;
import web_socket.message_objects.compilator_with_tyrion.WS_Message_Make_compilation;

import javax.persistence.*;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "Version_Object", description = "Model of Version_Object")
@Table(name="VersionObject")
public class Model_VersionObject extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_VersionObject.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
                                               @Id public String id;
                                                   public String version_name;
                @Column(columnDefinition = "TEXT") public String version_description;

                                       @JsonIgnore public boolean public_version;  // Používá se u Gridu, u C_programů atd..

                                       @JsonIgnore public boolean removed_by_user; // Defaultně false - když true - tak se to nemá uživateli vracet!

                @ApiModelProperty(required = true,
                        dataType = "integer",
                        value = "UNIX time in ms",
                        example = "1466163478925") public Date date_of_create;

    @ManyToOne(fetch = FetchType.LAZY) @JsonIgnore public Model_Person author;

    @JsonIgnore @OneToMany(mappedBy="version_object", cascade=CascadeType.ALL, fetch=FetchType.EAGER) public List<Model_FileRecord> files = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList private String cache_b_program_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_c_program_id;
    @JsonIgnore @Transient @TyrionCachedList private String cache_m_program_id;

    // Libraries ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @JsonIgnore @ManyToOne                                  public Model_Library library;

    @JsonIgnore @OneToMany(mappedBy = "example_library",
            cascade = CascadeType.ALL)                      public List<Model_CProgram> examples = new ArrayList<>();

    // C_Programs --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                                                              public Model_CProgram c_program;
    @JsonIgnore @OneToOne(mappedBy="version_object", cascade = CascadeType.ALL)                                 public Model_CCompilation c_compilation;

    @JsonIgnore @OneToMany(mappedBy="actual_c_program_version", fetch = FetchType.LAZY)                              public List<Model_Board>  c_program_version_boards  = new ArrayList<>(); // Používám pro zachycení, která verze C_programu na desce běží
    @JsonIgnore @OneToMany(mappedBy="actual_backup_c_program_version", fetch = FetchType.LAZY)                       public List<Model_Board>  c_program_version_backup_boards  = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="c_program_version_for_update",cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_CProgramUpdatePlan> c_program_update_plans = new ArrayList<>();
                                                                                                   @JsonIgnore  public Enum_Approval_state approval_state; // Zda je program schválený veřejný program
                                                                                         @OneToOne @JsonIgnore  public Model_CProgram default_program;

    // B_Programs --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)   public Model_BProgram b_program;

    @JsonIgnore  @OneToMany(mappedBy="c_program_version", cascade=CascadeType.ALL)   public List<Model_BPair>   b_pairs_c_program = new ArrayList<>(); // Určeno pro aktualizaci

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)     public List<Model_BProgramHwGroup> b_program_hw_groups = new ArrayList<>();


    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, mappedBy = "instance_versions") public List<Model_MProjectProgramSnapShot> b_program_version_snapshots = new ArrayList<>();    // Vazba kvůli puštěným B_programům

    // B_Program - Instance
    @JsonIgnore  @OneToMany(mappedBy="version_object", fetch = FetchType.LAZY) public List<Model_HomerInstanceRecord> instance_record = new ArrayList<>();


    // M_Program --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_MProgram m_program;
                                    @JsonIgnore @Column(columnDefinition = "TEXT")  public String m_program_virtual_input_output;

    @JsonIgnore @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "m_program_version") public List<Model_MProgramInstanceParameter> m_program_instance_parameters = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty
    public Swagger_Person_Short_Detail author(){
        if (this.author == null) {

            this.author = Model_Person.find.where().eq("version_objects.id", this.id).findUnique();
            if (this.author == null) return null;
        }
        return this.author.get_short_person();
    }

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @JsonIgnore @TyrionCachedList
    public Model_BProgram get_b_program(){

        if(cache_b_program_id == null){
            Model_BProgram bProgram = Model_BProgram.find.where().eq("version_objects.id", id).select("id").findUnique();
            if(bProgram == null) return null;
            cache_b_program_id = bProgram.id;
        }

        return Model_BProgram.get_byId(cache_b_program_id);

    }

    @Transient @JsonIgnore public Swagger_Library_Version_Short_Detail  get_short_library_version(){
        try {

            Swagger_Library_Version_Short_Detail help = new Swagger_Library_Version_Short_Detail();

            help.version_id = id;
            help.version_name = version_name;
            help.version_description = version_description;
            help.delete_permission = library.delete_permission();
            help.update_permission = library.update_permission();
            help.author = this.author.get_short_person();

            if(approval_state != null){
                help.publish_status = approval_state;
                help.community_publishing_permission = library.community_publishing_permission();
            }

            return help;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @Transient @JsonIgnore public Swagger_C_Program_Version_Short_Detail get_short_c_program_version(){
        try {

            Swagger_C_Program_Version_Short_Detail help = new Swagger_C_Program_Version_Short_Detail();

            help.version_id = id;
            help.version_name = version_name;
            help.version_description = version_description;
            help.library_compilation_version = c_compilation.firmware_version_lib;
            help.delete_permission = c_program.delete_permission();
            help.update_permission = c_program.update_permission();
            help.author = this.author.get_short_person();

            // Compilation status
            if(this.c_compilation != null){
                help.status = this.c_compilation.status;
            }else {
                help.status = Enum_Compile_status.file_with_code_not_found;
            }

            if(approval_state != null){
                help.publish_status = approval_state;
                help.community_publishing_permission = c_program.community_publishing_permission();
            }

            // Main status
            if(this.default_program != null){
                help.main_mark = true;
            }


            return help;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @Transient @JsonIgnore public Swagger_B_Program_Version_Short_Detail get_short_b_program_version(){
       try {

           Swagger_B_Program_Version_Short_Detail help = new Swagger_B_Program_Version_Short_Detail();

           help.version_id = id;
           help.version_name = version_name;
           help.version_description = version_description;
           help.delete_permission = get_b_program().delete_permission();
           help.update_permission = get_b_program().update_permission();
           help.author = this.author.get_short_person();

           return help;

       }catch (Exception e){
           terminal_logger.internalServerError(e);
           return null;
       }
    }

    @Transient @JsonIgnore public Swagger_M_Program_Version_Short_Detail get_short_m_program_version(){
        try {

            Swagger_M_Program_Version_Short_Detail help = new Swagger_M_Program_Version_Short_Detail();

            help.version_id = id;
            help.version_name = version_name;
            help.version_description = version_description;
            help.delete_permission = m_program.delete_permission();
            help.edit_permission = m_program.edit_permission();
            help.author = this.author.get_short_person();

            return help;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient  public void compile_program_thread(String library_compilation_version) {

        Model_VersionObject version = this;

        if(this.c_compilation == null) {

            Model_CCompilation cCompilation = new Model_CCompilation();
            cCompilation.status = Enum_Compile_status.compilation_in_progress;
            cCompilation.firmware_version_lib = library_compilation_version;
            cCompilation.version_object = this;
            cCompilation.save();

            this.c_compilation = cCompilation;
            this.update();
        }

        Thread compile_that = new Thread() {

            @Override
            public void run() {
                try {
                    version.compile_program_procedure();
                }catch (Exception e){
                    terminal_logger.internalServerError(e);
                }
            }
        };

        compile_that.start();

    }

    @JsonIgnore @Transient public Response_Interface compile_program_procedure(){


        Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.where().eq("c_programs.id", this.c_program.id).findUnique();
        if(typeOfBoard == null){

            terminal_logger.internalServerError(new Exception("compile_program_procedure:: Type_of_Board not found! Not found way how to compile version."));

            Result_BadRequest result = new Result_BadRequest();
            result.message = "Version is not version of C_Program";
            return result;
        }

        // Něco se kriticky posralo a soubor nebyl vytvořen, kde byl pozanmenán tag kompilace
        if(this.c_compilation == null) {

            terminal_logger.internalServerError(new Exception("compile_program_procedure:: this.c_compilation == null"));

            Model_CCompilation cCompilation = new Model_CCompilation();
            cCompilation.version_object = this;


            if(!typeOfBoard.cache_library_list.isEmpty()) {
                cCompilation.firmware_version_lib = typeOfBoard.cache_library_list.get(typeOfBoard.cache_library_list.size()).tag_name;
            }

            cCompilation.save();


            this.c_compilation = cCompilation;
            this.update();
        }

        c_compilation.status = Enum_Compile_status.compilation_in_progress;
        c_compilation.update();

        Model_FileRecord file = Model_FileRecord.find.where().eq("file_name", "code.json").eq("version_object.id", id).findUnique();
        if(file == null){

            terminal_logger.internalServerError(new Exception("File not found! Version is not compilable!"));

            c_compilation.status = Enum_Compile_status.file_with_code_not_found;
            c_compilation.update();

            Result_BadRequest result = new Result_BadRequest();
            result.message = "Server has no content from version";
            return result;

        }

        // Zpracování Json
        JsonNode json = Json.parse( file.get_fileRecord_from_Azure_inString() );

        Form<Swagger_C_Program_Version_Update> form = Form.form(Swagger_C_Program_Version_Update.class).bind(json);
        if(form.hasErrors()){

            terminal_logger.internalServerError(new Exception("File was found but json is not parsable!"));
            c_compilation.status = Enum_Compile_status.json_code_is_broken;
            c_compilation.update();

            Result_BadRequest result = new Result_BadRequest();
            result.message = "Json code is broken - contact tech support!";
            return result;
        }

        Swagger_C_Program_Version_Update code_file = form.get();

        List<Swagger_Library_Record> library_files = new ArrayList<>();

        for (String lib_id : code_file.imported_libraries) {

            Model_VersionObject lib_version = Model_VersionObject.get_byId(lib_id);
            if (lib_version == null){

                Result_BadRequest result = new Result_BadRequest();
                result.message = "Error getting libraries - library version not found";
                return result;
            }

            if (lib_version.library == null){

                Result_BadRequest result = new Result_BadRequest();
                result.message = "Error getting libraries - some file is not a library";
                return result;
            }

            if (!lib_version.files.isEmpty()){
                for (Model_FileRecord f : lib_version.files) {

                    JsonNode j = Json.parse(f.get_fileRecord_from_Azure_inString());

                    Form<Swagger_Library_File_Load> lib_form = Form.form(Swagger_Library_File_Load.class).bind(j);
                    if (lib_form.hasErrors()){

                        Result_BadRequest result = new Result_BadRequest();
                        result.message = "Error importing libraries";
                        return result;
                    }
                    Swagger_Library_File_Load lib_help = lib_form.get();

                    for (Swagger_Library_Record lib_file : lib_help.files){
                        for (Swagger_Library_Record user_file : code_file.files){

                            if (!library_files.contains(lib_file)) library_files.add(lib_file);

                            if (lib_file.file_name.equals(user_file.file_name)){
                                if (library_files.contains(lib_file)) library_files.remove(lib_file);
                                break;
                            }
                        }
                    }
                }
            }
        }

        ObjectNode includes = Json.newObject();

        for(Swagger_Library_Record file_lib : library_files){
            includes.put(file_lib.file_name , file_lib.content);
        }

        if(code_file.files != null)
            for(Swagger_Library_Record user_file : code_file.files){
                includes.put(user_file.file_name , user_file.content);
            }


        // Kontroluji zda je nějaký kompilační cloud_compilation_server připojený
        if (!Model_CompilationServer.is_online()) {

            terminal_logger.warn("compile_program_procedure:: Server is offline!!!");

            c_compilation.status = Enum_Compile_status.server_was_offline;
            c_compilation.update();

            Result_ServerOffline result = new Result_ServerOffline();
            result.message = "Compilation cloud_compilation_server is offline! It will be compiled as soon as possible!";
            return result;
        }



        WS_Message_Make_compilation compilation = Model_CompilationServer.make_Compilation( new WS_Message_Make_compilation().make_request(typeOfBoard, c_compilation.firmware_version_lib, this.id, code_file.main, includes   ));


        // Když obsahuje chyby - vrátím rovnou Becki
        if(!compilation.build_errors.isEmpty()) {

            terminal_logger.trace("compile_program_procedure:: compilation contains user Errors");

            c_compilation.status = Enum_Compile_status.compiled_with_code_errors;
            c_compilation.update();

            Result_CompilationListError result_compilationListError = new Result_CompilationListError();
            result_compilationListError.errors = compilation.build_errors;
            return result_compilationListError;
        }

        if(compilation.interface_code == null || compilation.build_url == null){

            terminal_logger.internalServerError(new Exception("Missing fields ('interface_code' or 'build_url') in result from Code Server. Result: " + Json.toJson(compilation).toString()));

            c_compilation.status = Enum_Compile_status.json_code_is_broken;
            c_compilation.update();

            Result_BadRequest result = new Result_BadRequest();
            result.message = "Json code is broken - contact tech support!";
            return result;
        }

        if(compilation.error_message != null || !compilation.status.equals("success")){

            terminal_logger.internalServerError(new Exception("Error is empty, but status is not 'success' in result from Code Server. Result: " + Json.toJson(compilation).toString()));

            c_compilation.status = Enum_Compile_status.compilation_server_error;
            c_compilation.update();

            Result_ExternalServerSideError result = new Result_ExternalServerSideError();
            result.message = "Server side Error!";
            return result;

        }

        if(compilation.status.equals("success")) {

            terminal_logger.trace("compile_program_procedure:: compilation was successfull");

            try {

                terminal_logger.trace("compile_program_procedure:: try to download file");

                WSClient ws = Play.current().injector().instanceOf(WSClient.class);
                F.Promise<WSResponse> responsePromise = ws.url(compilation.build_url)
                        .setContentType("undefined")
                        .setRequestTimeout(7500)
                        .get();

                byte[] body = responsePromise.get(7500).asByteArray();

                if (body == null || body.length == 0){
                    throw new FileExistsException("Body length is 0");
                }

                terminal_logger.trace("compile_program_procedure:: Body is ok - uploading to Azure");

                // Daný soubor potřebuji dostat na Azure a Propojit s verzí
                c_compilation.bin_compilation_file = Model_FileRecord.create_Binary_file(c_compilation.get_path(), Model_FileRecord.get_encoded_binary_string_from_body(body), "compilation.bin");

                terminal_logger.trace("compile_program_procedure:: Body is ok - uploading to Azure was succesfull");
                c_compilation.status = Enum_Compile_status.successfully_compiled_and_restored;
                c_compilation.c_comp_build_url = compilation.build_url;
                c_compilation.firmware_build_id = compilation.build_id;
                c_compilation.virtual_input_output = compilation.interface_code;
                c_compilation.date_of_create = new Date();
                c_compilation.update();

                return new Result_Ok();

             //   return (ObjectNode) Json.toJson(new Swagger_Compilation_Ok());

            }catch (ConnectException e){

                terminal_logger.internalServerError(new Exception("Compilation Server is probably offline on URL: " + compilation.build_url, e));
                c_compilation.status = Enum_Compile_status.successfully_compiled_not_restored;
                c_compilation.update();

                Result_ExternalServerSideError result = new Result_ExternalServerSideError();
                result.message = "Server side Error!";
                return result;


            }catch (FileExistsException e){

                terminal_logger.internalServerError(new Exception("Compilation body is empty.", e));

                c_compilation.status = Enum_Compile_status.successfully_compiled_not_restored;
                c_compilation.update();

                Result_ExternalServerSideError result = new Result_ExternalServerSideError();
                result.message = "Server side Error!";
                return result;


            } catch (Exception e) {

                terminal_logger.internalServerError(e);

                c_compilation.status = Enum_Compile_status.compilation_server_error;
                c_compilation.update();

                Result_ExternalServerSideError result = new Result_ExternalServerSideError();
                result.message = "Server side Error!";
                return result;
            }
        }

        c_compilation.status = Enum_Compile_status.undefined;
        c_compilation.update();

        Result_ExternalServerSideError result = new Result_ExternalServerSideError();
        result.message = "Server side Error!";
        return result;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public String blob_version_link;

    @JsonIgnore @Transient public String get_path(){
        return  blob_version_link;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        this.id = UUID.randomUUID().toString();
        this.blob_version_link = "/versions/" + this.id;

        try {
            if(this.author == null) this.author = Controller_Security.get_person();
        }catch (Exception e){
            // this.author = null;
        }


        if(date_of_create == null) {
            date_of_create = new Date();
        }

        super.save();

        if(library != null){
            library.cache_list_version_objects_ids.add(0, id);
        }

        if(c_program != null){
            c_program.cache_list_version_objects_ids.add(0, id);
        }

        if(get_b_program() != null){
            get_b_program().cache_list_version_objects_ids.add(0, id);
        }

        if(m_program != null){
            m_program.cache_list_version_objects_ids.add(0, id);
        }

        cache.put(id, this);
    }

    @JsonIgnore @Transient @Override
    public void update(){

        // TODO informace o změně směr Becki!
        super.update();

        cache_refresh();
    }


    @JsonIgnore @Transient @Override
    public void delete(){

        removed_by_user = true;

        if(c_program != null){
            c_program.cache_list_version_objects_ids.remove(id);
        }

        if(get_b_program() != null){
            get_b_program().cache_list_version_objects_ids.remove(id);
        }

        if(m_program != null){
            m_program.cache_list_version_objects_ids.remove(id);
        }

        super.update();
    }

    @JsonIgnore @Transient
    public Model_VersionObject cache_refresh(){
        cache.remove(id);
        return get_byId(id);
    }


/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have \"Object\".read_permission = true, you can read / get version on this Object - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have \"Object\".update_permission = true, you can create / update on this Object - Or you need static/dynamic permission key";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_VersionObject.class.getSimpleName();

    public static Cache<String, Model_VersionObject> cache = null; // < ID, Model_VersionObject>

    @JsonIgnore
    public static Model_VersionObject get_byId(String id) {

        Model_VersionObject version = cache.get(id);
        if (version == null){

            version = Model_VersionObject.find.byId(id);
            if (version == null) return null;

            cache.put(id, version);
        }

        return version;
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String, Model_VersionObject> find = new Finder<>(Model_VersionObject.class);
}