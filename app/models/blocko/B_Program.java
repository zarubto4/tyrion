package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
public class B_Program extends Model {


    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String programId;
                                                             public String programName;
                        @Column(columnDefinition = "TEXT")   public String programDescription;
            @JsonIgnore @Column(columnDefinition = "TEXT")   public String programInString;
                                                             public Date dateOfCreate;
                                    @JsonIgnore @ManyToOne   public Project project;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL)      public List<Homer> successfullyUploaded = new ArrayList<>();

    @JsonIgnore @OneToMany(mappedBy="program", cascade = CascadeType.ALL) public List<ForUploadProgram> forUploadPrograms = new ArrayList<>();



    @JsonProperty public String listOfUploadedHomers(){  return "http://localhost:9000/project/listOfUploadedHomers/" + this.programId; }
    @JsonProperty public String listOfHomersWaitingForUpload(){  return "http://localhost:9000/project/listOfHomersWaitingForUpload/" + this.programId; }
    @JsonProperty public String programinJson(){  return "http://localhost:9000/project/programInJson/" + this.programId; }
    @JsonProperty public String project(){  return "http://localhost:9000/project/project/" + this.project.projectId; }





    public B_Program(){}
    public static Finder<String,B_Program> find = new Finder<>(B_Program.class);



}
