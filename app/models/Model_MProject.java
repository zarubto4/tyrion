package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.helps_objects.TyrionCachedList;
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
@Table(name="MProject")
public class Model_MProject extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_MProject.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true)      public String  id;
    @ApiModelProperty(required = true)                                                              public String  name;
    @Column(columnDefinition = "TEXT") @ApiModelProperty(required = false, value = "can be empty")  public String  description;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,
            value = "UNIX time stamp in millis", example = "14618543121234")                        public Date    date_of_create;

                                                    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_Project project;


    @JsonIgnore @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "m_project")  public List<Model_MProjectProgramSnapShot> snapShots = new ArrayList<>();
    @JsonIgnore @ApiModelProperty(required = true) @OneToMany(mappedBy="m_project", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_MProgram> m_programs = new ArrayList<>();


    @JsonIgnore  public boolean removed_by_user; // Defaultně false - když true - tak se to nemá uživateli vracet!


/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList private String cache_value_project_id;
    @JsonIgnore @Transient @TyrionCachedList public List<String> m_programs_ids = new ArrayList<>();


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public String                               project_id() {
        return project.id;

    }
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
            terminal_logger.internalServerError("get_short_m_project:", e);
            return null;
        }
    }

/* GET SQL PARAMETER - CACHE OBJECTS ------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_MProgram> get_m_programs_not_deleted(){
        try{

            if(m_programs_ids.isEmpty()){

                List<Model_MProgram> m_programs =  Model_MProgram.find.where().eq("m_project.id", id).eq("removed_by_user", false).orderBy("UPPER(name) ASC").select("id").findList();

                // Získání seznamu
                for (Model_MProgram m_program : m_programs) {
                    m_programs_ids.add(m_program.id);
                }

            }

            List<Model_MProgram> m_programs  = new ArrayList<>();

            for(String version_id : m_programs_ids){
                m_programs.add(Model_MProgram.get_byId(version_id));
            }

            return m_programs;

        }catch (Exception e){
            terminal_logger.internalServerError("getVersion_objects", e);
            return new ArrayList<Model_MProgram>();
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");

        this.id = UUID.randomUUID().toString();
        this.azure_m_project_link = project.get_path()  + "/m-projects/"  + this.id;

        super.save();

        if(project != null){
            project.cache_list_m_project_ids.add(id);
        }

        cache.put(id, this);

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

        if(project_id() != null){
            Model_Project.get_byId( project_id() ).cache_list_m_project_ids.remove(id);
        }

        cache.remove(id);

        if(project_id() != null ) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo(Model_Project.class, project_id(), project_id()))).start();

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

    @JsonIgnore   @Transient                                     public boolean create_permission(){
        if(Controller_Security.get_person().permissions_keys.containsKey("M_Project_create")) return true;
        return (project.update_permission());
    }
    @JsonProperty @Transient public boolean update_permission()  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("m_project_update_" + id)) return Controller_Security.get_person().permissions_keys.get("m_project_update_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("M_Project_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_MProject.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().permissions_keys.put("m_project_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("m_project_update_" + id, false);
        return false;

    }
    @JsonIgnore   @Transient public boolean read_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("m_project_read_" + id)) return Controller_Security.get_person().permissions_keys.get("m_project_read_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("M_Project_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if( Model_MProject.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().permissions_keys.put("m_project_m_project_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("m_project_read_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean edit_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("m_project_edit_" + id)) return Controller_Security.get_person().permissions_keys.get("m_project_edit_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("M_Project_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_MProject.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().permissions_keys.put("m_project_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("m_project_edit_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean delete_permission()  {
        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("m_project_delete_" + id)) return Controller_Security.get_person().permissions_keys.get("m_project_delete_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("M_Project_delete")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if( Model_MProject.find.where().where().eq("project.participants.person.id", Controller_Security.get_person().id ).where().eq("id", id).findRowCount() > 0){
            Controller_Security.get_person().permissions_keys.put("m_project_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("m_project_delete_" + id, false);
        return false;

    }
    public enum permissions{  M_Project_create, M_Project_update, M_Project_read,  M_Project_edit, M_Project_delete; }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_MProject.class.getSimpleName();

    public static Cache<String, Model_MProject> cache = null; // < ID, Model_BProgram>

    @JsonIgnore
    public static Model_MProject get_byId(String id) {

        Model_MProject m_project= cache.get(id);
        if (m_project == null){

            m_project = Model_MProject.find.byId(id);
            if (m_project == null) return null;

            cache.put(id, m_project);
        }

        return m_project;
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Model_MProject> find = new Finder<>(Model_MProject.class);
}

