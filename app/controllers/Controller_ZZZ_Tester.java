package controllers;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import io.intercom.api.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.*;
import org.apache.poi.ss.usermodel.*;
import org.bson.Document;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import play.Environment;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;

import utilities.enums.Currency;
import utilities.enums.ExtensionType;
import utilities.enums.InvoiceStatus;
import utilities.enums.PaymentMethod;
import utilities.enums.ProductEventType;
import utilities.financial.extensions.ExtensionInvoiceItem;
import utilities.financial.fakturoid.FakturoidService;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_Credit_list;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_Status;
import utilities.gsm_services.things_mobile.statistic_class.DataSim_overview;
import utilities.logger.Logger;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utilities.logger.YouTrack;
import utilities.scheduler.SchedulerController;
import utilities.scheduler.jobs.Job_ThingsMobileSimListOnlySynchronizer;
import utilities.swagger.output.Swagger_Hardware_Registration_Hash;


@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ZZZ_Tester extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_ZZZ_Tester.class);

// CONTROLLER CONFIGURATION ############################################################################################

    @Inject
    public Controller_ZZZ_Tester(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerController scheduler) {
        super(environment, ws, formFactory, youTrack, config, scheduler);
    }

    @Inject
    private FakturoidService fakturoid;

// CONTROLLER CONTENT ##################################################################################################

    @ApiOperation(value = "Hidden test Method", hidden = true)
     public Result test1() {
         try {

             new Job_ThingsMobileSimListOnlySynchronizer().execute(null);

             return ok();

         } catch (Exception e) {
             logger.internalServerError(e);
             return badRequest();
         }
     }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test2() {
        try {

           //  TM_Sim_Credit_list credit_list = Controller_Things_Mobile.sim_credit();


            TM_Sim_Status status = Controller_Things_Mobile.sim_status(882360002156971L);

            System.out.print("Credit:: \n"  + status.prettyPrint());

            /*
            List<String> places = Arrays.asList("002100373136510236363332","004100273136510236363332","002900363136510236363332","002C00443136510236363332","003500443036511935353233",
                    "004300443136510236363332","002700373136510236363332","004900283136510236363332","003E00363136510236363332");


            List<String> registration_hash = new ArrayList<>();

            for(String full_id : places) {
                System.out.println("Check Full_id:: " + full_id);
                Model_HardwareRegistrationEntity hw = Model_HardwareRegistrationEntity.getbyFull_id(full_id);

                if(hw != null) {
                    registration_hash.add(hw.hash_for_adding);
                }else {
                    System.err.println("Full Id:: "+  full_id +" not exist in central authority");
                }
            }

            System.out.println(registration_hash);
            */

           return ok(status);

        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3() {
        try {


            /*
            List<Model_Person> persons = Model_Person.find.all();

            for(Model_Person person : persons) {

                // Create
                io.intercom.api.User user = new io.intercom.api.User()
                        .setEmail(person.email)
                        .setName( person.first_name + " " + person.last_name)
                        .addCustomAttribute(io.intercom.api.CustomAttribute.newStringAttribute("alias", person.nick_name))
                        // .addCustomAttribute(io.intercom.api.CustomAttribute.newBooleanAttribute("browncoat", true))
                        .setUserId(person.id.toString());
                User.create(user);
            }
            */



            return ok();
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(e.toString());
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test4() {
        try {


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d HH:mm:ss");

            DateTime date_from = DateTime.parse("18-06-2018 05:00:00", DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss"));
            DateTime date_to = DateTime.parse("18-06-2018 15:00:00", DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss"));


            // Připojení na MongoClient v Azure
            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://byzance-bigdata:MUqF3g4j7onnYAs6kFOopZ2spAmYDVsCsyiqtCOlo0XFWNT2ADLUY682hCBMCBSVTgwTqg9wRKZIJRFD9efgsA==@byzance-bigdata.documents.azure.com:10255/?ssl=true&replicaSet=globaldb"));

            // Připojení na konkrétní Databázi clienta
            MongoDatabase database_hardware_registration = mongoClient.getDatabase("byzance-bigdata-00");
            MongoCollection<Document> collection = database_hardware_registration.getCollection("procter-and-gamble");


            String[] columns = {
                    "38305a35-f15a-4924-85e0-6ebd7aced0b7", // 1
                    "ced2857f-4393-4539-adf9-581a85829227", // 2
                    "e044f494-510e-4547-a2b3-644996d2c4f5", // 3
                    "573dfd81-1000-480f-82b4-df6ed0bd54b1", // 4
                    "f2f31f15-26d7-4917-882c-cc8a221f8736", // 5
                    "66dbaa19-c4f1-4216-9040-595d92f02f8a", // 6
                    "XXXX", // 7
                    "5239d16c-305f-40ae-9500-b47d0178390c", // 8
                    "062720bd-462e-4448-9581-374dc30ec0d5", // 9
            };

            String[] columns_id = {
                    "Time",
                    "Sonda 1",
                    "Sonda 2",
                    "Sonda 3",
                    "Sonda 4",
                    "Sonda 5",
                    "Sonda 6",
                    "Sonda 7",
                    "Sonda 8",
                    "Sonda 9",
            };


            String filename = "NewExcelFile.xls" ;

            // Create a Workbook
            Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

            // Create a Sheet
            Sheet sheet = workbook.createSheet("Data");

            /* CreationHelper helps us create instances of various things like DataFormat,
            Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
            CreationHelper createHelper = workbook.getCreationHelper();


            // Create a Font for styling header cells
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.RED.getIndex());

            // Create a CellStyle with the font
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Create a Row


            HashMap<String, String[]> map = new HashMap<>();

            for(int i = 0; i < columns_id.length; i++) {

                if(i == 0) sheet.createRow(0);

                sheet.getRow(0).createCell(i);

                System.out.println("SEt to Row 0 cell " + i + " value " + columns_id[i]);
                sheet.getRow(0).getCell(i).setCellValue(columns_id[i]);
            }

            for(int i = 0; i < columns.length; i++) {

                BasicDBObject query = new BasicDBObject();
                query.put("device_id", columns[i]);
                query.put("date", new BasicDBObject("$gt", date_from.getMillis() ).append("$lt",  date_to.getMillis() ));
                MongoCursor<Document> cursor = collection.find(query).iterator();

                while (cursor.hasNext()) {
                    try {

                        Document d = cursor.next();
                        String s = d.get("date").toString().replace(".", "").substring(0, 13).replace("E", "0");

                        if (s.length() == 12) {
                            s = s + "0";
                        }
                        if (s.length() == 11) {
                            s = s + "00";
                        }
                        if (s.length() == 10) {
                            s = s + "00";
                        }

                        SimpleDateFormat dt1 = new SimpleDateFormat("kk:mm:ss");

                        if( !map.containsKey( dt1.format(new Date(Long.decode(s))))) {
                            map.put( dt1.format(new Date(Long.decode(s))) , new String[10]);
                        }

                        map.get(dt1.format(new Date(Long.decode(s))))[i] = d.get("temperature").toString().replace(".", ",");

                    } catch (Exception e) {

                    }
                }
            }


            int i = 1;

            SortedSet<String> keys = new TreeSet<>(map.keySet());

            for(String key : keys) {

                sheet.createRow(i);

                sheet.getRow(i).createCell(0).setCellValue(key);

                for(int j = 0; j < map.get(key).length ; j++) {
                    sheet.getRow(i).createCell(j+1).setCellValue(map.get(key)[j]);
                }

                i++;
            }


            // Write the output to a file
            FileOutputStream fileOut = new FileOutputStream("pg.xlsx");
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();


            return ok();
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(e.toString());
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test5(UUID product_id) {
        try {
            Model_Product product = Model_Product.find.byId(product_id);
            if(product == null) {
                badRequest("Wrong product id!");
            }

            if(product.owner.contact == null) {
                badRequest("We need contact id!");
            }

            if(product.getExtensionIds().size() == 0) {
                badRequest("We need an extension!");
            }

            ZonedDateTime projectCreatedZDT = ZonedDateTime.of(2018, 1, 1, 12, 33, 10, 0, ZoneId.of("UTC"));
            Date projectCreated = Date.from(projectCreatedZDT.toInstant());

            Model_ProductExtension productExtension = new Model_ProductExtension();
            productExtension.product = product;
            productExtension.created = projectCreated;
            productExtension.name = "Extension for invoice";
            productExtension.description = "Testing extension";
            productExtension.color = "red";
            productExtension.type = ExtensionType.DATABASE;
            productExtension.configuration = "{\"minutePrice\":0.001}";
            productExtension.save();

            productExtension.setActive(true);

            List<Model_ProductEvent> eventsExtension = Model_ProductEvent.find.query().where()
                    .eq("product.id", product.id)
                    .and()
                    .eq("reference", productExtension.id)
                    .findList();
            for(Model_ProductEvent event: eventsExtension) {
                if(event.event_type == ProductEventType.EXTENSION_CREATED) {
                    event.created = projectCreated;
                    event.update();
                }

                if(event.event_type == ProductEventType.EXTENSION_ACTIVATED) {
                    event.created = Date.from(projectCreated.toInstant().plusMillis(100));
                    event.update();
                }
            }

            List<Model_ProductEvent> eventsProduct = Model_ProductEvent.find.query().where()
                    .eq("product.id", product.id)
                    .and()
                    .eq("reference", null)
                    .findList();
            for(Model_ProductEvent event: eventsProduct) {
                if(event.event_type == ProductEventType.PRODUCT_CREATED) {
                    event.created = Date.from(projectCreated.toInstant().minusMillis(100));
                    event.update();
                }
            }

            Instant productCreatedFirstMidnight = LocalDateTime.ofInstant(projectCreated.toInstant(), ZoneId.of("UTC")).toLocalDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant from = projectCreated.toInstant();
            Instant to = productCreatedFirstMidnight;
            for(int i = 0; i < 20; i++) {
                Model_ExtensionFinancialEvent financialEvent = new Model_ExtensionFinancialEvent();
                financialEvent.product_extension = productExtension;
                financialEvent.event_start = Date.from(from);
                financialEvent.event_end = Date.from(to);
                financialEvent.consumption = "{\"minutes\": "+ (((to.toEpochMilli() - from.toEpochMilli()) / 60000)) +"}";
                financialEvent.save();

                from = to;
                to = from.plusSeconds(3600 * 24);
            }

            ZonedDateTime startOfNextMonth = projectCreatedZDT.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0).plusMonths(1);
            Model_Invoice invoice = product.createInvoice(projectCreated, Date.from(startOfNextMonth.toInstant()));

            boolean res = invoice.status == InvoiceStatus.UNCONFIRMED ? true : fakturoid.createAndUpdateProforma(invoice);

            return ok(res + "");
        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test6(UUID product_id) {
        try {
            Model_Product product = Model_Product.find.byId(product_id);
            if(product == null) {
                badRequest("Wrong product id!");
            }

            if(product.owner.contact == null) {
                badRequest("We need contact id!");
            }

            Model_Invoice invoice = new Model_Invoice();
            invoice.product = product;
            invoice.created = new Date();
            invoice.currency = Currency.USD;
            invoice.method = PaymentMethod.INVOICE_BASED;
            invoice.save();

            List<ExtensionInvoiceItem> extensionInvoiceItems = new ArrayList<>();
            extensionInvoiceItems.add(new ExtensionInvoiceItem("item1", new BigDecimal("10.111"), "u1", new BigDecimal("1.15")));
            extensionInvoiceItems.add(new ExtensionInvoiceItem("item2", new BigDecimal("1000"), "u1", new BigDecimal("5.36")));
            extensionInvoiceItems.add(new ExtensionInvoiceItem("item3", new BigDecimal("1.003"), "u1", new BigDecimal("10.11")));
            extensionInvoiceItems.add(new ExtensionInvoiceItem("item4", new BigDecimal("1.888"), "u1", new BigDecimal("3.2")));
            extensionInvoiceItems.add(new ExtensionInvoiceItem("item5", new BigDecimal("45646.65"), "u1", new BigDecimal("0.05")));
            extensionInvoiceItems.add(new ExtensionInvoiceItem("item6", new BigDecimal("55"), "u1", new BigDecimal("3")));
            extensionInvoiceItems.add(new ExtensionInvoiceItem("item7", new BigDecimal("1"), "u1", new BigDecimal("-100")));

            for (ExtensionInvoiceItem item : extensionInvoiceItems) {
                Model_InvoiceItem invoiceItem = new Model_InvoiceItem();
                invoiceItem.invoice = invoice;
                invoiceItem.name = item.getName();
                invoiceItem.unit_name = item.getUnitName();
                invoiceItem.quantity = item.getQuantity();
                invoiceItem.unit_price = item.getUnitPrice();
                invoiceItem.save();

                invoice.invoice_items().add(invoiceItem);
            }

            invoice.status = InvoiceStatus.UNCONFIRMED ;
            invoice.update();

            invoice.saveEvent(invoice.created, ProductEventType.INVOICE_CREATED, "{status: " + invoice.status + "}");

            return ok();
        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }
}