package models.grid;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.blocko.Project;

import javax.persistence.*;
import java.util.Date;

@Entity
public class M_Program extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String programName;
                          @Column(columnDefinition = "TEXT")    public String programDescription;
              @JsonIgnore @Column(columnDefinition = "TEXT")    public String programInString;
                                                                public Date dateOfCreate;
                                       @JsonIgnore @ManyToOne   public Project project;


   // @JsonProperty public String listOfUploadedHomers(){  return "http://localhost:9000/project/listOfUploadedHomers/" + this.programId; }
   // @JsonProperty public String listOfHomersWaitingForUpload(){  return "http://localhost:9000/project/listOfHomersWaitingForUpload/" + this.programId; }
   // @JsonProperty public String programinJson(){  return "http://localhost:9000/project/b_programInJson/" + this.programId; }
   // @JsonProperty public String project(){  return "http://localhost:9000/project/project/" + this.project.projectId; }


    public static Finder<String,M_Program> find = new Finder<>(M_Program.class);
}

