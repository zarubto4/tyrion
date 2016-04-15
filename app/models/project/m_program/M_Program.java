package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.grid.Screen_Size_Type;
import utilities.Server;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class M_Program extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String program_name;
                            @Column(columnDefinition = "TEXT")  public String program_description;
                @JsonIgnore @Column(columnDefinition = "TEXT")  public String programInString;

                                                                public boolean height_lock;
                                                                public boolean width_lock;
                                                                public String qr_token;

    @JsonIgnore @ManyToOne      public M_Project m_project;
    @JsonIgnore @ManyToOne      public Screen_Size_Type screen_size_type;

    @Transient @JsonProperty public String m_project_id()             {  return m_project.id; }
    @Transient @JsonProperty public String screen_size_type_id()      {  return screen_size_type.id; }

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date date_of_create;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date last_update;



    @ApiModelProperty(required = false, value = "Visible here only when the object is NOT specifically required. Inversion value for \"m_code\" ")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @JsonProperty public String m_code_id()            {  return m_code == null ? qr_token : null ; }


    @ApiModelProperty(required = false, value = "Its here only if its possible to connect to B_Program")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty public String websocket_address()      {  return m_project.b_program_version == null ? null :  Server.tyrion_webSocketAddress + "/websocket/mobile/" + m_project.id + "/{terminal_id}"; }


    // Pokud nastavím M_Code (Slouží k zobrazení celého m_code v případě že vracím konkrétní objekt a né pole objektů kde je jen odkaz na získání codu
    @Transient @JsonIgnore public String m_code;
    @ApiModelProperty(required = false, value = "Visible here only when the object IS specifically required. Inversion value for \"m_code_url\" THIS or THAT!")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty public String m_code() {  return m_code == null ? null : m_code; }



/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void set_QR_Token() {
        while(true){ // I need Unique Value
            this.qr_token  = UUID.randomUUID().toString();
            if (M_Program.find.where().eq("qr_token", this.qr_token ).findUnique() == null) break;
        }
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty public Boolean edit_permission()  {  return ( M_Program.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Program.edit"); }
    @JsonProperty public Boolean delete_permisison(){  return ( M_Program.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Program.delete"); }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,M_Program> find = new Finder<>(M_Program.class);
}
