package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import play.mvc.Http;
import utilities.Server;
import utilities.authentication.Authentication;
import utilities.cache.CacheField;
import utilities.enums.GridAccess;
import utilities.enums.ServerMode;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_Unauthorized;
import utilities.errors.Exceptions.Tyrion_Exp_ForbidenPermission;
import utilities.errors.Exceptions.Tyrion_Exp_ObjectNotValidAnymore;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.swagger.input.Swagger_GridWidgetVersion_GridApp_source;
import utilities.swagger.output.Swagger_Mobile_Connection_Summary;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.*;

@Entity
@ApiModel(value = "MProgramInstanceParameter", description = "")
@Table(name="MProgramInstanceParameter")
public class Model_MProgramInstanceParameter extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_MProgramInstanceParameter.class);

/* DATABASE VALUE  ----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne public Model_MProjectProgramSnapShot grid_project_program_snapshot;
    @JsonIgnore @ManyToOne public Model_GridProgramVersion grid_program_version;

    @JsonIgnore public String connection_token;      // Token, pomocí kterého se vrátí konkrétní aplikace s podporou propojení na websocket
    @JsonIgnore public GridAccess snapshot_settings; // Typ Aplikace

    // TODO Zde lze dopsat práva pro jednotlivé uživatele
    // Podle emailů, podle Tokenů, podle domény atd. atd..

/* JSON PROPERTY VALUES ---------------------------------------------------------------------------------------------------------*/

    private static final String parameter_prefix = "part_";

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public GridAccess snapshot_settings() {

        if ( get_instance() == null) return GridAccess.TESTING;
        if (snapshot_settings == null) {
            snapshot_settings = GridAccess.PROJECT; // Nastavení default hodnoty pokud bude chybět
            update();
        }

        return snapshot_settings;
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true)
    public String grid_app_url() {

          if (snapshot_settings() == GridAccess.TESTING) {
                  return null;
          }

          // Má předgenerovaný token - který svou platnost pozbývá jen zrušením (přechodem na jiný typ sdílení)
          return Server.grid_app_main_url + "/" + connection_token();
    }

    @JsonProperty @ApiModelProperty(required = true, readOnly = true) public UUID grid_program_id()  { return grid_program_version.grid_program.id;}
    @JsonProperty @ApiModelProperty(required = true, readOnly = true) public String grid_program_name()  { return grid_program_version.grid_program.name;}
    @JsonProperty @ApiModelProperty(required = true, readOnly = true) public String grid_program_description()  { return grid_program_version.grid_program.description;}
    @JsonProperty @ApiModelProperty(required = true, readOnly = true) public UUID version_id()  { return grid_program_version.id;}
    @JsonProperty @ApiModelProperty(required = true, readOnly = true) public String version_name()  { return grid_program_version.name;}
    @JsonProperty @ApiModelProperty(required = true, readOnly = true) public String version_description()  { return grid_program_version.description;}

/* JSON IGNORE  ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient private Model_Instance instance;  // Jelikož se několikrát odkazuji na instanci - dočasnou proměnou snižuji počet SQL vyhledávání
    @JsonIgnore @Transient private boolean instance_exist_searched = false; // Abych se neptal znova

    @JsonIgnore
    private Model_Instance get_instance() {
        if (instance_exist_searched) return instance;
        instance_exist_searched = true;
        instance = Model_Instance.find.query().where().eq("actual_instance.version.b_program_version_snapshots.id", grid_project_program_snapshot.id).findOne(); // TODO fix query
        return instance;
    }


    @JsonIgnore
    String connection_token() {

        // If there is no instance - token is not required for showing.
        if (get_instance() == null) {
            return null;
        } else {

            if (connection_token == null) connection_token = parameter_prefix + UUID.randomUUID().toString() + UUID.randomUUID().toString();
            update();

            return  connection_token;
        }
    }

    @JsonIgnore
    public Swagger_Mobile_Connection_Summary get_connection_summary(Http.Context context) throws _Base_Result_Exception {

        // OBJEKT který se variabilně naplní a vrátí
        Swagger_Mobile_Connection_Summary summary = new Swagger_Mobile_Connection_Summary();

        // Nastavení SSL
        if (Server.mode == ServerMode.DEVELOPER) {
            summary.grid_app_url = "ws://";
        } else {
            summary.grid_app_url = "wss://";
        }

        switch (snapshot_settings()) {

            case TESTING: {
                throw new Result_Error_Unauthorized();
            }

            case PUBLIC: {

                summary.grid_app_url += Model_HomerServer.getById(instance.server_id()).get_Grid_APP_URL() + instance.id + "/" + grid_project_program_snapshot.grid_project_id() + "/"  + connection_token();
                summary.grid_program = Model_GridProgram.get_m_code(grid_program_version).asText();
                summary.grid_project_id = grid_program_version.get_grid_program().get_grid_project_id();
                summary.grid_program_id = grid_program_id();
                summary.grid_program_version_id = grid_program_version.id;
                summary.instance_id = get_instance().id;
                summary.source_code_list = version_separator(Model_GridProgram.get_m_code(grid_program_version));

                return summary;
            }

            case PROJECT: {

                // Check Token
                String token = new Authentication().getUsername(context);
                if (token == null) throw new Result_Error_PermissionDenied();

                // Check Person By Token (who send request)
                Model_Person person = _BaseController.person();
                if (person == null) throw new Result_Error_PermissionDenied();

                //Chekc Permission
                check_read_permission()

                Model_GridTerminal terminal = new Model_GridTerminal();
                terminal.device_name = "Unknown";
                terminal.device_type = "Unknown";

                if ( Http.Context.current().request().headers().get("User-Agent")[0] != null) terminal.user_agent =  Http.Context.current().request().headers().get("User-Agent")[0];
                else  terminal.user_agent = "Unknown browser";

                terminal.person = person;
                terminal.save();

                summary.grid_app_url += Model_HomerServer.getById(instance.server_id()).get_Grid_APP_URL() + instance.id + "/" + grid_project_program_snapshot.grid_project_id() + "/" + terminal.terminal_token;
                summary.grid_project_id = grid_program_version.get_grid_program().get_grid_project_id();
                summary.grid_program = Model_GridProgram.get_m_code(grid_program_version).asText();
                summary.grid_program_id = grid_program_id();
                summary.grid_program_version_id = grid_program_version.id;
                summary.instance_id = get_instance().id;
                summary.source_code_list = version_separator(Model_GridProgram.get_m_code(grid_program_version));

                return summary;
            }

            /* TODO doimplementovat v budoucnu

            case only_for_project_members_and_imitated_emails: {

                summary.grid_app_url += instance.cloud_homer_server.server_url + instance.cloud_homer_server.grid_port + "/" + instance.b_program_name() + "/#token";
                summary.grid_program = Model_MProgram.get_m_code(grid_program_version);
                summary.instance_id = get_instance().id;

                return summary;
            }

            */
        }

        throw new VerifyError("Invalid settings on Instance Grid App permissions");
    }


/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save - save to database, id: {}",  this.id);

        connection_token = parameter_prefix + UUID.randomUUID().toString() + UUID.randomUUID().toString();

        super.save();
    }

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* Helper Class --------------------------------------------------------------------------------------------------------*/

    /**
     * Modelové schéma určené k parsování m_programu která přišla z Becki
     */
    private List<Swagger_GridWidgetVersion_GridApp_source> version_separator(JsonNode m_program) {

        try {

            // List for returning
            List<Swagger_GridWidgetVersion_GridApp_source> list = new ArrayList<>();

            // Create object
            M_Program_Parser program_parser = Json.fromJson(m_program, M_Program_Parser.class);

            // Loking for objects
            for (Widget_Parser widget_parser : program_parser.screens.main.get(0).widgets) {

                Swagger_GridWidgetVersion_GridApp_source detail = new Swagger_GridWidgetVersion_GridApp_source();
                detail.id          = widget_parser.type.version_id;
                detail.logic_json = Model_WidgetVersion.getById(widget_parser.type.version_id).logic_json;

                list.add(detail);
            }
            return list;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    public static class M_Program_Parser{

        public M_Program_Parser() {}

        @Valid  public Screen_Parser screens;
    }

    public static class Screen_Parser{

        public Screen_Parser() {}

        @Valid public List<Main_Parser> main = new ArrayList<>();
    }

    public static class Main_Parser{

        public Main_Parser() {}

        @Valid public List<Widget_Parser> widgets  = new ArrayList<>();
    }

    public static class Widget_Parser{

        public Widget_Parser() {}

        @Valid  public Type_Parser type;
    }

    public static class Type_Parser{

        public Type_Parser() {}

        @Valid public String version_id;
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public boolean read_permission() {

        // check permission if program is in instance
        if (get_instance() != null) {
            return  get_instance().read_permission();
        }

        // if not (for programers of blocko versions)
        grid_program_version.get_grid_program().check_read_permission();
    }

    @JsonProperty @ApiModelProperty(required = true)
    public boolean edit_permission() {
        // check permission if program is in instance
        return get_instance() != null && get_instance().edit_permission();
    }

    public enum Permission { Library_create, Library_edit, Library_delete, Library_update }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_MProgramInstanceParameter.class)
    public static Cache<UUID, Model_MProgramInstanceParameter> cache;

    public static Model_MProgramInstanceParameter getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_MProgramInstanceParameter getById(UUID id) throws _Base_Result_Exception {
        Model_MProgramInstanceParameter instanceParameter = cache.get(id);
        if (instanceParameter == null) {

            instanceParameter = find.query().where().idEq(id).eq("deleted", false).findOne();
            if (instanceParameter == null) throw new Result_Error_NotFound(Model_MProgramInstanceParameter.class);

            cache.put(id, instanceParameter);
        }

        return instanceParameter;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_MProgramInstanceParameter> find = new Finder<>(Model_MProgramInstanceParameter.class);
}
