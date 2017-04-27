package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "Model of C_Compilation",
        value = "C_Compilation")
public class Model_CCompilation extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_CCompilation.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                         @Id public String id;
                                                                 @JsonIgnore public Date date_of_create;

    @JsonIgnore @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
                                   @JoinColumn(name="c_compilation_version") public Model_VersionObject version_object;

                                                                @JsonIgnore  public Enum_Compile_status status;

    @ApiModelProperty(required = true, value = virtual_input_output_docu) @Column(columnDefinition = "TEXT")                public String virtual_input_output;
                                                            @JsonIgnore   @Column(columnDefinition = "TEXT")                public String c_comp_build_url;
    @JsonIgnore   @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY) @JoinColumn(name="bin_compilation_file_id")  public Model_FileRecord bin_compilation_file;

    @JsonIgnore  public String firmware_version_core;
    @JsonIgnore  public String firmware_version_mbed;
    @JsonIgnore  public String firmware_version_lib;
    @JsonIgnore  public String firmware_build_id;
    @JsonIgnore  public String firmware_build_datetime;


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public Model_FileRecord compilation(){
        return Model_FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "compilation.bin").findUnique();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_CCompilation.find.byId(this.id) == null) break;
        }
        this.date_of_create = new Date();
        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        // Call notification about model update
        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_VersionObject.class, version_object.c_program.project_id(), version_object.id))).start();

        super.update();
    }


    @JsonIgnore @Override public void delete() {
        terminal_logger.error("delete:: This object is not legitimate to remove. ");

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
            terminal_logger.internalServerError("Model_CCompilation:: notification_compilation_start", e);
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
            terminal_logger.internalServerError("Model_CCompilation:: notification_compilation_success", e);
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
            terminal_logger.internalServerError("Model_CCompilation:: notification_compilation_unsuccessful_warn", e);
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
            terminal_logger.internalServerError("Model_CCompilation:: notification_compilation_unsuccessful_error", e);
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
                terminal_logger.internalServerError("Model_CCompilation:: notification_new_actualization_request_on_version", e);
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

