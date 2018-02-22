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
import utilities.cache.CacheField;
import utilities.enums.BusinessModel;
import utilities.enums.PaymentMethod;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.swagger.input.Swagger_TariffLabel;
import utilities.swagger.input.Swagger_TariffLabelList;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Tariff", description = "Model of Tariff")
@Table(name="Tariff")
public class Model_Tariff extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Tariff.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(unique = true)  public String identifier;

                            public boolean active;

    @Enumerated(EnumType.STRING) @JsonIgnore public BusinessModel business_model;

                            public Integer order_position;

                            public boolean company_details_required;
                            public boolean payment_details_required;
                            public boolean payment_method_required;

                @JsonIgnore public Long credit_for_beginning;  // Kredit, který se po zaregistrování připíše uživatelovi k dobru. (Náhrada Trial Verze)

                            public String color;
                            public String awesome_icon;
                @JsonIgnore public String labels_json;

   @JsonIgnore @OneToMany(mappedBy="tariff_included", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("order_position ASC")  public List<Model_ProductExtension> extensions_included = new ArrayList<>();
    @JsonIgnore @OneToMany(mappedBy="tariff_optional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)  @OrderBy("order_position ASC") public List<Model_ProductExtension> extensions_optional = new ArrayList<>();


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty public List<Pair> payment_methods() {

        List<Pair> methods = new ArrayList<>();

        methods.add( new Pair( PaymentMethod.BANK_TRANSFER.name(), "Bank transfers") );
        methods.add( new Pair( PaymentMethod.CREDIT_CARD.name()  , "Credit Card Payment"));

        return methods;
    }

    @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL) @ApiModelProperty("Visible only for Administrator with Special Permission") @Transient public Long credit_for_beginning() {
        try {
            this.check_update_permission();
            return credit_for_beginning;
        } catch (_Base_Result_Exception e){
            return null;
        }
    }


    @JsonProperty public Double price() {
        try {
            return total_per_month();
        } catch (Exception e) {
            logger.internalServerError(e);
            return 0d;
        }
    }

    @JsonProperty public List<Swagger_TariffLabel> labels() {
        try {

            if (labels_json== null || labels_json.length() < 4) return new ArrayList<>();

            ObjectNode request_list = Json.newObject();
            request_list.set("labels", Json.parse(labels_json));

            return baseFormFactory.formFromJsonWithValidation(Swagger_TariffLabelList.class, request_list).labels;

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty
    public List<Model_ProductExtension> extensions_included() {
        try {
            this.check_update_permission();
            return Model_ProductExtension.find.query().where().eq("tariff_included.id", id).orderBy("order_position").findList();
        } catch (_Base_Result_Exception e){
            return Model_ProductExtension.find.query().where().eq("tariff_included.id", id).eq("active", true).orderBy("order_position").findList();
        }
    }

    @JsonProperty
    public List<Model_ProductExtension> extensions_optional() {
        try {
            this.check_update_permission();
            return  Model_ProductExtension.find.query().where().eq("tariff_optional.id", id).orderBy("order_position").findList();
        } catch (_Base_Result_Exception e){
            return  Model_ProductExtension.find.query().where().eq("tariff_optional.id", id).eq("active", true).orderBy("order_position").findList();
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Double total_per_month() {
        try {
            Long total_price = 0L;

            for (Model_ProductExtension extension : this.extensions_included) {
                Long price = extension.getDailyPrice();

                if (price != null)
                    total_price += price;
            }
            return (double) total_price;

        } catch (Exception e) {
            logger.internalServerError(e);
            return 0d;
        }
    }



/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        order_position = Model_Tariff.find.query().findCount() + 1;
        super.save();
    }

/* ORDER ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void up() {

        check_update_permission();

        Model_Tariff up = Model_Tariff.find.query().where().eq("order_position", (order_position-1) ).findOne();
        if (up == null) return;

        up.order_position += 1;
        up.update();

        this.order_position -= 1;
        this.update();
    }

    @JsonIgnore @Transient
    public void down() {

        check_update_permission();

        Model_Tariff down = Model_Tariff.find.query().where().eq("order_position", (order_position+1) ).findOne();
        if (down == null) return;

        down.order_position -= 1;
        down.update();

        this.order_position += 1;
        this.update();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    public class Pair {

        public Pair(String json_identifier, String user_description) {
            this.json_identifier = json_identifier;
            this.user_description = user_description;
        }

        @ApiModelProperty(required = true, readOnly = true)
        public String json_identifier;

        @ApiModelProperty(required = true, readOnly = true)
        public String user_description;

    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.Tariff_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.Tariff_read.name())) return;
        throw new Result_Error_PermissionDenied();
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

    @CacheField(value = Model_Tariff.class, timeToIdle = CacheField.DayCacheConstant)
    @JsonIgnore public static Cache<UUID, Model_Tariff> cache;

    public static Model_Tariff getById(String id) throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_Tariff getById(UUID id) throws _Base_Result_Exception {

        Model_Tariff tariff = cache.get(id);

        if (tariff == null) {

            tariff = Model_Tariff.find.byId(id);
            if (tariff == null) throw new Result_Error_NotFound(Model_Tariff.class);

            cache.put(id, tariff);
        }

        // Check Permission
        tariff.check_read_permission();
        return tariff;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Tariff> find = new Finder<>(Model_Tariff.class);
}