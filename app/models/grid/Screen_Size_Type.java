package models.grid;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import models.project.global.Project;
import models.project.m_program.M_Program;

import javax.persistence.*;
import java.util.List;

@Entity
public class Screen_Size_Type extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String name;


                                                                public Integer portrait_height;
                                                                public Integer portrait_width;
                                                                public Integer portrait_square_height;
                                                                public Integer portrait_square_width;
                                                                public Integer portrait_min_screens;
                                                                public Integer portrait_max_screens;

                                                                public Integer landscape_height;
                                                                public Integer landscape_width;
                                                                public Integer landscape_square_height;
                                                                public Integer landscape_square_width;
                                                                public Integer landscape_min_screens;
                                                                public Integer landscape_max_screens;


                                                                // Informace zda daný typ obrazovky podporuje režim
                                                                // na výšku i na šířku
                                                                public boolean height_lock  ;
                                                                public boolean width_lock;
                                                                public boolean touch_screen;

                                     @JsonIgnore @ManyToOne     public Project project;


    @JsonIgnore  @OneToMany(mappedBy="screen_size_type", cascade = CascadeType.ALL)     public List<M_Program> m_program_s;

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   public Boolean create_permission(){  return  project != null ? Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).findUnique().create_permission() : SecurityController.getPerson().has_permission("Screen_Size_Type_create"); }
    @JsonProperty public Boolean read_permission()  {  return  project != null ? Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).findUnique().read_permission()   : SecurityController.getPerson().has_permission("Screen_Size_Type_read"); }

    @JsonProperty public Boolean edit_permission()  {  return  project != null ? Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).findUnique().read_permission()   : SecurityController.getPerson().has_permission("Screen_Size_Type_edit"); }
    @JsonProperty public Boolean delete_permission(){  return  project != null ? Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).findUnique().read_permission()   : SecurityController.getPerson().has_permission("Screen_Size_Type_delete"); }

    public enum permissions{ Screen_Size_Type_create, Screen_Size_Type_read, Screen_Size_Type_edit, Screen_Size_Type_delete }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<String,Screen_Size_Type> find = new Finder<>(Screen_Size_Type.class);

}
