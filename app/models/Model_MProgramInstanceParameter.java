package models;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import utilities.Server;
import utilities.enums.Enum_MProgram_SnapShot_settings;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.UUID;


@Entity

public class Model_MProgramInstanceParameter extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_MProgramInstanceParameter.class);

/* DATABASE VALUE  ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Id @ApiModelProperty(required = true)  public UUID id;

    @JsonIgnore @ManyToOne()  public Model_MProjectProgramSnapShot m_project_program_snapshot;
    @JsonIgnore @ManyToOne()  public Model_VersionObject m_program_version;

    @JsonProperty public String                          connection_token;        // Token, pomocí kterého se vrátí konkrétní aplikace s podporou propojení na websocket
    @JsonProperty public Enum_MProgram_SnapShot_settings snapshot_settings;       // Typ Aplikace


/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true, readOnly = true) public String grid_app_url()  {

      switch (snapshot_settings){

            case absolutely_public:{

                return Server.grid_app_main_url + "/dhkahjshkfjsadgjkhjghkasdfjghkfsadjhkgafdshjgkadsfghjkadfsghjksdfkhjgsadfjhkgadfshjkgadfsjhkgsadfjhkg" ; // Lock je nesystémové dočasné řešení Cokoliv za lomitkem značí nemožnst výběru

            }

            case public_with_token:{

                return Server.grid_app_main_url + "/dhkahjshkfjsadgjkhjghkasdfjghkfsadjhkgafdshjgkadsfghjkadfsghjksdfkhjgsadfjhkgadfshjkgadfsjhkgsadfjhkg" ; // Lock je nesystémové dočasné řešení Cokoliv za lomitkem značí nemožnst výběru

            }

            case only_for_project_members:{

                Model_HomerInstance instance = Model_HomerInstance.find.where().eq("actual_instance.version_object.b_program_version_snapshots.id", m_project_program_snapshot.id).findUnique();
                return Server.grid_app_main_url + "/" +  instance.blocko_instance_name + "/" + m_program_version.m_program.id + "/" + "lock"; // Lock je nesystémové dočasné řešení Cokoliv za lomitkem značí nemožnst výběru

            }

            case only_for_project_members_and_imitated_emails:{

                return Server.grid_app_main_url + "/dhkahjshkfjsadgjkhjghkasdfjghkfsadjhkgafdshjgkadsfghjkadfsghjksdfkhjgsadfjhkgadfshjkgadfsjhkgsadfjhkg" ; // Lock je nesystémové dočasné řešení Cokoliv za lomitkem značí nemožnst výběru

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



/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/



}
