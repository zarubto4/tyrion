package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.beans.Transient;
import java.io.*;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;

@Entity
@ApiModel( value = "Blob", description = "Model of Blob")
@Table(name="Blob")
public class Model_Blob extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Blob.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String name;

    @JsonIgnore
    public String path;

    @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "picture")     public Model_Person person;
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "program")     public Model_InstanceSnapshot snapshot;  // personal_picture
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "picture")     public Model_HardwareType hardware_type; // picture
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "picture")     public Model_Hardware hardware;          // private board_picture
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "file")        public Model_Log log;
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY)                           public Model_BootLoader boot_loader;
    @JsonIgnore @OneToMany(mappedBy="binary_file",fetch = FetchType.LAZY)   public List<Model_HardwareUpdate> updates = new ArrayList<>();
    @JsonIgnore @OneToOne(mappedBy="blob")                                  public Model_Compilation c_compilations_binary_file;

    @JsonIgnore @OneToOne(mappedBy = "file", fetch = FetchType.LAZY)    public Model_CProgramVersion    c_program_version;
    @JsonIgnore @OneToOne(mappedBy = "file", fetch = FetchType.LAZY)    public Model_LibraryVersion     library_version;
    @JsonIgnore @OneToOne(mappedBy = "file", fetch = FetchType.LAZY)    public Model_BProgramVersion    b_program_version;
    @JsonIgnore @OneToOne(mappedBy = "file", fetch = FetchType.LAZY)    public Model_GridProgramVersion grid_program_version;
    @JsonIgnore @OneToOne(mappedBy = "file", fetch = FetchType.LAZY)    public Model_WidgetVersion      widget_version;
    @JsonIgnore @OneToOne(mappedBy = "file", fetch = FetchType.LAZY)    public Model_BlockVersion       block_version;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


    @JsonIgnore
    public String  get_file_path_for_direct_download() {
        return Server.azure_blob_Link + path;
    }


    /*
    * Ľink by šlo v rámci úspor cachovat. To jest uložit si pod ID objektu jeho link a dát mu platnost - pokud ho v cahce nenajdu
    * vytvořím link, pokud ano z cache vracím.
    * Teoreticky mužu mít variabliblní proměnou (vstup metody) do které dám v čas jak dluho se má v chache paměti / na jak dlouho se má vygenerovat link
    * // 30 sekund nic mocMM
    * */

    @JsonIgnore
    public String getPublicDownloadLink(int second) {
        try {

            // Separace na Container a Blob
            int slash = path.indexOf("/");
            String container_name = path.substring(0, slash);
            String real_file_path = path.substring(slash + 1);

            CloudAppendBlob blob = Server.blobClient.getContainerReference(container_name).getAppendBlobReference(real_file_path);

            // Create Policy
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            cal.setTime(new Date());
            cal.add(Calendar.SECOND, second);

            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
            policy.setSharedAccessExpiryTime(cal.getTime());


            String sas = blob.generateSharedAccessSignature(policy, null);

            String total_link = blob.getUri().toString() + "?" + sas;

            // TODO Cache total_link
            logger.trace("Direct download link:: {} ", total_link);
            return total_link;

        } catch (Exception e) {
            e.printStackTrace();

            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public String get_fileRecord_from_Azure_inString() {
        try {

            int slash = path.indexOf("/");
            String container_name = path.substring(0,slash);
            String real_file_path = path.substring(slash+1);

            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name );

            CloudBlob blob = container.getBlockBlobReference(real_file_path );


            InputStream input =  blob.openInputStream();
            InputStreamReader inr = new InputStreamReader(input, "UTF-8");
            String utf8str = org.apache.commons.io.IOUtils.toString(inr);

            return utf8str;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public static Model_Blob upload(String content, String name, String path) throws Exception {

        logger.debug("upload - path: {}", path);

        int slash = path.indexOf("/");

        CloudBlobContainer container = Server.blobClient.getContainerReference(path.substring(0, slash)); // Get container reference for container name from path
        CloudBlockBlob blob = container.getBlockBlobReference(path.substring(slash + 1) + "/" + name); // Path after container

        InputStream is = new ByteArrayInputStream(content.getBytes());
        blob.upload(is, -1);

        Model_Blob fileRecord = new Model_Blob();
        fileRecord.name = name;
        fileRecord.path = path + "/" + name;
        logger.trace("Azure save total path: " +  fileRecord.path);
        fileRecord.save();

        return fileRecord;
    }

    @JsonIgnore
    public static Model_Blob upload(File file, String name, String path) throws Exception{

        logger.debug("upload - path: {}", path);

        int slash = path.indexOf("/");

        CloudBlobContainer container = Server.blobClient.getContainerReference(path.substring(0, slash)); // Get container reference for container name from path
        CloudBlockBlob blob = container.getBlockBlobReference(path.substring(slash + 1) + "/" + name); // Path after container

        InputStream is = new FileInputStream(file);
        blob.upload(is, -1);

        Model_Blob fileRecord = new Model_Blob();
        fileRecord.name = name;
        fileRecord.path = path + "/" + name;
        logger.trace("Azure save total path: " +  fileRecord.path);
        fileRecord.save();

        return fileRecord;
    }

    @JsonIgnore
    public static Model_Blob upload(byte[] file, String name, String path) throws Exception{

        logger.debug("upload - path: {}", path);

        int slash = path.indexOf("/");

        CloudBlobContainer container = Server.blobClient.getContainerReference(path.substring(0, slash)); // Get container reference for container name from path
        CloudBlockBlob blob = container.getBlockBlobReference(path.substring(slash + 1) + "/" + name); // Path after container

        logger.trace("Azure save path: " + path );
        logger.trace("Azure Container: " + blob.getName());

        InputStream is = new ByteArrayInputStream(file);
        blob.upload(is, -1);

        Model_Blob fileRecord = new Model_Blob();
        fileRecord.name = name;
        fileRecord.path = path + "/" + name;

        logger.trace("Azure save total path: " +  fileRecord.path);
        fileRecord.save();

        return fileRecord;
    }

    @JsonIgnore
    public static Model_Blob upload(String file, String contentType, String name, String path) throws Exception{

        byte[] bytes = Model_Blob.get_decoded_binary_string_from_Base64(file);

        logger.trace("Azure file path  ::" + path);
        logger.trace("Azure file name  ::" + name);
        logger.trace("Azure contentType::" + contentType);

        int slash = path.indexOf("/");

        CloudBlobContainer container = Server.blobClient.getContainerReference(path.substring(0, slash)); // Get container reference for container name from path
        CloudBlockBlob blob = container.getBlockBlobReference(path.substring(slash + 1) + "/" + name); // Path after container

        InputStream is = new ByteArrayInputStream(bytes);
        blob.getProperties().setContentType(contentType);
        blob.upload(is, -1);

        Model_Blob fileRecord = new Model_Blob();
        fileRecord.name = name;
        fileRecord.path = path + "/" + name;

        logger.trace("Azure save total path: " +  fileRecord.path);
        fileRecord.save();

        return fileRecord;
    }

    @JsonIgnore
    public void removeBlob() {
        try {

            int slash =  path.indexOf("/");
            String container_name =  path.substring(0, slash);
            String file_path_slash =  path.substring(slash+1);

            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
            CloudBlob blob = container.getBlockBlobReference(file_path_slash);
            blob.delete();

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    /**
     *  Metoda slouží k rekurzivnímu procháázení úrovně adresáře v Azure data storage a mazání jeho obsahu.
     *  Azure data storage je totiž jednoúrovňové datové skladiště! KJde není možné vytvářet složky, v nich složky
     *  a do nich dávat soubory. Jménbo souboru sice má podobu neco/neco2/něco3/soubor.txt ale je to přímá cesta.
     *  Proto když je potřeba promazat něco v něco a všechno dál, je nutné nasadit rekurzivní algoritmus, který se
     *  podívá na obsah a vše promazává.
     *
     */
    @JsonIgnore
    public static void azureDelete(CloudBlobContainer container, String pocatekMazani) throws Exception {

        for (ListBlobItem blobItem : container.listBlobs( pocatekMazani + "/" )) {

            if (blobItem instanceof CloudBlob) ((CloudBlob) blobItem).deleteIfExists();

            // Break & loop
            String help = blobItem.getUri().toString().substring(0,blobItem.getUri().toString().length() -1);

            azureDelete(container, pocatekMazani +  help.substring( help.lastIndexOf("/") ,help.length()) );
        }
    }

    @JsonIgnore
    public static byte[] get_decoded_binary_string_from_Base64(String binary_file) throws Exception {

        return Base64.getDecoder().decode(binary_file.getBytes());
    }

    @JsonIgnore
    public static String get_encoded_binary_string_from_body(byte[] bytes) throws Exception {
        return new String(Base64.getEncoder().encode( bytes ));
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @Override
    public boolean delete() {
        try {
            logger.debug("delete - removing file from blob server, id: {}", this.id);
            this.removeBlob();
        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public String cache_public_link() throws StorageException, URISyntaxException, InvalidKeyException {

        // Separace na Container a Blob
        int slash = path.indexOf("/");
        String container_name = path.substring(0, slash);
        String real_file_path = path.substring(slash + 1);

        CloudAppendBlob blob = Server.blobClient.getContainerReference(container_name).getAppendBlobReference(real_file_path);

        // Create Policy
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, 24);

        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
        policy.setSharedAccessExpiryTime(cal.getTime());

        String sas = blob.generateSharedAccessSignature(policy, null);

        String total_link = blob.getUri().toString() + "?" + sas;

        Model_Blob.cache_public_link.put(id, total_link);

        return total_link;
    }
/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient
    public static String get_path_for_bin() throws Exception {

        CloudBlobContainer container = Server.blobClient.getContainerReference("bin-files");
        return container.getName() + "/" + UUID.randomUUID().toString();

    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        //true
    }

    // Create Permission is always JsonIgnore
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        //true
    }

    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_update_" + id)) _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_update_" + id);
        if (_BaseController.person().has_permission(Model_BProgram.Permission.BProgram_delete.name())) return;
        check_delete_permission();
    }

    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {

        if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_delete_" + this.id)) _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_delete_" + this.id);
        if (_BaseController.person().has_permission(Model_BProgram.Permission.BProgram_delete.name())) return;


        UUID id = null;

        id = Model_Person.find.query().where().eq("picture.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_Person.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_InstanceSnapshot.find.query().where().eq("program.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_InstanceSnapshot.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_HardwareType.find.query().where().eq("picture.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_HardwareType.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_Hardware.find.query().where().eq("picture.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_Hardware.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_Log.find.query().where().eq("file.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_Log.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_BootLoader.find.query().where().eq("file.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_BootLoader.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_HardwareUpdate.find.query().where().eq("binary_file.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_HardwareUpdate.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_Compilation.find.query().where().eq("blob.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_Compilation.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_CProgramVersion.find.query().where().eq("file.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_CProgramVersion.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_LibraryVersion.find.query().where().eq("file.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_LibraryVersion.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_BProgramVersion.find.query().where().eq("file.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_BProgramVersion.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_GridProgramVersion.find.query().where().eq("file.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_GridProgramVersion.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_WidgetVersion.find.query().where().eq("file.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_WidgetVersion.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        id = Model_BlockVersion.find.query().where().eq("file.id", this.id).select("id").findSingleAttribute();

        if(id != null) {
            Model_BlockVersion.getById(id).check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, true);
            return;
        }

        _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + this.id, false);
        logger.error("Unsupported object in Model Blob when system tried to remove this object!");
        throw new Result_Error_NotSupportedException();
    }

    public enum Permission { Blob_create, Blob_read, Blob_update, Blob_edit, Blob_delete }

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = String.class, duration = CacheField.HalfDayCacheConstant, maxElements = 500, automaticProlonging = false)
    public static Cache<UUID, String> cache_public_link;

    public static Model_Blob getById(UUID id) throws _Base_Result_Exception  {

        return Model_Blob.find.byId(id);

    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Blob> find = new Finder<>(Model_Blob.class);
}
