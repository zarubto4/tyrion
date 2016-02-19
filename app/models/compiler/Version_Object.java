package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Cloud;
import models.project.b_program.B_Program_Homer;
import models.project.c_program.C_Program;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Version_Object extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String  id;
                                                             public String version_name;
                     @Column(columnDefinition = "TEXT")      public String  versionDescription;
                                                             public Date    dateOfCreate;
                                           @JsonIgnore       public Integer azureLinkVersion;

    @JsonIgnore  @OneToMany(mappedBy="version_object", cascade=CascadeType.ALL)  public List<FileRecord> files = new ArrayList<>();


                                    @JsonIgnore  @ManyToOne  public LibraryGroup  libraryGroup;
                                    @JsonIgnore  @ManyToOne  public SingleLibrary singleLibrary;

                                    @JsonIgnore  @ManyToOne  public C_Program c_program;
                                    @JsonIgnore  @ManyToOne  public B_Program     b_program;

    @JsonIgnore   @OneToOne(mappedBy="version_object",  cascade=CascadeType.ALL)  public B_Program_Cloud b_program_cloud;
    @JsonIgnore   @OneToMany(mappedBy="version_object", cascade=CascadeType.ALL)  public List<B_Program_Homer> b_program_homers = new ArrayList<>();

    //@JsonIgnore  @ManyToOne  public B_Program_Homer b_program_homer;

    @JsonProperty  @JsonInclude(JsonInclude.Include.NON_EMPTY) public String   allFiles()    { return "http://localhost:9000/file/listOfFiles/" +  this.id;}
    @JsonProperty  @JsonInclude(JsonInclude.Include.NON_EMPTY) public Integer  files()       { return files.size(); }


    public static Finder<String, Version_Object> find = new Finder<>(Version_Object.class);
}
