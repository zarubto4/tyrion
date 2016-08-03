package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.B_Pair;
import models.project.b_program.B_Program;
import models.project.b_program.Homer_Instance;
import models.project.c_program.C_Compilation;
import models.project.c_program.C_Program;
import models.project.c_program.actualization.Actualization_procedure;
import models.project.c_program.actualization.C_Program_Update_Plan;
import models.project.m_program.M_Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Version_Object extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true)  public String  id;
                                                            @ApiModelProperty(required = true)  public String version_name;
                     @Column(columnDefinition = "TEXT")     @ApiModelProperty(required = true)  public String version_description;



    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
            example = "1466163478925")                                                          public Date date_of_create;



    @JsonIgnore @OneToMany(mappedBy="version_object", cascade=CascadeType.ALL, fetch = FetchType.EAGER ) public List<FileRecord> files = new ArrayList<>();

                                     @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST)     public LibraryGroup library_group;
                                     @JsonIgnore  @ManyToOne(cascade = CascadeType.PERSIST)     public SingleLibrary single_library;
                                     @JsonIgnore  @ManyToOne(cascade = CascadeType.ALL)         public C_Program      c_program;
               @JsonIgnore  @OneToOne(mappedBy="version_object", cascade = CascadeType.ALL)     public C_Compilation  c_compilation;
                                                                                @JsonIgnore     public boolean compilation_in_progress; // Používáme jako flag pro mezičas kdy se verze kompiluje a uživatel vyvolá get Version
                                                                                @JsonIgnore     public boolean compilable;
                                @JsonIgnore @OneToMany(mappedBy="actual_c_program_version")     public List<Board>  c_program_version_boards  = new ArrayList<>(); // Používám pro zachycení, která verze C_programu na desce běží
    @JsonIgnore @OneToMany(mappedBy="c_program_version_for_update",cascade=CascadeType.ALL)     public List<C_Program_Update_Plan> c_program_update_plans = new ArrayList<>();

                                       @JsonIgnore @ManyToOne(cascade = CascadeType.PERSIST)    public B_Program      b_program;
           @JsonIgnore   @OneToOne(mappedBy="version_object", cascade = CascadeType.PERSIST)    public Homer_Instance homer_instance;


    @JsonIgnore  @OneToMany(mappedBy="c_program_version", cascade=CascadeType.ALL)  public List<B_Pair> b_pairs_c_program = new ArrayList<>();
    @JsonIgnore  @OneToMany(mappedBy="padavan_board_pair",cascade=CascadeType.ALL)  public List<B_Pair> padavan_board_pairs = new ArrayList<>();

    @JsonIgnore  @OneToOne(mappedBy="yoda_board_pair",cascade=CascadeType.ALL) public B_Pair yoda_board_pair;

    // M_Project -------------------------
    @JsonIgnore  @OneToOne(mappedBy="b_program_version", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)   public M_Project m_project;


    // Actual Procedure -------------------------
    @JsonIgnore @OneToMany(mappedBy="b_program_version_procedure", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Actualization_procedure>  actualization_procedures  = new ArrayList<>();


/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/





/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore public String blob_version_link;


    @JsonIgnore @Override public void save() {

        while(true){ // I need Unique Value
            this.blob_version_link = "/versions/" + UUID.randomUUID().toString();
            if (Version_Object.find.where().eq("blob_version_link", blob_version_link ).findUnique() == null) break;
        }

        super.save();
    }


    @JsonIgnore @Transient
    public String get_path(){
        return  blob_version_link;
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have \"Object\".read_permission = true, you can read / get version on this Object - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have \"Object\".update_permission = true, you can create / update on this Object - Or you need static/dynamic permission key";

    // ZDE BY NIKDY NEMĚLY BÝT OPRÁVNĚNÍ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! - TOMÁŠ Z.
    // Oprávnění volejte na objektu kterého se to týká např.  version.b_program.read_permission()...

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Version_Object> find = new Finder<>(Version_Object.class);


}
