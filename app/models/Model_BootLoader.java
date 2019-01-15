package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import io.swagger.annotations.ApiModel;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationLevel;
import utilities.enums.NotificationType;
import utilities.hardware.update.Updatable;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.*;
import java.util.*;

@Entity
@ApiModel( value = "BootLoader", description = "Model of BootLoader")
@Table(name="BootLoader")
public class Model_BootLoader extends NamedModel implements Permissible, Updatable {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BootLoader.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                          public String version_identifier; // HW identifikator od kluků ve formátu b255.255.255 -> ex. b0.1.6 || b0.1.77
    @Column(columnDefinition = "TEXT")    public String changing_note;

    @JsonIgnore @OneToMany(mappedBy="bootloader",cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_HardwareUpdate> updates = new ArrayList<>();

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)      public Model_HardwareType hardware_type;
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY)       public Model_HardwareType main_hardware_type;

    @JsonIgnore  @OneToMany(mappedBy="actual_boot_loader", fetch = FetchType.LAZY)  public List<Model_Hardware> hardware = new ArrayList<>();
    @JsonIgnore  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)       public Model_Blob file;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty public boolean main_bootloader() {
        try {
            return getMainHardwareType() != null;
        } catch(Exception e){
            logger.internalServerError(e);
            return false;
        }
    }

    @JsonProperty
    public String  file_path() {
        try {
            return getBlob().link;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_Blob getBlob() {
        return isLoaded("file") ? file : Model_Blob.find.query().where().eq("boot_loader.id", id).findOne();
    }

    @JsonIgnore
    public UUID getHardwareTypeId() {
        if (idCache().get(Model_HardwareType.class) == null) {
            idCache().add(Model_HardwareType.class, (UUID) Model_HardwareType.find.query().where().eq("boot_loaders.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_HardwareType.class);
    }

    @JsonIgnore
    public Model_HardwareType getHardwareType() {
        return isLoaded("hardware_type") ? hardware_type : Model_HardwareType.find.query().where().eq("boot_loaders.id", id).findOne();
    }

    @JsonIgnore
    public Model_HardwareType getMainHardwareType() {
        return isLoaded("main_hardware_type") ? main_hardware_type : Model_HardwareType.find.query().nullable().where().eq("main_boot_loader.id", id).findOne();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    // Bootloader
    @JsonIgnore
    public static Model_Notification notification_bootloader_procedure_first_information_single(Model_HardwareUpdate plan) {
        return new Model_Notification()
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.WARNING)
                .setChainType(NotificationType.CHAIN_START)   // Deliberately -> chain notification for the reason that the user has to clicked on himself for removal .
                .setNotificationId(plan.id)
                .setText(new Notification_Text().setText("Attention. You have entered the bootloader update command for Bootloader version "))
                .setText(new Notification_Text().setBoldText().setColor(Becki_color.byzance_red).setText(plan.getBootloader().version_identifier + " "))
                .setText(new Notification_Text().setText(" for device "))
                .setObject(plan.getHardware())
                .setText(new Notification_Text().setText(". "))
                .setText(new Notification_Text().setText("Bootloader update is a critical action. " +
                        "Do not disconnect the device from the power supply during the update. " +
                        "The critical time to update is 3 seconds on average. Wait for confirmation of the notification please! "))
                .setNewLine()
                .setText(new Notification_Text().setText("We show you in hardware overview only what's currently on the device. " +
                        "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                        "In the details of the instance or hardware overview, you can see the status of each procedures. " +
                        "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."));
    }

    // Bootloader
    @JsonIgnore
    public static Model_Notification notification_bootloader_procedure_success_information_single(Model_HardwareUpdate plan) {
        return new Model_Notification()
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.SUCCESS)
                .setChainType(NotificationType.CHAIN_START)   // Deliberately -> chain notification for the reason that the user has to clicked on himself for removal .
                .setNotificationId(plan.id)
                .setText(new Notification_Text().setText("Success! Bootloader version "))
                .setText(new Notification_Text().setBoldText().setColor(Becki_color.byzance_red).setText(plan.getBootloader().version_identifier + " "))
                .setText(new Notification_Text().setText("  is done for device "))
                .setObject(plan.getHardware())
                .setText(new Notification_Text().setText(". "))
                .setText(new Notification_Text().setText("Have a nice Day!"));
    }

    @JsonIgnore
    public static Model_Notification notification_bootloader_procedure_first_information_list(List<Model_HardwareUpdate> plans) {

        if ( plans.size() == 0 )  throw new IllegalArgumentException("notification_bootloader_procedure_first_information_list:: List is empty! ");
        if ( plans.size() == 1 ) {
            return notification_bootloader_procedure_first_information_single(plans.get(0));
        }

        return new Model_Notification()
                .setImportance(NotificationImportance.LOW)
                .setLevel(NotificationLevel.WARNING)
                .setChainType(NotificationType.CHAIN_START)   // Deliberately -> chain notification for the reason that the user has to clicked on himself for removal .
                .setNotificationId(plans.get(0).getId())
                .setText(new Notification_Text().setText("Attention. I have entered the bootloader update command for Bootloader version "))
                .setText(new Notification_Text().setBoldText().setColor(Becki_color.byzance_red).setText(plans.get(0).getBootloader().version_identifier + " "))
                .setText(new Notification_Text().setText("for " + plans.size() + " devices. "))
                .setText(new Notification_Text().setText("Bootloader update is a critical action. " +
                        "Do not disconnect the device from the power supply during the update. " +
                        "The critical time to update is 3 seconds on average. Wait for confirmation of the notification please! " +
                        "We show you in hardware overview only what's currently on the device. " +
                        "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                        "In the details of the instance or hardware overview, you can see the status of each procedure. " +
                        "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."));
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        super.save();

        if (getHardwareType() != null) {
            getHardwareType().boot_loaders();
            getHardwareType().idCache().add(this.getClass(), id);
        }
    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete :: Delete object Id: {} ", this.id);

        if (getHardwareType() != null) {
            getHardwareType().idCache().remove(this.getClass(), id);
        }

        return super.delete();
    }

/* BLOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore  private String azure_product_link;

    @JsonIgnore
    public String get_path() {

        if (azure_product_link == null) {
            this.azure_product_link = "bootloaders";
            this.update();
        }

        return azure_product_link;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.BOOTLOADER;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_BootLoader.class)
    public static CacheFinder<Model_BootLoader> find = new CacheFinder<>(Model_BootLoader.class);
}
