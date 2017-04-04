package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.loggy.Loggy;

import javax.persistence.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of FileRecord",
        value = "FileRecord")
public class Model_FileRecord extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @ApiModelProperty(required = true)                      public String id;
    @ApiModelProperty(required = true)                          public String file_name;
                                                 @JsonIgnore    public String file_path;

                @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "picture")     public Model_Person person;   // personal_picture
                @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "picture")     public Model_TypeOfBoard type_of_board;   // type_of_board_picture
                @JsonIgnore @OneToOne(fetch = FetchType.LAZY, mappedBy = "file")        public Model_Log log;
                                    @JsonIgnore @OneToOne()                             public Model_BootLoader boot_loader;
                                   @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)       public Model_VersionObject version_object;
             @JsonIgnore @OneToMany(mappedBy="binary_file",fetch = FetchType.LAZY)      public List<Model_CProgramUpdatePlan> c_program_update_plan  = new ArrayList<>();
    @JsonIgnore @OneToOne(mappedBy="bin_compilation_file")                              public Model_CCompilation c_compilations_binary_file;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_FileRecord.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Transient
    public String get_path() {
        return  file_path;
    }

    @JsonIgnore @Transient
    public String get_fileRecord_from_Azure_inString(){
        try {

            logger.trace("Model_FileRecord:: get_fileRecord_from_Azure_inString");

            int slash = file_path.indexOf("/");
            String container_name = file_path.substring(0,slash);
            String real_file_path = file_path.substring(slash+1);

            logger.trace("Azure load path: " + file_path );

            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name );

            CloudBlob blob = container.getBlockBlobReference(real_file_path );


            InputStream input =  blob.openInputStream();
            InputStreamReader inr = new InputStreamReader(input, "UTF-8");
            String utf8str = org.apache.commons.io.IOUtils.toString(inr);

            return utf8str;

        }catch (Exception e){
            Loggy.internalServerError("Model_FileRecord:: get_fileRecord_from_Azure_inString:", e);

            return null;
        }
    }

    @JsonIgnore @Transient public JsonNode get_file_As_Json(){
        try {

            return  new ObjectMapper().readTree(this.get_fileRecord_from_Azure_inString());

        }catch (Exception e){
            logger.error("Error when parsing Json File to Json Node", e);
            return null;
        }
    }

    @JsonIgnore @Transient
    public static Model_FileRecord uploadAzure_Version(String file_content, String file_name, String file_path, Model_VersionObject version_object) throws Exception{

        logger.debug("Azure upload: "+ file_path + version_object.get_path() + "/" + file_name );

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
    }

    @JsonIgnore @Transient
    public static Model_FileRecord uploadAzure_Version(File file, String file_name, String file_path, Model_VersionObject version_object) throws Exception{

        logger.debug("Azure upload: "+ file_path + version_object.get_path() + "/" + file_name);


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
        try {
            file.delete();
        }catch (Exception e){}

        return fileRecord;
    }

    @JsonIgnore @Transient
    public static Model_FileRecord uploadAzure_File(File file, String file_name, String file_path) throws Exception{

        logger.debug("Azure upload: "+ file_path);

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

        logger.debug("Azure file:"+ file.substring(0,50));

        byte[] bytes = Model_FileRecord.get_decoded_binary_string_from_Base64(file);

        logger.debug("Azure load:"+ file_path);
        logger.debug("Azure name:" + file_name);
        logger.debug("Azure contentType:" + contentType);

        String container_name = file_path.substring(0,file_path.indexOf("/"));
        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);

        CloudBlockBlob blob = container.getBlockBlobReference(file_name);

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

            int slash =  this.get_path().indexOf("/");
            String container_name =  this.get_path().substring(0, slash);
            String file_path =  this.get_path().substring(slash+1);

            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
            CloudBlob blob = container.getBlockBlobReference(file_path);
            blob.delete();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JsonIgnore @Transient
    public static Model_FileRecord create_Binary_file(String file_path, String file_content, String file_name) throws Exception{

        logger.debug("Azure create_Binary_file: " + file_path +"/"+ file_name );

        int slash = file_path.indexOf("/");
        String container_name = file_path.substring(0,slash);
        String real_file_path = file_path.substring(slash+1);

        logger.debug("Azure save path: " + file_path );
        logger.debug("Azure Container: " + container_name);
        logger.debug("Real File  Path: " + real_file_path);

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
     *  Proto když je potřeba promazat něco v něco2 a všechno dál, je nutné nasadit rekurzivní algoritmus, který se
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


    @Override
    public void delete(){
        this.remove_file_from_Azure();
        super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_FileRecord> find = new Finder<>(Model_FileRecord.class);

}
