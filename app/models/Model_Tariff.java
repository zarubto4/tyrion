package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.ForbiddenException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.libs.Json;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.Server;
import utilities.enums.BusinessModel;
import utilities.enums.EntityType;
import utilities.enums.PaymentMethod;
import utilities.logger.Logger;
import utilities.model.OrderedNamedModel;
import utilities.model.Publishable;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.permission.WithPermission;
import utilities.swagger.input.Swagger_TariffLabel;
import utilities.swagger.input.Swagger_TariffLabelList;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@ApiModel("Tariff")
@Table(name="Tariff")
public class Model_Tariff extends OrderedNamedModel implements Permissible, Publishable {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Tariff.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Column(unique = true)  public String identifier;

                            public boolean active;

                @JsonIgnore public BusinessModel business_model;

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

    @WithPermission @JsonProperty @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Visible only for Administrator with Special Permission")
    public Double credit_for_beginning() {
        try {
            return credit_for_beginning.doubleValue();
        } catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }


    @JsonProperty
    public Double price() {
        try {
            return total_per_month();

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

        } catch (Exception e) {
            logger.internalServerError(e);
            return new ArrayList<>();
        }
    }

    @JsonProperty
    public List<Model_TariffExtension> extensions_included() {
        // TODO order
        try {
            // TODO this.check_update_permission();
            return extensions_included;
        } catch (ForbiddenException e){
            return extensions_included.stream().filter(ex -> ex.active).collect(Collectors.toList());
        }
    }

    @JsonProperty
    public List<Model_TariffExtension> extensions_recommended() {
        // TODO order
        try {
            // TODO this.check_update_permission();
            return extensions_recommended;
        } catch (ForbiddenException e){
            return extensions_recommended.stream().filter(ex -> ex.active).collect(Collectors.toList());
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public boolean isPublic() {
        return true;
    }

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

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.TARIFF;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE, Action.ACTIVATE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_Tariff.class)
    public static CacheFinder<Model_Tariff> find = new CacheFinder<>(Model_Tariff.class);
}