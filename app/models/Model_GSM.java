package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.enums.EntityType;
import utilities.enums.TimePeriod;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile_Analytics;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_Block;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_List;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_Status;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_Unblock;
import utilities.gsm_services.things_mobile.statistic_class.DataSim_overview;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.model.UnderProject;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.swagger.input.Swagger_DataConsumption_Filter;
import utilities.swagger.input.Swagger_GSM_Edit;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@ApiModel( value = "GSM", description = "Model of GSM")
@Table(name="gsm")
public class Model_GSM extends TaggedModel implements Permissible, UnderProject {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GSM.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;

    public Long msi_number;

    @JsonIgnore public String provider; // Sem ukládáme kdo dodává simakrty, ThingsMobile, T-Mobile, Vodafone etc.. (Ano, zatím máme integraci jen na ThingsMobile)
    @JsonIgnore public UUID registration_hash; // Sem ukládáme kro dodává simakrty, ThingsMobile, T-Mobile, Vodafone etc.. (Ano, zatím máme integraci jen na ThingsMobile)

    @JsonIgnore  public boolean blocked;

    /* Konfigurace SIMkarty - je nutné vždy spárovat se službou ThingsMobile
    public Long    daily_traffic_threshold;                     // Přípustná hodnota v KB
    public boolean daily_traffic_threshold_exceeded_limit;      // Umožnit překročit limit
    public boolean daily_traffic_threshold_notify_type;         // Zákazník bude informován o překročení

    public Long    monthly_traffic_threshold;                   // Přípustná hodnota v KB
    public boolean monthly_traffic_threshold_exceeded_limit;    // Umožnit překročit limit
    public boolean monthly_traffic_threshold_notify_type;       // Zákazník bude informován o překročení

    public Long    total_traffic_threshold;                     // Přípustná hodnota v KB
    public boolean total_traffic_threshold_exceeded_limit;      // Umožnit překročit limit
    public boolean total_traffic_threshold_notify_type;         // Zákazník bude informován o překročení
    */

    /* Konfigurace SIMkarty - je nutné vždy spárovat se službou ThingsMobile  */
    public boolean daily_traffic_threshold_notify_type;         // Zákazník bude informován o překročení
    public boolean weekly_traffic_threshold_notify_type;         // Zákazník bude informován o překročení
    public boolean monthly_traffic_threshold_notify_type;       // Zákazník bude informován o překročení
    public boolean total_traffic_threshold_notify_type;         // Zákazník bude informován o překročení

    public boolean daily_statistic;          // Zákazník bude informován o překročení
    public boolean weekly_statistic;         // Zákazník bude informován o překročení
    public boolean monthly_statistic;        // Zákazník bude informován o překročení


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty
    @ApiModelProperty(required = false, value = "If value is in -1 - its mean that we have no idea, probably missing traffic or error on partner server")
    public Long daily_data_traffic() {
        try {

            LocalDateTime midnight = LocalDateTime.now().toLocalDate().atStartOfDay();
            DataSim_overview overview = Controller_Things_Mobile_Analytics.group_stats(Collections.singletonList(this.msi_number), new Swagger_DataConsumption_Filter(), midnight, midnight.plusDays(1), TimePeriod.DAY);

            System.out.print("Kolik mám datragramů " + overview.datagram.size());
            return  overview.datagram.get(0).data_consumption;

        } catch (Exception e) {
            logger.internalServerError(e);
            return -1L;
        }
    }

    @JsonProperty
    @ApiModelProperty(required = false, value = "If value is in -1 - its mean that we have no idea, probably missing traffic or error on partner server")
    public TM_Sim_List sim_tm_status() {
        try {

            return Controller_Things_Mobile.sim_status(msi_number);

        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }


/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_project_id() {

        if (idCache().get(Model_Project.class) == null) {
            idCache().add(Model_Project.class, Model_Project.find.query().where().eq("gsm.id", id).select("id").findSingleAttributeList());
        }

        return idCache().get(Model_Project.class);
    }

    @JsonIgnore @Override
    public Model_Project getProject() {
        return isLoaded("project") ? project : Model_Project.find.query().nullable().where().eq("gsm.id", id).findOne();
    }


/* OPERATIONS ----------------------------------------------------------------------------------------------------------*/

    // For users - owners of SIM modules
    public void block() {
       TM_Sim_Block result = Controller_Things_Mobile.sim_block(this.msi_number);
       if (result.done) {
           this.blocked = true;
       } else {
           // Error
       }
    }

    // For users - owners of SIM modules
    public void unblock() {
        TM_Sim_Unblock result = Controller_Things_Mobile.sim_unblock(this.msi_number);
        if (result.done) {
            this.blocked = false;
        } else {
            // Error
        }
    }

    // For users - owners of SIM modules
    public void set_thresholds(Swagger_GSM_Edit treshold) {
        Controller_Things_Mobile.sim_set_tresHolds(msi_number, treshold);
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Override
    public void save() {

        super.save();

        // Set name to local ID
        Controller_Things_Mobile.update_sim_name(this.msi_number, this.id.toString());
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.GSM;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.ACTIVATE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @InjectCache(keyType = Long.class, value = TM_Sim_Status.class, duration = InjectCache.TenMinutesCacheConstant, automaticProlonging = false, maxElements = 1000, name = "TM_Sim_Status_Cache")
    public static Cache<Long, TM_Sim_Status> tm_status_cache;

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_GSM.class)
    public static CacheFinder<Model_GSM> find = new CacheFinder<>(Model_GSM.class);
}