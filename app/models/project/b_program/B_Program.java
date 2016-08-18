package models.project.b_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.SecurityController;
import controllers.WebSocketController_Incoming;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import models.project.global.Project;
import models.project.m_program.M_Project;
import play.data.Form;
import utilities.swagger.documentationClass.Swagger_Homer_DeviceList_Result;
import utilities.swagger.outboundClass.B_Program_State;
import utilities.swagger.outboundClass.Swagger_B_Program_Version;
import utilities.webSocket.WS_Homer_Cloud;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity
public class B_Program extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
                                                             public String name;
                        @Column(columnDefinition = "TEXT")   public String program_description;

    @ApiModelProperty(required = true,
                     dataType = "integer", readOnly = true,
                     value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
                     example = "1466163478925")              public Date lastUpdate;
    @ApiModelProperty(required = true,
            dataType = "integer", readOnly = true,
            value = "UNIX time in milis - Date: number of miliseconds elapsed since  Thursday, 1 January 1970",
            example = "1466163478925")                       public Date dateOfCreate;
                                    @JsonIgnore @ManyToOne   public Project project;

    @JsonIgnore   @OneToOne(mappedBy="b_program",cascade=CascadeType.ALL) public M_Project m_project;

    @JsonIgnore   @OneToMany(mappedBy="b_program", cascade=CascadeType.ALL) @OrderBy("id DESC") public List<Version_Object> version_objects = new ArrayList<>();
                                                                    @JsonProperty @Transient     public String   project_id() {  return project.id; }


/* JSON PROPERTY METHOD ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public List<Swagger_B_Program_Version> program_versions() {

        List<Swagger_B_Program_Version> versions = new ArrayList<>();

        for(Version_Object v : version_objects){

            Swagger_B_Program_Version b_program_version = new Swagger_B_Program_Version();
            b_program_version.version_Object = v;
            b_program_version.hardware_groups = v.b_program_hw_groups;

            FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", v.id).eq("file_name", "program.js").findUnique();
            if(fileRecord != null) b_program_version.program             = fileRecord.get_fileRecord_from_Azure_inString();

            versions.add(b_program_version);
        }

        return versions;
    }


    @JsonProperty @Transient public B_Program_State program_state(){

        B_Program_State state = new B_Program_State();
        state.m_project_id = m_project != null ? m_project.id : null;

        Version_Object version_object = where_program_run();

        if(version_object == null){
            state.uploaded = false;
            return state;
        }

        state.uploaded = true;
        state.version_id = version_object.id;

        if( version_object.homer_instance.cloud_homer_server != null ) {
            state.where = "cloud";
            state.set_Cloud_State(version_object.homer_instance, version_object.homer_instance.cloud_homer_server, WebSocketController_Incoming.incomingConnections_homers.containsKey( version_object.homer_instance.blocko_instance_name )  );

            if(WebSocketController_Incoming.incomingConnections_homers.containsKey( version_object.homer_instance.blocko_instance_name ) ){
                WS_Homer_Cloud homer = (WS_Homer_Cloud) WebSocketController_Incoming.incomingConnections_homers.get( version_object.homer_instance.blocko_instance_name );
                try {
                    JsonNode result = WebSocketController_Incoming.homer_get_device_list(homer);

                    Form<Swagger_Homer_DeviceList_Result> form = Form.form(Swagger_Homer_DeviceList_Result.class).bind(result);
                    Swagger_Homer_DeviceList_Result help = form.get();

                    if(help != null &&  help.status.equals("success")){
                       List<Board> boardList = Board.find.where().idIn(help.deviceList).findList();
                        state.online_boards.addAll(boardList);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }



        }
        else {
            state.where = "homer";
            state.set_Local_State(version_object.homer_instance, version_object.homer_instance.private_server, WebSocketController_Incoming.incomingConnections_homers.containsKey( version_object.homer_instance.blocko_instance_name ) );
            // Tady doplnit dotaz na HW který tam běží
            System.out.println("------------------------NApiču dopiš co je u cloudu");

        }

        return state;
    }

/* Private Documentation Class -----------------------------------------------------------------------------------------*/

    // Určeno pro metodu program_versions tohoto objektu

    // Objekt určený k vracení verze
    @JsonIgnore @Transient
    public Swagger_B_Program_Version program_version(Version_Object version_object){

        Swagger_B_Program_Version b_program_version = new Swagger_B_Program_Version();

        b_program_version.version_Object    = version_object;
        b_program_version.hardware_groups   = version_object.b_program_hw_groups;

        FileRecord fileRecord = FileRecord.find.where().eq("version_object.id", version_object.id).eq("file_name", "program.js").findUnique();
        if(fileRecord != null) b_program_version.program             = fileRecord.get_fileRecord_from_Azure_inString();

        return b_program_version;
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient  public Version_Object where_program_run(){
        Version_Object version_object = Version_Object.find
                    .where()
                    .eq("b_program.id", id)
                    .isNotNull("homer_instance")
        .findUnique();
        return  version_object;
    }


/* BlOB DATA  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore            private String azure_b_program_link;


    @JsonIgnore @Override public void save() {
        while(true){ // I need Unique Value
            this.azure_b_program_link = project.get_path() + "/b-programs/"  + UUID.randomUUID().toString();
            if (B_Program.find.where().eq("azure_b_program_link", azure_b_program_link ).findUnique() == null) break;
        }
        super.save();
    }

    @JsonIgnore @Transient
    public String get_path(){
        return azure_b_program_link;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient public boolean create_permission()  {  return  ( project.read_permission() ) || SecurityController.getPerson().has_permission("B_Program_create");  }
    @JsonProperty @Transient public boolean update_permission()  {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_update");  }
    @JsonIgnore   @Transient public boolean read_permission()    {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_read");   }
    @JsonProperty @Transient public boolean edit_permission()    {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_edit");    }
    @JsonProperty @Transient public boolean delete_permission()  {  return  ( B_Program.find.where().where().eq("project.ownersOfProject.id", SecurityController.getPerson().id ).where().eq("id", id).findRowCount() > 0) || SecurityController.getPerson().has_permission("B_Program_delete");  }

    public enum permissions{ B_Program_create, B_Program_update, B_Program_read, B_Program_edit , B_Program_delete}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
     public static Model.Finder<String,B_Program> find = new Finder<>(B_Program.class);
}

