package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.libs.Json;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.*;
import utilities.homer_auto_deploy.DigitalOceanThreadRegister;
import utilities.homer_auto_deploy.DigitalOceanTyrionService;
import utilities.homer_auto_deploy.models.common.Swagger_ExternalService;
import utilities.logger.Logger;
import utilities.model.Publishable;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.network.JsonNetworkStatus;
import utilities.network.Networkable;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.permission.WithPermission;

import javax.persistence.*;
import java.util.*;
import java.util.List;

@Entity
@ApiModel(description = "Model of HomerServer", value = "HomerServer")
@Table(name="HomerServer")
public class Model_HomerServer extends TaggedModel implements Permissible, UnderProject, Publishable, Networkable {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_HomerServer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore public String connection_identifier;
    @JsonIgnore public String hash_certificate;

    @JsonIgnore @Column(columnDefinition = "TEXT") public String json_additional_parameter;        // DB dokument - smožností rozšíření na cokoliv

    @ApiModelProperty(required = true, readOnly = true) public Integer mqtt_port;                       // MqTT Port
    @ApiModelProperty(required = true, readOnly = true) public Integer grid_port;                       // Grid APP
    @ApiModelProperty(required = true, readOnly = true) public Integer web_view_port;                   // Blocko web View
    @ApiModelProperty(required = true, readOnly = true) public Integer hardware_logger_port;              // HW logger
    @ApiModelProperty(required = true, readOnly = true) public Integer rest_api_port;                   // Rest APi Port

    @ApiModelProperty(required = true, readOnly = true) public String server_url;       // Může být i IP adresa
    @ApiModelProperty(required = true, readOnly = true) public String server_version;   // Může být i IP adresa

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;
    @JsonIgnore @OneToMany(mappedBy = "server_main", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Instance> instances = new ArrayList<>();

    public HomerType server_type;                     // Určující typ serveru
    public Date time_stamp_configuration;             // Čas konfigurace

    // Stav Deploy
    public Integer days_in_archive;
    public boolean logging;
    public boolean interactive;
    public LogLevel log_level;


    // Příznak, beukládaný do databáze, je true v případě že probíhá deployment na serveru
    @Transient @JsonIgnore public boolean deployment_in_progress = false;

/* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonNetworkStatus @Transient
    public NetworkStatus online_state;

    @WithPermission @ApiModelProperty(readOnly = true) @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public String connection_identificator() {
        return connection_identifier;
    }

    @WithPermission @ApiModelProperty(readOnly = true) @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public String hash_certificate() {
        return hash_certificate;
    }

    @ApiModelProperty(required = false, readOnly = true, value = "Visible only when server is in deployment state")
    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean deployment_in_progress() {
        try {

            if (idCache().get(DigitalOceanThreadRegister.class) != null) {
               return true;
            }

            return null;

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient public UUID get_project_id() {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, Model_Project.find.query().where().eq("servers.id", id).select("id").findSingleAttributeList());
        }

        return idCache().get(Model_Project.class);

    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return isLoaded("project") ? project : Model_Project.find.query().nullable().where().eq("servers.id", id).findOne();
    }

    @JsonIgnore @Transient public Swagger_ExternalService external_settings() {
        if(json_additional_parameter == null ) return null;
        return formFromJsonWithValidation(Swagger_ExternalService.class, Json.parse(json_additional_parameter));
    }

    @JsonIgnore @Override
    public boolean isPublic() {
        return server_type != HomerType.PRIVATE;
    }

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        logger.debug("save::Creating new Object");
        this.time_stamp_configuration = new Date();

        // TODO - ADD SSH public KEY by USER
        if (hash_certificate == null)
            hash_certificate = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
        if (connection_identifier == null)
            connection_identifier = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

        // Save Object
        super.save();
    }

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("delete::Delete object Id: {}",  this.id);

        try {
            if (external_settings() != null) {
                DigitalOceanTyrionService.remove(this);
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }

        return super.delete();
    }


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    public static Model_HomerServer get_destination_server() {


        UUID server_id = null;
        Integer count = null;

        if (Server.mode == ServerMode.PRODUCTION) {

            logger.debug("get_destination_server:: Creating new instance in production mode on production server");

            for (UUID unique_identificator_help : Model_HomerServer.find.query().where().eq("server_type", HomerType.PUBLIC).select("id").<UUID>findSingleAttributeList()) {

                Integer actual_Server_count = Model_Instance.find.query().where().eq("server_main.id", server_id).findCount();

                if (actual_Server_count == 0) {
                    server_id = unique_identificator_help;
                    break;
                } else if (server_id == null) {

                    server_id = unique_identificator_help;
                    count = actual_Server_count;

                } else if (actual_Server_count < count) {
                    server_id = unique_identificator_help;
                    count = actual_Server_count;
                }
            }

            logger.debug("get_destination_server:: Detination server is " + server_id);
            return Model_HomerServer.find.byId(server_id);

        }


        /*
            Pro stage server platí komplikovanější vyjímka - v případě že má stejné možnosti jako production server se chová jako production,
            to jest přiděluje instance na public servery. Pokud nejsou k dispozici - registrují se všechny na main.
        */
        if (Server.mode == ServerMode.STAGE) {


            if (Model_HomerServer.find.query().where().eq("server_type", HomerType.PUBLIC).findCount() < 1) {

                return Model_HomerServer.find.query().where().eq("server_type", HomerType.MAIN).findOne();

            } else {

                for (UUID unique_identificator_help : Model_HomerServer.find.query().where().eq("server_type", HomerType.PUBLIC).select("id").<UUID>findSingleAttributeList()) {

                    Integer actual_Server_count = Model_Instance.find.query().where().eq("server_main.id", server_id).findCount();

                    if (actual_Server_count == 0) {
                        server_id = unique_identificator_help;
                        break;
                    } else if (server_id == null) {

                        server_id = unique_identificator_help;
                        count = actual_Server_count;

                    } else if (actual_Server_count < count) {
                        server_id = unique_identificator_help;
                        count = actual_Server_count;

                    }
                }

                return Model_HomerServer.find.byId(server_id);
            }
        }

        /*
                V Developer režimu se instance a vše další přiděluje na Test server, který je vytvořen pomocí demodat. Spoléhá se na to,
                že se vytváří jen jeden server.
         */
        if (Server.mode == ServerMode.DEVELOPER) {
            return Model_HomerServer.find.query().where().eq("server_type", HomerType.TEST).setMaxRows(1).findOne();
        }

        return null;

    }

    @JsonIgnore
    public String get_Grid_APP_URL() {
        return server_url + ":" + grid_port + "/";
    }

    @JsonIgnore
    public String get_WebView_APP_URL() {
        return server_url + ":" + web_view_port + "/";
    }

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.HOMER;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_HomerServer.class)
    public static CacheFinder<Model_HomerServer> find = new CacheFinder<>(Model_HomerServer.class);
}
