package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.blob.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.Server;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.io.*;
import java.util.*;

@Entity
@ApiModel( value = "FileRecord", description = "Model of FileRecord")
@Table(name="FileRecord")
public class Model_FileRecord extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_FileRecord.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @ApiModelProperty(required = true)                      public String id;
    @ApiModelProperty(required = true)                          public String file_name;
                                                 @JsonIgnore    public String file_path;

                @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "picture")     public Model_Person person;               // personal_picture
                @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "picture")     public Model_TypeOfBoard type_of_board;   // type_of_board_picture
                @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "picture")     public Model_Board board;                 // board_picture
                @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "file")        public Model_Log log;
                @JsonIgnore @OneToOne(fetch = FetchType.LAZY)                           public Model_BootLoader boot_loader;
                @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)                          public Model_VersionObject version_object;
                @JsonIgnore @OneToMany(mappedBy="binary_file",fetch = FetchType.LAZY)   public List<Model_CProgramUpdatePlan> c_program_update_plan  = new ArrayList<>();
                @JsonIgnore @OneToOne(mappedBy="bin_compilation_file")                  public Model_CCompilation c_compilations_binary_file;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/
/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient public String  get_file_path_for_direct_download(){

        return Server.azure_blob_Link + file_path;
    }


    /*
    * Ľink by šlo v rámci úspor cachovat. To jest uložit si pod ID objektu jeho link a dát mu platnost - pokud ho v cahce nenajdu
    * vytvořím link, pokud ano z cache vracím.
    * Teoreticky mužu mít variabliblní proměnou (vstup metody) do které dám v čas jak dluho se má v chache paměti / na jak dlouho se má vygenerovat link
    * // 30 sekund nic mocMM
    * */

    @JsonIgnore @Transient public String  get_file_path_for_direct_download_public_link(int second){
        try {

            // Separace na Container a Blob
            int slash = file_path.indexOf("/");
            String container_name = file_path.substring(0, slash);
            String real_file_path = file_path.substring(slash + 1);

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
            terminal_logger.trace("Direct download link:: {} ", total_link);
            return total_link;

        }catch (Exception e){
            e.printStackTrace();

            terminal_logger.internalServerError(e);
            return null;
        }
    }





    @JsonIgnore @Transient public String get_fileRecord_from_Azure_inString(){
        try {

            int slash = file_path.indexOf("/");
            String container_name = file_path.substring(0,slash);
            String real_file_path = file_path.substring(slash+1);

            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name );

            CloudBlob blob = container.getBlockBlobReference(real_file_path );


            InputStream input =  blob.openInputStream();
            InputStreamReader inr = new InputStreamReader(input, "UTF-8");
            String utf8str = org.apache.commons.io.IOUtils.toString(inr);

            return utf8str;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @Transient public JsonNode get_file_As_Json(){
        try {

            return new ObjectMapper().readTree(this.get_fileRecord_from_Azure_inString());

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @Transient
    public static Model_FileRecord uploadAzure_Version(String file_content, String file_name, String file_path, Model_VersionObject version_object) throws Exception{
        try {

            terminal_logger.debug("uploadAzure_Version:: Azure upload: "+ file_path + version_object.get_path() + "/" + file_name );

            int slash = file_path.indexOf("/");
            String container_name = file_path.substring(0,slash);
            String real_file_path = file_path.substring(slash+1);
            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name );

            CloudBlockBlob blob = container.getBlockBlobReference( real_file_path + version_object.get_path() + "/" + file_name);

            InputStream is = new ByteArrayInputStream(file_content.getBytes());
            blob.upload(is, -1);

            Model_FileRecord fileRecord = new Model_FileRecord();
            fileRecord.file_name = file_name;
            fileRecord.version_object = version_object;
            fileRecord.file_path =  file_path   + version_object.get_path() + "/" + file_name;
            fileRecord.save();

            version_object.files.add(fileRecord);
            version_object.update();

            return fileRecord;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @Transient
    public static Model_FileRecord uploadAzure_Version(File file, String file_name, String file_path, Model_VersionObject version_object) throws Exception{

        try {

            terminal_logger.debug("uploadAzure_Version:: Azure upload: "+ file_path + version_object.get_path() + "/" + file_name);


            int slash = file_path.indexOf("/");
            String container_name = file_path.substring(0,slash);
            String real_file_path = file_path.substring(slash+1);
            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name );

            CloudBlockBlob blob = container.getBlockBlobReference( real_file_path + version_object.get_path() + "/" + file_name);

            InputStream is = new FileInputStream(file);
            blob.upload(is, -1);

            Model_FileRecord fileRecord = new Model_FileRecord();
            fileRecord.file_name = file_name;
            fileRecord.version_object = version_object;
            fileRecord.file_path =   file_path + version_object.get_path()+ "/" + file_name;
            fileRecord.save();

            version_object.files.add(fileRecord);
            version_object.update();

            // Sobor smažu z adresáře
            file.delete();

            return fileRecord;

        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;
        }
    }

    @JsonIgnore @Transient
    public static Model_FileRecord uploadAzure_File(File file, String file_name, String file_path) throws Exception{

        terminal_logger.debug("uploadAzure_File:: Azure upload: "+ file_path);

        String container_name = file_path.substring(0,file_path.indexOf("/"));
        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);

        CloudBlockBlob blob = container.getBlockBlobReference(file_name);

        InputStream is = new FileInputStream(file);
        blob.upload(is, -1);

        Model_FileRecord fileRecord = new Model_FileRecord();
        fileRecord.file_name = file_name;
        fileRecord.file_path = file_path;
        fileRecord.save();

        return fileRecord;
    }

    @JsonIgnore @Transient
    public static Model_FileRecord uploadAzure_File(String file, String contentType, String file_name, String file_path) throws Exception{

        byte[] bytes = Model_FileRecord.get_decoded_binary_string_from_Base64(file);


        terminal_logger.trace("Azure file path  ::" + file_path);
        terminal_logger.trace("Azure file name  ::" + file_name);
        terminal_logger.trace("Azure contentType::" + contentType);

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

        Model_FileRecord fileRecord = new Model_FileRecord();
        fileRecord.file_name = file_name;
        fileRecord.file_path = file_path;
        fileRecord.save();

        return fileRecord;
    }

    @JsonIgnore @Transient
    public void remove_file_from_Azure(){
        try{

            int slash =  file_path.indexOf("/");
            String container_name =  file_path.substring(0, slash);
            String file_path_slash =  file_path.substring(slash+1);

            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
            CloudBlob blob = container.getBlockBlobReference(file_path_slash);
            blob.delete();

        }catch (Exception e){
            terminal_logger.internalServerError(e);
        }
    }

    @JsonIgnore @Transient
    public static Model_FileRecord create_Binary_file(String file_path, String file_content, String file_name) throws Exception{

        terminal_logger.debug("create_Binary_file:: Azure create_Binary_file: " + file_path +"/"+ file_name );

        int slash = file_path.indexOf("/");
        String container_name = file_path.substring(0,slash);
        String real_file_path = file_path.substring(slash+1);

        terminal_logger.trace("Azure save path: " + file_path );
        terminal_logger.trace("Azure Container: " + container_name);
        terminal_logger.trace("Real File  Path: " + real_file_path);

        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
        CloudBlockBlob blob = container.getBlockBlobReference( real_file_path +"/" + file_name );

        InputStream is = new ByteArrayInputStream(file_content.getBytes());
        blob.upload(is, -1);

        Model_FileRecord fileRecord = new Model_FileRecord();
        fileRecord.file_name = file_name;
        fileRecord.file_path = file_path + "/" + file_name;
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
    @JsonIgnore @Transient
    public static void azureDelete(CloudBlobContainer container, String pocatekMazani) throws Exception {

        for (ListBlobItem blobItem : container.listBlobs( pocatekMazani + "/" )) {

            if (blobItem instanceof CloudBlob) ((CloudBlob) blobItem).deleteIfExists();

            // Break & loop
            String help = blobItem.getUri().toString().substring(0,blobItem.getUri().toString().length() -1);

            azureDelete(container, pocatekMazani +  help.substring( help.lastIndexOf("/") ,help.length()) );
        }
    }

    @JsonIgnore @Transient
    public static String get_encoded_binary_string_from_File(File binary_file) throws Exception {

        FileInputStream fileInputStreamReader = new FileInputStream(binary_file);
        byte[] bytes = new byte[(int)binary_file.length()];
        fileInputStreamReader.read(bytes);

        return new String(Base64.getEncoder().encode(bytes));

    }

    @JsonIgnore @Transient
    public static byte[] get_decoded_binary_string_from_Base64(String binary_file) throws Exception {

        return Base64.getDecoder().decode(binary_file.getBytes());

    }

    @JsonIgnore @Transient
    public static String get_encoded_binary_string_from_body(byte[] bytes) throws Exception {
        return new String(Base64.getEncoder().encode( bytes ));
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_FileRecord.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.id);

        super.update();
    }


    @Override
    public void delete(){
        try {
            terminal_logger.debug("delete File from Azure", this.id);
            this.remove_file_from_Azure();
        }catch (Exception e) {
            terminal_logger.internalServerError(e);
        }

        terminal_logger.debug("delete :: Delete object Id: {} ", this.id);
        super.delete();
    }


/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static final String CACHE_PUBLIC_LINK = Model_FileRecord.class.getSimpleName() + "_PUBLIC_LINK";

    // public static Cache<String, Model_FileRecord> cache;         // Server_cache Override during server initialization
    public static Cache<String, String> cache_public_link;      // Server_cache Override during server initialization
/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model_FileRecord get_byId(String id){

      return Model_FileRecord.find.byId(id);
    }

    public static Model.Finder<String, Model_FileRecord> find = new Finder<>(Model_FileRecord.class);

}
