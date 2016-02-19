package models.grid;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.project.global.Project;
import models.project.m_program.M_Program;

import javax.persistence.*;
import java.util.List;

@Entity
public class Screen_Size_Type extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String name;
                                                                public Integer height;
                                                                public Integer width;

                                                                // Informace zda daný typ obrazovky podporuje režim
                                                                // na výšku i na šířku
                                                                public boolean height_lock;
                                                                public boolean width_lock;
                                                                public boolean touch_screen;
                                     @JsonIgnore @ManyToOne     public Project project;


    @JsonIgnore  @OneToMany(mappedBy="screen_size_type", cascade = CascadeType.ALL)     public List<M_Program> m_program_s;





    // Pokud je Screen Size type privátní pro jeden určitý uživatelský projekt
    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) public String private_type(){  return project==null? null:"true";}




    public static Finder<String,Screen_Size_Type> find = new Finder<>(Screen_Size_Type.class);
}
