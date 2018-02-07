package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationLevel;
import utilities.enums.NotificationType;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Text;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel( value = "BootLoader", description = "Model of BootLoader")
@Table(name="BootLoader")
public class Model_BootLoader extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BootLoader.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                          public String version_identifier; // HW identifikator od kluků ve formátu b255.255.255 -> ex. b0.1.6 || b0.1.77
    @Column(columnDefinition = "TEXT")    public String changing_note;

    @JsonIgnore @OneToMany(mappedBy="bootloader",cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_CProgramUpdatePlan> c_program_update_plans = new ArrayList<>();

    @JsonIgnore  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)                 public Model_TypeOfBoard type_of_board;       // TODO Cachovat - a opravit kde je nevhodná návaznost
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY)                                                  public Model_TypeOfBoard main_type_of_board;

    @JsonIgnore  @OneToMany(mappedBy="actual_boot_loader", fetch = FetchType.LAZY)                 public List<Model_Hardware> boards  = new ArrayList<>();
                 @OneToOne(mappedBy = "boot_loader", cascade = CascadeType.ALL)                    public Model_Blob file;                // TODO Cachovat - a opravit kde je nevhodná návaznost

/* CACHE VALUES --------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Cached public UUID cache_type_of_board_id;
    @JsonIgnore @Transient @Cached public UUID cache_main_type_of_board_id;
    @JsonIgnore @Transient @Cached public UUID cache_file_id;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty public boolean main_bootloader() { return get_main_type_of_board() != null;}
    @JsonProperty public String  file_path() {
        try {

            if (cache_file_id != null ) {
                String link = Model_Blob.cache_public_link.get(cache_file_id);
                if (link != null) return link;
            }

            if (file == null) { // TODO Cachovat - a opravit kde je nevhodná návaznost
                return null;
            }

            this.cache_file_id = file.id;

            // Separace na Container a Blob
            int slash = file.file_path.indexOf("/");
            String container_name = file.file_path.substring(0, slash);
            String real_file_path = file.file_path.substring(slash + 1);

            CloudAppendBlob blob = Server.blobClient.getContainerReference(container_name).getAppendBlobReference(real_file_path);

            // Create Policy
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            cal.setTime(new Date());
            cal.add(Calendar.HOUR, 5);

            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
            policy.setSharedAccessExpiryTime(cal.getTime());

            String sas = blob.generateSharedAccessSignature(policy, null);

            String total_link = blob.getUri().toString() + "?" + sas;

            logger.debug("file_path - total link: {}", total_link);

            Model_Blob.cache_public_link.put(cache_file_id, total_link);

            // Přesměruji na link
            return total_link;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_TypeOfBoard get_type_of_board() {

        if (cache_type_of_board_id == null) {

            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.query().where().eq("boot_loaders.id", id).select("id").findOne();
            if (typeOfBoard == null) {
                return null;
            }

            cache_type_of_board_id = typeOfBoard.id;
        }

        return Model_TypeOfBoard.getById(cache_type_of_board_id);
    }

    @JsonIgnore
    public Model_TypeOfBoard get_main_type_of_board() {
        try {

            if (cache_main_type_of_board_id == null) {

                Model_TypeOfBoard main = Model_TypeOfBoard.find.query().where().eq("main_boot_loader.id", id).select("id").findOne();
                if (main == null) {
                    cache_main_type_of_board_id = null;
                    return null;
                }

                cache_main_type_of_board_id = main.id;
            }

            return Model_TypeOfBoard.getById(cache_main_type_of_board_id);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    // Bootloader
    public static void notification_bootloader_procedure_first_information_single(Model_CProgramUpdatePlan plan) {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.LOW)
                    .setLevel(NotificationLevel.WARNING)
                    .setChainType(NotificationType.CHAIN_START)   // Deliberately -> chain notification for the reason that the user has to clicked on himself for removal .
                    .setNotificationId(plan.id)
                    .setText(new Notification_Text().setText("Attention. You have entered the bootloader update command for Bootloader version "))
                    .setText(new Notification_Text().setBoldText().setColor(Becki_color.byzance_red).setText(plan.get_bootloader().version_identifier + " "))
                    .setText(new Notification_Text().setText(" for device "))
                    .setObject(plan.get_board())
                    .setText(new Notification_Text().setText(". "))
                    .setText(new Notification_Text().setText("Bootloader update is a critical action. " +
                            "Do not disconnect the device from the power supply during the update. " +
                            "The critical time to update is 3 seconds on average. Wait for confirmation of the notification please! "))
                    .setNewLine()
                    .setText(new Notification_Text().setText("We show you in hardware overview only what's currently on the device. " +
                            "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                            "In the details of the instance or hardware overview, you can see the status of each procedures. " +
                            "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                    .send_under_project(plan. get_board().project_id());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // Bootloader
    public static void notification_bootloader_procedure_success_information_single(Model_CProgramUpdatePlan plan) {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.LOW)
                    .setLevel(NotificationLevel.SUCCESS)
                    .setChainType(NotificationType.CHAIN_START)   // Deliberately -> chain notification for the reason that the user has to clicked on himself for removal .
                    .setNotificationId(plan.id)
                    .setText(new Notification_Text().setText("Success! Bootloader version "))
                    .setText(new Notification_Text().setBoldText().setColor(Becki_color.byzance_red).setText(plan.get_bootloader().version_identifier + " "))
                    .setText(new Notification_Text().setText("  is done for device "))
                    .setObject(plan.get_board())
                    .setText(new Notification_Text().setText(". "))
                    .setText(new Notification_Text().setText("Have a nice Day!"))
                    .send_under_project(plan.get_board().project_id());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public static void notification_bootloader_procedure_first_information_list(List<Model_CProgramUpdatePlan> plans) {
        try {

            new Thread( () -> {

                if ( plans.size() == 0 )  throw new IllegalArgumentException("notification_set_static_backup_procedure_first_information_list:: List is empty! ");
                if ( plans.size() == 1 ) {
                    notification_bootloader_procedure_first_information_single(plans.get(0));
                    return;
                }

                new Model_Notification()
                        .setImportance(NotificationImportance.LOW)
                        .setLevel(NotificationLevel.WARNING)
                        .setChainType(NotificationType.CHAIN_START)   // Deliberately -> chain notification for the reason that the user has to clicked on himself for removal .
                        .setNotificationId(plans.get(0).actualization_procedure.id)
                        .setText(new Notification_Text().setText("Attention. I have entered the bootloader update command for Bootloader version "))
                        .setText(new Notification_Text().setBoldText().setColor(Becki_color.byzance_red).setText(plans.get(0).get_bootloader().version_identifier + " "))
                        .setText(new Notification_Text().setText("for " + plans.size() + " devices. "))
                        .setText(new Notification_Text().setText("Bootloader update is a critical action. " +
                                "Do not disconnect the device from the power supply during the update. " +
                                "The critical time to update is 3 seconds on average. Wait for confirmation of the notification please! " +
                                "We show you in hardware overview only what's currently on the device. " +
                                "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                                "In the details of the instance or hardware overview, you can see the status of each procedure. " +
                                "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                        .send_under_project(plans.get(0).get_board().project_id());

            }).start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        super.save();

        if (get_type_of_board() != null) {
            get_type_of_board().boot_loaders();
            get_type_of_board().cache_bootloaders_id.add(id);
        }
        cache.put(id, this);
    }

    @JsonIgnore @Override
    public void update() {

        super.update();
    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete :: Delete object Id: {} ", this.id);

        if (get_type_of_board() != null) {
            get_type_of_board().cache_bootloaders_id.remove(id);
        }

        cache.remove(id);
        return super.delete();
    }

/* BLOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore  private String azure_product_link;

    @JsonIgnore
    public CloudBlobContainer get_Container() {
        try {

            return Server.blobClient.getContainerReference("bootloaders"); // Jméno kontejneru

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public String get_path() {

        if (azure_product_link == null) {
            this.azure_product_link = get_Container().getName() + "/" + this.id;
            this.update();
        }

        return azure_product_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @ApiModelProperty(required = true) public boolean create_permission() {  return BaseController.person().has_permission("BootLoader_create");}
    @JsonProperty @ApiModelProperty(required = true) public boolean update_permission() {  return BaseController.person().has_permission("BootLoader_update");}
    @JsonIgnore   @ApiModelProperty(required = true) public boolean read_permission()  {  return true; }
    @JsonProperty @ApiModelProperty(required = true) public boolean edit_permission()  {  return BaseController.person().has_permission("BootLoader_read");}
    @JsonProperty @ApiModelProperty(required = true) public boolean delete_permission() {  return BaseController.person().has_permission("BootLoader_delete");}

    public enum Permission { BootLoader_create,  BootLoader_update, BootLoader_read, BootLoader_edit, BootLoader_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE = Model_BootLoader.class.getSimpleName();

    @CacheField(value = Model_BootLoader.class, timeToIdle = 600)
    public static Cache<UUID, Model_BootLoader> cache;

    public static Model_BootLoader getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_BootLoader getById(UUID id) {

        Model_BootLoader bootloader = cache.get(id);
        if (bootloader == null) {

            bootloader = find.byId(id);
            if (bootloader == null) return null;

            cache.put(id, bootloader);
        }

        return bootloader;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_BootLoader> find = new Finder<>(Model_BootLoader.class);
}
