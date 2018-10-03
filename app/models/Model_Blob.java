package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.azure.storage.blob.*;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.beans.Transient;
import java.io.*;
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
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "file")        public Model_BootLoader boot_loader;
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

    @JsonIgnore
    public String getPublicDownloadLink() {
        try {

            if (cache_public_link.containsKey(this.id)) {
                return cache_public_link.get(this.id);
            }

            long start = System.currentTimeMillis();

            // Separace na Container a Blob
            int slash = path.indexOf("/");
            String container_name = path.substring(0, slash);
            String real_file_path = path.substring(slash + 1);

            CloudAppendBlob blob = Server.blobClient.getContainerReference(container_name).getAppendBlobReference(real_file_path);

            // Create Policy
            Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            cal.setTime(new Date());
            cal.add(Calendar.HOUR, 32);

            SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
            policy.setSharedAccessExpiryTime(cal.getTime());

            String sas = blob.generateSharedAccessSignature(policy, null);

            logger.info("getPublicDownloadLink - generating link took {} ms", System.currentTimeMillis() - start);

            String total_link = blob.getUri().toString() + "?" + sas;

            cache_public_link.put(this.id, total_link);

            logger.trace("getPublicDownloadLink - link: {} ", total_link);
            return total_link;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public String downloadString() {
        try {

            if (cache_string_file.containsKey(this.id)) {
                return cache_string_file.get(this.id);
            }

            int slash = path.indexOf("/");
            String container_name = path.substring(0,slash);
            String real_file_path = path.substring(slash+1);

            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name );
            CloudBlockBlob blob = container.getBlockBlobReference(real_file_path);
            String file = blob.downloadText();

            cache_string_file.put(this.id, file);

            return file;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public static Model_Blob upload(String content, String name, String path) throws Exception {
        return upload(content.getBytes(), name, path);
    }

    @JsonIgnore
    public static Model_Blob upload(File file, String name, String path) throws Exception{
        return upload(new FileInputStream(file), name, path);
    }

    @JsonIgnore
    public static Model_Blob upload(byte[] file, String name, String path) throws Exception {
        return upload(new ByteArrayInputStream(file), name, path);
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

    /**
     * Core method for uploading files to blob server.
     * @param inputStream from the file that is uploaded
     * @param name of the file
     * @param path to the file
     * @return new Model_Blob reference
     * @throws Exception when some error occur during upload
     */
    private static Model_Blob upload(InputStream inputStream, String name, String path) throws Exception {

        int slash = path.indexOf("/");

        CloudBlobContainer container = Server.blobClient.getContainerReference(path.substring(0, slash)); // Get container reference for container name from path
        CloudBlockBlob blob = container.getBlockBlobReference(path.substring(slash + 1) + "/" + name); // Path after container

        blob.upload(inputStream, -1);

        Model_Blob modelBlob = new Model_Blob();
        modelBlob.name = name;
        modelBlob.path = path + "/" + name;
        logger.trace("Azure save total path: " +  modelBlob.path);
        modelBlob.save();

        return modelBlob;
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

            cache_public_link.remove(this.id);
            cache_string_file.remove(this.id);

            this.removeBlob();
        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public static String get_path_for_bin() throws Exception {

        CloudBlobContainer container = Server.blobClient.getContainerReference("bin-files");
        return container.getName() + "/" + UUID.randomUUID().toString();
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = String.class, duration = CacheField.DayCacheConstant, maxElements = 1000, automaticProlonging = false, name = "Model_Blob_Link")
    public static Cache<UUID, String> cache_public_link;

    @CacheField(value = String.class, duration = CacheField.DayCacheConstant, maxElements = 500, name = "Model_Blob_File")
    public static Cache<UUID, String> cache_string_file;

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Blob.class)
    public static CacheFinder<Model_Blob> find = new CacheFinder<>(Model_Blob.class);
}
