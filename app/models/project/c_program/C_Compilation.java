package models.project.c_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.FileRecord;
import models.compiler.Version_Object;

import javax.persistence.*;
import java.util.Date;

@Entity
public class C_Compilation extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

              @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)      public String id;
                                                          @JsonIgnore      public Date date_of_create;

    @JsonIgnore @OneToOne(cascade = CascadeType.ALL)   @JoinColumn(name="c_compilation_version")      public Version_Object version_object;


    @ApiModelProperty(required = true, value = virtual_input_output_docu) @Column(columnDefinition = "TEXT")       public String virtual_input_output;
                                                            @JsonIgnore   @Column(columnDefinition = "TEXT")       public String c_comp_build_url;
                                                            @JsonIgnore   @ManyToOne(cascade = CascadeType.ALL)    public FileRecord bin_compilation_file;

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public FileRecord compilation(){
        return FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "compilation.bin").findUnique();
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,C_Compilation> find = new Finder<>(C_Compilation.class);



/* DESCRIPTION - DOCUMENTATION ---------------------------------------------------------------------------------------------------------*/
    @JsonIgnore @Transient public final static String virtual_input_output_docu = "dsafsdfsdf"; // TODO https://youtrack.byzance.cz/youtrack/issue/TYRION-304

}

