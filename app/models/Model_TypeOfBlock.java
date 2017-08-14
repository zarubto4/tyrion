package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.helps_objects.TyrionCachedList;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_Blocko_Block_Short_Detail;
import utilities.swagger.outboundClass.Swagger_TypeOfBlock_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of TypeOfBlock",
        value = "TypeOfBlock")
public class Model_TypeOfBlock extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_SecurityRole.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @Id @ApiModelProperty(required = true) public String id;
                                                            @ApiModelProperty(required = true) public String name;
                         @Column(columnDefinition = "TEXT") @ApiModelProperty(required = true) public String description;

                                                            @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public Model_Project project;
                                                            @JsonIgnore                                     public Integer order_position;
                                                            @JsonIgnore                                     public boolean removed_by_user;

    @JsonIgnore @OneToMany(mappedBy="type_of_block", cascade=CascadeType.ALL, fetch = FetchType.LAZY) @ApiModelProperty(required = true) public List<Model_BlockoBlock> blocko_blocks = new ArrayList<>();




/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @TyrionCachedList private String cache_value_project_id;
    @JsonIgnore @Transient @TyrionCachedList private List<String> blocko_block_ids = new ArrayList<>();

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(value = "This value will be in Json only if TypeOfBlock is private!", readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty @Transient public String project_id() {

        if(cache_value_project_id == null){
            Model_Project project = Model_Project.find.where().eq("type_of_blocks.id", id).select("id").findUnique();
            cache_value_project_id = project.id;
        }

        return cache_value_project_id;
    }

    @JsonProperty @Transient public List<Swagger_Blocko_Block_Short_Detail> blocks() {
        try {

            List<Swagger_Blocko_Block_Short_Detail> short_detail_blocks = new ArrayList<>();

            for (Model_BlockoBlock block : get_blocko_blocks()) {
                short_detail_blocks.add( block.get_blocko_block_short_detail() ) ;
            }

            return short_detail_blocks;

        }catch (Exception e){
            terminal_logger.internalServerError( e);
            return null;
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @TyrionCachedList
    public List<Model_BlockoBlock> get_blocko_blocks(){
        try {

            // Cache
            if(blocko_block_ids.isEmpty()) {

                List<Model_BlockoBlock> blockoBlocks = Model_BlockoBlock.find.where().eq("type_of_block.id", id).eq("removed_by_user", false).order().asc("order_position").select("id").findList();

                // Získání seznamu
                for (Model_BlockoBlock blockoBlock : blockoBlocks) {
                    blocko_block_ids.add(blockoBlock.id);
                }
            }

            List<Model_BlockoBlock> blockoBlock = new ArrayList<>();

            for(String blockoBlock_id : blocko_block_ids){
                blockoBlock.add(Model_BlockoBlock.get_byId(blockoBlock_id));
            }

            return blockoBlock;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return new ArrayList<Model_BlockoBlock>();
        }
    }

    @JsonIgnore @Transient public Swagger_TypeOfBlock_Short_Detail get_type_of_block_short_detail(){
        Swagger_TypeOfBlock_Short_Detail help = new Swagger_TypeOfBlock_Short_Detail();
        help.id = id;
        help.name = name;
        help.description = description;
        help.blocko_blocks.addAll(blocks());
        help.edit_permission = edit_permission();
        help.delete_permission = delete_permission();
        help.update_permission = update_permission();
        return help;
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        if(project == null){
            order_position = Model_TypeOfBlock.find.where().isNull("project").findRowCount() + 1;
        }else {
            order_position = Model_TypeOfBlock.find.where().eq("project.id", project.id).findRowCount() + 1;
        }

        this.id = UUID.randomUUID().toString();

        super.save();

        if(project != null){
            project.cache_list_type_of_blocks_ids.add(id);
        }

        cache.put(id, this);
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object value: {}",  this.id);

        super.update();

    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);

        removed_by_user = true;
        super.update();

        if(project_id() != null){
            Model_Project.get_byId(project_id()).cache_list_type_of_blocks_ids.remove(id);
        }

        cache.remove(id);

    }


/* ORDER ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void up(){

        // Čísla mohou být shodná!! TODO

        Model_TypeOfBlock up = Model_TypeOfBlock.find.where().eq("order_position", (order_position-1) ).isNull("project").findUnique();
        if(up == null)return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down(){

        Model_TypeOfBlock down = Model_TypeOfBlock.find.where().eq("order_position", (order_position+1) ).isNull("project").findUnique();
        if(down == null)return;

        down.order_position -= 1;
        down.update();

        this.order_position += 1;
        this.update();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have Project.read_permission = true, you can read TypeOfBlock on this Project ( You get ids of list of TypeOfBLocks in object \"project\" in json)  - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have Project.update_permission = true, you can create TypeOfBlock on this Project - Or you need static/dynamic permission key if user want create public TypeOfBlock";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient   public boolean create_permission()  {
        return   (project != null && project.update_permission()) || Controller_Security.get_person().permissions_keys.containsKey("TypeOfBlock_create");
    }

    @JsonIgnore @Transient   public boolean read_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("type_of_block_read_" + id)) return Controller_Security.get_person().permissions_keys.get("type_of_block_read_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("TypeOfBlock_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if(project_id() != null && Model_Project.get_byId(project_id()).read_permission()){
            Controller_Security.get_person().permissions_keys.put("type_of_block_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("type_of_block_read_" + id, false);
        return false;
    }
    @JsonProperty @Transient public boolean update_permission()  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("type_of_block_update_" + id)) return Controller_Security.get_person().permissions_keys.get("type_of_block_update_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("TypeOfBlock_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if(project_id() != null && Model_Project.get_byId(project_id()).edit_permission()){
            Controller_Security.get_person().permissions_keys.put("type_of_block_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("type_of_block_edit_" + id, false);
        return false;

    }
    @JsonProperty @Transient public boolean edit_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("type_of_block_edit_" + id)) return Controller_Security.get_person().permissions_keys.get("type_of_block_edit_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("TypeOfBlock_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if(project_id() != null && Model_Project.get_byId(project_id()).edit_permission()){
            Controller_Security.get_person().permissions_keys.put("type_of_block_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("type_of_block_edit_" + id, false);
        return false;
    }
    @JsonProperty @Transient public boolean delete_permission()  {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if(Controller_Security.get_person().permissions_keys.containsKey("type_of_block_delete_" + id)) return Controller_Security.get_person().permissions_keys.get("type_of_block_delete_"+ id);
        if(Controller_Security.get_person().permissions_keys.containsKey("TypeOfBlock_delete")) return true;



        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if(project_id() != null && Model_Project.get_byId(project_id()).edit_permission()){
            Controller_Security.get_person().permissions_keys.put("type_of_block_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        Controller_Security.get_person().permissions_keys.put("type_of_block_delete_" + id, false);
        return false;
    }



    public enum permissions{TypeOfBlock_create, TypeOfBlock_read, TypeOfBlock_edit , TypeOfBlock_delete, TypeOfBlock_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_TypeOfBlock> find = new Finder<>(Model_TypeOfBlock.class);

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_TypeOfBlock.class.getSimpleName();
    public static Cache<String, Model_TypeOfBlock> cache = null;               // < Model_CProgram_id, Model_TypeOfBlock>

    @JsonIgnore
    public static Model_TypeOfBlock get_byId(String id) {

        Model_TypeOfBlock type_of_block = cache.get(id);
        if (type_of_block == null){

            type_of_block = find.where().idEq(id).eq("removed_by_user", false).findUnique();
            if (type_of_block == null) return null;

            cache.put(id, type_of_block);
        }

        return type_of_block;
    }

    @JsonIgnore
    public static List<Model_TypeOfBlock> get_all() {

        // Získání všech dostupných Skupin - Ty jsou cachovány v listu v projektu
        // A statické (což jsou public zde )

        List<Model_TypeOfBlock> typeOfBlocks = find.where().isNull("project").findList();
        typeOfBlocks.addAll( find.where().eq("project.participants.person.id", Controller_Security.get_person().id ).eq("removed_by_user", false).order().asc("name").findList() );
        typeOfBlocks.addAll( find.where().isNull("project").eq("removed_by_user", false).order().asc("order_position").findList());
        return typeOfBlocks;
    }

    @JsonIgnore
    public static Model_TypeOfBlock get_publicByName(String name) {
        return find.where().isNull("project").eq("removed_by_user", false).eq("name",name).findUnique();
    }

    @JsonIgnore
    public static List<Model_TypeOfBlock> get_public() {
        return find.where().isNull("project").eq("removed_by_user", false).order().asc("order_position").findList();
    }
}
