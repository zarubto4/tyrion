package models;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModelProperty;
import play.mvc.Http;
import utilities.Server;
import utilities.enums.Enum_MProgram_SnapShot_settings;
import utilities.enums.Enum_Tyrion_Server_mode;
import utilities.errors.Exceptions.Tyrion_Exp_ForbidenPermission;
import utilities.errors.Exceptions.Tyrion_Exp_ObjectNotValidAnymore;
import utilities.errors.Exceptions.Tyrion_Exp_Unauthorized;
import utilities.logger.Class_Logger;
import utilities.login_entities.Secured_API;
import utilities.swagger.outboundClass.Swagger_Mobile_Connection_Summary;
import web_socket.message_objects.homer_instance.WS_Message_Grid_token_verification;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;


@Entity
@Table(name = "model_mprogram_instance_paramete")
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

    @JsonProperty @Transient String connection_token(){

        // If there is no instance - token is not required for showing.
        if(get_instance() == null) {
            return null;

        }else {

            if( snapshot_settings() == Enum_MProgram_SnapShot_settings.absolutely_public  && ( connection_token == null || connection_token.length() < 1) ){

                System.out.println("connection_token() absolutely_public nastavuji UUID");
                connection_token = parameter_prefix + UUID.randomUUID().toString();   // parameter token _
                this.update();
            }else {

                System.out.println("connection_token() absolutely_public UUID je nastavené ");
            }


        }


            return connection_token;

    }

    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public Enum_MProgram_SnapShot_settings snapshot_settings()  {

        if( get_instance() == null) return Enum_MProgram_SnapShot_settings.not_in_instance;
        if(snapshot_settings == null){
            snapshot_settings = Enum_MProgram_SnapShot_settings.only_for_project_members; // Nastavení default hodnoty pokud bude chybět
            update();
        }

        return snapshot_settings;
    }

    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String grid_app_url()  {

      switch (snapshot_settings()){

          case not_in_instance:{
                  return null;
          }


            case absolutely_public:{

                // Má předgenerovaný token - který svou platnost pozbývá jen zrušením (přechodem na jiný typ sdílení)

                return Server.grid_app_main_url + "/grid?"
                        + "p="      + this.id.toString()                      // p >> id
                        + "&t="       + connection_token()                    // t >> conection token
                        + "&l=1";
            }

            case public_with_token:{

                return Server.grid_app_main_url + "/grid?"
                        + "p="      + this.id.toString()                      // p >> id
                        + "&l=1";
            }

            case only_for_project_members:{

                return Server.grid_app_main_url + "/grid?"
                        + "p="      + this.id.toString()                      // p >> id
                        + "&l=1";
            }

            case only_for_project_members_and_imitated_emails:{

                return Server.grid_app_main_url + "/grid?"
                        + "p="      + this.id.toString()                      // p >> id
                        + "&l=1";
            }
        }

        terminal_logger.error("grid_app_url:: Not recognize snapshot_settings");
        return null;
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

    /**
     *
     * @param request_connection_token - Can be null!!
     * @return Swagger_Mobile_Connection_Summary
     * @throws VerifyError
     */
    @JsonIgnore  @Transient public Swagger_Mobile_Connection_Summary get_connection_summary(String request_connection_token, Http.Context context) throws Tyrion_Exp_ForbidenPermission, Tyrion_Exp_ObjectNotValidAnymore, Tyrion_Exp_Unauthorized {

        // OBJEKT který se variabilně naplní a vrátí
        Swagger_Mobile_Connection_Summary summary = new Swagger_Mobile_Connection_Summary();


        // Nastavení SSL
        if(Server.server_mode  == Enum_Tyrion_Server_mode.developer) {
            summary.url = "ws://";
        }else{
            summary.url = "wss://";
        }

        switch (snapshot_settings()){


            case not_in_instance:{

                throw new Tyrion_Exp_ObjectNotValidAnymore("Token is required");
            }

            case absolutely_public:{

                summary.url += get_instance().cloud_homer_server.server_url + ":" + instance.cloud_homer_server.grid_port + "/" + instance.blocko_instance_name + "/" + connection_token();
                summary.token = connection_token();
                summary.m_program = Model_MProgram.get_m_code(m_program_version);

                return summary;
            }

            case public_with_token:{

                if( request_connection_token == null){
                    throw new Tyrion_Exp_Unauthorized("Token is required");
                }

                summary.url += instance.cloud_homer_server.server_url + ":" + instance.cloud_homer_server.grid_port + "/" + instance.blocko_instance_name + "/" + connection_token();
                summary.m_program = Model_MProgram.get_m_code(m_program_version);
                summary.token = request_connection_token;

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

                summary.url += instance.cloud_homer_server.server_url + ":" +  instance.cloud_homer_server.grid_port + "/" + instance.blocko_instance_name + "/" + terminal.terminal_token;
                summary.m_program = Model_MProgram.get_m_code(m_program_version);
                summary.token = terminal.terminal_token;

                return summary;
            }


            // TODO doimplementovat
            case only_for_project_members_and_imitated_emails:{

                summary.url += instance.cloud_homer_server.server_url + instance.cloud_homer_server.grid_port + "/" + instance.b_program_name() + "/#token";
                summary.m_program = Model_MProgram.get_m_code(m_program_version);

                return summary;
            }

        }

        throw new VerifyError("Invalid settings on Instance Grid App permissions");

    }


    @JsonIgnore  @Transient public boolean verify_token_for_homer_grid_connection(WS_Message_Grid_token_verification verification){

       if(!get_instance().blocko_instance_name.equals( verification.instanceId)) return false;

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

        super.save();
    }


    @JsonIgnore @Transient
    public void synchronize() {

        terminal_logger.debug("Update :: Save object Id: {}",  this.id);

        switch (snapshot_settings()){

            case not_in_instance:{
                if(connection_token != null) connection_token = null;
                break;
            }

            case absolutely_public:{
                if(connection_token != null) connection_token = null;
                break;
            }

            case public_with_token:{

                if(connection_token == null || connection_token.length() < 1){
                    this.connection_token = UUID.randomUUID().toString() + "-" +  UUID.randomUUID().toString() + "-" +  UUID.randomUUID().toString();
                }

                break;
            }

            case only_for_project_members:{
                if(connection_token != null) connection_token = null;
                break;
            }

            case only_for_project_members_and_imitated_emails:{
                if(connection_token != null) connection_token = null;
                break;
            }
        }


    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean read_permission(){

        // check permission if program is in instance
        if(get_instance() != null){
            return  get_instance().getB_program().read_permission();
        }

        // if not (for programers of blocko versions)
        return m_program_version.m_program.read_permission();
    }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean edit_permission(){
        // check permission if program is in instance
        if(get_instance() != null){
            return  get_instance().getB_program().edit_permission();
        }
        return false;
    }


    public enum permissions{Library_create, Library_edit, Library_delete, Library_update}

    /* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_MProgramInstanceParameter> find = new Model.Finder<>(Model_MProgramInstanceParameter.class);
}
