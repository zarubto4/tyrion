package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import models.Model_Invoice;
import models.Model_InvoiceItem;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

import java.util.List;

@ApiModel(description = "Json Model for new Producer",
          value = "Invoice_full_details")
public class Swagger_Invoice_FullDetails extends _Swagger_Abstract_Default {

    public Model_Invoice invoice;
    public List<Model_InvoiceItem> invoice_items;
}
