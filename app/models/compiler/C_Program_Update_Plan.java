package models.compiler;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Objekt slouží k aktualizačnímu plánu jednotlivých zařízení!
 *
 */

@Entity
public class C_Program_Update_Plan extends Model {


/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id  @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id; // Vlastní id je přidělováno

                    public Date date_of_create;
    @ManyToOne() public Version_Object c_program_version_for_update;
    @OneToOne() @JoinColumn(name="board_for_update_id") public Board board_for_update;


/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,C_Program_Update_Plan> find = new Model.Finder<>(C_Program_Update_Plan.class);


}
