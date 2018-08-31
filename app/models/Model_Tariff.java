package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.Server;
import utilities.cache.CacheField;
import utilities.enums.BusinessModel;
import utilities.enums.PaymentMethod;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.OrderedNamedModel;
import utilities.swagger.input.Swagger_TariffLabel;
import utilities.swagger.input.Swagger_TariffLabelList;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@ApiModel("Tariff")
@Table(name="Tariff")
public class Model_Tariff extends OrderedNamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Tariff.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(unique = true)  public String identifier;

                            public boolean active;

    @Enumerated(EnumType.STRING) @JsonIgnore public BusinessModel business_model;

                            public boolean owner_details_required;
                            public boolean payment_details_required;

                @JsonIgnore public BigDecimal credit_for_beginning;  // Kredit, který se po zaregistrování připíše uživatelovi k dobru. (Náhrada Trial Verze)

                            public String color;
                            public String awesome_icon;
                @JsonIgnore public String labels_json;

    @JoinTable(name = "tariff_extensions_included")
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_TariffExtension> extensions_included = new ArrayList<>();

    @JoinTable(name = "tariff_extensions_recommended")
    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)  public List<Model_TariffExtension> extensions_recommended = new ArrayList<>();

    /* CONSTUCTOR *****-----------------------------------------------------------------------------------------------------*/
    public Model_Tariff() {
        super(find);
    }


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty public List<PaymentMethod> payment_methods() {
        try{
            return  PaymentMethod.fromString(payment_methods);

        } catch (Exception e) {
            logger.error("Cannot get payment methods", e);
        }

        return Collections.emptyList();
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Visible only for Administrator with Special Permission")
    @Transient public Double credit_for_beginning() {
        try {
            this.check_update_permission();
            return credit_for_beginning.doubleValue();
        } catch (_Base_Result_Exception e){
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }


    @JsonProperty public Double price() {
        try {
            return total_per_month();

        }catch (_Base_Result_Exception e){
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return 0d;
        }
    }

    @JsonProperty public List<Swagger_TariffLabel> labels() {
        try {

            if (labels_json == null || labels_json.length() < 4) return new ArrayList<>();

            ObjectNode request_list = Json.newObject();
            request_list.set("labels", Json.parse(labels_json));

            return formFromJsonWithValidation(Swagger_TariffLabelList.class, request_list).labels;

        } catch (_Base_Result_Exception e) {
            //nothing
            return null;
        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty
    public List<Model_TariffExtension> extensions_included() {
        // TODO order
        try {
            this.check_update_permission();
            return extensions_included;
        } catch (_Base_Result_Exception e){
            return extensions_included.stream().filter(ex -> ex.active).collect(Collectors.toList());
        }
    }

    @JsonProperty
    public List<Model_TariffExtension> extensions_recommended() {
        // TODO order
        try {
            this.check_update_permission();
            return extensions_recommended;
        } catch (_Base_Result_Exception e){
            return extensions_recommended.stream().filter(ex -> ex.active).collect(Collectors.toList());
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public final static String payment_methods = PaymentMethod.INVOICE_BASED + "|" + PaymentMethod.CREDIT_CARD; // TODO maybe editable one day

    @JsonIgnore
    public Double total_per_month() {
        try {
            BigDecimal total = this.extensions_included.stream()
                    .map(extension -> extension.getPrice())
                    .reduce(BigDecimal::add)
                    .get();

            return total.setScale(Server.financial_price_scale, Server.financial_price_rounding).doubleValue();

        } catch (Exception e) {
            logger.internalServerError(e);
            return 0d;
        }
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.Tariff_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        // True
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.Tariff_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.Tariff_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    public enum Permission { Tariff_create, Tariff_read, Tariff_update, Tariff_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @CacheField(value = Model_Tariff.class, duration = CacheField.DayCacheConstant)
    @JsonIgnore public static Cache<UUID, Model_Tariff> cache;

    public static Model_Tariff getById(UUID id) throws _Base_Result_Exception {

        Model_Tariff tariff = cache.get(id);

        if (tariff == null) {

            tariff = Model_Tariff.find.byId(id);
            if (tariff == null) throw new Result_Error_NotFound(Model_Tariff.class);

            cache.put(id, tariff);
        }
        // Check Permission
        if(tariff.its_person_operation()) {
            tariff.check_read_permission();
        }
        return tariff;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Tariff> find = new Finder<>(Model_Tariff.class);
}