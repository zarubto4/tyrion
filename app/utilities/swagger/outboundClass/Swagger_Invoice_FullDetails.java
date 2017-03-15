package utilities.swagger.outboundClass;

import io.swagger.annotations.ApiModel;
import models.Model_Invoice;
import models.Model_InvoiceItem;

import java.util.List;

@ApiModel(description = "Json Model for new Producer",
          value = "Invoice_full_details")
public class Swagger_Invoice_FullDetails {

    public Model_Invoice invoice;
    public List<Model_InvoiceItem> invoice_items;
}
