package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.blocko.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class C_Program extends Model {


    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String programName;
                          @Column(columnDefinition = "TEXT") public String programDescription;
                                    @JsonIgnore @ManyToOne   public Project project;
                                               @JsonIgnore   public String azurePackageLink;
                                               @JsonIgnore   public String azureStorageLink;

     @OneToMany(mappedBy="c_program", cascade=CascadeType.ALL) @OrderBy("azureLinkVersion DESC") public List<Version> versions = new ArrayList<>();



    //@JsonProperty public String  description()     { return programDescription == null     ? null : "http://localhost:9000/compilation/program/description/" +  this.id;}
    //@JsonProperty public Integer versions()        { return versions.size(); }
    //@JsonProperty public Double  lastVersion()     { return versions.isEmpty()      ? null : versions.get(0).azureLinkVersion; }


/*
    "files":[
    {
        "filename":"main",
            "content":"superlong text"
    }
    ],
            "groupOfLibraries" : [],
            "libraries": [],
            "embeddedHWId":"11111",
*/






    public static Finder<String,C_Program> find = new Finder<>(C_Program.class);

}
