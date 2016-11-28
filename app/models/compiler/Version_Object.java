package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.notification.Notification;
import models.person.Person;
import models.project.b_program.B_Pair;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Hw_Group;
import models.project.b_program.instnace.Homer_Instance_Record;
import models.project.c_program.C_Compilation;
import models.project.c_program.C_Program;
import models.project.c_program.actualization.C_Program_Update_Plan;
import models.project.m_program.M_Program;
import models.project.m_program.M_Project_Program_SnapShot;
import org.apache.commons.io.FileExistsException;
import play.api.Play;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.enums.Approval_state;
import utilities.enums.Notification_importance;
import utilities.enums.Notification_level;
import utilities.swagger.outboundClass.Swagger_B_Program_Version;
import utilities.swagger.outboundClass.Swagger_C_Program_Version;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Version_Object extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)  public String id;
                                                            @ApiModelProperty(required = true)  public String version_name;
                     @Column(columnDefinition = "TEXT")     @ApiModelProperty(required = true)  public String version_description;

    @ManyToOne(fetch = FetchType.LAZY) @ApiModelProperty(required = false, value = "can be empty!")  public Person author;


                                                @JsonIgnore @ApiModelProperty(required = true)  public boolean public_version;

    // OBJEKT V KOŠI!! - SLOUŽÍ K ODSTRANĚNÍ Z POHLEDU UŽIVATELE - ALE NIKOLIV Z DATABÁZE!
    public boolean removed_by_user; // Defaultně false - když true - tak se to nemá uživateli vracet!

    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
            example = "1466163478925")                                                          public Date date_of_create;



    @JsonIgnore @OneToMany(mappedBy="version_object", cascade=CascadeType.ALL, fetch = FetchType.EAGER ) public List<FileRecord> files = new ArrayList<>();

                                     @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST)     public LibraryGroup library_group;
                                     @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST)     public SingleLibrary single_library;


    // C_Programs --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    @JsonIgnore @ManyToOne()                                                                                    public C_Program c_program;
    @JsonIgnore @OneToOne(mappedBy="version_object", cascade = CascadeType.ALL)                                 public C_Compilation c_compilation;


    @JsonIgnore @OneToMany(mappedBy="actual_c_program_version")                                                 public List<Board>  c_program_version_boards  = new ArrayList<>(); // Používám pro zachycení, která verze C_programu na desce běží
    @JsonIgnore @OneToMany(mappedBy="c_program_version_for_update",cascade=CascadeType.ALL)                     public List<C_Program_Update_Plan> c_program_update_plans = new ArrayList<>();

    @JsonIgnore @OneToOne                                                                                       public C_Program default_version_program;    // Použito pro defaulntí program vázaný na TypeOfBoard hlavní verze určená k aktivitám - typu hardwaru a taktéž firmware, který se nahrává na devices
    @JsonIgnore @OneToMany(mappedBy="first_default_version_object",fetch = FetchType.LAZY) @OrderBy("id DESC")  public List<C_Program> first_version_of_c_programs = new ArrayList<>(); // Vazba na prnví verzi uživateli vytvořenými C_Programi - tak aby nebylo první verzi nutné kopírovat


                                                                                                   @JsonIgnore  public Approval_state approval_state; // Zda je program schválený veřejný program

    // B_Programs --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST)                           public B_Program      b_program;

    @JsonIgnore  @OneToMany(mappedBy="c_program_version", cascade=CascadeType.ALL)   public List<B_Pair>   b_pairs_c_program = new ArrayList<>(); // Určeno pro aktualizaci

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)     public List<B_Program_Hw_Group> b_program_hw_groups = new ArrayList<>();


    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, mappedBy = "instance_versions")         public List<M_Project_Program_SnapShot> b_program_version_snapshots = new ArrayList<>();    // Bazba kvůli puštěným B_programům
    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL, mappedBy = "version_objects_program")   public List<M_Project_Program_SnapShot> m_project_program_snapshots = new ArrayList<>();    // Bazba kvůli puštěným B_programům


        // B_Program - Instance
        @JsonIgnore  @OneToMany(mappedBy="version_object") public List<Homer_Instance_Record> instance_record = new ArrayList<>();



    // M_Project --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)    public M_Program m_program;
    @JsonIgnore  public String qr_token;


    @JsonIgnore @Override public void delete() {

        for (C_Program c_program : this.first_version_of_c_programs){

            c_program.first_default_version_object = null;
            c_program.update();
        }

        if (default_version_program != null) {
            this.default_version_program.default_main_version = null;
            this.default_version_program.update();
        }

        super.delete();
    }



/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_compilation_start(){

        new Notification(Notification_importance.low, Notification_level.info)
                .setText("Server starts compilation of Version ")
                .setObject(Swagger_B_Program_Version.class, this.id, this.version_name + ".", this.c_program.project_id(), "black", false, true, false, false)
                .send(SecurityController.getPerson());
    }

    @JsonIgnore @Transient
    public void notification_compilation_success(){

        new Notification(Notification_importance.low, Notification_level.success)
                .setText("Compilation of Version ")
                .setObject(Swagger_B_Program_Version.class, this.id, this.version_name, this.c_program.project_id(), "black", false, true, false, false)
                .setText("was successful.")
                .send(SecurityController.getPerson());
    }

    @JsonIgnore @Transient
    public void notification_compilation_unsuccessful_warn(String reason){

        new Notification(Notification_importance.normal,  Notification_level.warning)
                .setText("Compilation of Version")
                .setObject(Swagger_B_Program_Version.class, this.id, this.version_name, this.c_program.project_id(), "black", false, true, false, false)
                .setText("was unsuccessful, for reason:")
                .setText(reason, "black", true, false, false)
                .send(SecurityController.getPerson());
    }

    @JsonIgnore @Transient
    public void notification_compilation_unsuccessful_error(String result){

        new Notification(Notification_importance.normal, Notification_level.error)
                .setText( "Compilation of Version")
                .setObject(Swagger_B_Program_Version.class, this.id, this.version_name, this.c_program.project_id(), "black", false, true, false, false)
                .setText("with critical Error:")
                .setText(result, "black", true, false, false)
                .send(SecurityController.getPerson());
    }

    @JsonIgnore @Transient
    public void notification_new_actualization_request_on_version(){

        new Notification(Notification_importance.low, Notification_level.info)
                .setText("New actualization task was added to Task Queue on Version ")
                .setObject(Swagger_C_Program_Version.class, this.id, this.version_name, this.c_program.project_id() )
                .setText(" from Program ")
                .setObject(C_Program.class, this.c_program.id, this.c_program.name, this.c_program.project_id())
                .send(SecurityController.getPerson());
    }
/* JSON IGNORE DATA  ---------------------------------------------------------------------------------------------------*/
    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    @JsonIgnore @Transient  public void compile_program_thread() {

        Version_Object version = this;

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


        TypeOfBoard typeOfBoard = TypeOfBoard.find.where().eq("c_programs.id", this.c_program.id).findUnique();
        if(typeOfBoard == null){

            logger.error("Version Object:: compile_program_procedure:: Type_of_Board not found!!! - Not found way how to compile that");

            ObjectNode result = Json.newObject();
            result.put("status", "error");
            result.put("error", "Version is not version of C_Program");
            result.put("error_code", 400);
            return result;
        }

        if(this.c_compilation == null) {

            this.c_compilation = new C_Compilation();
            this.update();
        }

        c_compilation.status = Compile_Status.compilation_in_progress;
        c_compilation.save();

        FileRecord file = FileRecord.find.where().eq("file_name", "code.json").eq("version_object.id", id).findUnique();
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
        System.out.println("JSON:::::" + json.toString());
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


        // Vytvářím objekt, jež se zašle přes websocket ke kompilaci
        ObjectNode request = Json.newObject();
        request.put("messageType", "build");
        request.put("target", typeOfBoard.compiler_target_name);
        request.put("libVersion", "v0");
        request.put("versionId", this.id);
        request.put("code", code_file.main);
        request.set("includes", code_file.includes() == null ? Json.newObject() : code_file.includes() );


        // Kontroluji zda je nějaký kompilační cloud_compilation_server připojený
        if (!Cloud_Compilation_Server.is_online()) {

            logger.warn("Version Object:: compile_program_procedure:: Server is offline!!!");

            c_compilation.status = Compile_Status.server_was_offline;
            c_compilation.update();

            ObjectNode result = Json.newObject();
            result.put("status", "error");
            result.put("error", "Compilation cloud_compilation_server is offline! It will be compiled as soon as possible!");
            result.put("error_code", 477);
            return result;
        }

        JsonNode json_compilation_result = Cloud_Compilation_Server.make_Compilation(request);


        // NotificationController.successful_compilation(SecurityController.getPerson(), this); TODO Notifikace

        // Když obsahuje chyby - vrátím rovnou Becki
        if(json_compilation_result.has("buildErrors")) {
            logger.debug("Version Object:: compile_program_procedure:: compilation contains user Errors");

            Form<Swagger_Compilation_Build_Error> form_compilation =  Form.form(Swagger_Compilation_Build_Error.class).bind(json_compilation_result.get("buildErrors").get(0) );
            Swagger_Compilation_Build_Error swagger_compilation_build_error = form_compilation.get();

            c_compilation.status = Compile_Status.compiled_with_code_errors;
            c_compilation.update();

            return (ObjectNode) Json.toJson( swagger_compilation_build_error );
        }

        if(!json_compilation_result.has("interface_code") || !json_compilation_result.has("buildUrl")){

            logger.error("Version Object:: compile_program_procedure:: Json Result from Compilation server has not required labels!");

            c_compilation.status = Compile_Status.json_code_is_broken;
            c_compilation.update();

            ObjectNode result = Json.newObject();
            result.put("status", "error");
            result.put("error", "Json code is broken - contact tech support!");
            result.put("error_code", 400);
            return result;
        }

        if(json_compilation_result.has("error")){

            logger.error("Version Object:: compile_program_procedure:: Json Result from Compilation server has not required labels!");


            c_compilation.status = Compile_Status.compilation_server_error;
            c_compilation.update();

            ObjectNode result = Json.newObject();
            result.put("status", "error");
            result.put("error", "Server side Error");
            result.put("error_code", 400);
            return result;
        }

        if(json_compilation_result.get("status").asText().equals("success")) {
            logger.debug("Version Object:: compile_program_procedure:: compilation was successfull");

            try {

                logger.debug("Version Object:: compile_program_procedure:: try to download file");


                WSClient ws = Play.current().injector().instanceOf(WSClient.class);
                F.Promise<WSResponse> responsePromise = ws.url(json_compilation_result.get("buildUrl").asText())
                        .setContentType("undefined")
                        .setRequestTimeout(2500)
                        .get();


                byte[] body = responsePromise.get(2500).asByteArray();

                if (body == null || body.length == 0){
                    throw new FileExistsException();
                }

                logger.debug("Version Object:: compile_program_procedure:: Body is ok - uploading to Azure");

                // Daný soubor potřebuji dostat na Azure a Propojit s verzí
                c_compilation.bin_compilation_file = FileRecord.create_Binary_file(c_compilation.get_path(), FileRecord.get_encoded_binary_string_from_body(body), "compilation.bin");

                logger.debug("Version Object:: compile_program_procedure:: Body is ok - uploading to Azure was succesfull");
                c_compilation.status = Compile_Status.successfully_compiled_and_restored;
                c_compilation.c_comp_build_url = json_compilation_result.get("buildId").asText();
                c_compilation.virtual_input_output = json_compilation_result.get("interface_code").toString();
                c_compilation.date_of_create = new Date();
                c_compilation.update();

                return (ObjectNode) Json.toJson(new Swagger_Compilation_Ok());


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

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore public String blob_version_link;


    @JsonIgnore @Override public void save() {

        while(true){ // I need Unique Value
            this.blob_version_link = "/versions/" + UUID.randomUUID().toString();
            if (Version_Object.find.where().eq("blob_version_link", blob_version_link ).findUnique() == null) break;
        }

        try {
            if(this.author == null)
            this.author = SecurityController.getPerson();
        }catch (Exception e){
            this.author = null;
        }

        super.save();
    }


    @JsonIgnore @Transient
    public String get_path(){
        return  blob_version_link;
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have \"Object\".read_permission = true, you can read / get version on this Object - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have \"Object\".update_permission = true, you can create / update on this Object - Or you need static/dynamic permission key";

    // ZDE BY NIKDY NEMĚLY BÝT OPRÁVNĚNÍ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! - TOMÁŠ Z.
    // Oprávnění volejte na objektu kterého se to týká např.  version.b_program.read_permission()...

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Version_Object> find = new Finder<>(Version_Object.class);


}
