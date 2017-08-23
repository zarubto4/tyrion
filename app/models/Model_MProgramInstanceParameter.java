package models;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.Form;
import play.i18n.Lang;
import play.mvc.Http;
import utilities.Server;
import utilities.enums.Enum_MProgram_SnapShot_settings;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.errors.Exceptions.Tyrion_Exp_ForbidenPermission;
import utilities.errors.Exceptions.Tyrion_Exp_ObjectNotValidAnymore;
import utilities.errors.Exceptions.Tyrion_Exp_Unauthorized;
import utilities.logger.Class_Logger;
import utilities.login_entities.Secured_API;
import utilities.swagger.documentationClass.Swagger_GridWidgetVersion_GridApp_source;
import utilities.swagger.outboundClass.Swagger_Mobile_Connection_Summary;
import web_socket.message_objects.homer_instance_with_tyrion.verification.WS_Message_Grid_token_verification;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.*;


@Entity
@ApiModel( value = "MProgramInstanceParameter", description = "")
@Table(name="MProgramInstanceParameter")
public class Model_MProgramInstanceParameter extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_MProgramInstanceParameter.class);

/* DATABASE VALUE  ----------------------------------------------------------------------------------------------------*/

    @Id @ApiModelProperty(required = true) public UUID id;

    @JsonIgnore @ManyToOne()  public Model_MProjectProgramSnapShot m_project_program_snapshot; //(Vazba Done)
    @JsonIgnore @ManyToOne()  public Model_VersionObject m_program_version;                    //(Vazba Done)

    @JsonIgnore public String                            connection_token;        // Token, pomocí kterého se vrátí konkrétní aplikace s podporou propojení na websocket
    @JsonIgnore public Enum_MProgram_SnapShot_settings   snapshot_settings;       // Typ Aplikace



    // TODO Zde lze dopsat práva pro jednotlivé uživatele
    // Podle emailů, podle Tokenů, podle domény atd. atd..


/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    public static final String parameter_prefix = "part_";

    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public Enum_MProgram_SnapShot_settings snapshot_settings()  {

        if( get_instance() == null) return Enum_MProgram_SnapShot_settings.not_in_instance;
        if(snapshot_settings == null){
            snapshot_settings = Enum_MProgram_SnapShot_settings.only_for_project_members; // Nastavení default hodnoty pokud bude chybět
            update();
        }

        return snapshot_settings;
    }

    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String grid_app_url()  {

          if( snapshot_settings() == Enum_MProgram_SnapShot_settings.not_in_instance){
                  return null;
          }

          // Má předgenerovaný token - který svou platnost pozbývá jen zrušením (přechodem na jiný typ sdílení)
          return Server.grid_app_main_url  + connection_token();

    }

    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String m_program_id()  { return m_program_version.m_program.id;}
    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String m_program_name()  { return m_program_version.m_program.name;}
    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String m_program_description()  { return m_program_version.m_program.description;}
    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String version_object_id()  { return m_program_version.id;}
    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String version_object_name()  { return m_program_version.version_name;}
    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String version_object_description()  { return m_program_version.version_description;}


/* JSON IGNORE  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore  @Transient  private Model_HomerInstance instance;  // Jelikož se několikrát odkazuji na instanci - dočasnou proměnou snižuji počet SQL vyhledávání
    @JsonIgnore  @Transient  private boolean instance_exist_searched = false;        // Abych se neptal znova

    @JsonIgnore  @Transient private Model_HomerInstance get_instance(){
        if(instance_exist_searched) return instance;
        instance_exist_searched = true;
        instance = Model_HomerInstance.find.where().eq("actual_instance.version_object.b_program_version_snapshots.id", m_project_program_snapshot.id).findUnique();
        return instance;
    }


    @JsonIgnore @Transient String connection_token(){

        // If there is no instance - token is not required for showing.
        if(get_instance() == null) {
            return null;
        }else {

            if(connection_token == null) connection_token = parameter_prefix + UUID.randomUUID().toString() + UUID.randomUUID().toString();
            update();

           return  connection_token;
        }
    }

    @JsonIgnore  @Transient public Swagger_Mobile_Connection_Summary get_connection_summary(Http.Context context) throws Tyrion_Exp_ForbidenPermission, Tyrion_Exp_ObjectNotValidAnymore, Tyrion_Exp_Unauthorized {

        // OBJEKT který se variabilně naplní a vrátí
        Swagger_Mobile_Connection_Summary summary = new Swagger_Mobile_Connection_Summary();


        // Nastavení SSL
        if(Server.server_mode  == Enum_Tyrion_Server_mode.developer) {
            summary.grid_app_url = "ws://";
        }else{
            summary.grid_app_url = "wss://";
        }

        switch (snapshot_settings()){


            case not_in_instance:{

                throw new Tyrion_Exp_ObjectNotValidAnymore("Token is required");
            }

            case absolutely_public:{

                summary.grid_app_url += Model_HomerServer.get_byId(instance.server_id()).get_Grid_APP_URL() + instance.id + "/" + connection_token();
                summary.m_program = Model_MProgram.get_m_code(m_program_version).asText();
                summary.m_project_id = m_program_version.m_program.m_project_id();
                summary.m_program_id = m_program_id();
                summary.m_program_version_id = m_program_version.id;
                summary.instance_id = get_instance().id;
                summary.source_code_list = version_separator(Model_MProgram.get_m_code(m_program_version));

                // Separátor verzí


                return summary;
            }


            case only_for_project_members:{

                String token = new Secured_API().getUsername(context);

                if(token == null || token.length() < 20) throw new Tyrion_Exp_Unauthorized("Login is required");

                Model_Person person = Controller_Security.get_person();
                if(person == null) throw new Tyrion_Exp_Unauthorized("Login is required");

                if(!read_permission()) throw new Tyrion_Exp_ForbidenPermission("Login is required");


                Model_GridTerminal terminal = new Model_GridTerminal();
                terminal.device_name = "Unknown";
                terminal.device_type = "Unknown";
                terminal.date_of_create = new Date();

                if( Http.Context.current().request().headers().get("User-Agent")[0] != null) terminal.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
                else  terminal.user_agent = "Unknown browser";

                terminal.person = person;
                terminal.save();

                summary.grid_app_url += Model_HomerServer.get_byId(instance.server_id()).get_Grid_APP_URL() + instance.id + "/" + terminal.terminal_token;
                summary.m_project_id = m_program_version.m_program.m_project_id();
                summary.m_program = Model_MProgram.get_m_code(m_program_version).asText();
                summary.m_program_id = m_program_id();
                summary.m_program_version_id = m_program_version.id;
                summary.instance_id = get_instance().id;
                summary.source_code_list = version_separator(Model_MProgram.get_m_code(m_program_version));

                return summary;
            }


            /* TODO doimplementovat v budoucnu

            case only_for_project_members_and_imitated_emails:{

                summary.grid_app_url += instance.cloud_homer_server.server_url + instance.cloud_homer_server.grid_port + "/" + instance.b_program_name() + "/#token";
                summary.m_program = Model_MProgram.get_m_code(m_program_version);
                summary.instance_id = get_instance().id;

                return summary;
            }
            */

        }

        throw new VerifyError("Invalid settings on Instance Grid App permissions");

    }


    @JsonIgnore  @Transient public boolean verify_token_for_homer_grid_connection(WS_Message_Grid_token_verification verification){

       if(!get_instance().id.equals( verification.instance_id)) return false;

       switch (snapshot_settings()){

           case not_in_instance:{
               return true;
           }

           default: {
              return connection_token.equals(verification.token);
           }

       }

    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/


    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("Save :: Save object Id: {}",  this.id);

        connection_token = parameter_prefix + UUID.randomUUID().toString() + UUID.randomUUID().toString();

        super.save();
    }


/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* Helper Class --------------------------------------------------------------------------------------------------------*/

    /**
     * Modelové schéma určené k parsování m_programu která přišla z Becki
     */
    private List<Swagger_GridWidgetVersion_GridApp_source> version_separator(JsonNode m_program){

        try {

            // List for returning
            List<Swagger_GridWidgetVersion_GridApp_source> list = new ArrayList<>();

            // Parsing Json
            Form<M_Program_Parser> form = Form.form(M_Program_Parser.class).bind( (ObjectNode) new ObjectMapper().readTree(m_program.asText()));
            if(form.hasErrors()) throw new Exception("M_Program_Parser: Parsing from Blob Server: "  + form.errorsAsJson(Lang.forCode("en-US")).toString());

            // Create object
            M_Program_Parser program_parser = form.get();

            // Loking for objects
            for(Widget_Parser widget_parser : program_parser.screens.main.get(0).widgets){

                Swagger_GridWidgetVersion_GridApp_source detail = new Swagger_GridWidgetVersion_GridApp_source();
                detail.id          = widget_parser.type.version_id;
                detail.logic_json = Model_GridWidgetVersion.get_byId(widget_parser.type.version_id).logic_json;

                list.add(detail);
            }
            return list;

        } catch (Exception e) {
            terminal_logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    public static class M_Program_Parser{

        public M_Program_Parser(){}

        @Valid  public Screen_Parser screens;
    }

    public static  class Screen_Parser{

        public Screen_Parser(){}

        @Valid public List<Main_Parser> main = new ArrayList<>();

    }

    public static  class Main_Parser{

        public Main_Parser(){}

        @Valid public List<Widget_Parser> widgets  = new ArrayList<>();
    }

    public static  class Widget_Parser{

        public Widget_Parser(){}

        @Valid  public Type_Parser type;

    }

    public static  class Type_Parser{

        public Type_Parser(){}

        @Valid public String version_id;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public boolean read_permission(){

        // check permission if program is in instance
        if(get_instance() != null){
            return  get_instance().getB_program().read_permission();
        }

        // if not (for programers of blocko versions)
        return m_program_version.m_program.read_permission();
    }

    @JsonProperty @Transient @ApiModelProperty(required = true)
    public boolean edit_permission(){
        // check permission if program is in instance
        if(get_instance() != null){
            return  get_instance().getB_program().edit_permission();
        }
        return false;
    }

    public enum permissions{Library_create, Library_edit, Library_delete, Library_update}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_MProgramInstanceParameter get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);

    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model.Finder<String,Model_MProgramInstanceParameter> find = new Model.Finder<>(Model_MProgramInstanceParameter.class);
}
