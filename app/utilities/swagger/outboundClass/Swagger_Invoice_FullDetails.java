package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import models.project.global.financial.Invoice;
import models.project.global.financial.Invoice_item;

import java.util.List;

@ApiModel(description = "Json Model for new Producer",
          value = "Invoice_full_details")
public class Swagger_Invoice_FullDetails {

    public Invoice invoice;
    public List<Invoice_item> invoice_items;
}
