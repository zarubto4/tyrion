package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.enums.Compile_Status;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of C_Compilation",
        value = "C_Compilation")
public class Model_CCompilation extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                         @Id public String id;
                                                                 @JsonIgnore public Date date_of_create;

    @JsonIgnore @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
                                   @JoinColumn(name="c_compilation_version") public Model_VersionObject version_object;

                                                                @JsonIgnore  public Compile_Status status; // Používáme jako flag pro mezičas kdy se verze kompiluje a uživatel vyvolá get Version

    @ApiModelProperty(required = true, value = virtual_input_output_docu) @Column(columnDefinition = "TEXT")                public String virtual_input_output;
                                                            @JsonIgnore   @Column(columnDefinition = "TEXT")                public String c_comp_build_url;
    @JsonIgnore   @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY) @JoinColumn(name="bin_compilation_file_id")  public Model_FileRecord bin_compilation_file;

    @JsonIgnore  public String firmware_version_core;
    @JsonIgnore  public String firmware_version_mbed;
    @JsonIgnore  public String firmware_version_lib;
    @JsonIgnore  public String firmware_build_id;
    @JsonIgnore  public String firmware_build_datetime;   // Kdy bylo vybylděno

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_CCompilation.find.byId(this.id) == null) break;
        }
        this.date_of_create = new Date();
        super.save();
    }

    @JsonIgnore @Transient
    public Model_FileRecord compilation(){
        return Model_FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "compilation.bin").findUnique();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public String get_path(){
        return version_object.c_program.get_path() + version_object.get_path();
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Model_CCompilation> find = new Finder<>(Model_CCompilation.class);

    /* DESCRIPTION - DOCUMENTATION ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore @Transient public final static String virtual_input_output_docu = "dsafsdfsdf"; // TODO https://youtrack.byzance.cz/youtrack/issue/TYRION-304

}

