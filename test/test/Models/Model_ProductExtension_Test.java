package test.Models;

import helpers.ObjectCreator;
import models.*;
import org.junit.Test;
import test.TyrionTest;
import utilities.enums.ProductEventType;
import utilities.financial.extensions.consumptions.ResourceConsumption;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class Model_ProductExtension_Test extends TyrionTest {

    @Test
    public void getExtensionEvents_All() {
        Model_Product product = ObjectCreator.createProduct();
        Model_ProductExtension productExtension = ObjectCreator.createProductExtension(product, new Date(90));

        // created event, check
        List<Model_ProductEvent> afterCreatedEvents = productExtension.getExtensionEvents(false);
        assertEquals(1, afterCreatedEvents.size());
        assertEquals(new Date(90), afterCreatedEvents.get(0).created);

        Model_ProductEvent event1 = afterCreatedEvents.get(0);
        Model_ProductEvent event3 = productExtension.saveEvent(new Date(9100), ProductEventType.EXTENSION_DEACTIVATED, null);
        Model_ProductEvent event2 = productExtension.saveEvent(new Date(910), ProductEventType.EXTENSION_ACTIVATED, null);
        Model_ProductEvent event4 = productExtension.saveEvent(new Date(9110), ProductEventType.EXTENSION_DELETED, null);

        // check if we received all events, ASCENDING order
        List<Model_ProductEvent> extensionsAsc = productExtension.getExtensionEvents(true);
        assertEquals(4, extensionsAsc.size());

        // check order
        assertEquals(event1.id, extensionsAsc.get(0).id);
        assertEquals(event2.id, extensionsAsc.get(1).id);
        assertEquals(event3.id, extensionsAsc.get(2).id);
        assertEquals(event4.id, extensionsAsc.get(3).id);

        // check if we received all events, DESCENDING order
        List<Model_ProductEvent> extensionsDesc = productExtension.getExtensionEvents(false);
        assertEquals(4, extensionsDesc.size());

        // check order
        assertEquals(event4.id, extensionsDesc.get(0).id);
        assertEquals(event3.id, extensionsDesc.get(1).id);
        assertEquals(event2.id, extensionsDesc.get(2).id);
        assertEquals(event1.id, extensionsDesc.get(3).id);
    }

    @Test
    public void getLastExtensionEvent() {
        Model_Product product = ObjectCreator.createProduct();
        Model_ProductExtension productExtension = ObjectCreator.createProductExtension(product, new Date(90));

        // created event, check
        List<Model_ProductEvent> afterCreatedEvents = productExtension.getExtensionEvents(false);
        assertEquals(1, afterCreatedEvents.size());
        assertEquals(new Date(90), afterCreatedEvents.get(0).created);

        afterCreatedEvents.get(0);
        productExtension.saveEvent(new Date(9100), ProductEventType.EXTENSION_DEACTIVATED, null);
        Model_ProductEvent event2 = productExtension.saveEvent(new Date(910), ProductEventType.EXTENSION_ACTIVATED, null);
        Model_ProductEvent event4 = productExtension.saveEvent(new Date(9110), ProductEventType.EXTENSION_DELETED, null);

        Model_ProductEvent res = productExtension.getLastExtensionEvent(new Date(10));
        assertNull(res);

        Model_ProductEvent res2 = productExtension.getLastExtensionEvent(new Date(950));
        assertNotNull(res2);
        assertEquals(res2.id, event2.id);

        Model_ProductEvent res3 = productExtension.getLastExtensionEvent(new Date(910));
        assertNotNull(res3);
        assertEquals(res3.id, event2.id);

        Model_ProductEvent res4 = productExtension.getLastExtensionEvent(new Date(9999999));
        assertNotNull(res4);
        assertEquals(res4.id, event4.id);
    }

    @Test
    public void wasActive_Date() {
        Model_Product product = ObjectCreator.createProduct();
        Model_ProductExtension productExtension = ObjectCreator.createProductExtension(product, new Date(90));

        // created event, check
        List<Model_ProductEvent> afterCreatedEvents = productExtension.getExtensionEvents(false);
        assertEquals(1, afterCreatedEvents.size());
        assertEquals(new Date(90), afterCreatedEvents.get(0).created);

        afterCreatedEvents.get(0);
        productExtension.saveEvent(new Date(9100), ProductEventType.EXTENSION_DEACTIVATED, null);
        productExtension.saveEvent(new Date(910), ProductEventType.EXTENSION_ACTIVATED, null);
        productExtension.saveEvent(new Date(9110), ProductEventType.EXTENSION_DELETED, null);

        boolean wasActive = productExtension.wasActive(new Date(50));
        assertFalse(wasActive);

        boolean wasActive2 = productExtension.wasActive(new Date(100));
        assertFalse(wasActive2);

        boolean wasActive3 = productExtension.wasActive(new Date(910));
        assertTrue(wasActive3);

        boolean wasActive4 = productExtension.wasActive(new Date(920));
        assertTrue(wasActive4);

        boolean wasActive5 = productExtension.wasActive(new Date(9100));
        assertFalse(wasActive5);

        boolean wasActive6 = productExtension.wasActive(new Date(999999));
        assertFalse(wasActive6);
    }

    @Test
    public void wasActive_FromTo() {
        Model_Product product = ObjectCreator.createProduct();
        Model_ProductExtension productExtension = ObjectCreator.createProductExtension(product, new Date(90));

        // created event, check
        List<Model_ProductEvent> afterCreatedEvents = productExtension.getExtensionEvents(false);
        assertEquals(1, afterCreatedEvents.size());
        assertEquals(new Date(90), afterCreatedEvents.get(0).created);

        afterCreatedEvents.get(0);
        productExtension.saveEvent(new Date(9100), ProductEventType.EXTENSION_DEACTIVATED, null);
        productExtension.saveEvent(new Date(910), ProductEventType.EXTENSION_ACTIVATED, null);
        productExtension.saveEvent(new Date(9110), ProductEventType.EXTENSION_DELETED, null);

        boolean wasActive = productExtension.wasActive(new Date(50), new Date(80));
        assertFalse(wasActive);

        boolean wasActive2 = productExtension.wasActive(new Date(100), new Date(200));
        assertFalse(wasActive2);

        boolean wasActive3 = productExtension.wasActive(new Date(80), new Date(1000));
        assertTrue(wasActive3);

        boolean wasActive4 = productExtension.wasActive(new Date(1100), new Date(1200));
        assertTrue(wasActive4);

        boolean wasActive5 = productExtension.wasActive(new Date(300), new Date(99999));
        assertTrue(wasActive5);

        boolean wasActive6 = productExtension.wasActive(new Date(999000), new Date(999999));
        assertFalse(wasActive6);
    }

    @Test
    public void getFirstActivationTime() {
        Model_Product product = ObjectCreator.createProduct();
        Model_ProductExtension productExtension = ObjectCreator.createProductExtension(product, new Date(90));

        // created event, check
        List<Model_ProductEvent> afterCreatedEvents = productExtension.getExtensionEvents(false);
        assertEquals(1, afterCreatedEvents.size());
        assertEquals(new Date(90), afterCreatedEvents.get(0).created);

        afterCreatedEvents.get(0);
        productExtension.saveEvent(new Date(9100), ProductEventType.EXTENSION_DEACTIVATED, null);
        productExtension.saveEvent(new Date(910), ProductEventType.EXTENSION_ACTIVATED, null);
        productExtension.saveEvent(new Date(9110), ProductEventType.EXTENSION_DELETED, null);

        Date fistActivationTime = productExtension.getFirstActivationTime();
        assertEquals(new Date(910), fistActivationTime);
    }

    @Test
    public void getFinancialEventsNotInvoiced() {
        Model_Product product = ObjectCreator.createProduct();
        Model_ProductExtension extension = ObjectCreator.createProductExtension(product, Date.from(Instant.now().minus(7, ChronoUnit.DAYS)));
        List<Model_ExtensionFinancialEvent> origEventsAsc = createEventsAsc(extension, 4, false);

        Model_Invoice invoice = ObjectCreator.createInvoice(product);
        invoice.save();
        origEventsAsc.get(0).invoice = invoice;
        origEventsAsc.stream().forEach(e -> e.save());

        List<Model_ExtensionFinancialEvent> unpaidAsc = extension.getFinancialEventsNotInvoiced(true);
        assertEquals(3, unpaidAsc.size());
        assertEquals(origEventsAsc.get(1).id, unpaidAsc.get(0).id);
        assertEquals(origEventsAsc.get(2).id, unpaidAsc.get(1).id);
        assertEquals(origEventsAsc.get(3).id, unpaidAsc.get(2).id);

        List<Model_ExtensionFinancialEvent> unpaidDesc = extension.getFinancialEventsNotInvoiced(false);
        assertEquals(3, unpaidDesc.size());
        assertEquals(origEventsAsc.get(1).id, unpaidDesc.get(2).id);
        assertEquals(origEventsAsc.get(2).id, unpaidDesc.get(1).id);
        assertEquals(origEventsAsc.get(3).id, unpaidDesc.get(0).id);
    }

    @Test
    public void getFinancialEventLast() {
        Model_Product product = ObjectCreator.createProduct();
        Model_ProductExtension extension = ObjectCreator.createProductExtension(product, Date.from(Instant.now().minus(7, ChronoUnit.DAYS)));
        List<Model_ExtensionFinancialEvent> events = createEventsAsc(extension, 4, true);

        Model_ExtensionFinancialEvent last = extension.getFinancialEventLast();

        assertNotNull(last);
        assertEquals(events.get(3).id, last.id);
    }

    @Test
    public void createFinancialEvent() throws Exception {
        Model_ProductExtension extension = spy(Model_ProductExtension.class);

        ResourceConsumption consumption1 = new ResourceConsumption() {
            public int value1 = 10;
            public String value2 = "test1";


            public boolean isEmpty() {
                return false;
            }

            public String toReadableString() {
                return "";
            }
        };

        doReturn(true).when(extension).wasActive(any(Date.class), any(Date.class));
        doReturn(consumption1).when(extension).getResourceConsumption(any(Date.class), any(Date.class));
        Model_ExtensionFinancialEvent financialEvent = extension.createFinancialEvent(new Date(1500), new Date(1000), new Date(1400));

        assertEquals(new Date(1500), financialEvent.created);
        assertEquals(new Date(1000), financialEvent.event_start);
        assertEquals(new Date(1400), financialEvent.event_end);
        assertEquals(extension, financialEvent.product_extension);
        assertNull(financialEvent.invoice);
        assertEquals("{\"value1\":10,\"value2\":\"test1\"}", financialEvent.consumption);

        doReturn(false).when(extension).wasActive(any(Date.class), any(Date.class));
        Model_ExtensionFinancialEvent financialEventEmpty = extension.createFinancialEvent(new Date(1500), new Date(1000), new Date(1400));
        assertEquals("", financialEventEmpty.consumption);
    }

    @Test
    public void updateHistory() throws Exception {
        fail(); // TODO
//        Instant minusTwoDays = Instant.now().minus(2, ChronoUnit.DAYS);
//        Date now = new Date();
//
//        Model_Product product = ObjectCreator.createAndActivateProduct();
//
//        Model_ProductExtension extension = spy(Model_ProductExtension.class);
//        extension.product = product;
////        extension.name = "Extension 1";
////        extension.description = "description extension 1";
////        extension.type = ExtensionType.PROJECT;
////        extension.active = true;
////        extension.deleted = false;
////        extension.color = "blue-madison";
////        extension.configuration = "{\"price\":1000,\"count\":100}";
////        extension.created = time;
////        extension.updated = time;
//        extension.save();
////        extension.saveEvent(time, ProductEventType.CREATED, null);
////        extension.saveEvent(time, ProductEventType.EXTENSION_ACTIVATED, null);
//
//        // old:
////        Model_ProductExtension extension = ObjectCreator.createAndActivateProductExtension(product, Date.from(minusTwoDays));
//
//        ResourceConsumption consumption1 = new ResourceConsumption() {
//            public int value1 = 10;
//            public String value2 = "test1";
//        };
//        long price1 = 545435l;
//
//        ResourceConsumption consumption2 = new ResourceConsumption() {
//            public int value1 = 11;
//            public String value2 = "test2";
//        };
//
//        ZoneId timeZoneId = ZoneId.of("UTC");
//        Instant event2Start = ZonedDateTime.ofInstant(minusTwoDays, timeZoneId).plus(1, ChronoUnit.DAYS).toLocalDate().atStartOfDay(timeZoneId).toInstant();
//
//        ExtensionService extensionServiceMock = mock(ExtensionService.class);
//        when(extensionServiceMock.getResourceConsumption(any(Model_ProductExtension.class), eq(Date.from(minusTwoDays)), any(Date.class))).thenReturn(consumption1);
//        when(extensionServiceMock.getResourceConsumption(any(Model_ProductExtension.class), eq(Date.from(event2Start)), any(Date.class))).thenReturn(consumption2);
//
//        List<Model_ExtensionFinancialEvent> newEvents = extension.updateHistory(false);
//
//        assertEquals(2, newEvents.size());
//
//        assertEquals(extension, newEvents.get(1).product_extension);
//        assertEquals(Json.toJson(consumption1).toString(), newEvents.get(1).consumption);
//        assertEquals(Date.from(minusTwoDays), newEvents.get(1).event_start);
//        // assertEquals(price1, allEvents.get(1).event_end);
//        assertNull(newEvents.get(1).invoice);
//
//        assertEquals(Json.toJson(consumption2).toString(), newEvents.get(0).consumption);

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
