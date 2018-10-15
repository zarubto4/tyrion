package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.libs.Json;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.enums.ExtensionType;
import utilities.financial.extensions.configurations.Configuration;
import utilities.financial.extensions.consumptions.ResourceConsumption;
import utilities.financial.extensions.extensions.Extension;
import utilities.logger.Logger;
import utilities.model.OrderedNamedModel;
import utilities.model.Publishable;
import utilities.permission.Action;
import utilities.permission.Permissible;
import utilities.permission.WithPermission;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@ApiModel(value = "TariffExtension", description = "Model of TariffExtension")
@Table(name="TariffExtension")
public class Model_TariffExtension extends OrderedNamedModel implements Permissible, Publishable {
    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_ProductExtension.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true) public String color;
    @ApiModelProperty(required = true) public ExtensionType type;

      @Column(columnDefinition = "TEXT") @JsonIgnore public String configuration;
      @Column(columnDefinition = "TEXT") @JsonIgnore public String consumption;
    @JsonProperty @ApiModelProperty(required = true) public boolean active;

       @JoinTable(name = "tariff_extensions_included")
       @JsonIgnore @ManyToMany(mappedBy="extensions_included", fetch = FetchType.LAZY)  public List<Model_Tariff> tariffs_included = new ArrayList<>();

       @JoinTable(name = "tariff_extensions_recommended")
       @JsonIgnore @ManyToMany(mappedBy="extensions_recommended", fetch = FetchType.LAZY)  public List<Model_Tariff> tariffs_recommended = new ArrayList<>();

/* CONSTUCTOR *****-----------------------------------------------------------------------------------------------------*/
    public Model_TariffExtension() {
        super(find);
    }

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

    /**
     * ONLY FOR FRONTEND! For calculations usegetPrice()
     * @return
     */
    @JsonProperty @ApiModelProperty(required = true) @Transient
    public double price() {
        return getPrice().setScale(Server.financial_price_scale, Server.financial_price_rounding).doubleValue();
    }

    @WithPermission @JsonProperty @ApiModelProperty(required = false, value = "Visible only for Administrator with Special Permission")  @JsonInclude(JsonInclude.Include.NON_NULL)
    public String config() {
        try {
            if (configuration == null) {
                throw new NullPointerException();
            }
            return Json.toJson(Configuration.getConfiguration(type, configuration)).toString();

        } catch (NullPointerException e) {
            return "{\"error\":\"configuration is not set yet\"}";
        } catch (Exception e) {
            logger.internalServerError(e);
            return "{\"error\":\"config file error, or required permission\"}";
        }
    }

    @WithPermission @JsonProperty @ApiModelProperty(required = false, value = "Visible only for Administrator with Special Permission")  @JsonInclude(JsonInclude.Include.NON_NULL)
    public String consumption() {
        try {

            if (consumption == null) {
                throw new NullPointerException();
            }
            return Json.toJson(ResourceConsumption.getConsumption(type, consumption)).toString();

        } catch (NullPointerException e) {
            return "{\"error\":\"consumption is not set yet\"}";
        } catch (Exception e) {
            logger.internalServerError(e);
            return "{\"error\":\"config file error, or required permission\"}";
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public boolean isPublic() {
        return true;
    }

    @JsonIgnore
    public Extension createExtension() throws Exception {
        Class<? extends Extension> extensionClass = type.getExtensionClass();
        if(extensionClass == null) {
            throw new IllegalStateException("No extension class.");
        }

        return extensionClass.newInstance();
    }

    @JsonIgnore
    public BigDecimal getPrice() {
        try {
            // not very nice, but since this class violates a lot of programming principles anyway, we let it be for this moment
            // to be fixed one day
            Extension extension = createExtension();
            Configuration config = Configuration.getConfiguration(type, configuration);
            ResourceConsumption consump = ResourceConsumption.getConsumption(type, consumption);
            return extension.getPrice(config, consump);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.TARIFF_EXTENSION;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_TariffExtension.class)
    public static CacheFinder<Model_TariffExtension> find = new CacheFinder<>(Model_TariffExtension.class);
}
