package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import models.grid.Screen_Size_Type;
import utilities.Server;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class M_Program extends Model{

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String program_name;
                            @Column(columnDefinition = "TEXT")  public String program_description;
                @JsonIgnore @Column(columnDefinition = "TEXT")  public String programInString;

                                                                public boolean height_lock;
                                                                public boolean width_lock;

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date date_of_create;
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date last_update;
                                                                public String qr_token;

                                    @JsonIgnore @ManyToOne      public M_Project m_project_object; // TODO přejmenovat zpět
                                    @JsonIgnore @ManyToOne      public Screen_Size_Type screen_size_type_object;

                                    @Transient @JsonProperty public String m_project()             {  return Server.tyrion_serverAddress + "/grid/m_project/" + m_project_object.id; }
                                    @Transient @JsonProperty public String screen_size_type()      {  return Server.tyrion_serverAddress + "/grid/screen_type/" + screen_size_type_object.id; }

    @ApiModelProperty(required = false, value = "Visible here only when the object is NOT specifically required. Inversion value for \"m_code\" ")
    @JsonInclude(JsonInclude.Include.NON_NULL)  @JsonProperty public String m_code_url()            {  return m_code == null ? Server.tyrion_serverAddress + "/grid/m_program/token/" + qr_token : null ; }

    @ApiModelProperty(required = false, value = "Its here only if its possible to connect to B_Program")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty public String websocket_address()      {  return m_project_object.b_program_version == null ? null :  Server.tyrion_webSocketAddress + "/websocket/mobile/" + m_project_object.id + "/{terminal_id}"; }

    // Pokud nastavím M_Code
    @Transient @JsonIgnore public String m_code;
    @ApiModelProperty(required = false, value = "Visible here only when the object IS specifically required. Inversion value for \"m_code_url\" THIS or THAT!")
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty public String m_code() {  return m_code == null ? null : m_code; }


    //***** Private ****************************************************************************************************

    @JsonIgnore
    public void set_QR_Token() {
        while(true){ // I need Unique Value
            this.qr_token  = UUID.randomUUID().toString();
            if (M_Program.find.where().eq("qr_token", this.qr_token ).findUnique() == null) break;
        }
    }

    public static Finder<String,M_Program> find = new Finder<>(M_Program.class);


}
