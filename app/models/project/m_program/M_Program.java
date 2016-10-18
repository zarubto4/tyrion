package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;
import models.grid.Screen_Size_Type;
import models.project.global.Project;
import utilities.Server;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class M_Program extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;


    //# Název a popis Programu
    @JsonInclude(JsonInclude.Include.NON_NULL)                                      public String program_name;
    @JsonInclude(JsonInclude.Include.NON_NULL)  @Column(columnDefinition = "TEXT")  public String program_description;


    @JsonInclude(JsonInclude.Include.NON_NULL)                                     public String version_name;
    @JsonInclude(JsonInclude.Include.NON_NULL) @Column(columnDefinition = "TEXT")  public String version_description;

    //# NAstavení Programu
    @JsonIgnore     public boolean height_lock;
    @JsonIgnore     public boolean width_lock;
    @ApiModelProperty(required = true)      public String qr_token;


    //# Vazby Programu
    @JsonIgnore @ManyToOne      public M_Project m_project;                 // Jen u Main (prvního prvku) - ostatní se dotazují viz metody níže
    @JsonIgnore @ManyToOne      public Screen_Size_Type screen_size_type;   // Jen u Main (prvního prvku) - ostatní se dotazují viz metody níže


    // Každá verze má datum vytvoření
    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp in millis", example = "1458315085338") public Date date_of_create;


    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)  public M_Program parent_program;    // Zvolen jiný spůsob verzování - a asi do budoucna ten správný - kdy je verze vázána na verzi první. Verze první nejde smazat.
    @JsonIgnore @OneToMany(mappedBy="parent_program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("date_of_create asc") public List<M_Program> versions = new ArrayList<>();   // Hlavní verze má na sobě napojené všechny další

    @JsonInclude(JsonInclude.Include.NON_NULL) @Column(columnDefinition = "TEXT")  public String m_code;                // TODO do Azure!      // Pokud nastavím M_Code (Slouží k zobrazení celého m_code v případě že vracím konkrétní objekt a né pole objektů kde je jen odkaz na získání codu
    @JsonInclude(JsonInclude.Include.NON_NULL) @Column(columnDefinition = "TEXT")  public String virtual_input_output;


/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @Transient @JsonProperty @ApiModelProperty(required = true) public  String m_project_id()             {  return m_project != null           ? m_project.id : parent_program.m_project_id();}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  String screen_size_type_id()      {  return screen_size_type != null    ? screen_size_type.id : parent_program.screen_size_type_id();}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  boolean height_lock()             {  return m_project != null           ? height_lock: parent_program.height_lock();}
    @Transient @JsonProperty @ApiModelProperty(required = true) public  boolean width_lock()              {  return m_project != null           ? width_lock : parent_program.width_lock();}


    @ApiModelProperty(required = false, value = "Its here only if its possible to connect to B_Program") @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty public String websocket_address(){
        return (m_project == null || m_project.b_program_version == null) ? null :  Server.tyrion_webSocketAddress + "/websocket/mobile/" + m_project.id + "/{terminal_id}";
    }


    @JsonIgnore
    public List<M_Program> get_m_program_versions() {
        if(m_project != null ) {
            this.versions.add(0, this);
            return this.versions;
        }else {
           return null;
        }
    }




/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @Transient @JsonIgnore
    public void set_QR_Token() {
        while(true){ // I need Unique Value
            this.qr_token  = UUID.randomUUID().toString();
            if (M_Program.find.where().eq("qr_token", this.qr_token ).findUnique() == null) break;
        }
    }

    @Transient @JsonIgnore
    public M_Project get_m_project(){
        return m_project != null ? m_project : parent_program.m_project;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Floating shared documentation for Swagger
    @JsonIgnore @Transient public static final String read_permission_docs              = "read: If user have M_Project.read_permission = true, you can create M_program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String create_permission_docs            = "create: If user have M_Project.update_permission = true, you can create M_Program on this M_Project - Or you need static/dynamic permission key";
    @JsonIgnore @Transient public static final String read_qr_token_permission_docs     = "read: Private settings for M_Program";


    @JsonIgnore   @Transient public boolean create_permission(){  return ( Project.find.where().where().eq("ownersOfProject.id", SecurityController.getPerson().id ).eq("m_projects.id", m_project == null ? parent_program.m_project.id : m_project.id).findUnique().create_permission() ) || SecurityController.getPerson().has_permission("M_Program_create");      }
    @JsonIgnore   @Transient public boolean read_permission()  {  return ( M_Program.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Program_read"); }
    @JsonProperty @Transient public boolean read_qr_token_permission() { return  true; } // TODO pokud uživatel vyloženě nebude chtít zakázat public přístup
    @JsonProperty @Transient public boolean edit_permission() {return SecurityController.getPerson() != null && ((M_Program.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Program_edit"));}
    @JsonProperty @Transient public boolean delete_permission(){
       if (SecurityController.getPerson() == null) return false;
        return ( M_Program.find.where().eq("m_project.project.ownersOfProject.id", SecurityController.getPerson().id).eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("M_Program_delete");
    }

    public enum permissions{ M_Program_create, M_Program_read, M_Program_edit, M_Program_delete }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,M_Program> find = new Finder<>(M_Program.class);
}
