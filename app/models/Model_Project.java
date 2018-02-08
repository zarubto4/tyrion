package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Expr;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.cache.IdsList;
import utilities.enums.*;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Button;
import utilities.notifications.helps_objects.Notification_Text;
import javax.persistence.*;
import javax.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Project", description = "Model of Project")
@Table(name="Project")
public class Model_Project extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Project.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Product product;

    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_BProgram>              b_programs   = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_CProgram>              c_programs   = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Library>               libraries    = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_MProject>              m_projects   = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Block>                 blocks       = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_Widget>                widgets      = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_HardwareRegistration>  hardware     = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_HardwareGroup> hardware_groups = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) @OrderBy("created desc") public List<Model_Invitation>            invitations  = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="project", cascade = CascadeType.ALL) @OrderBy("id asc")       public List<Model_ProjectParticipant>    participants = new ArrayList<>();

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public List<UUID> cache_hardware_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_c_program_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_library_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_b_program_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_m_project_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_hardware_group_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_widget_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_block_ids;
    @JsonIgnore @Transient @Cached public List<UUID> cache_instance_ids;
    @JsonIgnore @Transient @Cached private UUID cache_product_id;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @ApiModelProperty(required = true) public List<Model_HardwareRegistration>    hardware()   { return active() ? getHardware()  : new ArrayList<>(); }
    @JsonProperty @ApiModelProperty(required = true) public List<Model_BProgram>                b_programs() { return active() ? getBPrograms() : new ArrayList<>(); }
    @JsonProperty @ApiModelProperty(required = true) public List<Model_CProgram>                c_programs() { return active() ? getCPrograms() : new ArrayList<>(); }
    @JsonProperty @ApiModelProperty(required = true) public List<Model_Library>                 libraries()  { return active() ? getLibraries() : new ArrayList<>(); }
    @JsonProperty @ApiModelProperty(required = true) public List<Model_MProject>                m_projects() { return active() ? getMProjects() : new ArrayList<>(); }
    @JsonProperty @ApiModelProperty(required = true) public List<Model_Widget>                  widgets()    { return active() ? getWidgets()   : new ArrayList<>(); }
    @JsonProperty @ApiModelProperty(required = true) public List<Model_Block>                   blocks()     { return active() ? getBlocks()    : new ArrayList<>(); }
    @JsonProperty @ApiModelProperty(required = true) public List<Model_Instance>                instances()  { return active() ? getInstances() : new ArrayList<>(); }
    @JsonProperty @ApiModelProperty(required = true) public boolean active() { return getProduct().active;}

    @JsonProperty @ApiModelProperty(required = true) public String product_name() { return getProduct().name;}
    @JsonProperty @ApiModelProperty(required = true) public UUID product_id()   { return getProduct().id;}

    @JsonProperty @ApiModelProperty(required = true) public List<Model_ProjectParticipant> participants() {

        List<Model_ProjectParticipant> project_participants = new ArrayList<>(this.participants);

        for (Model_Invitation invitation : invitations) {

            Model_Person person = Model_Person.find.query().where().eq("mail", invitation.email).findOne();

            Model_ProjectParticipant project_participant = new Model_ProjectParticipant();

            if (person != null) {

                project_participant.person = person;
            } else {
                project_participant.user_email = invitation.email;
            }

            project_participant.state = ParticipantStatus.INVITED;

            if (!project_participants.contains(project_participant)) {
                project_participants.add(project_participant);
            }
        }

        return project_participants;
    }

/* JSON IGNORE METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonIgnore
    public boolean isParticipant(Model_Person person) {

        return participants.stream().anyMatch(participant -> participant.person.id.equals(person.id));
    }

    @JsonIgnore @Override public void save() {
        this.blob_project_link = product.get_path() + "/projects/" + UUID.randomUUID();
        super.save();
    }

    @JsonIgnore @Override public void update() {

        logger.debug("update - updating in database, id: {}",  this.id);

        try {

            if (cache.containsKey(this.id)) cache.replace(this.id, this);

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        super.update();
    }

    @JsonIgnore @Override public boolean delete() {

        logger.debug("delete - deleting from database, id: {} ", this.id);
        deleted = true;
        cache.remove(this.id);

        this.update();
        return false;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* GET SQL PARAMETER - CACHE OBJECTS ------------------------------------------------------------------------------------*/

    @JsonIgnore
    public List<Model_HardwareRegistration> getHardware() {
        try {

            List<Model_HardwareRegistration> hardwareRegistrations;

            if (cache_hardware_ids == null) {

                hardwareRegistrations = Model_HardwareRegistration.find.query().where().eq("project.id", id).findList();

                // Získání seznamu
                for (Model_HardwareRegistration hardware : hardwareRegistrations) {
                    cache_hardware_ids.add(hardware.id);
                    hardware.cache();
                }

                return hardwareRegistrations;
            } else {
                hardwareRegistrations = new ArrayList<>();

                for (UUID id : cache_hardware_ids) {
                    hardwareRegistrations.add(Model_HardwareRegistration.getById(id));
                }
            }

            return hardwareRegistrations;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<Model_CProgram> getCPrograms() {
        try {

            List<Model_CProgram> cPrograms;

            if (cache_c_program_ids == null) {

                cPrograms = Model_CProgram.find.query().where().eq("project.id", id).orderBy("UPPER(name) ASC").findList();

                // Získání seznamu
                for (Model_CProgram cProgram : cPrograms) {
                    cache_c_program_ids.add(cProgram.id);
                    cProgram.cache();
                }

                return cPrograms;
            } else {
                cPrograms = new ArrayList<>();

                for (UUID id : cache_c_program_ids) {
                    cPrograms.add(Model_CProgram.getById(id));
                }
            }

            return cPrograms;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<Model_Library> getLibraries() {
        try {

            List<Model_Library> libraries;

            if (cache_library_ids == null) {

                libraries = Model_Library.find.query().where().eq("project.id", id).orderBy("UPPER(name) ASC").findList();

                // Získání seznamu
                for (Model_Library library : libraries) {
                    cache_library_ids.add(library.id);
                    library.cache();
                }

                return libraries;
            } else {
                libraries = new ArrayList<>();

                for (UUID id : cache_library_ids) {
                    libraries.add(Model_Library.getById(id));
                }
            }

            return libraries;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<Model_BProgram> getBPrograms() {
        try {

            List<Model_BProgram> bPrograms;

            if (cache_b_program_ids == null) {

                bPrograms = Model_BProgram.find.query().where().eq("project.id", id).orderBy("UPPER(name) ASC").findList();

                // Získání seznamu
                for (Model_BProgram bProgram : bPrograms) {
                    cache_b_program_ids.add(bProgram.id);
                    bProgram.cache();
                }

                return bPrograms;
            } else {
                bPrograms = new ArrayList<>();

                for (UUID id : cache_b_program_ids) {
                    bPrograms.add(Model_BProgram.getById(id));
                }
            }

            return bPrograms;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<Model_MProject> getMProjects() {
        try {

            List<Model_MProject> mProjects;

            if (cache_m_project_ids == null) {

                mProjects = Model_MProject.find.query().where().eq("project.id", id).orderBy("UPPER(name) ASC").findList();

                // Získání seznamu
                for (Model_MProject mProject : mProjects) {
                    cache_m_project_ids.add(mProject.id);
                    mProject.cache();
                }

                return mProjects;
            } else {
                mProjects = new ArrayList<>();

                for (UUID id : cache_m_project_ids) {
                    mProjects.add(Model_MProject.getById(id));
                }
            }

            return mProjects;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<Model_HardwareGroup> getHardwareGroups() {
        try {

            List<Model_HardwareGroup> groups;

            if (cache_hardware_group_ids == null) {

                groups = Model_HardwareGroup.find.query().where().eq("project.id", id).orderBy("UPPER(name) ASC").findList();

                // Získání seznamu
                for (Model_HardwareGroup group : groups) {
                    cache_hardware_group_ids.add(group.id);
                    group.cache();
                }

                return groups;
            } else {
                groups = new ArrayList<>();

                for (UUID id : cache_hardware_group_ids) {
                    groups.add(Model_HardwareGroup.getById(id));
                }
            }

            return groups;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<Model_Widget> getWidgets() {
        try {

            List<Model_Widget> widgets;

            if (cache_widget_ids == null) {

                widgets = Model_Widget.find.query().where().eq("project.id", id).orderBy("UPPER(name) ASC").findList();

                // Získání seznamu
                for (Model_Widget widget : widgets) {
                    cache_widget_ids.add(widget.id);
                    widget.cache();
                }

                return widgets;
            } else {
                widgets = new ArrayList<>();

                for (UUID id : cache_widget_ids) {
                    widgets.add(Model_Widget.getById(id));
                }
            }

            return widgets;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<Model_Block> getBlocks() {
        try {

            List<Model_Block> blocks;

            if (cache_block_ids == null) {

                blocks = Model_Block.find.query().where().eq("project.id", id).orderBy("UPPER(name) ASC").findList();

                // Získání seznamu
                for (Model_Block block : blocks) {
                    cache_block_ids.add(block.id);
                    block.cache();
                }

                return blocks;
            } else {
                blocks = new ArrayList<>();

                for (UUID id : cache_block_ids) {
                    blocks.add(Model_Block.getById(id));
                }
            }

            return blocks;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public List<Model_Instance> getInstances() {
        try {

            List<Model_Instance> instances;

            if (cache_instance_ids == null) {

                instances = Model_Instance.find.query().where().eq("project.id", id).orderBy("UPPER(name) ASC").findList();

                // Získání seznamu
                for (Model_Instance instance : instances) {
                    cache_instance_ids.add(instance.id);
                    instance.cache();
                }

                return instances;
            } else {
                instances = new ArrayList<>();

                for (UUID id : cache_instance_ids) {
                    instances.add(Model_Instance.getById(id));
                }
            }

            return instances;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonIgnore
    public Model_Product getProduct() {

        if (cache_product_id == null) {
            Model_Product product = Model_Product.find.query().where().eq("projects.id", id).findOne();
            if (product == null) return null;

            product.cache();

            cache_product_id = product.id;
            return product;
        }

        return Model_Product.getById(cache_product_id);
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void notification_project_invitation(Model_Person person, Model_Invitation invitation) {

        Model_Person owner = BaseController.person();

        new Model_Notification()
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("User "))
                .setObject(owner)
                .setText(new Notification_Text().setText(" invited you into the project "))
                .setObject(this)
                .setText(new Notification_Text().setText(". Do you accept the invitation?"))
                .setButton( new Notification_Button().setAction(NotificationAction.ACCEPT_PROJECT_INVITATION).setPayload(invitation.id.toString()).setColor(Becki_color.byzance_green).setText("Yes")  )
                .setButton( new Notification_Button().setAction(NotificationAction.REJECT_PROJECT_INVITATION).setPayload(invitation.id.toString()).setColor(Becki_color.byzance_red).setText("No")  )
                .send(person);
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_rejected(Model_Person owner) {

        Model_Person person = BaseController.person();

        new Model_Notification()
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("User "))
                .setObject(person)
                .setText(new Notification_Text().setText(" did not accept your invitation to the project "))
                .setObject(this)
                .setText(new Notification_Text().setText("."))
                .send(owner);
    }

    @JsonIgnore @Transient
    public void notification_project_invitation_accepted(Model_Person invitee, Model_Person owner) {

        new Model_Notification()
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("User "))
                .setObject(invitee)
                .setText(new Notification_Text().setText(" accepted your invitation to the project "))
                .setObject(this)
                .setText(new Notification_Text().setText("."))
                .send(owner);
    }

    @JsonIgnore @Transient
    public void notification_project_participant_change_status(Model_ProjectParticipant participant) {

        Model_Person person = BaseController.person();

        new Model_Notification()
                .setImportance(NotificationImportance.NORMAL)
                .setLevel(NotificationLevel.INFO)
                .setText(new Notification_Text().setText("User "))
                .setObject(person)
                .setText(new Notification_Text().setText(" changed your status in project "))
                .setObject(this)
                .setText(new Notification_Text().setText(" to " + participant.state.name() + ". You have different permissions now."))
                .send(participant.person);
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore private String blob_project_link;

    @JsonIgnore
    public String getPath() {
        return  blob_project_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: For all project: User can read project on API: {GET /project) - get Project by logged Person ";

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public boolean create_permission() {
        return product.active && product.create_permission();
    }

    @JsonProperty
    public boolean update_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("project_update_" + id)) return BaseController.person().has_permission("project_update_"+ id);
        if (BaseController.person().has_permission("Project_update")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_Project.find.query().where().eq("participants.person.id", BaseController.personId()).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("project_update_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("project_update_" + id, false);
        return false;
    }

    @JsonIgnore
    public boolean read_permission()      {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("project_read_" + id)) return BaseController.person().has_permission("project_read_"+ id);
        if (BaseController.person().has_permission("Project_read")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
        if ( Model_Project.find.query().where().eq("participants.person.id", BaseController.personId()).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("project_read_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("project_read_" + id, false);
        return false;
    }

    @JsonProperty
    public boolean edit_permission()      {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("project_edit_" + id)) return BaseController.person().has_permission("project_edit_"+ id);
        if (BaseController.person().has_permission("Project_edit")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_Project.find.query().where().eq("participants.person.id", BaseController.personId()).eq("id", id).findCount() > 0) {
            BaseController.person().cache_permission("project_edit_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("projecte_edit_" + id, false);
        return false;
    }

    @JsonProperty
    public boolean delete_permission()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("project_delete_" + id)) {
            return BaseController.person().has_permission("project_delete_"+ id);
        }
        if (BaseController.person().has_permission("Project_delete")) {
            return true;
        }

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_ProjectParticipant.find.query().where().eq("project.id", id).eq("person.id", BaseController.personId()).eq("state", ParticipantStatus.OWNER).findCount() > 0) {
            BaseController.person().cache_permission("project_delete_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("project_delete_" + id, false);
        return false;
    }

    @JsonProperty
    public boolean unshare_permission()   {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("project_share_" + id)) return BaseController.person().has_permission("project_share_"+ id);
        if (BaseController.person().has_permission("Project_unshare")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_ProjectParticipant.find.query().where().eq("project.id", id).eq("person.id", BaseController.personId()).disjunction().add(Expr.eq("state", ParticipantStatus.OWNER)).add(Expr.eq("state", ParticipantStatus.ADMIN)).findCount()> 0) {
            BaseController.person().cache_permission("project_share_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("project_share_" + id, false);
        return false;
    }

    @JsonProperty
    public boolean share_permission ()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("project_unshare_" + id)) return BaseController.person().has_permission("project_unshare_"+ id);
        if (BaseController.person().has_permission("Project_share")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_ProjectParticipant.find.query().where().eq("project.id", id).eq("person.id", BaseController.personId()).disjunction().add(Expr.eq("state", ParticipantStatus.OWNER)).add(Expr.eq("state", ParticipantStatus.ADMIN)).findCount()> 0) {
            BaseController.person().cache_permission("project_unshare_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("project_unshare_" + id, false);
        return false;
    }

    @JsonProperty
    public boolean admin_permission ()    {

        // Cache už Obsahuje Klíč a tak vracím hodnotu
        if (BaseController.person().has_permission("project_admin_permission_" + id)) return BaseController.person().has_permission("project_admin_permission_"+ id);
        if (BaseController.person().has_permission("Project_admin")) return true;

        // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) - Zde je prostor pro to měnit strukturu oprávnění
        if (Model_ProjectParticipant.find.query().where().eq("project.id", id).where().eq("person.id", BaseController.personId()).where().disjunction().add(Expr.eq("state", ParticipantStatus.OWNER)).add(Expr.eq("state", ParticipantStatus.ADMIN)).findCount()> 0) {
            BaseController.person().cache_permission("project_admin_permission_" + id, true);
            return true;
        }

        // Přidávám do listu false a vracím false
        BaseController.person().cache_permission("project_admin_permission_" + id, false);
        return false;

    }

    @JsonIgnore
    public boolean financial_permission() {return this.product.financial_permission("project");}

    public enum Permission { Project_update, Project_read, Project_unshare , Project_share, Project_edit, Project_delete, Project_admin }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_Project.class)
    public static Cache<UUID, Model_Project> cache ;

    @CacheField(value = IdsList.class, name = "Model_Project_Person_Ids")
    public static Cache<UUID, IdsList> token_cache;

    public static Model_Project getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_Project getById(UUID id) {

        Model_Project project = cache.get(id);
        if (project == null) {

            project = find.query().where().idEq(id).eq("deleted", false).findOne();
            if (project == null) return null;

            cache.put(id, project);
        }

        return project;
    }

    public static void becki_person_id_subscribe(UUID person_id) {

        List<Model_Project> list_of_projects = Model_Project.find.query().where().eq("participants.person.id", person_id).disjunction()
                    .eq("state", ParticipantStatus.ADMIN)
                    .eq("state", ParticipantStatus.MEMBER)
                    .eq("state", ParticipantStatus.OWNER)
                .endJunction()
                .findList();

        for (Model_Project project : list_of_projects ) {

            IdsList idlist = token_cache.get(project.id);

            if (idlist == null) idlist = new IdsList();

            if (!idlist.list.contains(person_id)) idlist.list.add(person_id);

            token_cache.put(project.id, idlist);
        }
    }

    public static void becki_person_id_unsubscribe(UUID person_id) {

        List<Model_Project> list_of_projects = Model_Project.find.query().where().eq("participants.person.id", person_id).disjunction()
                .eq("state", ParticipantStatus.ADMIN)
                .eq("state", ParticipantStatus.MEMBER)
                .eq("state", ParticipantStatus.OWNER)
                .endJunction()
                .findList();

        for (Model_Project project : list_of_projects ) {

            IdsList idlist = token_cache.get(project.id);

            if (idlist != null) {
                if (idlist.list.contains(person_id)) {
                    idlist.list.remove(person_id);
                }
            }
        }
    }

    public static List<UUID> get_project_becki_person_ids_list(UUID project_id) {

        if (token_cache == null) {
            logger.error("get_project_becki_person_ids_list - token_cache is null");
            return new ArrayList<>();
        }

        if (project_id == null) {
            logger.error("get_project_becki_person_ids_list - project_id is null");
            return new ArrayList<>();
        }

        IdsList idlist = token_cache.get(project_id);

        if (idlist == null) {

            idlist = new IdsList();

            Model_Project project = getById(project_id);
            if (project == null) return idlist.list;

            for (Model_ProjectParticipant participant : project.participants) {

                if (participant.state == ParticipantStatus.INVITED) continue;

                if (!idlist.list.contains(participant.person.id))  idlist.list.add(participant.person.id);
            }

            token_cache.put(project_id, idlist);
        }

        return idlist.list;
    }

    public void cache_refresh() {
        if (cache.containsKey(this.id)) {
            this.refresh();
            cache.replace(this.id, this);
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Project> find = new Finder<>(Model_Project.class);
}