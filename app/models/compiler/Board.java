package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.b_program.B_Pair;
import models.project.b_program.Homer;
import models.project.global.Project;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
public class Board extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id                                                          public String id; // Vlastní id je přidělováno
                         @Column(columnDefinition = "TEXT")      public String personal_description;
                                    @JsonIgnore  @ManyToOne      public TypeOfBoard type_of_board;  // Typ desky
                                                                 public boolean isActive;
                                               @JsonIgnore       public Date date_of_create;

                                    @JsonIgnore @ManyToOne       public Project project;
                                    @JsonIgnore @ManyToOne       public Homer homer;


    @JsonIgnore  @OneToMany(mappedBy="board",cascade=CascadeType.ALL)    public List<B_Pair> b_pair = new ArrayList<>();


    @JsonProperty  @Transient public String type_of_board_id()   { return type_of_board == null ? null : type_of_board.id; }
    @JsonProperty  @Transient public String project_id()         { return       project == null ? null : project.id; }
    @JsonProperty  @Transient public String homer_id()           { return         homer == null ? null : homer.id; }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String connection_permission_docs    = "read: If user want connect Project with board, he needs two Permission! Project.update_permission == true and also Board.first_connect_permission == true. " +
                                                                                      "- Or user need combination of static/dynamic permission key and Board.first_connect_permission == true";
    @JsonIgnore @Transient public static final String disconnection_permission_docs = "read: If user want remove Board from Project, he needs one single permission Project.update_permission, where hardware is registered. - Or user need static/dynamic permission key";

                                       @JsonIgnore   @Transient public Boolean create_permission(){  return   SecurityController.getPerson().has_permission("Board_Create"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean edit_permission()  {  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_edit")  ;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean read_permission()  {  return  (project != null && project.read_permission()  )|| SecurityController.getPerson().has_permission("Board_read")  ;}
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean delete_permission(){  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_delete");}
    @JsonProperty @Transient @ApiModelProperty(required = true) public Boolean update_permission(){  return  (project != null && project.update_permission())|| SecurityController.getPerson().has_permission("Board_update");}

    @ApiModelProperty(required = false, value = "It will be visible in Json object, only if value is true. This is an extraordinary value")
    @JsonProperty  @JsonInclude(JsonInclude.Include.NON_NULL) @Transient public Boolean first_connect_permission(){  return   project != null ? null : true;}


    public enum permissions{Board_read, Board_Create, Board_edit, Board_delete, Board_update}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, Board> find = new Finder<>(Board.class);


}
