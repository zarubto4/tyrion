package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.grid.Screen_Size_Type;

import javax.persistence.*;
import java.util.Date;

@Entity
public class M_Program extends Model{

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String program_name;
                            @Column(columnDefinition = "TEXT")  public String program_description;
                @JsonIgnore @Column(columnDefinition = "TEXT")  public String programInString;

                                                                public boolean height_lock;
                                                                public boolean width_lock;

                                                                public Date date_of_create;
                                                                public Date last_update;
                                                                public String qr_token;

                                    @JsonIgnore @ManyToOne      public M_Project m_project_object;
                                    @JsonIgnore @ManyToOne      public Screen_Size_Type screen_size_type_object;

    @JsonProperty public String program()               {  return "http://localhost:9000/grid/m_project/program/" + id;}
    @JsonProperty public String m_project()             {  return "http://localhost:9000/grid/m_project/" + m_project_object.id; }
    @JsonProperty public String screen_size_type()      {  return "http://localhost:9000/grid/screen_type/" + screen_size_type_object.id; }

    public static Finder<String,M_Program> find = new Finder<>(M_Program.class);
}
