package models;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.annotation.JsonIgnore;
import exceptions.BadRequestException;
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

            S3Object s3Object = Server.space.getObject(Server.bucket_name, this.path);
            return org.apache.commons.io.IOUtils.toString(new BufferedReader(new InputStreamReader(s3Object.getObjectContent())));

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


    public static Model_Blob upload_picture(String b64ImageData, String path) throws Exception {

        String[] base64Components = b64ImageData.split(",");

        if (base64Components.length != 2) {
            throw new BadRequestException("Invalid base64 data: " + b64ImageData);
        }


        String base64Data = base64Components[0];
        String file_type = base64Data.substring(base64Data.indexOf('/') + 1, base64Data.indexOf(';'));
        String file_name = UUID.randomUUID().toString() + "." + file_type;

        if (!(file_type.equals("jpg") ||  file_type.equals("jpeg") ||  file_type.equals("png"))) {
            throw new BadRequestException("Invalid file type: " + file_type);
        }

        byte[] binary_data = org.apache.commons.codec.binary.Base64.decodeBase64((b64ImageData.substring(b64ImageData.indexOf(",")+1)).getBytes());

        return upload(binary_data, file_type, file_name, path);
    }

    public static Model_Blob upload_bin_file(String bin_file, String path) throws Exception {

        String[] base64Components = bin_file.split(",");

        if (base64Components.length != 2) {
            throw new BadRequestException("Invalid base64 data: " + bin_file);
        }


        String base64Data = base64Components[0];
        String file_type = base64Data.substring(base64Data.indexOf('/') + 1, base64Data.indexOf(';'));
        String file_name = UUID.randomUUID().toString() + "." + file_type;

        if (!(file_type.equals("bin"))) {
            throw new BadRequestException("Invalid file type: " + file_type);
        }

        byte[] binary_data = org.apache.commons.codec.binary.Base64.decodeBase64((bin_file.substring(bin_file.indexOf(",")+1)).getBytes());

        return upload(binary_data, file_type, file_name, path);
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
        fileRecord.link = Server.space.getUrl(Server.bucket_name, fileRecord.path).toString();

        // Save Model
        fileRecord.save();

        logger.trace("SpaceDigitalOcean save total path: " +  fileRecord.path);
        logger.trace("SpaceDigitalOcean save total path: " +  fileRecord.link);
        logger.trace("SpaceDigitalOcean save idh: " +  fileRecord.id);

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
        super.save();
    }

    @Override
    public boolean delete() {
        try {

            logger.debug("delete - removing file from blob server, id: {}", this.id);

            if(cache_string_file.containsKey(this.id)) cache_string_file.remove(this.id);

            Server.space.deleteObject(Server.bucket_name, this.path);

            return super.delete();

        } catch (Exception e) {
            // logger.internalServerError(e);
            return false;
        }
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

    @InjectCache(value = String.class, duration = InjectCache.DayCacheConstant, maxElements = 500, name = "Model_Blob_File")
    public static Cache<UUID, String> cache_string_file;

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Blob.class)
    public static CacheFinder<Model_Blob> find = new CacheFinder<>(Model_Blob.class);
}
