package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;
import models.project.c_program.C_Compilation;
import models.project.c_program.actualization.C_Program_Update_Plan;
import utilities.UtilTools;

import javax.persistence.*;
import java.io.File;
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

                                    @JsonIgnore @OneToOne()     public Person person;
                                   @JsonIgnore @ManyToOne()     public Version_Object version_object;
             @JsonIgnore @OneToMany(mappedBy="binary_file")     public List<C_Program_Update_Plan> c_program_update_plen  = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="bin_compilation_file")     public List<C_Compilation> c_compilations_binary_files  = new ArrayList<>();

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    // Určeno pro načítání souborů z Azure pro Tyriona
    // Trochu nedomyšleno, že u File Record nevím, jaký je mateřský objekt - ale vím, že má vždy jen jeden
    // záznam - to znamená, že file_record je vázán pouze bud k b programu, nebo jen k c programu atd...
    @JsonIgnore @Transient public File get_fileRecord_from_Azure_inFile() throws Exception{

        return UtilTools.file_get_File_from_Azure(file_path);

    }

    //product/product/3b7c6115-a314-4e01-8af4-224a509fc058/projects/116d57b6-e728-4e0f-b9b1-3b8ce05b6c8a/c-programs/fcc19406-38bc-4f75-b338-559f2c1d87a6/versions/bfabb0af-0521-42d0-be47-76466f514fff/code.json
    //        product/3b7c6115-a314-4e01-8af4-224a509fc058/projects/116d57b6-e728-4e0f-b9b1-3b8ce05b6c8a/c-programs/fcc19406-38bc-4f75-b338-559f2c1d87a6/versions/bfabb0af-0521-42d0-be47-76466f514fff/code.json
    @JsonIgnore @Transient  public String get_fileRecord_from_Azure_inString(){
        try {
            logger.debug("FileRecord: get_fileRecord_from_Azure_inString");
            File file = this.get_fileRecord_from_Azure_inFile();
            return UtilTools.get_String_from_file(file);


        }catch (Exception e){
            logger.error("Error when parsing Json File to Json Node", e);
            return null;
        }
    }

    @JsonIgnore @Transient public JsonNode get_file_As_Json(){
          try {

              File file = this.get_fileRecord_from_Azure_inFile();
              String string =  UtilTools.get_String_from_file(file);
              return  new ObjectMapper().readTree(string);

          }catch (Exception e){
             logger.error("Error when parsing Json File to Json Node", e);
             return null;
          }
    }




    @JsonIgnore @Transient
    public String get_path() {
        return  file_path;
    }



    /* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, FileRecord> find = new Finder<>(FileRecord.class);

}
