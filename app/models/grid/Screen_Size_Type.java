package models.grid;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.global.Project;
import models.project.m_program.M_Program;

import javax.persistence.*;
import java.util.List;

@Entity
public class Screen_Size_Type extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;
    @Column(unique=true)                                    @ApiModelProperty(required = true) public String name;


                                                            @ApiModelProperty(required = true) public Integer portrait_height;
                                                            @ApiModelProperty(required = true) public Integer portrait_width;
                                                            @ApiModelProperty(required = true) public Integer portrait_square_height;
                                                            @ApiModelProperty(required = true) public Integer portrait_square_width;
                                                            @ApiModelProperty(required = true) public Integer portrait_min_screens;
                                                            @ApiModelProperty(required = true) public Integer portrait_max_screens;

                                                            @ApiModelProperty(required = true) public Integer landscape_height;
                                                            @ApiModelProperty(required = true) public Integer landscape_width;
                                                            @ApiModelProperty(required = true) public Integer landscape_square_height;
                                                            @ApiModelProperty(required = true) public Integer landscape_square_width;
                                                            @ApiModelProperty(required = true) public Integer landscape_min_screens;
                                                            @ApiModelProperty(required = true) public Integer landscape_max_screens;


                                                                                               // Informace zda daný typ obrazovky podporuje režim
                                                                                               // na výšku i na šířku
                                                            @ApiModelProperty(required = true) public boolean height_lock  ;
                                                            @ApiModelProperty(required = true) public boolean width_lock;
                                                            @ApiModelProperty(required = true) public boolean touch_screen;

                                     @JsonIgnore @ManyToOne     public Project project;


    @JsonIgnore  @OneToMany(mappedBy="screen_size_type", cascade = CascadeType.ALL)     public List<M_Program> m_program_s;

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs   = "read: User can read all public Screen_Size_Type objects or private objects, where user have permission to read";
    @JsonIgnore @Transient public static final String create_permission_docs = "create: User can create Screen_Size_Type only on own Project (Project.update_permission = true) - Its private object shared in Project - Or user can create public object for everyone but static/dynamic permission key is required - \"Screen_Size_Type_create\" ";


    @JsonIgnore   @Transient                                    public boolean create_permission(){  return  project != null ? Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).findUnique().create_permission() : SecurityController.getPerson().has_permission("Screen_Size_Type_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return  ( project == null ? true : Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).findUnique().read_permission() ) || SecurityController.getPerson().has_permission("Screen_Size_Type_read"); }

    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return  project != null ? Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).findUnique().read_permission()   : SecurityController.getPerson().has_permission("Screen_Size_Type_edit"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return  project != null ? Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).findUnique().read_permission()   : SecurityController.getPerson().has_permission("Screen_Size_Type_delete"); }

    public enum permissions{ Screen_Size_Type_create, Screen_Size_Type_read, Screen_Size_Type_edit, Screen_Size_Type_delete }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<String,Screen_Size_Type> find = new Finder<>(Screen_Size_Type.class);

}
