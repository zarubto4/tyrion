package models.project.c_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.compiler.FileRecord;
import models.compiler.Version_Object;

import javax.persistence.*;
import java.util.Date;

@Entity
public class C_Compilation extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

              @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)      public String id;
                                                          @JsonIgnore      public Date dateOfCreate;

    @JsonIgnore @OneToOne   @JoinColumn(name="c_compilation_version")      public Version_Object version_object;


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public FileRecord compilation(){
        return FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "compilation.bin").findUnique();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,C_Compilation> find = new Finder<>(C_Compilation.class);

}

// Tenhle objekt by měl mít teoreticky jen krátkou životnost a každý den by se měli mazat historie kompilací, protože je zbytečné je držet v paměti.
