package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import io.swagger.annotations.ApiModel;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.enums.NotificationImportance;
import utilities.enums.NotificationLevel;
import utilities.enums.NotificationType;
import utilities.errors.Exceptions._Base_Result_Exception;
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
public class Model_BootLoader extends NamedModel implements Permissible {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_BootLoader.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                          public String version_identifier; // HW identifikator od kluků ve formátu b255.255.255 -> ex. b0.1.6 || b0.1.77
    @Column(columnDefinition = "TEXT")    public String changing_note;

    @JsonIgnore @OneToMany(mappedBy="bootloader",cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_HardwareUpdate> updates = new ArrayList<>();

    @JsonIgnore  @ManyToOne(fetch = FetchType.LAZY)     public Model_HardwareType hardware_type;
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY)       public Model_HardwareType main_hardware_type;

    @JsonIgnore  @OneToMany(mappedBy="actual_boot_loader", fetch = FetchType.LAZY)                 public List<Model_Hardware> hardware = new ArrayList<>();
                 @OneToOne(mappedBy = "boot_loader", cascade = CascadeType.ALL)                    public Model_Blob file;


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @JsonProperty public boolean main_bootloader() {
        try {

            return getMainHardwareType() != null;

        } catch (_Base_Result_Exception e){
            logger.internalServerError(e);
            return false;
        } catch(Exception e){
            logger.internalServerError(e);
            return false;
        }
    }

    @JsonProperty public String  file_path() {
        try {

            if (idCache().get(Model_Blob.class) != null) {
                String link = Model_Blob.find.byId(idCache().get(Model_Blob.class)).getPublicDownloadLink();
                if (link != null) {
                    return link;
                }
            }

            if (file == null) {
                logger.error("File nto exist inside bootloader!");
                return null;
            }

            String total_link = file.getPublicDownloadLink();
            idCache().add(Model_Blob.class, file.id);


            logger.trace("path - total link: {}", total_link);

            // Přesměruji na link
            return total_link;

        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID getHardwareTypeId() {
        if (idCache().get(Model_HardwareType.class) == null) {
            idCache().add(Model_HardwareType.class, (UUID) Model_HardwareType.find.query().where().eq("boot_loaders.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_HardwareType.class);
    }

    @JsonIgnore
    public Model_HardwareType getHardwareType() {
        try {
            return Model_HardwareType.find.byId(getHardwareTypeId());
        }catch (Exception e) {
            return null;
        }
    }

    @JsonIgnore
    public UUID getMainHardwareTypeId() {

        // System.out.println("getMainHardwareTypeId for bootloader " + this.name);

        if (idCache().get(Model_HardwareType.Model_HardwareType_Main.class) == null) { // Záměrně random! Protože potřebuji uložit stejný typ objektu do paměti dvakrát a rozpoznání je jen podle typu třídy

            // System.out.println("getMainHardwareTypeId cache is null " + this.name);

            UUID main = (UUID) Model_HardwareType.find.query().where().eq("main_boot_loader.id", id).select("id").findSingleAttribute();
            if (main != null) {
                logger.warn("getMainHardwareTypeId for bootloader {} is not null Model_HardwareType main ", main);
                idCache().add(Model_HardwareType.Model_HardwareType_Main.class, main);
            } else {
                logger.warn("getMainHardwareTypeId for bootloader {} is null - but its probably ok", this.name);
            }

        }

        return idCache().get(Model_HardwareType.Model_HardwareType_Main.class);
    }

    @JsonIgnore
    public Model_HardwareType getMainHardwareType() {
        try {

            UUID id = getMainHardwareTypeId();
            logger.warn("getMainHardwareType for bootloader {} getMainHardwareTypeId: id {} ", this.name, id);
            if(id != null) {
                return Model_HardwareType.find.byId(id);
            } else  {
                return null;
            }

        }catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    // Bootloader
    @JsonIgnore
    public static void notification_bootloader_procedure_first_information_single(Model_HardwareUpdate plan) {
        try {

            new Model_Notification()
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
                            "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                    .send_under_project(plan.getHardware().get_project_id());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // Bootloader
    @JsonIgnore
    public static void notification_bootloader_procedure_success_information_single(Model_HardwareUpdate plan) {
        try {

            new Model_Notification()
                    .setImportance(NotificationImportance.LOW)
                    .setLevel(NotificationLevel.SUCCESS)
                    .setChainType(NotificationType.CHAIN_START)   // Deliberately -> chain notification for the reason that the user has to clicked on himself for removal .
                    .setNotificationId(plan.id)
                    .setText(new Notification_Text().setText("Success! Bootloader version "))
                    .setText(new Notification_Text().setBoldText().setColor(Becki_color.byzance_red).setText(plan.getBootloader().version_identifier + " "))
                    .setText(new Notification_Text().setText("  is done for device "))
                    .setObject(plan.getHardware())
                    .setText(new Notification_Text().setText(". "))
                    .setText(new Notification_Text().setText("Have a nice Day!"))
                    .send_under_project(plan.getHardware().get_project_id());

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    public static void notification_bootloader_procedure_first_information_list(List<Model_HardwareUpdate> plans) {
        try {

            new Thread(() -> {

                if ( plans.size() == 0 )  throw new IllegalArgumentException("notification_set_static_backup_procedure_first_information_list:: List is empty! ");
                if ( plans.size() == 1 ) {
                    notification_bootloader_procedure_first_information_single(plans.get(0));
                    return;
                }

                new Model_Notification()
                        .setImportance(NotificationImportance.LOW)
                        .setLevel(NotificationLevel.WARNING)
                        .setChainType(NotificationType.CHAIN_START)   // Deliberately -> chain notification for the reason that the user has to clicked on himself for removal .
                        .setNotificationId(plans.get(0).getActualizationProcedureId())
                        .setText(new Notification_Text().setText("Attention. I have entered the bootloader update command for Bootloader version "))
                        .setText(new Notification_Text().setBoldText().setColor(Becki_color.byzance_red).setText(plans.get(0).getBootloader().version_identifier + " "))
                        .setText(new Notification_Text().setText("for " + plans.size() + " devices. "))
                        .setText(new Notification_Text().setText("Bootloader update is a critical action. " +
                                "Do not disconnect the device from the power supply during the update. " +
                                "The critical time to update is 3 seconds on average. Wait for confirmation of the notification please! " +
                                "We show you in hardware overview only what's currently on the device. " +
                                "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                                "In the details of the instance or hardware overview, you can see the status of each procedure. " +
                                "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                        .send_under_project(plans.get(0).getHardware().get_project_id());

            }).start();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
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
    public void update() {

        super.update();
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

    @Override
    public EntityType getEntityType() {
        return EntityType.BOOTLOADER;
    }

    @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_BootLoader.class)
    public static CacheFinder<Model_BootLoader> find = new CacheFinder<>(Model_BootLoader.class);
}
