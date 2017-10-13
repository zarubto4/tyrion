package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_HardwareGroup_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "BoardGroup", description = "Model of Board Group")
@Table(name="BoardGroup")
public class Model_BoardGroup extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_BlockoBlock.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id public UUID id;

    public String name;                                                             // Jméno, které si uživatel pro hardware group nasatvil
    @Column(columnDefinition = "TEXT") public String description;                   // Popisek, který si uživatel nasstavil

    @JsonIgnore public Date date_of_create;
    @JsonIgnore public boolean removed_by_user;

    @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY) public Model_Project project;  // Projekt, pod který Hardware Group spadá

    @JsonIgnore @ManyToMany(fetch = FetchType.LAZY) public List<Model_Board> boards  = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList public Integer cache_group_size = null;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public int group_size(){

        if(cache_group_size == null) {
            cache_group_size = Model_Board.find.where().eq("board_groups.id", id).findRowCount();
        }

        return cache_group_size;
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public Swagger_HardwareGroup_Short_Detail get_group_short_detail(){

        Swagger_HardwareGroup_Short_Detail short_detail = new Swagger_HardwareGroup_Short_Detail();

        short_detail.id = id.toString();
        short_detail.name = name;
        short_detail.description = description;

        return short_detail;

    }

    // TODO teoreticky cachovat?
    @JsonIgnore @Transient public List<String> get_hardware_id_list(){

        List<Model_Board> boards = Model_Board.find.where().eq("board_groups.id", id).select("id").findList();
        List<String> ids = new ArrayList<>();
        for(Model_Board board : boards) {
            ids.add(board.id);
        }

        return ids;
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");


        date_of_create = new Date();

        super.save();
        super.refresh();

        project.cache_hardware_groups_ids.add(id.toString());

        //  if create something under project
        //  if(project != null ) new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo(Model_Project.class, project_id(), project_id()))).start();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        // Case 1.1 :: We delete the object
        super.update();

        // Case 1.2 :: After Update - we send notification to frontend (Only if it is desirable)
        // new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( _Model_ExampleModelName.class, "project.id", "model.id"))).start();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("delete: Delete object Id: {} ", this.id);

        // Case 1.1 :: We delete the object

        // Case 1.2 :: After Update - we send notification to frontend (Only if it is desirable)
        // new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( _Model_ExampleModelName.class, "project.id", "model.id"))).start();

        // Case 2.1 :: We delete the object with change of ORM parameter  @JsonIgnore  public boolean removed_by_user;
        this.removed_by_user = true;
        project.cache_hardware_groups_ids.remove(id.toString());
        this.update();

        // Case 1.2 :: After Delete - we send notification to frontend (Only if it is desirable)
        //new Thread(() -> Update_echo_handler.addToQueue(new WS_Message_Update_model_echo( Model_Project.class, "project.id", "model.id"))).start();

        // Case 3 :: In some cases, it is not possible to delete an object - it is therefore impossible to delete the object by the method
        //terminal_logger.internalServerError(new Exception("This object is not legitimate to remove."));
        //throw new IllegalAccessError("Delete is not supported under " + getClass().getSimpleName());

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    public void make_log_to_non_sql_database(){
        new Thread( () -> {
            try {
                //Server.documentClient.createDocument(Server.online_status_collection.getSelfLink(), DM_Board_Connect.make_request(this.id), null, true);
            } catch (Exception e) {
                terminal_logger.internalServerError("make_log_to_non_sql_database:", e);
            }
        }).start();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Create Permission is always JsonIgnore
    @JsonIgnore   @Transient public boolean create_permission()  {  return  project != null && project.edit_permission(); }
    @JsonIgnore   @Transient public boolean read_permission()    {  return  project != null && project.read_permission(); }
    @JsonProperty @Transient public boolean update_permission()  {  return  project != null && project.update_permission(); }
    @JsonProperty @Transient public boolean edit_permission()    {  return  project != null && project.update_permission(); }
    @JsonProperty @Transient public boolean delete_permission()  {  return  project != null && project.update_permission(); }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE        = Model_BoardGroup.class.getSimpleName();
    public static Cache<String, Model_BoardGroup> cache;         // Server_cache Override during server initialization

    @JsonIgnore public static Model_BoardGroup get_byId(String id){

        Model_BoardGroup group = cache.get(id);
        if (group == null) {

            group = find.byId(id);
            if (group == null) return null;

            cache.put(id, group);
        }

        return group;
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_BoardGroup> find = new Model.Finder<>(Model_BoardGroup.class);
}
