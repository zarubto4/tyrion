package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.compiler.Version_Object;
import models.project.global.Project;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class M_Project extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String  id;
                                                                public String  program_name;
                          @Column(columnDefinition = "TEXT")    public String  program_description;
                                                                public Date    date_of_create;
                                       @JsonIgnore @ManyToOne   public Project project;

    @JsonIgnore @OneToOne   @JoinColumn(name="vrs_obj_id")      public Version_Object b_program_version;
                                                                public boolean auto_incrementing;


    @OneToMany(mappedBy="m_project_object", cascade = CascadeType.ALL) public List<M_Program> m_programs = new ArrayList<>();


    @JsonProperty @Transient public String project()                    {  return Server.serverAddress + "/project/project/" + project.id; }
    @JsonProperty @Transient public String b_progam_connected_version() {  return b_program_version == null ? null : Server.serverAddress + "/project/b_program/version/" + b_program_version.id;}
    @JsonProperty @Transient public String b_program()                  {  return b_program_version == null ? null : Server.serverAddress + "/project/b_program/" + b_program_version.b_program.b_program_id; }

    public static Finder<String,M_Project> find = new Finder<>(M_Project.class);
}

