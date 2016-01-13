package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
public class HomerProgram extends Model {


    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String programId;
                                                            public String programName;
                                                            public String programDescription;
    @JsonIgnore @Column(columnDefinition = "TEXT")          public String programInString;
                                                            public Date dateOfCreate;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL)      public List<Homer> successfullyUploaded = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="program", cascade = CascadeType.ALL) public List<ForUploadProgram> forUploadPrograms = new ArrayList<>();
    @JsonIgnore @ManyToOne                                  public Project project;

    @JsonProperty public String listOfUploadedHomers(){  return "http://localhost:9000/project/listOfUploadedHomers/" + this.programId; }
    @JsonProperty public String listOfHomersWaitingForUpload(){  return "http://localhost:9000/project/listOfHomersWaitingForUpload/" + this.programId; }
    @JsonProperty public String programinJson(){  return "http://localhost:9000/project/programInJson/" + this.programId; }
    @JsonProperty public String projectinJson(){  return "http://localhost:9000/project/" + this.project.projectId; }





    public HomerProgram(){}
    public static Finder<String,HomerProgram> find = new Finder<>(HomerProgram.class);



}
