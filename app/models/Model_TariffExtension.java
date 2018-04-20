package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.libs.Json;
import utilities.enums.ExtensionType;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.financial.extensions.configurations.Configuration;
import utilities.financial.extensions.extensions.Extension;
import utilities.logger.Logger;
import utilities.model.OrderedNamedModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "TariffExtension", description = "Model of TariffExtension")
@Table(name="TariffExtension")
public class Model_TariffExtension extends OrderedNamedModel {
    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_ProductExtension.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                       @ApiModelProperty(required = true) public String color;

          @Enumerated(EnumType.STRING) @ApiModelProperty(required = true) public ExtensionType type;
                           @Column(columnDefinition = "TEXT") @JsonIgnore public String configuration;
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

    @JsonProperty @ApiModelProperty(required = true) @Transient
    public Double price() {
        try {
            return getDoubleDailyPrice();
        } catch (Exception e) {
            return null;
        }
    }

    @JsonProperty  @ApiModelProperty(required = false, value = "Visible only for Administrator with Special Permission")  @JsonInclude(JsonInclude.Include.NON_NULL)
    public String config() {
        try {

            check_update_permission();
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

    /* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

        /**
         * Method serves for information purposes only.
         * Returned value is shown to users, because real price is Double USD * 1000.
         * @return Real price in Double.
         */
        @JsonIgnore
        public Double getDoubleDailyPrice() {
            try {
                Long price = Extension.getDailyPrice(type, configuration);
                return (double) price;

            } catch (Exception e) {
                logger.internalServerError(e);
                return 0d;
            }
        }

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Model_TariffExtension.Permission.TariffExtension_create.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Model_TariffExtension.Permission.TariffExtension_read.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Model_TariffExtension.Permission.TariffExtension_update.name())) return;
        throw new Result_Error_PermissionDenied();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Model_TariffExtension.Permission.TariffExtension_delete.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    @JsonProperty @ApiModelProperty(required = true) public void check_act_deactivate_permission()  throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Model_TariffExtension.Permission.TariffExtension_act_deactivate.name())) return;
        throw new Result_Error_PermissionDenied();
    }

    public enum Permission { TariffExtension_create, TariffExtension_read, TariffExtension_update, TariffExtension_act_deactivate, TariffExtension_delete }

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_TariffExtension> find = new Finder<>(Model_TariffExtension.class);

    public static Model_TariffExtension getById(UUID id) {
        return find.query().where().idEq(id).eq("deleted", false).findOne();
    }
}
