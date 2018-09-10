package utilities.financial.services;

import models.Model_Invoice;
import utilities.enums.InvoiceStatus;

import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class InvoiceService {
    public Collection<Model_Invoice> getPaidInvoicesWithNoDetails() {
        return Model_Invoice.find.query()
                .where()
                .eq("status", InvoiceStatus.PAID)
                .and()
                .isNull("fakturoid_id")
                .findList();
    }

    public Collection<Model_Invoice> getInvoices(InvoiceStatus status) {
        return Model_Invoice.find.query()
                .where()
                .eq("status", status)
                .findList();
    }
}
