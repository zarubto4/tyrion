package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.ehcache.Cache;
import play.libs.Json;
import play.mvc.Result;
import utilities.cache.CacheField;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_Block;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_Unblock;
import utilities.logger.Logger;
import utilities.model.TaggedModel;
import utilities.swagger.input.Swagger_InstanceSnapShotConfiguration;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel( value = "GSM", description = "Model of GSM")
@Table(name="gsm")
public class Model_GSM extends TaggedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GSM.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Project project;

    public Long msi_number;
    public String provider; // Sem ukládáme kdo dodává simakrty, ThingsMobile, T-Mobile, Vodafone etc.. (Ano, zatím máme integraci jen na ThingsMobile)
    @JsonIgnore public UUID registration_hash; // Sem ukládáme kro dodává simakrty, ThingsMobile, T-Mobile, Vodafone etc.. (Ano, zatím máme integraci jen na ThingsMobile)

    @JsonIgnore public String private_additional_information; // Sem si ukládáme dodatečné informace, třeba kdy jsme provedli billing

    public boolean blocked;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @Transient


   // @ApiModelProperty(dataType = "DataSim_overview")
    public JsonNode data_overview() {
        try {
            if(private_additional_information != null) {
                return Json.parse(this.private_additional_information);
            } else {
                DataSim_overview overview = baseFormFactory.formFromJsonWithValidation(DataSim_overview.class, Json.parse(this.private_additional_information));
                return null;
            }
        } catch (Exception e) {
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_project_id() {

        if (cache().get(Model_Project.class) == null) {
            cache().add(Model_Project.class, Model_Project.find.query().where().eq("gsm.id", id).select("id").findSingleAttributeList());
        }

        return cache().get(Model_Project.class);
    }

    @JsonIgnore
    public Model_Project get_project() {
        try {
            return Model_Project.getById(get_project_id());
        } catch (Exception e) {
            return null;
        }
    }


/* OPERATIONS ----------------------------------------------------------------------------------------------------------*/

    // For users - owners of SIM modules
    public void block() {
       // Kontrola oprávnění
       this.check_update_permission();

       TM_Sim_Block result = Controller_Things_Mobile.sim_block(this.msi_number);
       if (result.done) {
           this.blocked = true;
       } else {
           // Error
       }
    }

    // For users - owners of SIM modules
    public void unblock() {
        // Kontrola oprávnění
        this.check_update_permission();

        TM_Sim_Unblock result = Controller_Things_Mobile.sim_unblock(this.msi_number);
        if (result.done) {
            this.blocked = false;
        } else {
            // Error
        }
    }

    // K této mětodě chybí v Controoller_GSM Metoda kterou musíš taky komplet se Swagger objektem napsat
    // For users - owners of SIM modules
    // TODO 3.10. Setup sim traffic threshold z PDF dokumentace
    public void set_trashholds(Integer daily_traffic_threshold, boolean daily_traffic_threshold_excedded_limit,
                              Integer monthly_traffic_threshold, boolean monthly_traffic_threshold_excedded_limit,
                              Integer total_traffic_threshold, boolean total_traffic_threshold_excedded_limit) {
        // Kontrola oprávnění
        this.check_update_permission();

        // https://www.thingsmobile.com/services/business- api/setupSimTrafficThreeshold
        // Pozor dokumentace říká "Block sim exceed limit (0 = false, 1 = true)"
        // takže budeš muset true převest na "1" a false na "O"

        String hokus_pokus = total_traffic_threshold_excedded_limit ? "1" : "0";

        // TODO Controller_Things_Mobile.set_trashholds(......)
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore
    @Override
    public void save() {
        logger.debug("save :: Creating new Object");
        super.save();
    }

    @JsonIgnore
    @Override
    public void update() {
        logger.debug("update :: Update object Id: {}", this.id);
        super.update();
    }

    @JsonIgnore
    @Override
    public boolean delete() {
        logger.debug("delete: Delete object Id: {} ", this.id);
        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public class DataSim_overview {

        public void DataSim_overview(){}
        public List<DataSim_DataGram> datagram = new ArrayList<>();
    }

    public class DataSim_DataGram {

        public void DataSim_DataGram(){}

        public String period_name;
        public Date from;
        public Date to;
        public Long data_consumption; // v KB
        public List<DataSim_DataGram> detailed_datagram = new ArrayList<>();
    }


    // TODO MARTIN - Takto by měl vypadat na konci zpracovan datagram

    /*
       {
        "datagram" : [
            {
                "period_name" : "leden",
                "from" : 131231231231,
                "to" : 123141231312331,
                "data_consumption" : 31412213,
                "detailed_datagram" : [
                    {
                         "period_name" : "week-1",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 31311,
                         "detailed_datagram" : []
                    },
                    {
                         "period_name" : "week-2",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 31311.
                         "detailed_datagram" : []
                    },
                    {
                         "period_name" : "week-3",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 1231231.
                         "detailed_datagram" : []
                    }
                ]
            },
            {
                "period_name" : "unor",
                "from" : 131231231231,
                "to" : 123141231312331,
                "data_consumption" : 32123,
                "detailed_datagram" : [
                    {
                         "period_name" : "week-5",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 31311,
                         "detailed_datagram" : []
                    },
                    {
                         "period_name" : "week-6",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 31311.
                         "detailed_datagram" : []
                    },
                    {
                         "period_name" : "week-7",
                         "from" : 131231231231,
                         "to" : 123141231312331,
                         "data_consumption" : 1231231.
                         "detailed_datagram" : []
                    }
                ]
            }
       ]
     */

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void check_read_permission() throws _Base_Result_Exception {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_read_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_read_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.GSM_read.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
            this.get_project().check_read_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_read_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonIgnore @Override
    public void check_create_permission() throws _Base_Result_Exception {
        logger.error("check_create_permission - mot allowed");
        throw new Result_Error_PermissionDenied();
    }

    @JsonIgnore @Override
    public void check_update_permission() throws _Base_Result_Exception {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_update_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_update_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.GSM_update.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
            this.get_project().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonIgnore @Override
    public void check_delete_permission() throws _Base_Result_Exception {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_delete_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_delete_" + id);
                return;
            }

            if (_BaseController.person().has_permission(Permission.GSM_delete.name())) return;

            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
            this.get_project().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, true);

        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, false);
            throw new Result_Error_PermissionDenied();
        }
    }

    @JsonProperty @Transient
    public boolean un_registration_permission() {
        try {

            // Cache už Obsahuje Klíč a tak vracím hodnotu
            if (_BaseController.person().has_permission(this.getClass().getSimpleName() + "_un_register_" + id)) {
                _BaseController.person().valid_permission(this.getClass().getSimpleName() + "_un_register_" + id);
                return true;
            }

            if (_BaseController.person().has_permission(Permission.GSM_update.name())) return true;


            // Hledám Zda má uživatel oprávnění a přidávám do Listu (vracím true) -- Zde je prostor pro to měnit strukturu oprávnění
            this.get_project().check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_un_register_" + id, true);

            return true;
        } catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_un_register_" + id, false);
            return false;
        }
    }

    public enum Permission { GSM_create, GSM_read, GSM_update, GSM_edit, GSM_delete }

    /* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(Model_GSM.class)
    public static Cache<UUID, Model_GSM> cache;


    @JsonIgnore
    public static Model_GSM getById(UUID id) {
        Model_GSM gsm = cache.get(id);
        if (gsm == null) {

            gsm = find.query().where().idEq(id).eq("deleted", false).findOne();
            if (gsm == null) throw new Result_Error_NotFound(Model_Block.class);

            cache.put(id, gsm);
        }

        // Check Permission
        if(gsm.its_person_operation()) {
            gsm.check_read_permission();
        }
        return gsm;
    }

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_GSM> find = new Finder<>(Model_GSM.class);

}