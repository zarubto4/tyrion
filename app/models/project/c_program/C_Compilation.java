package models.project.c_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import utilities.enums.Compile_Status;

import javax.persistence.*;
import java.util.Date;

@Entity
public class C_Compilation extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

              @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)      public String id;
                                                          @JsonIgnore      public Date date_of_create;

    @JsonIgnore @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)   @JoinColumn(name="c_compilation_version") public Version_Object version_object;

    @JsonIgnore  public Compile_Status status; // Používáme jako flag pro mezičas kdy se verze kompiluje a uživatel vyvolá get Version

    @ApiModelProperty(required = true, value = virtual_input_output_docu) @Column(columnDefinition = "TEXT")       public String virtual_input_output;
                                                            @JsonIgnore   @Column(columnDefinition = "TEXT")       public String c_comp_build_url;
    @JsonIgnore   @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY) @JoinColumn(name="bin_compilation_file_id")  public FileRecord bin_compilation_file;

    @JsonIgnore  public String firmware_version_core;
    @JsonIgnore  public String firmware_version_mbed;
    @JsonIgnore  public String firmware_version_lib;
    @JsonIgnore  public String firmware_build_id;
    @JsonIgnore  public String firmware_build_datetime;   // Kdy bylo vybylděno


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public FileRecord compilation(){
        return FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "compilation.bin").findUnique();
    }

    @JsonIgnore @Transient @Override
    public void save(){
        this.date_of_create = new Date();
    }


/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path(){
        return version_object.c_program.get_path() + version_object.get_path();
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,C_Compilation> find = new Finder<>(C_Compilation.class);

    /* DESCRIPTION - DOCUMENTATION ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore @Transient public final static String virtual_input_output_docu = "dsafsdfsdf"; // TODO https://youtrack.byzance.cz/youtrack/issue/TYRION-304

}

