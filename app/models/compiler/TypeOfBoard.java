package models.compiler;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.project.c_program.C_Program;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class TypeOfBoard extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
     @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) @ApiModelProperty(required = true) public String id;
                                                             @ApiModelProperty(required = true) public String name;
                       @Column(columnDefinition = "TEXT")    @ApiModelProperty(required = true) public String description;
                                                                        @JsonIgnore  @ManyToOne public Producer producer;
                                                                        @JsonIgnore  @ManyToOne public Processor processor;
                                                             @ApiModelProperty(required = true) public boolean connectible_to_internet;

    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL) public List<Board> boards = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="type_of_board", cascade = CascadeType.ALL) public List<C_Program> c_programs = new ArrayList<>();


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(readOnly =true) @Transient
    @JsonProperty public String processor_id      (){ return processor == null ? null : processor.id;}

    @ApiModelProperty(readOnly =true) @Transient
    @JsonProperty public String producer_id      (){return producer == null ? null :  producer.id;}

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return SecurityController.getPerson().has_permission("TypeOfBoard_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return true; }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission()  {  return SecurityController.getPerson().has_permission("TypeOfBoard_edit");   }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return SecurityController.getPerson().has_permission("TypeOfBoard_delete"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean register_new_device_permission(){ return SecurityController.getPerson().has_permission("TypeOfBoard_register_new_device"); }

    public enum permissions{TypeOfBoard_create, TypeOfBoard_edit, TypeOfBoard_delete, TypeOfBoard_register_new_device}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String, TypeOfBoard> find = new Finder<>(TypeOfBoard.class);


}
