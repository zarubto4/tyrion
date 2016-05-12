package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import models.project.b_program.B_Program;
import models.project.c_program.C_Program;
import play.libs.Json;
import utilities.UtilTools;

import javax.persistence.*;
import java.io.File;
import java.util.Scanner;


@Entity
public class FileRecord extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String file_name;

        @JsonIgnore  @ManyToOne(cascade=CascadeType.ALL)     public Version_Object version_object;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    // @JsonProperty public String file_content_link()   { return Server.tyrion_serverAddress + "/file/fileRecord/" +id; }

    @JsonProperty public String content() {
        try {
            return get_fileRecord_from_Azure_inString();
        } catch (Exception e) {
            return null;
        }

    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    // Určeno pro načítání souborů z Azure pro Tyriona
    // Trochu nedomyšleno, že u File Record nevím, jaký je mateřský objekt - ale vím, že má vždy jen jeden
    // záznam - to znamená, že file_record je vázán pouze bud k b programu, nebo jen k c programu atd...
    @JsonIgnore @Transient public File get_fileRecord_from_Azure_inFile() throws Exception{

      Integer azureLinkVersion = version_object.azureLinkVersion;
      String  azurePackageLink = "";
      String  azureStorageLink = "";
      String  container = "";

        System.out.println(Json.toJson(version_object));

        if( version_object.b_program != null){
                 container = "b-program";
                 B_Program b_program = B_Program.find.byId(version_object.b_program.id);
                 azurePackageLink = b_program.azurePackageLink;
                 azureStorageLink = b_program.azureStorageLink;
             }
        else if( version_object.c_program != null){
                container = "c-program";
                C_Program c_program = C_Program.find.byId(version_object.c_program.id);
                azurePackageLink = c_program.azurePackageLink;
                azureStorageLink = c_program.azureStorageLink;
        }
        //else if( version_object.m_project != null){} Todo Na M_Program - zatím není verze implementována
        else if( version_object.singleLibrary != null){
                container = "libraries";
                SingleLibrary singleLibrary = SingleLibrary.find.byId(version_object.singleLibrary.id);
                azurePackageLink = singleLibrary.azurePackageLink;
                azureStorageLink = singleLibrary.azureStorageLink;
        }
        else if( version_object.libraryGroup != null){
                container = "libraries";
                LibraryGroup libraryGroup = LibraryGroup.find.byId(version_object.libraryGroup.id);
                azurePackageLink = libraryGroup.azurePackageLink;
                azureStorageLink = libraryGroup.azureStorageLink;
        }


        if(azurePackageLink.length() < 1) throw new Exception("FileRecord (uvnitř třídy) nenašel cestu k požadovanému souboru");
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

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient  public Boolean edit_permission()  {
        return  (   FileRecord.find.where()
                        .or(
                                com.avaje.ebean.Expr.or(
                                        com.avaje.ebean.Expr.and(
                                                com.avaje.ebean.Expr.eq("version_object.b_program.project.ownersOfProject.id", SecurityController.getPerson().id),
                                                com.avaje.ebean.Expr.eq("id",id)
                                        ),
                                        com.avaje.ebean.Expr.and(
                                                com.avaje.ebean.Expr.eq("version_object.c_program.project.ownersOfProject.id", SecurityController.getPerson().id),
                                                com.avaje.ebean.Expr.eq("id",id)
                                        )
                                ),
                                com.avaje.ebean.Expr.or(
                                        com.avaje.ebean.Expr.and(
                                            com.avaje.ebean.Expr.eq("version_object.singleLibrary.project.ownersOfProject.id", SecurityController.getPerson().id),
                                            com.avaje.ebean.Expr.eq("id",id)
                                        ),
                                        com.avaje.ebean.Expr.and(
                                                com.avaje.ebean.Expr.eq("version_object.libraryGroup.project.ownersOfProject.id", SecurityController.getPerson().id),
                                                com.avaje.ebean.Expr.eq("id",id)
                                        )
                                )
                        )
                        .or(
                                com.avaje.ebean.Expr.and(
                                        com.avaje.ebean.Expr.eq("version_object.m_project.project.ownersOfProject.id", SecurityController.getPerson().id),
                                        com.avaje.ebean.Expr.eq("id",id)
                                ),
                                com.avaje.ebean.Expr.and(                      // TODO M_Project Version!!!
                                        com.avaje.ebean.Expr.eq("version_object.b_program.project.ownersOfProject.id", SecurityController.getPerson().id),
                                        com.avaje.ebean.Expr.eq("id",id)
                                )
                        )
                        .findRowCount() > 0
                        ||
                        SecurityController.getPerson().has_permission("FileRecord.edit")
                );
    }

    @JsonProperty @Transient  public Boolean delete_permission() {
        return  (   FileRecord.find.where()
                    .or(
                            com.avaje.ebean.Expr.or(
                                    com.avaje.ebean.Expr.and(
                                            com.avaje.ebean.Expr.eq("version_object.b_program.project.ownersOfProject.id", SecurityController.getPerson().id),
                                            com.avaje.ebean.Expr.eq("id",id)
                                    ),
                                    com.avaje.ebean.Expr.and(
                                            com.avaje.ebean.Expr.eq("version_object.c_program.project.ownersOfProject.id", SecurityController.getPerson().id),
                                            com.avaje.ebean.Expr.eq("id",id)
                                    )
                            ),
                            com.avaje.ebean.Expr.or(
                                    com.avaje.ebean.Expr.and(
                                            com.avaje.ebean.Expr.eq("version_object.singleLibrary.project.ownersOfProject.id", SecurityController.getPerson().id),
                                            com.avaje.ebean.Expr.eq("id",id)
                                    ),
                                    com.avaje.ebean.Expr.and(
                                            com.avaje.ebean.Expr.eq("version_object.libraryGroup.project.ownersOfProject.id", SecurityController.getPerson().id),
                                            com.avaje.ebean.Expr.eq("id",id)
                                    )
                            )
                    )
                    .or(
                            com.avaje.ebean.Expr.and(
                                    com.avaje.ebean.Expr.eq("version_object.m_project.project.ownersOfProject.id", SecurityController.getPerson().id),
                                    com.avaje.ebean.Expr.eq("id",id)
                            ),
                            com.avaje.ebean.Expr.and(                      // TODO M_Project Version!!!
                                    com.avaje.ebean.Expr.eq("version_object.b_program.project.ownersOfProject.id", SecurityController.getPerson().id),
                                    com.avaje.ebean.Expr.eq("id",id)
                            )
                    )
                    .findRowCount() > 0
                    ||
                    SecurityController.getPerson().has_permission("FileRecord.edit")
                 );
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, FileRecord> find = new Finder<>(FileRecord.class);

}
