package utilities.swagger.input;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.data.validation.Constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model if user wants to create new Product for projects",
        value = "Product_New")
public class Swagger_Product_New extends Swagger_NameAndDescription {

    @Constraints.Required
    @ApiModelProperty(required = true, value =  "Id of a selected tariff.")
    public UUID tariff_id;

    @ApiModelProperty(required = false, value = "List of Ids of Extensions")
    public List<UUID> extension_ids = new ArrayList<>();

    @ApiModelProperty(required = false, value =  "If null new customer is created. Customer can be company or single person.")
    public UUID owner_id;

    @ApiModelProperty(required = false, value =  "Contact data for new product owner. Used if owner_id is null.")
    public Swagger_Contact_Update owner_new_contact;

    @ApiModelProperty(required = false, value =  "Product payment details.")
    public Swagger_PaymentDetails_Update payment_details;
}