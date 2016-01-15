package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Version extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String versionName;
                     @Column(columnDefinition = "TEXT")      public String versionDescription;
                                                             public Date   dateOfCreate;
                                           @JsonIgnore       public Double azureLinkVersion;

    @JsonIgnore  @ManyToMany(cascade = CascadeType.ALL)  public List<LibraryRecord> records = new ArrayList<>();
    @JsonIgnore  @ManyToOne  public LibraryGroup libraryGroup;
    @JsonIgnore  @ManyToOne  public SingleLibrary singleLibrary;

    @JsonProperty  @JsonInclude(JsonInclude.Include.NON_EMPTY) public String  getAllFiles() { return "http://localhost:9000/compilation/library/listOfFiles/" +  this.id;}
    @JsonProperty  @JsonInclude(JsonInclude.Include.NON_EMPTY) public Integer files()       { return records.size(); }


    public static Finder<String, Version> find = new Finder<>(Version.class);
}
