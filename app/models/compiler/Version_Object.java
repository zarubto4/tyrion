package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.B_Pair;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Cloud;
import models.project.b_program.B_Program_Homer;
import models.project.c_program.C_Compilation;
import models.project.c_program.C_Program;
import models.project.m_program.M_Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Version_Object extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String  id;
                                                             public String version_name;
                     @Column(columnDefinition = "TEXT")      public String version_description;


    @JsonIgnore    public String azureLinkVersion;

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1461918607") public Date date_of_create;

    @OneToMany(mappedBy="version_object", cascade=CascadeType.ALL, fetch = FetchType.EAGER )    public List<FileRecord> files = new ArrayList<>();

                                                                   @JsonIgnore  @ManyToOne      public LibraryGroup library_group;
                                                                   @JsonIgnore  @ManyToOne      public SingleLibrary single_library;

                                                                   @JsonIgnore  @ManyToOne      public C_Program      c_program;
             @JsonIgnore   @OneToOne(mappedBy="version_object", cascade = CascadeType.ALL)      public C_Compilation  c_compilation;
                                                                              @JsonIgnore       public boolean compilation_in_progress; // Používáme jako flag pro mezičas kdy se verze kompiluje a uživatel vyvolá get Version
                                                                              @JsonIgnore       public boolean compilable;
    @JsonIgnore @OneToMany(mappedBy="actual_c_program_version", cascade = CascadeType.ALL)      public List<Board>  c_program_version_boards  = new ArrayList<>(); // Používám pro zachycení, která verze C_programu na desce běží
    @JsonIgnore @OneToMany(mappedBy="c_program_version_for_update",cascade=CascadeType.ALL)     public List<C_Program_Update_Plan> c_program_update_plans = new ArrayList<>();

                                                        @JsonIgnore @ManyToOne      public B_Program       b_program;
    @JsonIgnore   @OneToOne(mappedBy="version_object",cascade=CascadeType.ALL)      public B_Program_Homer b_program_homer;
    @JsonIgnore   @OneToOne(mappedBy="version_object",cascade=CascadeType.ALL)      public B_Program_Cloud b_program_cloud;


    @JsonIgnore   @OneToMany(mappedBy="c_program_version",cascade=CascadeType.ALL)  public List<B_Pair> b_pairs_c_program = new ArrayList<>();
    @JsonIgnore   @OneToMany(mappedBy="b_program_version",cascade=CascadeType.ALL)  public List<B_Pair> b_pairs_b_program = new ArrayList<>();

    @JsonIgnore  @OneToOne(mappedBy="version_master_board",cascade=CascadeType.ALL) public B_Pair master_board_b_pair;


    @JsonIgnore   @OneToOne(mappedBy="b_program_version")   public M_Project m_project;


/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/



/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public static final String read_permission_docs   = "read: If user have \"Object\".read_permission = true, you can read / get version on this Object - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: If user have \"Object\".update_permission = true, you can create / update on this Object - Or you need static/dynamic permission key";

    // ZDE BY NIKDY NEMĚLY BÝT OPRÁVNĚNÍ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! - TOMÁŠ Z.
    // Oprávnění volejte na objektu kterého se to týká např.  version.b_program.read_permission()...

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Version_Object> find = new Finder<>(Version_Object.class);


}
