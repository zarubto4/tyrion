package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.project.c_program.C_Program_Update_Plan;
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

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String file_name;
                                                 @JsonIgnore    public String file_path;

                                   @JsonIgnore @ManyToOne()     public Version_Object version_object;
             @JsonIgnore @OneToMany(mappedBy="binary_file")     public List<C_Program_Update_Plan> c_program_update_plen  = new ArrayList<>();


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    // Určeno pro načítání souborů z Azure pro Tyriona
    // Trochu nedomyšleno, že u File Record nevím, jaký je mateřský objekt - ale vím, že má vždy jen jeden
    // záznam - to znamená, že file_record je vázán pouze bud k b programu, nebo jen k c programu atd...
    @JsonIgnore @Transient public File get_fileRecord_from_Azure_inFile() throws Exception{

        return UtilTools.file_get_File_from_Azure(file_path);

    }

    @JsonIgnore @Transient  public String get_fileRecord_from_Azure_inString(){
        try {

            File file = this.get_fileRecord_from_Azure_inFile();
            return UtilTools.get_String_from_file(file);


        }catch (Exception e){
            logger.error("Error when parsing Json File to Json Node", e);
            return null;
        }
    }

    @JsonIgnore @Transient public String get_Encoded_binary_file_From_Azure_inString() throws Exception{

        return UtilTools. get_encoded_binary_file_from_azure(this.file_path);

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


    /* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, FileRecord> find = new Finder<>(FileRecord.class);

}
