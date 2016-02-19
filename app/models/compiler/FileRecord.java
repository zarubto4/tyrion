package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import utilities.a_main_utils.UtilTools;

import javax.persistence.*;
import java.io.File;
import java.util.Scanner;


@Entity
public class FileRecord extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String fileName;

                                 @JsonIgnore  @ManyToOne     public Version_Object version_object;


    @JsonProperty public String fileContent()   { return "http://localhost:9000/file/fileRecord/" +id; }




// Mimo Objekt

    // Určeno pro načítání souborů z Azure pro Tyriona
    // Trochu nedomyšleno, že u File Record nevím, jaký je mateřský objekt - ale vím, že má vždy jen jeden
    // záznam - to znamená, že file_record je vázán pouze bud k b programu, nebo jen k c programu atd...
    @JsonIgnore public File get_fileRecord_from_Azure_inFile() throws Exception{

      Integer azureLinkVersion = version_object.azureLinkVersion;
      String  azurePackageLink = "";
      String  azureStorageLink = "";
      String  container = "";

        if( version_object.b_program != null){
                 container = "b-program";
                 azurePackageLink = version_object.b_program.azurePackageLink;
                 azureStorageLink = version_object.b_program.azureStorageLink;
             }
        else if( version_object.c_program != null){
                container = "c-program";
                azurePackageLink = version_object.c_program.azurePackageLink;
                azureStorageLink = version_object.c_program.azureStorageLink;
        }
        //else if( version_object.m_program != null){} Todo Na M_Program - zatím není verze implementována
        else if( version_object.singleLibrary != null){
                container = "libraries";
                azurePackageLink = version_object.singleLibrary.azurePackageLink;
                azureStorageLink = version_object.singleLibrary.azureStorageLink;
        }
        else if( version_object.libraryGroup != null){
                container = "libraries";
                azurePackageLink = version_object.libraryGroup.azurePackageLink;
                azureStorageLink = version_object.libraryGroup.azureStorageLink;
        }

        if(azurePackageLink.length() < 1) throw new Exception("FileRecord (uvnitř třídy) nenašel cestu k požadovanému souboru");

        /*
        System.out.println("Hledaný soubor je: ");
        System.out.println("container: " + container);
        System.out.println("azureStorageLink: " + azureStorageLink);
        System.out.println("azurePackageLink: " + azurePackageLink);
        System.out.println("azureLinkVersion: " + azureLinkVersion);
        */

        return UtilTools.file_get_File_from_Azure(container, azureStorageLink, azurePackageLink, azureLinkVersion, fileName);

    }

    @JsonIgnore public String get_fileRecord_from_Azure_inString() throws Exception{

        File file = this.get_fileRecord_from_Azure_inFile();

        Scanner scanner = new Scanner( file );
        String text = scanner.useDelimiter("\\A").next();
        scanner.close();

        file.delete();

        return text;

    }

    public static Finder<String, FileRecord> find = new Finder<>(FileRecord.class);

}
