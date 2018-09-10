package test.Models;

import helpers.ObjectCreator;
import models.*;
import org.junit.Test;
import test.TyrionTest;
import utilities.enums.ProductEventType;
import utilities.financial.extensions.ExtensionInvoiceItem;
import utilities.financial.extensions.configurations.Configuration;
import utilities.financial.extensions.extensions.Extension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class Model_Product_Test extends TyrionTest {
    @Test
    public void getExtensionCount() {
        Model_Product product = ObjectCreator.createProduct();
        assertEquals(0, product.getExtensionCount());

        Model_ProductExtension extension1 = ObjectCreator.createProductExtension(product, new Date());
        assertEquals(1, product.getExtensionCount());

        ObjectCreator.createProductExtension(product, new Date());
        assertEquals(2, product.getExtensionCount());

        extension1.delete();
        assertEquals(1, product.getExtensionCount());
    }

    @Test
    public void getExtensions() {
        Model_Product product = ObjectCreator.createProduct();
        assertEquals(0, product.getExtensionIds().size());

        Model_ProductExtension extension1 = ObjectCreator.createProductExtension(product, new Date());
        assertEquals(1, product.getExtensionIds().size());
        assertEquals(extension1.id, product.getExtensions().get(0).id);

        extension1.delete();
        assertEquals(0, product.getExtensionIds().size());
    }

    @Test
    public void getExtensionIds() {
        Model_Product product = ObjectCreator.createProduct();
        assertEquals(0, product.getExtensionIds().size());

        Model_ProductExtension extension1 = ObjectCreator.createProductExtension(product, new Date());
        assertEquals(1, product.getExtensionIds().size());
        assertEquals(extension1.id, product.getExtensionIds().get(0));

        extension1.delete();
        assertEquals(0, product.getExtensionIds().size());
    }

    @Test
    public void getActiveExtensions() {
        Date i1 = Date.from(Instant.now().minus(24, ChronoUnit.HOURS));
        Date i2 = Date.from(Instant.now().minus(23, ChronoUnit.HOURS));
        Date i3 = Date.from(Instant.now().minus(22, ChronoUnit.HOURS));
        Date i4 = Date.from(Instant.now().minus(21, ChronoUnit.HOURS));
        Date i5 = Date.from(Instant.now().minus(20, ChronoUnit.HOURS));
        Date i6 = Date.from(Instant.now().minus(19, ChronoUnit.HOURS));

        Model_Product product = ObjectCreator.createProduct();

        Model_ProductExtension extension1 = ObjectCreator.createAndActivateProductExtension(product, i1);
        extension1.saveEvent(i2, ProductEventType.EXTENSION_DEACTIVATED, null);
        extension1.saveEvent(i6, ProductEventType.EXTENSION_ACTIVATED, null);

        Model_ProductExtension extension2 = ObjectCreator.createAndActivateProductExtension(product, i1);
        extension2.saveEvent(i3, ProductEventType.EXTENSION_DEACTIVATED, null);
        extension2.saveEvent(i4, ProductEventType.EXTENSION_ACTIVATED, null);

        Model_ProductExtension extension3 = ObjectCreator.createAndActivateProductExtension(product, i4);
        extension3.removed = i6;
        extension3.deleted = true;
        extension3.update();
        extension3.saveEvent(i6, ProductEventType.EXTENSION_DEACTIVATED, null);
        extension3.saveEvent(i6, ProductEventType.EXTENSION_DELETED, null);

        Collection<Model_ProductExtension> activeExtensions = product.getActiveExtensions(i1, i3);
        assertEquals(2, activeExtensions.size());
        Iterator<Model_ProductExtension> iterator = activeExtensions.iterator();
        assertThat( activeExtensions, contains(
                hasProperty("id", is(iterator.next().id)),
                hasProperty("id", is(iterator.next().id))
        ));

        Collection<Model_ProductExtension> activeExtensions2 = product.getActiveExtensions(i3, i4);
        assertEquals(0, activeExtensions2.size());

        Collection<Model_ProductExtension> activeExtensions3 = product.getActiveExtensions(i5, new Date());
        assertEquals(3, activeExtensions3.size());
    }

    @Test
    public void updateHistory() {
        fail(); // TODO
    }

    @Test
    public void getFinancialEventsNotInvoiced() {
        Model_Product product = ObjectCreator.createProduct();
        Model_ProductExtension extension1 = ObjectCreator.createProductExtension(product, Date.from(Instant.now().minus(7, ChronoUnit.DAYS)));
        Model_ProductExtension extension2 = ObjectCreator.createProductExtension(product, Date.from(Instant.now().minus(5, ChronoUnit.DAYS)));

        List<Model_ExtensionFinancialEvent> origEventsAsc1 = createEventsAsc(extension1, 7, false);
        List<Model_ExtensionFinancialEvent> origEventsAsc2 = createEventsAsc(extension2, 5, false);

        Model_Invoice invoice = ObjectCreator.createInvoice(product);
        invoice.save();
        origEventsAsc1.get(0).invoice = invoice;
        origEventsAsc1.get(1).invoice = invoice;
        origEventsAsc1.get(6).invoice = invoice;
        origEventsAsc1.stream().forEach(e -> e.save());

        origEventsAsc2.get(0).invoice = invoice;
        origEventsAsc2.stream().forEach(e -> e.save());

        List<Model_ExtensionFinancialEvent> unpaidAsc = product.getFinancialEventsNotInvoiced(true);
        assertEquals(8, unpaidAsc.size());
        assertEquals(origEventsAsc1.get(2).id, unpaidAsc.get(0).id);
        assertEquals(origEventsAsc2.get(4).id, unpaidAsc.get(7).id);

        List<Model_ExtensionFinancialEvent> unpaidDesc = product.getFinancialEventsNotInvoiced( false);
        assertEquals(8, unpaidDesc.size());
        assertEquals(origEventsAsc2.get(4).id, unpaidDesc.get(0).id);
        assertEquals(origEventsAsc1.get(2).id, unpaidDesc.get(7).id);
    }

    @Test
    public void getFinancialEventFirstNotInvoiced() {
        Model_Product product = ObjectCreator.createProduct();
        Model_ProductExtension extension = ObjectCreator.createProductExtension(product, Date.from(Instant.now().minus(7, ChronoUnit.DAYS)));
        List<Model_ExtensionFinancialEvent> origEventsAsc = createEventsAsc(extension, 4, false);

        Model_Invoice invoice = ObjectCreator.createInvoice(product);
        invoice.save();
        origEventsAsc.get(0).invoice = invoice;
        origEventsAsc.get(1).invoice = invoice;
        origEventsAsc.stream().forEach(e -> e.save());

        Model_ExtensionFinancialEvent firstUnpaid = product.getFinancialEventFirstNotInvoiced();
        assertEquals(origEventsAsc.get(2).id, firstUnpaid.id);
    }

    @Test
    public void createInvoice() {
        fail(); // TODO
    }

    @Test
    public void createInvoiceItems() throws Exception  {
        long extension1CreatedTime = 1000;
        long extension2CreatedTime = 1800;
        long eventPeriod = 100;
        long invoiceFrom = 1500;
        long invoiceTo = 1500;

        Model_Product product = spy(Model_Product.class);
        ObjectCreator.setupProduct(product);

        Model_ProductExtension productExtension1 = spy(Model_ProductExtension.class);
        ObjectCreator.setupExtension(productExtension1, product, new Date(extension1CreatedTime));

        Model_ProductExtension productExtension2 = spy(Model_ProductExtension.class);
        ObjectCreator.setupExtension(productExtension2, product, new Date(extension2CreatedTime));

        Model_ExtensionFinancialEvent financialEvent1 = new Model_ExtensionFinancialEvent();
        financialEvent1.event_start = new Date(extension1CreatedTime);
        financialEvent1.event_end = new Date(extension1CreatedTime + eventPeriod);
        financialEvent1.consumption = "";

        Model_ExtensionFinancialEvent financialEvent2 = new Model_ExtensionFinancialEvent();
        financialEvent2.event_start = new Date(extension2CreatedTime);
        financialEvent2.event_end = new Date(extension2CreatedTime + eventPeriod);
        financialEvent2.consumption = "";

        Model_ExtensionFinancialEvent financialEvent3 = new Model_ExtensionFinancialEvent();
        financialEvent3.event_start = new Date(extension2CreatedTime+ eventPeriod );
        financialEvent3.event_end = new Date(extension2CreatedTime + eventPeriod * 2);
        financialEvent3.consumption = "";

        ExtensionInvoiceItem invoiceItem1 = new ExtensionInvoiceItem("item1", new BigDecimal(1), "u1", new BigDecimal(1));

        ExtensionInvoiceItem invoiceItem2 = new ExtensionInvoiceItem("item2", new BigDecimal(10), "u1", new BigDecimal(10));
        ExtensionInvoiceItem invoiceItem3 = new ExtensionInvoiceItem("item3", new BigDecimal(7), "u1", new BigDecimal(2));

        when(product.getActiveExtensions(any(Date.class), any(Date.class))).thenReturn(Arrays.asList(productExtension1, productExtension2));

        when(productExtension1.getFinancialEventsNotInvoiced(any(Boolean.class))).thenReturn(Arrays.asList(financialEvent1));
        when(productExtension2.getFinancialEventsNotInvoiced(any(Boolean.class))).thenReturn(Arrays.asList(financialEvent2, financialEvent3));

        Extension extension1 = mock(Extension.class);
        when(extension1.getInvoiceItems(any(Configuration.class), any(List.class))).thenReturn(Arrays.asList(invoiceItem1));

        Extension extension2 = mock(Extension.class);
        when(extension2.getInvoiceItems(any(Configuration.class), any(List.class))).thenReturn(Arrays.asList(invoiceItem2, invoiceItem3));


        Model_Invoice invoice = new Model_Invoice();
        invoice.save();
        List<ExtensionInvoiceItem> invoiceItems = product.createInvoiceItems(new Date(invoiceFrom), new Date(invoiceTo), invoice);

        assertEquals(3, invoiceItems.size());
    }

    /**
     *
     * @param extension
     * @param num
     * @return events from oldest to newest
     */
    private static List<Model_ExtensionFinancialEvent> createEventsAsc(Model_ProductExtension extension, int num, boolean save) {
        List<Model_ExtensionFinancialEvent> events = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Model_ExtensionFinancialEvent financialEvent = ObjectCreator.createFinanciaEvent(extension, (num - i -1), save);
            events.add(financialEvent);
        }

        return events;
    }
}
