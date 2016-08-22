package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.compiler.Board;
import models.compiler.Version_Object;

import javax.persistence.*;


@Entity
public class B_Pair extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;


    @JsonIgnore @ManyToOne()                              public Version_Object c_program_version;      // Týká se aktualizace C_Programu na HW.
    @JsonIgnore @ManyToOne()                              public Board board;


    // B_Program - Skupiny HW pod Yodou
                                                                 @JsonIgnore @ManyToOne()   public B_Program_Hw_Group device_board_pair;  // Devices
    @JsonIgnore @OneToOne(cascade=CascadeType.ALL)  @JoinColumn(name="main_board_pair_id")  public B_Program_Hw_Group main_board_pair;    // Master Boards - třeba Yoda



/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty   @Transient public String c_program_version_id() { return c_program_version == null ? null : c_program_version.id;}
    @JsonProperty   @Transient public String c_program_id()         { return c_program_version == null ? null : c_program_version.c_program.id;}
    @JsonProperty   @Transient public String virtual_input_output() { return ( c_program_version == null || c_program_version.c_compilation == null) ? null : ( c_program_version.c_compilation.virtual_input_output == null ? null : c_program_version.c_compilation.virtual_input_output); }
    @JsonProperty   @Transient public String board_id()             { return board.id;}


    @JsonProperty  @Transient  public String type_of_board_id()     { return board.type_of_board.id; }
    @JsonProperty  @Transient  public String type_of_board_name()   { return board.type_of_board.name; }

    /* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,B_Pair> find = new Finder<>(B_Pair.class);

}
