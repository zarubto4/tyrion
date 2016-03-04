package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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




    @OneToMany(mappedBy="m_project_object", cascade = CascadeType.ALL) public List<M_Program> m_programs = new ArrayList<>();


    @JsonProperty @Transient public String project()    {  return Server.serverAddress + "/project/project/" + project.id; }


    public static Finder<String,M_Project> find = new Finder<>(M_Project.class);
}

