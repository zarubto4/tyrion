package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.enums.Enum_Homer_instance_type;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.outboundClass.Swagger_B_Program_Short_Detail;
import utilities.swagger.outboundClass.Swagger_B_Program_State;
import utilities.swagger.outboundClass.Swagger_B_Program_Version;
import utilities.swagger.outboundClass.Swagger_B_Program_Version_Short_Detail;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(value = "BProgram", description = "Model of BProgram")
public class Model_BProgram extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_BProgram.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                         @Id public String id;
                                                             public String name;
                        @Column(columnDefinition = "TEXT")   public String description;
    @JsonIgnore @OneToOne(cascade = CascadeType.ALL)         public Model_HomerInstance instance; // TODO - do budoucna více instnací!!!! http://youtrack.byzance.cz/youtrack/issue/TYRION-502

    @ApiModelProperty(required = true,
                     dataType = "integer", readOnly = true,
                     value = "UNIX time in ms",
                     example = "1466163478925")              public Date last_update;
    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in ms",
            example = "1466163478925")                       public Date date_of_create;
                                    @JsonIgnore @ManyToOne   public Model_Project project;
                                    @JsonIgnore              public boolean removed_by_user; // Defaultně false - když true - tak se to nemá uživateli vracet!
    @JsonIgnore   @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_VersionObject> version_objects = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public String   project_id() {  return project.id; }

    @JsonProperty @Transient public List<Swagger_B_Program_Version_Short_Detail> program_versions() {

        List<Swagger_B_Program_Version_Short_Detail> versions = new ArrayList<>();

        for(Model_VersionObject version : getVersion_objects()){
            versions.add(version.get_short_b_program_version());
        }

        return versions;
    }

    @JsonProperty @Transient public Swagger_B_Program_State instance_details(){
        try {

            Swagger_B_Program_State state = new Swagger_B_Program_State();

            if (instance.actual_instance == null) {
                state.uploaded = false;
                return state;
            }

            // Je nahrán
            state.uploaded = true;          // Jestli je aktuální - nebo plánovaný
            state.instance_online = instance.instance_online();

            if (Server.server_mode == Enum_Tyrion_Server_mode.developer) {
                // /#token - frontend pouze nahradí substring - můžeme tedy do budoucna za adresu přidávat další parametry
                state.instance_remote_url = "ws://" + instance.cloud_homer_server.server_url + instance.cloud_homer_server.web_view_port + "/" + instance.id + "/#token";
            } else {
                state.instance_remote_url = "wss://" + instance.cloud_homer_server.server_url + instance.cloud_homer_server.web_view_port + "/" + instance.id + "/#token";
            }


            // Jaká verze Blocko Programu?
            state.version_id = instance.actual_instance.version_object.id;
            state.version_name = instance.actual_instance.version_object.version_name;

            // Instnace ID
            state.instance_id = instance.id;

            // Informace o Serveru
            state.server_id = instance.cloud_homer_server.unique_identificator;
            state.server_name = instance.cloud_homer_server.personal_server_name;
            state.server_online = instance.cloud_homer_server.server_is_online();

            return state;

        }catch (Exception e){
            terminal_logger.internalServerError("instance_details:", e);
            return null;
        }
    }


/* GET Variable short type of objects ----------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_B_Program_Short_Detail get_b_program_short_detail(){
        try {

            Swagger_B_Program_Short_Detail help = new Swagger_B_Program_Short_Detail();
            help.id = id;
            help.name = name;
            help.description = description;

            help.edit_permission = edit_permission();
            help.delete_permission = delete_permission();
            help.update_permission = update_permission();

            return help;

        }catch (Exception e){
            terminal_logger.internalServerError("get_b_program_short_detail:", e);
            return null;
        }
    }

/* Private Documentation Class -----------------------------------------------------------------------------------------*/


    // Objekt určený k vracení verze
    @JsonIgnore @Transient
    public Swagger_B_Program_Version program_version(Model_VersionObject version_object){

        Swagger_B_Program_Version b_program_version = new Swagger_B_Program_Version();

        b_program_version.version_object                = version_object;

        b_program_version.remove_permission = delete_permission();
        b_program_version.edit_permission   = edit_permission();

        b_program_version.hardware_group                = version_object.b_program_hw_groups;
        b_program_version.m_project_program_snapshots   = version_object.b_program_version_snapshots;

        Model_FileRecord fileRecord = Model_FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "program.js").findUnique();
        if(fileRecord != null) b_program_version.program             = fileRecord.get_fileRecord_from_Azure_inString();

        return b_program_version;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public List<Model_VersionObject> getVersion_objects() {
        return Model_VersionObject.find.where().eq("b_program.id", id).eq("removed_by_user", false).order().desc("date_of_create").findList();
    }


/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");

        while(true){ // I need Unique Value
            this.id = UUID.randomUUID().toString();
            this.azure_b_program_link = project.get_path() + "/b-programs/"  + this.id;
            if (Model_BProgram.find.byId(this.id) == null) break;
        }


        if(instance == null){

            Model_HomerInstance instance = new Model_HomerInstance();
            instance.instance_type = Enum_Homer_instance_type.INDIVIDUAL;
            instance.cloud_homer_server = Model_HomerServer.get_destination_server();
            instance.save();
            this.instance = instance;

        }
        super.save();

        if(project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, project_id(), project_id()))).start();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        if(project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_BProgram.class, project_id(), id))).start();
    }

    @JsonIgnore @Override public void delete() {

      terminal_logger.debug("update :: Delete object Id: {} ", this.id);

      instance.remove_instance_from_server();
      instance.removed_by_user = true;
      instance.update();

      this.removed_by_user = true;

      super.update();

     if(project != null) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, project_id(), project_id()))).start();

    }

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String azure_b_program_link;

    @JsonIgnore @Transient
    public String get_path(){
        return azure_b_program_link;
    }


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission()  {  return  ( project.read_permission() ) || Controller_Security.get_person().has_permission("B_Program_create");  }
    @JsonProperty @Transient public boolean update_permission()  {  return  ( Model_BProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("B_Program_update");  }
    @JsonIgnore   @Transient public boolean read_permission()    {  return  ( Model_BProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("B_Program_read");   }
    @JsonProperty @Transient public boolean edit_permission()    {  return  ( Model_BProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("B_Program_edit");    }
    @JsonProperty @Transient public boolean delete_permission()  {  return  ( Model_BProgram.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("B_Program_delete");  }

    public enum permissions{ B_Program_create, B_Program_update, B_Program_read, B_Program_edit , B_Program_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
     public static Model.Finder<String,Model_BProgram> find = new Finder<>(Model_BProgram.class);
}

