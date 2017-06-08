package models;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.enums.Enum_MProgram_SnapShot_settings;
import utilities.logger.Class_Logger;

import javax.persistence.*;
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

    @JsonProperty @Transient String connection_token(){

        // If there is no instance - token is not required for showing.
        if(get_instance() == null) {
            return null;

        }else {

            if( snapshot_settings() == Enum_MProgram_SnapShot_settings.absolutely_public  && ( connection_token == null || connection_token.length() < 1) ){

                System.out.println("connection_token() absolutely_public nastavuji UUID");
                connection_token = UUID.randomUUID().toString();
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

                return Server.grid_app_main_url + "/grid?"
                        + "s="      + snapshot_settings.name()              // s >> settings
                        + "&i="      + get_instance().blocko_instance_name   // i >> instance
                        + "&m="       + version_object_id()                   // m >> m_program
                        + "&t="       + connection_token()                   // t >> conection token
                        + "&l=1";

            }

            case public_with_token:{

                return Server.grid_app_main_url + "/grid?"
                        + "s="      + snapshot_settings.name()              // s >> settings
                        + "&i="      + get_instance().blocko_instance_name   // i >> instance
                        + "&t="      + connection_token()                    // t >> conection token
                        + "&m="       + version_object_id()                   // m >> m_program
                        + "&l=1";

            }

            case only_for_project_members:{

                return Server.grid_app_main_url + "/grid?"
                        + "s="      + snapshot_settings.name()              // s >> settings
                        + "&i="      + get_instance().blocko_instance_name   // i >> instance
                        + "&m="       + version_object_id()                   // m >> m_program
                        + "&l=1";

            }

            case only_for_project_members_and_imitated_emails:{

                return Server.grid_app_main_url + "/grid?"
                        + "s="      + snapshot_settings.name()              // s >> settings
                        + "&i="      + get_instance().blocko_instance_name   // i >> instance
                        + "&m="       + version_object_id()                   // m >> m_program
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

    private Model_HomerInstance get_instance(){
        if(instance_exist_searched) return instance;
        instance_exist_searched = true;
        instance = Model_HomerInstance.find.where().eq("actual_instance.version_object.b_program_version_snapshots.id", m_project_program_snapshot.id).findUnique();
        return instance;
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
