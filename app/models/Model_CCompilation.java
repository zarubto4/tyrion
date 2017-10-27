package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.azure.documentdb.DocumentClientException;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.document_db.DocumentDB;
import utilities.document_db.document_objects.DM_CompilationServer_Connect;
import utilities.document_db.document_objects.DM_CompilationServer_Disconnect;
import utilities.enums.Enum_Compile_status;
import utilities.enums.Enum_Notification_importance;
import utilities.enums.Enum_Notification_level;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.notifications.helps_objects.Notification_Text;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@ApiModel(value = "C_Compilation", description = "Model of C_Compilation")
@Table(name="CCompilation")
public class Model_CCompilation extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_CCompilation.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id public UUID id;

    @JsonIgnore public Date date_of_create;

    @JsonIgnore @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY) @JoinColumn(name="c_compilation_version") public Model_VersionObject version_object;

                                                                @JsonIgnore  public Enum_Compile_status status;

    @ApiModelProperty(required = true, value = virtual_input_output_docu) @Column(columnDefinition = "TEXT")                public String virtual_input_output;
                                                            @JsonIgnore   @Column(columnDefinition = "TEXT")                public String c_comp_build_url;
    @JsonIgnore   @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY) @JoinColumn(name="bin_compilation_file_id")  public Model_FileRecord bin_compilation_file;

    @JsonIgnore  public String firmware_version_core;
    @JsonIgnore  public String firmware_version_mbed;
    @JsonIgnore  public String firmware_version_lib;        // v1.0.1, v1.0.2 etc...
    @JsonIgnore  public String firmware_build_id;
    @JsonIgnore  public String firmware_build_datetime;


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public Model_FileRecord compilation(){
        return Model_FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "compilation.bin").findUnique();
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        date_of_create = new Date();

        super.save();
    }

    @JsonIgnore @Transient @Override
    public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        // Call notification about model update
        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_CProgram.class, version_object.c_program.project_id(), version_object.c_program.id))).start();

        super.update();
        this.version_object.cache_refresh();
    }


    @JsonIgnore @Transient @Override
    public void delete() {
        terminal_logger.internalServerError(new Exception("This object is not legitimate to remove."));
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_compilation_start(){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.normal)
                    .setLevel(Enum_Notification_level.info)
                    .setText(new Notification_Text().setText("Server starts compilation of Version "))
                    .setObject(this)
                    .send(Controller_Security.get_person());

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void notification_compilation_success(){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.normal)
                    .setLevel(Enum_Notification_level.success)
                    .setText(new Notification_Text().setText("Compilation of Version "))
                    .setObject(this)
                    .setText(new Notification_Text().setText("was successful."))
                    .send(Controller_Security.get_person());

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }

    }

    @JsonIgnore @Transient
    public void notification_compilation_unsuccessful_warn(String reason){
        try {
            new Model_Notification()
                    .setImportance(Enum_Notification_importance.normal)
                    .setLevel(Enum_Notification_level.warning)
                    .setText(new Notification_Text().setText("Compilation of Version "))
                    .setObject(this)
                    .setText(new Notification_Text().setText("was unsuccessful, for reason:"))
                    .setText(new Notification_Text().setText(reason).setBoldText())
                    .send(Controller_Security.get_person());
        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void notification_compilation_unsuccessful_error(String result){
        try {
            new Model_Notification()
                    .setImportance(Enum_Notification_importance.normal)
                    .setLevel(Enum_Notification_level.error)
                    .setText(new Notification_Text().setText("Compilation of Version"))
                    .setObject(this)
                    .setText(new Notification_Text().setText("with critical Error:"))
                    .setText(new Notification_Text().setText(result).setBoldText())
                    .send(Controller_Security.get_person());
        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public void notification_new_actualization_request_on_version(){

        new Thread(() -> {
            try {
                new Model_Notification()
                        .setImportance(Enum_Notification_importance.normal)
                        .setLevel(Enum_Notification_level.info)
                        .setText(new Notification_Text().setText("New actualization task was added to Task Queue on Version "))
                        .setObject(this)
                        .setText(new Notification_Text().setText(" from Program "))
                        .setObject(this.version_object.c_program)
                        .send(Controller_Security.get_person());
            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }
        }).start();
    }



/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path(){
        return version_object.c_program.get_path() + version_object.get_path();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Model_CCompilation> find = new Finder<>(Model_CCompilation.class);

/* DESCRIPTION - DOCUMENTATION ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore @Transient public final static String virtual_input_output_docu = "dsafsdfsdf"; // TODO https://youtrack.byzance.cz/youtrack/issue/TYRION-304

}

