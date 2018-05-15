package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import org.ehcache.Cache;
import play.mvc.Result;
import utilities.cache.CacheField;
import utilities.enums.ProgramType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.TaggedModel;

import javax.persistence.*;
import java.time.LocalDate;
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
    public String provider; // Sem ukládáme kro dodává simakrty, ThingsMobile, T-Mobile, Vodafone etc.. (Ano, zatím máme integraci jen na ThingsMobile)
    @JsonIgnore public UUID registration_hash; // Sem ukládáme kro dodává simakrty, ThingsMobile, T-Mobile, Vodafone etc.. (Ano, zatím máme integraci jen na ThingsMobile)

    @JsonIgnore public String private_additional_information; // Sem si ukládáme dodatečné informace, třeba kdy jsme provedli billing

    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

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

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void check_read_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore @Override
    public void check_create_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore @Override
    public void check_update_permission() throws _Base_Result_Exception {

    }

    @JsonIgnore @Override
    public void check_delete_permission() throws _Base_Result_Exception {

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