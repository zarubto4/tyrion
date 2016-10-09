package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;
import models.project.c_program.C_Compilation;
import models.project.c_program.actualization.C_Program_Update_Plan;
import utilities.Server;

import javax.persistence.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Entity
public class FileRecord extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @ApiModelProperty(required = true)                          public String id;
    @ApiModelProperty(required = true)                          public String file_name;
                                                 @JsonIgnore    public String file_path;

                                    @JsonIgnore @OneToOne(fetch = FetchType.LAZY)       public Person person;   // personal_picture
                                    @JsonIgnore @OneToOne()                             public BootLoader boot_loader;
                                   @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)       public Version_Object version_object;
             @JsonIgnore @OneToMany(mappedBy="binary_file",fetch = FetchType.LAZY)      public List<C_Program_Update_Plan> c_program_update_plen  = new ArrayList<>();
    @JsonIgnore @OneToOne(mappedBy="bin_compilation_file")                              public C_Compilation c_compilations_binary_file;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path() {
        return  file_path;
    }

    @JsonIgnore @Transient  public String get_fileRecord_from_Azure_inString(){
        try {

            logger.debug("FileRecord: get_fileRecord_from_Azure_inString");

            int slash = file_path.indexOf("/");
            String container_name = file_path.substring(0,slash);
            String real_file_path = file_path.substring(slash+1);

            logger.debug("Azure load path: " + file_path );
            logger.debug("Azure Container: " + container_name);
            logger.debug("Real File  Path: " + real_file_path);

            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name );

            CloudBlob blob = container.getBlockBlobReference(real_file_path );


            InputStream input =  blob.openInputStream();
            InputStreamReader inr = new InputStreamReader(input, "UTF-8");
            String utf8str = org.apache.commons.io.IOUtils.toString(inr);

            return utf8str;

        }catch (Exception e){
            logger.error("Get File from Azure in string ", e);
            e.printStackTrace();
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

    @Override
    public void delete(){
        this.remove_file_from_Azure();
        super.delete();
    }





    /* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, FileRecord> find = new Finder<>(FileRecord.class);

}
