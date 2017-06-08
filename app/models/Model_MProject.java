package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Class_Logger;
import utilities.models_update_echo.Update_echo_handler;
import utilities.swagger.outboundClass.Swagger_M_Program_Short_Detail;
import utilities.swagger.outboundClass.Swagger_M_Project_Short_Detail;
import web_socket.message_objects.tyrion_with_becki.WS_Message_Update_model_echo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "M_Project", description = "Model of M_Project")
public class Model_MProject extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_MProject.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)      public String  id;
    @ApiModelProperty(required = true)                                                              public String  name;
    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = false, value = "can be empty")  public String  description;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,
            value = "UNIX time stamp in millis", example = "14618543121234")                        public Date    date_of_create;

                                                                            @JsonIgnore @ManyToOne  public Model_Project project;


    @JsonIgnore @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "m_project")  public List<Model_MProjectProgramSnapShot> snapShots = new ArrayList<>();
    @JsonIgnore @ApiModelProperty(required = true) @OneToMany(mappedBy="m_project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_MProgram> m_programs = new ArrayList<>();


    @JsonIgnore  public boolean removed_by_user; // Defaultně false - když true - tak se to nemá uživateli vracet!


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public String                               project_id() {  return project.id; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_M_Program_Short_Detail> m_programs() { List<Swagger_M_Program_Short_Detail>   l = new ArrayList<>();    for( Model_MProgram m  :  get_m_programs_not_deleted())    l.add(m.get_m_program_short_detail()); return l;}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    /* GET Variable short type of objects ------------------------------------------------------------------------------*/
    @JsonIgnore @Transient public Swagger_M_Project_Short_Detail get_short_m_project(){
        try {
            Swagger_M_Project_Short_Detail swagger_m_project_short_detail = new Swagger_M_Project_Short_Detail();
            swagger_m_project_short_detail.id = id;
            swagger_m_project_short_detail.name = name;
            swagger_m_project_short_detail.description = description;

            swagger_m_project_short_detail.edit_permission = edit_permission();
            swagger_m_project_short_detail.delete_permission = delete_permission();
            swagger_m_project_short_detail.update_permission = update_permission();

            for (Model_MProgram program :  get_m_programs_not_deleted())
                swagger_m_project_short_detail.programs.add(program.get_m_program_short_detail());

            return swagger_m_project_short_detail;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

/* GET SQL PARAMETER - CACHE OBJECTS ------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_MProgram> get_m_programs_not_deleted(){
        return Model_MProgram.find.where().eq("m_project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").findList();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");

        while(true){ // I need Unique Value
            this.id = UUID.randomUUID().toString();
            this.azure_m_project_link = project.get_path()  + "/m-projects/"  + this.id;
            if (Model_MProject.find.byId(this.id) == null) break;
        }

        super.save();

        if(project != null ) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo(Model_Project.class, project_id(), project_id()))).start();

    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        super.update();

        new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_MProject.class, project_id(), id))).start();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);

        removed_by_user = true;
        super.update();

        if(project != null ) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo(Model_Project.class, project_id(), project_id()))).start();

    }



/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/


/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore             private String azure_m_project_link;

    @JsonIgnore @Transient
    public String get_path(){
        return  azure_m_project_link;
    }


/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read M_project on this Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create M_project on this Project - Or you need static/dynamic permission key";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                     public boolean create_permission(){  return ( Model_Project.find.where().eq("participants.person.id",   Controller_Security.get_person().id).eq("id", project.id ).findUnique().create_permission() ) || Controller_Security.get_person().has_permission("M_Project_create");      }
    @JsonIgnore   @Transient                                     public boolean read_permission()  {  return ( Model_MProject.find.where().eq("project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("M_Project_read"); }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean update_permission(){  return ( Model_MProject.find.where().eq("project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("M_Project_update"); }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean edit_permission()  {  return ( Model_MProject.find.where().eq("project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("M_Project_edit"); }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean delete_permission(){  return ( Model_MProject.find.where().eq("project.participants.person.id", Controller_Security.get_person().id).eq("id", id).findRowCount() > 0) || Controller_Security.get_person().has_permission("M_Project_delete"); }

    public enum permissions{  M_Project_create, M_Project_update, M_Project_read,  M_Project_edit, M_Project_delete; }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Model_MProject> find = new Finder<>(Model_MProject.class);
}

