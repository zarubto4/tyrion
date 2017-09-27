package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.enums.Enum_Notification_importance;
import utilities.enums.Enum_Notification_level;
import utilities.enums.Enum_Notification_type;
import utilities.logger.Class_Logger;
import utilities.notifications.helps_objects.Becki_color;
import utilities.notifications.helps_objects.Notification_Text;
import utilities.response.GlobalResult;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.Executors;

@Entity
@ApiModel( value = "BootLoader", description = "Model of BootLoader")
@Table(name="BootLoader")
public class Model_BootLoader extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_BootLoader.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @ApiModelProperty(required = true)    public UUID id;

    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in ms",
            example = "1466163478925")    public Date date_of_create;

                                          public String name;
    @Column(columnDefinition = "TEXT")    public String description;
                                          public String version_identificator; // HW identifikator od kluků ve formátu b255.255.255 -> ex. b0.1.6 || b0.1.77
    @Column(columnDefinition = "TEXT")    public String changing_note;


    @JsonIgnore @OneToMany(mappedBy="bootloader",cascade=CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_CProgramUpdatePlan> c_program_update_plans = new ArrayList<>();

    @JsonIgnore  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)                public Model_TypeOfBoard type_of_board;
                                                           @JsonIgnore  @OneToOne()                public Model_TypeOfBoard main_type_of_board;

    @JsonIgnore  @OneToMany(mappedBy="actual_boot_loader")                                         public List<Model_Board> boards  = new ArrayList<>();
                 @OneToOne(mappedBy = "boot_loader", cascade = CascadeType.ALL)                    public Model_FileRecord file;


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    @Transient @JsonProperty public boolean main_bootloader(){ return main_type_of_board != null;}
    @Transient @JsonProperty public String  file_path(){
        try {

            if (file == null) {
                return null;
            }

            // Separace na Container a Blob
            int slash = file.file_path.indexOf("/");
            String container_name = file.file_path.substring(0, slash);
            String real_file_path = file.file_path.substring(slash + 1);

            CloudAppendBlob blob = Server.blobClient.getContainerReference(container_name).getAppendBlobReference(real_file_path);

            // Create Policy
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, 24);

            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
            policy.setSharedAccessExpiryTime(cal.getTime());


            String sas = blob.generateSharedAccessSignature(policy, null);

            System.out.println("sas " + sas);
            System.out.println("path blobu " + blob.getUri().getPath());


            String total_link = blob.getUri().toString() + "?" + sas;

            terminal_logger.debug("cloud_file_get_bootloader_version:: Total Link:: " + total_link);

            // Přesměruji na link
            return total_link;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }



/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/


/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/



    // Bootloader
    @JsonIgnore
    public static void notification_bootloader_procedure_first_information_single(Model_CProgramUpdatePlan plan){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.low)
                    .setLevel(Enum_Notification_level.warning)
                    .setChainType(Enum_Notification_type.CHAIN_START)   // Deliberately -> chain notification for the reason that the user has to clicked on himself for removal .
                    .setId(plan.id.toString())
                    .setText(new Notification_Text().setText("Attention. You have entered the bootloader update command for Bootloader version "))
                    .setText(new Notification_Text().setBoldText().setColor(Becki_color.byzance_red).setText(plan.bootloader.version_identificator + " "))
                    .setText(new Notification_Text().setText(" for device "))
                    .setObject(plan.board)
                    .setText(new Notification_Text().setText(". "))
                    .setText(new Notification_Text().setText("Bootloader update is a critical action. " +
                            "Do not disconnect the device from the power supply during the update. " +
                            "The critical time to update is 3 seconds on average. Wait for confirmation of the notification please! "))
                    .setnewLine()
                    .setText(new Notification_Text().setText("We show you in hardware overview only what's currently on the device. " +
                            "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                            "In the details of the instance or hardware overview, you can see the status of each procedures. " +
                            "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                    .send_under_project(plan.board.project_id());

        } catch (Exception e) {
            terminal_logger.internalServerError("notification_bootloader_procedure_first_information_single:", e);
        }
    }

    // Bootloader
    @JsonIgnore
    public static void notification_bootloader_procedure_success_information_single(Model_CProgramUpdatePlan plan){
        try {

            new Model_Notification()
                    .setImportance(Enum_Notification_importance.low)
                    .setLevel(Enum_Notification_level.success)
                    .setChainType(Enum_Notification_type.CHAIN_START)   // Deliberately -> chain notification for the reason that the user has to clicked on himself for removal .
                    .setId(plan.id.toString())
                    .setText(new Notification_Text().setText("Success! Bootloader version "))
                    .setText(new Notification_Text().setBoldText().setColor(Becki_color.byzance_red).setText(plan.bootloader.version_identificator + " "))
                    .setText(new Notification_Text().setText("  is done for device "))
                    .setObject(plan.board)
                    .setText(new Notification_Text().setText(". "))
                    .setText(new Notification_Text().setText("Have a nice Day!"))
                    .send_under_project(plan.board.project_id());


        } catch (Exception e) {
            terminal_logger.internalServerError("notification_bootloader_procedure_first_information_single:", e);
        }
    }





    @JsonIgnore
    public static void notification_bootloader_procedure_first_information_list(List<Model_CProgramUpdatePlan> plans){
        try {

            new Thread( () -> {

                if( plans.size() == 0 )  throw new IllegalArgumentException("notification_set_static_backup_procedure_first_information_list:: List is empty! ");
                if( plans.size() == 1 ) {
                    notification_bootloader_procedure_first_information_single(plans.get(0));
                    return;
                }

                new Model_Notification()
                        .setImportance(Enum_Notification_importance.low)
                        .setLevel(Enum_Notification_level.warning)
                        .setChainType(Enum_Notification_type.CHAIN_START)   // Deliberately -> chain notification for the reason that the user has to clicked on himself for removal .
                        .setId(plans.get(0).actualization_procedure.id.toString())
                        .setText(new Notification_Text().setText("Attention. I have entered the bootloader update command for Bootloader version "))
                        .setText(new Notification_Text().setBoldText().setColor(Becki_color.byzance_red).setText(plans.get(0).bootloader.version_identificator + " "))
                        .setText(new Notification_Text().setText("for " + plans.size() + " devices. "))
                        .setText(new Notification_Text().setText("Bootloader update is a critical action. " +
                                "Do not disconnect the device from the power supply during the update. " +
                                "The critical time to update is 3 seconds on average. Wait for confirmation of the notification please! " +
                                "We show you in hardware overview only what's currently on the device. " +
                                "Each update is assigned to the queue of tasks and will be made as soon as possible or according to schedule. " +
                                "In the details of the instance or hardware overview, you can see the status of each procedure. " +
                                "If the update command was not time-specific (immediately) and the device is online, the data transfer may have already begun."))
                        .send_under_project(plans.get(0).board.project_id());

            }).start();

        } catch (Exception e) {
            terminal_logger.internalServerError("notification_bootloader_procedure_first_information_list:", e);
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override public void save() {

        terminal_logger.debug("save :: Creating new Object");

        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {} ", this.id);

        super.update();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.id);

        super.delete();
    }

/* BLOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore  private String azure_product_link;

    @JsonIgnore @Transient
    public CloudBlobContainer get_Container(){
        try {

            return Server.blobClient.getContainerReference("bootloaders"); // Jméno kontejneru

        }catch (Exception e){
            terminal_logger.internalServerError("get_Container:", e);
            return null;
        }
    }

    @JsonIgnore @Transient
    public String get_path(){

        if (azure_product_link == null) {
            this.azure_product_link = get_Container().getName() + "/" + this.id;
            this.update();
        }

        return azure_product_link;
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean create_permission(){  return Controller_Security.get_person().has_permission("BootLoader_create");}
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean update_permission(){  return Controller_Security.get_person().has_permission("BootLoader_update");}
    @JsonIgnore   @Transient  @ApiModelProperty(required = true) public boolean read_permission()  {  return true; }
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.get_person().has_permission("BootLoader_read");}
    @JsonProperty @Transient  @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.get_person().has_permission("BootLoader_delete");}

    public enum permissions{  BootLoader_create,  BootLoader_update, BootLoader_read ,  BootLoader_edit, BootLoader_delete; }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_BootLoader get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);

    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_BootLoader> find = new Finder<>(Model_BootLoader.class);

}
