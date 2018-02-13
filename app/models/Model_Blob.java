package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.storage.blob.*;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.model.VersionModel;

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
    public String path; // TODO rozdělit path na container a real file path

    @JsonIgnore
    public String container; // TODO

    @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "picture")     public Model_Person person;
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "program")     public Model_InstanceSnapshot snapshot;  // personal_picture
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "picture")     public Model_HardwareType hardware_type; // picture
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "picture")     public Model_Hardware hardware;          // private board_picture
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "file")        public Model_Log log;
    @JsonIgnore @OneToOne(fetch = FetchType.LAZY)                           public Model_BootLoader boot_loader;
    @JsonIgnore @OneToMany(mappedBy="binary_file",fetch = FetchType.LAZY)   public List<Model_HardwareUpdate> updates = new ArrayList<>();
    @JsonIgnore @OneToOne(mappedBy="blob")                                  public Model_Compilation c_compilations_binary_file;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                          public Model_CProgramVersion c_program_version;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                          public Model_LibraryVersion  library_version;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                          public Model_BProgramVersion b_program_version;
    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                          public Model_BProgramVersion grid_program_version;

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
        fileRecord.path = path;
        fileRecord.save();

        return fileRecord;
    }

    @JsonIgnore
    public static Model_Blob uploadAzure_Version(String file_content, String file_name, String file_path, VersionModel version) throws Exception{
        try {

            logger.debug("uploadAzure_Version:: Azure upload: "+ file_path + version.get_path() + "/" + file_name );

            int slash = file_path.indexOf("/");
            String container_name = file_path.substring(0,slash);
            String real_file_path = file_path.substring(slash+1);
            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);

            CloudBlockBlob blob = container.getBlockBlobReference( real_file_path + version.get_path() + "/" + file_name);

            InputStream is = new ByteArrayInputStream(file_content.getBytes());
            blob.upload(is, -1);

            Model_Blob fileRecord = new Model_Blob();
            fileRecord.name =  file_name;
            fileRecord.path =  file_path  + version.get_path() + "/" + file_name;

            if(version.getClass().getName().equals(Model_CProgramVersion.class.getName())) {
                fileRecord.c_program_version = (Model_CProgramVersion) version;
            } else if(version.getClass().getName().equals(Model_LibraryVersion.class.getName())){
                fileRecord.library_version = (Model_LibraryVersion) version;
            } else if(version.getClass().getName().equals(Model_BProgramVersion.class.getName())){
                fileRecord.b_program_version = (Model_BProgramVersion) version;
            }

            fileRecord.save();

            version.files.add(fileRecord);
            version.update();

            return fileRecord;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public static Model_Blob uploadAzure_Version(File file, String file_name, String file_path, VersionModel version) throws Exception{

        try {

            logger.debug("uploadAzure_Version:: Azure upload: "+ file_path + version.get_path() + "/" + file_name);


            int slash = file_path.indexOf("/");
            String container_name = file_path.substring(0,slash);
            String real_file_path = file_path.substring(slash+1);
            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name );

            CloudBlockBlob blob = container.getBlockBlobReference( real_file_path + version.get_path() + "/" + file_name);

            InputStream is = new FileInputStream(file);
            blob.upload(is, -1);

            Model_Blob fileRecord = new Model_Blob();
            fileRecord.name = file_name;
            fileRecord.path =   file_path + version.get_path()+ "/" + file_name;

            if(version.getClass().getName().equals(Model_CProgramVersion.class.getName())) {
                fileRecord.c_program_version = (Model_CProgramVersion) version;
            } else if(version.getClass().getName().equals(Model_LibraryVersion.class.getName())){
                fileRecord.library_version = (Model_LibraryVersion) version;
            } else if(version.getClass().getName().equals(Model_BProgramVersion.class.getName())){
                fileRecord.b_program_version = (Model_BProgramVersion) version;
            }

            fileRecord.save();

            version.files.add(fileRecord);
            version.update();

            // Sobor smažu z adresáře
            file.delete();

            return fileRecord;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore
    public static Model_Blob uploadAzure_File(File file, String file_name, String file_path) throws Exception{

        logger.debug("uploadAzure_File:: Azure upload: "+ file_path);

        String container_name = file_path.substring(0,file_path.indexOf("/"));
        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);

        CloudBlockBlob blob = container.getBlockBlobReference(file_name);

        InputStream is = new FileInputStream(file);
        blob.upload(is, -1);

        Model_Blob fileRecord = new Model_Blob();
        fileRecord.name = file_name;
        fileRecord.path = file_path;
        fileRecord.save();

        return fileRecord;
    }

    @JsonIgnore
    public static Model_Blob uploadAzure_File(String file, String contentType, String file_name, String file_path) throws Exception{

        byte[] bytes = Model_Blob.get_decoded_binary_string_from_Base64(file);


        logger.trace("Azure file path  ::" + file_path);
        logger.trace("Azure file name  ::" + file_name);
        logger.trace("Azure contentType::" + contentType);

        String container_name = file_path.substring(0,file_path.indexOf("/"));
        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);


        int slash = file_path.indexOf("/");
        String real_file_path = file_path.substring(slash+1);


        // Opravil jsem zde nahrávání obrázků z HW stárnky a type of board stránky - pokud se to psorralo někde jinde
        // tak bych to probral jestli to nejsednotit pomocí statických proměných celé flow nahrávání - ale myslím, že jsem podchytil všechny
        // usecase

        // Zde musí být plná cesta bez kontejneru!
        CloudBlockBlob blob = container.getBlockBlobReference(real_file_path);

        InputStream is = new ByteArrayInputStream(bytes);
        blob.getProperties().setContentType(contentType);
        blob.upload(is, -1);

        Model_Blob fileRecord = new Model_Blob();
        fileRecord.name = file_name;
        fileRecord.path = file_path;
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

    @JsonIgnore
    public static Model_Blob create_Binary_file(String file_path, byte[] file_content, String file_name) throws Exception{

        logger.debug("create_Binary_file:: Azure create_Binary_file: " + file_path +"/"+ file_name );

        int slash = file_path.indexOf("/");
        String container_name = file_path.substring(0,slash);
        String real_file_path = file_path.substring(slash+1);

        logger.trace("Azure save path: " + file_path );
        logger.trace("Azure Container: " + container_name);
        logger.trace("Real File  Path: " + real_file_path);

        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
        CloudBlockBlob blob = container.getBlockBlobReference( real_file_path +"/" + file_name );

        InputStream is = new ByteArrayInputStream(file_content);
        blob.upload(is, -1);

        Model_Blob fileRecord = new Model_Blob();
        fileRecord.name = file_name;
        fileRecord.path = file_path + "/" + file_name;
        fileRecord.save();

        return fileRecord;
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

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Create Permission is always JsonIgnore
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception { throw new Result_Error_NotSupportedException();}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = String.class, timeToIdle = 1800, maxElements = 500)
    public static Cache<UUID, String> cache_public_link;

    public static Model_Blob getById(String id) throws _Base_Result_Exception  {
        return getById(UUID.fromString(id));
    }

    public static Model_Blob getById(UUID id) throws _Base_Result_Exception  {
        return Model_Blob.find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Blob> find = new Finder<>(Model_Blob.class);
}
