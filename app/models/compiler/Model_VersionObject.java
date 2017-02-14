package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.notification.Model_Notification;
import models.person.Model_Person;
import models.project.b_program.Model_BPair;
import models.project.b_program.Model_BProgram;
import models.project.b_program.Model_BProgramHwGroup;
import models.project.b_program.instnace.Model_HomerInstanceRecord;
import models.project.c_program.Model_CCompilation;
import models.project.c_program.Model_CProgram;
import models.project.c_program.actualization.Model_CProgramUpdatePlan;
import models.project.m_program.Model_MProgram;
import models.project.m_program.Model_MProjectProgramSnapShot;
import org.apache.commons.io.FileExistsException;
import play.api.Play;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.enums.Approval_state;
import utilities.enums.Compile_Status;
import utilities.enums.Notification_importance;
import utilities.enums.Notification_level;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_New;
import utilities.swagger.documentationClass.Swagger_C_Program_Version_Update;
import utilities.swagger.documentationClass.Swagger_ImportLibrary_Version_New;
import utilities.swagger.documentationClass.Swagger_Library_File_Load;
import utilities.swagger.outboundClass.*;
import utilities.web_socket.message_objects.compilator_tyrion.WS_Make_compilation;

import javax.persistence.*;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Version_Object",
        value = "Version_Object")
public class Model_VersionObject extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
                                                                @Id @ApiModelProperty(required = true) public String id;
                                                                    @ApiModelProperty(required = true) public String version_name;
                             @Column(columnDefinition = "TEXT")     @ApiModelProperty(required = true) public String version_description;

    @ManyToOne(fetch = FetchType.LAZY) @ApiModelProperty(required = false, value = "can be empty!")    public Model_Person author;


                                                                                           @JsonIgnore public boolean public_version;  // Používá se u Gridu, u C_programů atd..

                                                                                           // OBJEKT V KOŠI!! - SLOUŽÍ K ODSTRANĚNÍ Z POHLEDU UŽIVATELE - ALE NIKOLIV Z DATABÁZE!
                                                                                           @JsonIgnore public boolean removed_by_user; // Defaultně false - když true - tak se to nemá uživateli vracet!

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,
            value = "UNIX time in ms", example = "1466163478925")                                      public Date date_of_create;



    @JsonIgnore @OneToMany(mappedBy="version_object", cascade=CascadeType.ALL, fetch=FetchType.EAGER ) public List<Model_FileRecord> files = new ArrayList<>();

    // Libraries ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @JsonIgnore @ManyToOne                                  public Model_ImportLibrary library;
    @JsonIgnore @ManyToMany(mappedBy = "library_versions")  public List<Model_VersionObject> c_program_versions = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy = "example_library",
            cascade = CascadeType.ALL)                      public List<Model_CProgram> examples = new ArrayList<>();

    // C_Programs --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    @JsonIgnore @ManyToOne()                                                                                    public Model_CProgram c_program;
    @JsonIgnore @OneToOne(mappedBy="version_object", cascade = CascadeType.ALL)                                 public Model_CCompilation c_compilation;

    @JsonIgnore @ManyToMany @JoinTable(name = "model_c_program_library_version",
            joinColumns = @JoinColumn(name = "library_version_id"),
            inverseJoinColumns = @JoinColumn(name = "c_program_version_id"))                                    public List<Model_VersionObject> library_versions = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="actual_c_program_version")                                                 public List<Model_Board>  c_program_version_boards  = new ArrayList<>(); // Používám pro zachycení, která verze C_programu na desce běží
    @JsonIgnore @OneToMany(mappedBy="c_program_version_for_update",cascade=CascadeType.ALL)                     public List<Model_CProgramUpdatePlan> c_program_update_plans = new ArrayList<>();
                                                                                                   @JsonIgnore  public Approval_state approval_state; // Zda je program schválený veřejný program
                                                                                         @OneToOne @JsonIgnore  public Model_CProgram default_program;

    // B_Programs --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST)                           public Model_BProgram b_program;

    @JsonIgnore  @OneToMany(mappedBy="c_program_version", cascade=CascadeType.ALL)   public List<Model_BPair>   b_pairs_c_program = new ArrayList<>(); // Určeno pro aktualizaci

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)     public List<Model_BProgramHwGroup> b_program_hw_groups = new ArrayList<>();


    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, mappedBy = "instance_versions")         public List<Model_MProjectProgramSnapShot> b_program_version_snapshots = new ArrayList<>();    // Vazba kvůli puštěným B_programům
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, mappedBy = "version_objects_program")   public List<Model_MProjectProgramSnapShot> m_project_program_snapshots = new ArrayList<>();    // Vazba kvůli puštěným M_programům


        // B_Program - Instance
        @JsonIgnore  @OneToMany(mappedBy="version_object") public List<Model_HomerInstanceRecord> instance_record = new ArrayList<>();



    // M_Project --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) public Model_MProgram m_program;
                                    @JsonIgnore @Column(columnDefinition = "TEXT") public String m_program_virtual_input_output;
                                                                       @JsonIgnore public String qr_token;




/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_ImportLibrary_Version_Short_Detail get_short_import_library_version(){
        Swagger_ImportLibrary_Version_Short_Detail help = new Swagger_ImportLibrary_Version_Short_Detail();

        help.version_id = id;
        help.library_id = library.id;
        help.version_name = version_name;
        help.version_description = version_description;

        for (Model_CProgram cProgram : examples){
            help.examples.add(cProgram.get_example_short_detail());
        }

        for (Model_FileRecord file : this.files){

            JsonNode json = Json.parse(file.get_fileRecord_from_Azure_inString());

            Form<Swagger_Library_File_Load> form = Form.form(Swagger_Library_File_Load.class).bind(json);
            if(form.hasErrors()) return null;

            Swagger_Library_File_Load lib_form = form.get();

            for (Swagger_ImportLibrary_Version_New.Library_File library_file : lib_form.library_files)
            help.library_files.add(library_file);
        }

        return help;
    }

    @Transient @JsonIgnore public Swagger_C_Program_Version_Short_Detail get_short_c_program_version(){
        Swagger_C_Program_Version_Short_Detail help = new Swagger_C_Program_Version_Short_Detail();

        help.version_id = id;
        help.version_name = version_name;
        help.version_description = version_description;
        help.delete_permission = c_program.delete_permission();
        help.update_permission = c_program.update_permission();
        help.author = this.author.get_short_person();
        help.status = this.c_compilation.status;

        return help;
    }

    @Transient @JsonIgnore public Swagger_B_Program_Version_Short_Detail get_short_b_program_version(){
        Swagger_B_Program_Version_Short_Detail help = new Swagger_B_Program_Version_Short_Detail();

        help.version_id = id;
        help.version_name = version_name;
        help.version_description = version_description;
        help.delete_permission = b_program.delete_permission();
        help.update_permission = b_program.update_permission();
        help.author = this.author.get_short_person();

        return help;
    }

    @Transient @JsonIgnore public Swagger_M_Program_Version_Short_Detail get_short_m_program_version(){
        Swagger_M_Program_Version_Short_Detail help = new Swagger_M_Program_Version_Short_Detail();

        help.version_id = id;
        help.version_name = version_name;
        help.version_description = version_description;
        help.delete_permission = m_program.delete_permission();
        help.edit_permission = m_program.edit_permission();
        help.author = this.author.get_short_person();

        return help;
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient  public void compile_program_thread() {

        Model_VersionObject version = this;

        Thread compile_that = new Thread() {

            @Override
            public void run() {
                try {

                    version.compile_program_procedure();

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        };

        compile_that.start();

    }

    @JsonIgnore @Transient public ObjectNode compile_program_procedure(){


        Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.where().eq("c_programs.id", this.c_program.id).findUnique();
        if(typeOfBoard == null){

            logger.error("Version Object:: compile_program_procedure:: Type_of_Board not found!!! - Not found way how to compile that");

            ObjectNode result = Json.newObject();
            result.put("status", "error");
            result.put("error", "Version is not version of C_Program");
            result.put("error_code", 400);
            return result;
        }

        if(this.c_compilation == null) {

            Model_CCompilation cCompilation = new Model_CCompilation();
            cCompilation.version_object = this;
            cCompilation.save();

            this.c_compilation = cCompilation;
            this.update();
        }

        c_compilation.status = Compile_Status.compilation_in_progress;
        c_compilation.update();

        Model_FileRecord file = Model_FileRecord.find.where().eq("file_name", "code.json").eq("version_object.id", id).findUnique();
        if(file == null){

            logger.error("Version Object:: compile_program_procedure:: File not found!!! - Version is not compilable!");

            c_compilation.status = Compile_Status.file_with_code_not_found;
            c_compilation.update();

            ObjectNode result = Json.newObject();
            result.put("status", "error");
            result.put("error", "Server has no content from version");
            result.put("error_code", 400);
            return result;
        }

        // Zpracování Json
        JsonNode json = Json.parse( file.get_fileRecord_from_Azure_inString() );

        Form<Swagger_C_Program_Version_Update> form = Form.form(Swagger_C_Program_Version_Update.class).bind(json);
        if(form.hasErrors()){

            logger.error("Version Object:: compile_program_procedure:: File found but json is not parsable!!! - Version!");
            c_compilation.status = Compile_Status.json_code_is_broken;
            c_compilation.update();

            ObjectNode result = Json.newObject();
            result.put("status", "error");
            result.put("error", "Json code is broken - contact tech support!");
            result.put("error_code", 400);
            return result;
        }
        Swagger_C_Program_Version_Update code_file = form.get();


        List<Swagger_C_Program_Version_New.Library_File> library_files = new ArrayList<>();

        for (String lib_id : code_file.library_files) {

            Model_VersionObject lib_version = Model_VersionObject.find.byId(lib_id);
            if (lib_version == null){

                ObjectNode result = Json.newObject();
                result.put("status", "error");
                result.put("error", "Error getting libraries - library version not found");
                result.put("error_code", 400);
                return result;
            }
            if (lib_version.library == null){

                ObjectNode result = Json.newObject();
                result.put("status", "error");
                result.put("error", "Error getting libraries - some file is not a library");
                result.put("error_code", 400);
                return result;
            }

            if (!lib_version.files.isEmpty()){
                for (Model_FileRecord f : lib_version.files) {

                    JsonNode j = Json.parse(f.get_fileRecord_from_Azure_inString());

                    Form<Swagger_C_Program_Version_New.Library_File> lib_form = Form.form(Swagger_C_Program_Version_New.Library_File.class).bind(j);
                    if (lib_form.hasErrors()){

                        ObjectNode result = Json.newObject();
                        result.put("status", "error");
                        result.put("error", "Error importing libraries");
                        result.put("error_code", 400);
                        return result;
                    }

                    Swagger_C_Program_Version_New.Library_File lib_file = lib_form.get();

                    for (Swagger_C_Program_Version_Update.User_File user_file : code_file.user_files){

                        if (lib_file.file_name.equals(user_file.file_name))break;
                        if (!library_files.contains(lib_file)) library_files.add(lib_file);

                    }
                }
            }
        }

        ObjectNode includes = Json.newObject();

        for(Swagger_C_Program_Version_New.Library_File file_lib : library_files){
            includes.put(file_lib.file_name , file_lib.content);
        }

        if(code_file.user_files != null)
            for(Swagger_C_Program_Version_Update.User_File user_file : code_file.user_files){
                includes.put(user_file.file_name , user_file.code);
            }





        // Kontroluji zda je nějaký kompilační cloud_compilation_server připojený
        if (!Model_CompilationServer.is_online()) {

            logger.warn("Version Object:: compile_program_procedure:: Server is offline!!!");

            c_compilation.status = Compile_Status.server_was_offline;
            c_compilation.update();

            ObjectNode result = Json.newObject();
            result.put("status", "error");
            result.put("error", "Compilation cloud_compilation_server is offline! It will be compiled as soon as possible!");
            result.put("error_code", 477);
            return result;
        }



        WS_Make_compilation compilation = Model_CompilationServer.make_Compilation( new WS_Make_compilation().make_request( typeOfBoard ,this.id, code_file.main, includes   ));

        // Controller_Notification.successful_compilation(Controller_Security.getPerson(), this); TODO Notifikace

        // Když obsahuje chyby - vrátím rovnou Becki
        if(!compilation.buildErrors.isEmpty()) {

            logger.debug("Version Object:: compile_program_procedure:: compilation contains user Errors");

            c_compilation.status = Compile_Status.compiled_with_code_errors;
            c_compilation.update();

            return (ObjectNode) Json.toJson( compilation.buildErrors );
        }

        if(compilation.interface_code == null || compilation.buildUrl == null){

            logger.error("Version Object:: compile_program_procedure:: Json Result from Compilation server has not required labels!");

            c_compilation.status = Compile_Status.json_code_is_broken;
            c_compilation.update();

            ObjectNode result = Json.newObject();
            result.put("status", "error");
            result.put("error", "Json code is broken - contact tech support!");
            result.put("error_code", 400);
            return result;
        }

        if(compilation.error != null || !compilation.status.equals("success")){

            logger.error("Version Object:: compile_program_procedure:: Json Result from Compilation server has not required labels!");


            c_compilation.status = Compile_Status.compilation_server_error;
            c_compilation.update();

            ObjectNode result = Json.newObject();
            result.put("status", "error");
            result.put("error", "Server side Error");
            result.put("error_code", 400);
            return result;
        }

        if(compilation.status.equals("success")) {

            logger.debug("Version Object:: compile_program_procedure:: compilation was successfull");

            try {

                logger.debug("Version Object:: compile_program_procedure:: try to download file");


                WSClient ws = Play.current().injector().instanceOf(WSClient.class);
                F.Promise<WSResponse> responsePromise = ws.url(compilation.buildUrl)
                        .setContentType("undefined")
                        .setRequestTimeout(7500)
                        .get();


                byte[] body = responsePromise.get(7500).asByteArray();

                if (body == null || body.length == 0){
                    throw new FileExistsException();
                }

                logger.debug("Version Object:: compile_program_procedure:: Body is ok - uploading to Azure");

                // Daný soubor potřebuji dostat na Azure a Propojit s verzí
                c_compilation.bin_compilation_file = Model_FileRecord.create_Binary_file(c_compilation.get_path(), Model_FileRecord.get_encoded_binary_string_from_body(body), "compilation.bin");

                logger.debug("Version Object:: compile_program_procedure:: Body is ok - uploading to Azure was succesfull");
                c_compilation.status = Compile_Status.successfully_compiled_and_restored;
                c_compilation.c_comp_build_url = compilation.buildUrl;
                c_compilation.firmware_build_id = compilation.buildId;
                c_compilation.virtual_input_output = compilation.interface_code;
                c_compilation.date_of_create = new Date();
                c_compilation.update();

                return (ObjectNode) Json.toJson(new Swagger_Compilation_Ok());

            }catch (ConnectException e){

                logger.error("Compilation Server is probably offline on URL:: " + compilation.buildUrl );
                c_compilation.status = Compile_Status.successfully_compiled_not_restored;
                c_compilation.update();

                ObjectNode result = Json.newObject();
                result.put("status", "error");
                result.put("error", "Server side Error");
                result.put("error_code", 400);
                return result;


            }catch (FileExistsException e){

                logger.debug("Version Object:: compile_program_procedure:: FileExistsException - Body is empty");

                c_compilation.status = Compile_Status.successfully_compiled_not_restored;
                c_compilation.update();

                ObjectNode result = Json.newObject();
                result.put("status", "error");
                result.put("error", "Server side Error");
                result.put("error_code", 400);
                return result;


            } catch (Exception e) {

                e.printStackTrace();

                c_compilation.status = Compile_Status.compilation_server_error;
                c_compilation.update();

                ObjectNode result = Json.newObject();
                result.put("status", "error");
                result.put("error", "Server side Error");
                result.put("error_code", 400);
                return result;
            }

        }

        c_compilation.status = Compile_Status.undefined;
        c_compilation.update();

        ObjectNode result = Json.newObject();
        result.put("status", "error");
        result.put("error", "Server side Error");
        result.put("error_code", 400);
        return result;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_compilation_start(){

        new Model_Notification(Notification_importance.low, Notification_level.info)
                .setText("Server starts compilation of Version ")
                .setObject(Swagger_B_Program_Version.class, this.id, this.version_name + ".", this.c_program.project_id(), "black", false, true, false, false)
                .send(Controller_Security.getPerson());
    }

    @JsonIgnore @Transient
    public void notification_compilation_success(){

        new Model_Notification(Notification_importance.low, Notification_level.success)
                .setText("Compilation of Version ")
                .setObject(Swagger_B_Program_Version.class, this.id, this.version_name, this.c_program.project_id(), "black", false, true, false, false)
                .setText("was successful.")
                .send(Controller_Security.getPerson());
    }

    @JsonIgnore @Transient
    public void notification_compilation_unsuccessful_warn(String reason){

        new Model_Notification(Notification_importance.normal,  Notification_level.warning)
                .setText("Compilation of Version")
                .setObject(Swagger_B_Program_Version.class, this.id, this.version_name, this.c_program.project_id(), "black", false, true, false, false)
                .setText("was unsuccessful, for reason:")
                .setText(reason, "black", true, false, false)
                .send(Controller_Security.getPerson());
    }

    @JsonIgnore @Transient
    public void notification_compilation_unsuccessful_error(String result){

        new Model_Notification(Notification_importance.normal, Notification_level.error)
                .setText( "Compilation of Version")
                .setObject(Swagger_B_Program_Version.class, this.id, this.version_name, this.c_program.project_id(), "black", false, true, false, false)
                .setText("with critical Error:")
                .setText(result, "black", true, false, false)
                .send(Controller_Security.getPerson());
    }

    @JsonIgnore @Transient
    public void notification_new_actualization_request_on_version(){

        new Model_Notification(Notification_importance.low, Notification_level.info)
                .setText("New actualization task was added to Task Queue on Version ")
                .setObject(Swagger_C_Program_Version.class, this.id, this.version_name, this.c_program.project_id() )
                .setText(" from Program ")
                .setObject(Model_CProgram.class, this.c_program.id, this.c_program.name, this.c_program.project_id())
                .send(Controller_Security.getPerson());
    }

/* BLOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public String blob_version_link;

    @JsonIgnore @Override public void save() {

        while(true){ // I need Unique Value

            this.id = UUID.randomUUID().toString();
            this.blob_version_link = "/versions/" + this.id;
            if (Model_VersionObject.find.byId(this.id) == null) break;
        }

        try {
            if(this.author == null)
            this.author = Controller_Security.getPerson();
        }catch (Exception e){
            // this.author = null;
        }

        super.save();
    }

    @JsonIgnore @Transient
    public String get_path(){
        return  blob_version_link;
    }


/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have \"Object\".read_permission = true, you can read / get version on this Object - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have \"Object\".update_permission = true, you can create / update on this Object - Or you need static/dynamic permission key";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_VersionObject> find = new Finder<>(Model_VersionObject.class);


}
