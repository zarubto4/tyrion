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

                      @JsonIgnore @ManyToOne(fetch = FetchType.EAGER)       public Version_Object b_program_version;
                      @JsonIgnore @ManyToOne()                              public Version_Object c_program_version;
                      @JsonIgnore @ManyToOne()                              public Board board;
         @JsonIgnore @OneToOne(cascade=CascadeType.ALL)   @JoinColumn(name="version_master_board")   public Version_Object version_master_board;

/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty   @Transient public String c_program_version_id() { return c_program_version == null ? null : c_program_version.id;}
    @JsonProperty   @Transient public String c_program_id()         { return c_program_version == null ? null : c_program_version.c_program.id;}
    @JsonProperty   @Transient public String virtual_input_output() { return c_program_version.c_compilation == null ? null : c_program_version.c_compilation.virtual_input_output; }
    @JsonProperty   @Transient public String board_id()             { return board == null ? null : board.id;}
/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,B_Pair> find = new Finder<>(B_Pair.class);

}
