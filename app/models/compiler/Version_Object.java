package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Cloud;
import models.project.b_program.B_Program_Homer;
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
                                                @JsonIgnore  public Integer azureLinkVersion;

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date date_of_create;

    @JsonIgnore  @OneToMany(mappedBy="version_object", cascade=CascadeType.ALL)  public List<FileRecord> files = new ArrayList<>();


                                    @JsonIgnore  @ManyToOne  public LibraryGroup  libraryGroup;
                                    @JsonIgnore  @ManyToOne  public SingleLibrary singleLibrary;

                                    @JsonIgnore  @ManyToOne  public C_Program     c_program;
                                    @JsonIgnore  @ManyToOne  public B_Program     b_program;

    @JsonIgnore   @OneToOne(mappedBy="version_object",  cascade=CascadeType.ALL)  public B_Program_Cloud b_program_cloud;
    @JsonIgnore   @OneToOne(mappedBy="version_object",  cascade=CascadeType.ALL)  public B_Program_Homer b_program_homer;
    @JsonIgnore   @OneToOne(mappedBy="b_program_version",cascade=CascadeType.ALL) public M_Project m_program;




    @JsonProperty public List<String> files_id(){ List<String> l = new ArrayList<>();  for( FileRecord m : files) l.add(m.id); return l;  }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Version_Object> find = new Finder<>(Version_Object.class);

}
