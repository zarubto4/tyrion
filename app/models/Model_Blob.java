package models;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microsoft.azure.storage.blob.*;
import io.swagger.annotations.ApiModel;
import org.apache.http.annotation.Obsolete;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
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

    @JsonIgnore
    public String link;

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


    @JsonIgnore public String storage_type; // Supported AWS_DigitalOcean or AzureBlob

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Obsolete
    @Deprecated
    public String downloadString() {
        try {

            if(storage_type.equals("AzureBlob")) {

                if (cache_string_file.containsKey(this.id)) {
                    return cache_string_file.get(this.id);
                }

                int slash = path.indexOf("/");
                String container_name = path.substring(0, slash);
                String real_file_path = path.substring(slash + 1);

                System.out.println("AzureBlob file ID:: " + this.id);
                System.out.println("AzureBlob container_name:: " + container_name);
                System.out.println("AzureBlob real_file_path:: " + real_file_path);

                CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
                CloudBlockBlob blob = container.getBlockBlobReference(real_file_path);
                String file = blob.downloadText();

                cache_string_file.put(this.id, file);

                return file;

            } else {
                S3Object s3Object = Server.space.getObject(Server.bucket_name, this.path);
                return org.apache.commons.io.IOUtils.toString(new BufferedReader(new InputStreamReader(s3Object.getObjectContent())));
            }

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }



    @JsonIgnore
    public static Model_Blob upload(File file, String content_type, String name, String path) throws Exception {
        return upload(new FileInputStream(file), content_type, file.length(), name, path);
    }

    @JsonIgnore
    public static Model_Blob upload(String file, String content_type, String name, String path) throws Exception{
        return upload(file.getBytes(), content_type, name, path);
    }

    @JsonIgnore
    public static Model_Blob upload(byte[] file, String content_type, String name, String path) throws Exception {
        return upload(new ByteArrayInputStream(file), content_type, (long) file.length, name, path);
    }


    /**
     * Core method for uploading files to blob server.
     * @param inputStream from the file that is uploaded
     * @param name of the file
     * @param path to the file
     * @return new Model_Blob reference
     * @throws Exception when some error occur during upload
     */
    private static Model_Blob upload(InputStream inputStream, String contentType, Long size, String name, String path) throws Exception {

        // Create Metadata
        ObjectMetadata om = new ObjectMetadata();
        om.setContentLength(size);
        om.setContentType(contentType);


        // Save it to Server
        Server.space.putObject(Server.bucket_name, path + "/" + name, inputStream, om);
        Server.space.setObjectAcl(Server.bucket_name, path + "/" + name, CannedAccessControlList.PublicRead);

        // Create Model
        Model_Blob fileRecord = new Model_Blob();
        fileRecord.name = name;
        fileRecord.path = path + "/" + name;
        fileRecord.link = Server.space.getUrl(Server.bucket_name, path + "/" + name).toString();

        // Save Model
        fileRecord.save();

        logger.trace("SpaceDigitalOcean save total path: " +  fileRecord.path);
        logger.trace("SpaceDigitalOcean save total path: " +  fileRecord.link);

        return fileRecord;
    }

    /**
     * Make a duplication on Path
     * @param original
     * @param new_path
     * @return
     * @throws Exception
     */
    public static Model_Blob duplicate(Model_Blob original, String new_path) throws Exception {

        Model_Blob duplicate = new Model_Blob();
        duplicate.name = original.name;
        duplicate.path = original.path;

        Server.space.copyObject(Server.bucket_name, original.path, Server.bucket_name, new_path);
        Server.space.setObjectAcl(Server.bucket_name, duplicate.path, CannedAccessControlList.PublicRead);

        duplicate.link = Server.space.getUrl(Server.bucket_name, duplicate.path).toString();
        duplicate.save();

        return duplicate;
    }

    @JsonIgnore
    public static byte[] get_decoded_binary_string_from_Base64(String binary_file) {
        return Base64.getDecoder().decode(binary_file.getBytes());
    }

    @JsonIgnore
    public static String get_encoded_binary_string_from_body(byte[] bytes) {
        return new String(Base64.getEncoder().encode( bytes ));
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/


    @Override
    public void save() {
        storage_type = "AWS_DigitalOcean";
    }

    @Override
    public boolean delete() {
        try {
            logger.debug("delete - removing file from blob server, id: {}", this.id);

            cache_public_link.remove(this.id);
            cache_string_file.remove(this.id);

            Server.space.deleteObject(Server.bucket_name, this.path);
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
        return "bin-files" + "/" + UUID.randomUUID().toString();
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @InjectCache(value = String.class, duration = InjectCache.DayCacheConstant, maxElements = 1000, automaticProlonging = false, name = "Model_Blob_Link")
    public static Cache<UUID, String> cache_public_link;

    @InjectCache(value = String.class, duration = InjectCache.DayCacheConstant, maxElements = 500, name = "Model_Blob_File")
    public static Cache<UUID, String> cache_string_file;

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Blob.class)
    public static CacheFinder<Model_Blob> find = new CacheFinder<>(Model_Blob.class);
}
