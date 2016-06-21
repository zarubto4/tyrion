package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.project.b_program.B_Program;
import models.project.c_program.C_Program;
import play.libs.Json;
import utilities.UtilTools;

import javax.persistence.*;
import java.io.File;
import java.util.Scanner;


@Entity
public class FileRecord extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String file_name;

                                   @JsonIgnore @ManyToOne()  public Version_Object version_object;
                                               @JsonIgnore   public String name_of_parent_object;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    // Určeno pro načítání souborů z Azure pro Tyriona
    // Trochu nedomyšleno, že u File Record nevím, jaký je mateřský objekt - ale vím, že má vždy jen jeden
    // záznam - to znamená, že file_record je vázán pouze bud k b programu, nebo jen k c programu atd...
    @JsonIgnore @Transient public File get_fileRecord_from_Azure_inFile() throws Exception{

      String  azureLinkVersion = version_object.azureLinkVersion;
      String  azurePackageLink = "";
      String  azureStorageLink = "";
      String  container = "";

        System.out.println(Json.toJson(version_object));

        switch (name_of_parent_object){

            case "B_Program" : {
                container = "b-program";
                B_Program b_program = B_Program.find.byId(version_object.b_program.id);
                azurePackageLink = b_program.azurePackageLink;
                azureStorageLink = b_program.azureStorageLink;
                break;
            }

            case "C_Program" : {
                container = "c-program";
                C_Program c_program = C_Program.find.byId(version_object.c_program.id);
                azurePackageLink = c_program.azurePackageLink;
                azureStorageLink = c_program.azureStorageLink;
                break;
            }

            case "SingleLibrary" : {
                container = "libraries";
                SingleLibrary singleLibrary = SingleLibrary.find.byId(version_object.single_library.id);
                azurePackageLink = singleLibrary.azurePackageLink;
                azureStorageLink = singleLibrary.azureStorageLink;
                break;
            }

            case "LibraryGroup" : {
                container = "libraries";
                LibraryGroup libraryGroup = LibraryGroup.find.byId(version_object.library_group.id);
                azurePackageLink = libraryGroup.azurePackageLink;
                azureStorageLink = libraryGroup.azureStorageLink;
                break;
            }

            default: {
                logger.error("FileRecord (uvnitř třídy) nenašel cestu k požadovanému souboru");
                throw new Exception("FileRecord (uvnitř třídy) nenašel cestu k požadovanému souboru");
            }

        }

        return UtilTools.file_get_File_from_Azure(container, azurePackageLink, azureStorageLink,  azureLinkVersion, file_name);

    }

    @JsonIgnore @Transient  public String get_fileRecord_from_Azure_inString() throws Exception {

        File file = this.get_fileRecord_from_Azure_inFile();

        Scanner scanner = new Scanner( file );
        String text = scanner.useDelimiter("\\A").next();
        scanner.close();

        file.delete();

        return text;

    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, FileRecord> find = new Finder<>(FileRecord.class);

}
