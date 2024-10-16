package controllers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.*;
import mongo.ModelMongo_Hardware_BatchCollection;
import mongo.ModelMongo_Hardware_RegistrationEntity;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.libs.ws.WSClient;
import play.mvc.Result;
import utilities.Server;
import utilities.enums.Currency;
import utilities.enums.*;
import utilities.financial.extensions.ExtensionInvoiceItem;
import utilities.financial.fakturoid.FakturoidService;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.gsm_services.things_mobile.help_json_class.TM_Sim_Status;
import utilities.logger.Logger;
import utilities.model.EchoService;
import utilities.notifications.NotificationService;
import utilities.permission.PermissionService;
import utilities.scheduler.jobs.Job_ThingsMobile_SimData_Synchronize;
import websocket.interfaces.Compiler;
import websocket.WebSocketService;
import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Id;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ZZZ_Tester extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_ZZZ_Tester.class);

// CONTROLLER CONFIGURATION ############################################################################################

    protected final WebSocketService webSocketService;

    @Inject
    public Controller_ZZZ_Tester(WSClient ws, _BaseFormFactory formFactory, Config config, PermissionService permissionService,
                                 NotificationService notificationService, WebSocketService webSocketService, EchoService echoService) {
        super(ws, formFactory, config, permissionService, notificationService, echoService);
        this.webSocketService = webSocketService;
    }

    @Inject
    private FakturoidService fakturoid;

// CONTROLLER CONTENT ##################################################################################################

    @Entity("QueryResultStats")
    public static class QueryResultStats {

        @Id
        public Long msisdn;
        public Long consumption_total;
        public List<Record> avarage_per_hour;

    }

    @Entity("Record")
    public static class Record {

        public Long consumption_total;
        public Date from;
        public Date to;

    }


    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test1() {
        try {



            Integer c = Model_Widget.find.query().where().eq("id", UUID.fromString("00000000-0000-0000-0000-000000000001")).findCount();


            System.out.println("Počet Widgetů? " + c);

            /*
            // this.webSocketService.create(Compiler.class, UUID.randomUUID(), "wss://echo.websocket.org");
            System.out.println("Blob downloaded:: size ");
            // Migrační script

            // Stažení souboru v separátním vláknu - jinak HTTP stále dokola zkoušelo vyžádat si odpověď od serveru po 30 sekundách
            new Thread(() -> {
                Integer size = Model_Blob.find.query().where().eq("storage_type", "AzureBlob").setIncludeSoftDeletes().findCount();
                List<Model_Blob> blobs = Model_Blob.find.query().where().eq("storage_type", "AzureBlob").setIncludeSoftDeletes().findList();

                for (int i = 0; i < blobs.size(); i++) {

                    Model_Blob blob = blobs.get(i);

                    //  Model_Blob blob = Model_Blob.find.byId(UUID.fromString("dcb819aa-da57-4db9-89d0-12532986c13b"));
                    try {

                        System.out.println("Blob downloaded:: size " + blob.downloadString().length() + ". Actual file:: " + (i + 1) + "/" + size);

                        // To Bytes
                        byte[] byteimage = blob.downloadString().getBytes();

                        InputStream is = new ByteArrayInputStream(byteimage);
                        ObjectMetadata om = new ObjectMetadata();
                        om.setContentLength(byteimage.length);
                        om.setContentType("application/octet-stream");


                        System.out.println("File Path starého souboru k uložení:: " + blob.path);

                        // Korekční filtr
                        blob.path = blob.path.replaceAll("bootloader.bin/bootloader.bin", "bootloader.bin");


                        System.out.println("File Path starého souboru po korektuře:: " + blob.path);
                        System.out.println("File link:: " + blob.link);
                        System.out.println("File Name:: " + blob.name);
                        System.out.println("Zdroj souboru:: " + blob.storage_type);

                        Server.space.putObject(Server.bucket_name, blob.path, is, om);
                        Server.space.setObjectAcl(Server.bucket_name, blob.path, CannedAccessControlList.PublicRead);
                        System.out.println("Link ke stažení souboru: " + Server.space.getUrl(Server.bucket_name, blob.path).toString());


                        blob.storage_type = "AWS_DigitalOcean";
                        blob.link = Server.space.getUrl(Server.bucket_name, blob.path).toString();

                        blob.update();

                    } catch (Exception e) {
                        System.out.println("Nepovedlo se to :(( u  " + blob.id);

                        blob.storage_type = "Broken";
                        blob.update();
                    }

                }
            }).start();

            */
            return ok();

        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test2() {
        try {

            this.webSocketService.test();

            return ok();

        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3() {
        try {

            // TM_Sim_Status status = Controller_Things_Mobile.sim_status(882360002156971L);
            // System.out.print("Sim Status:: \n" + status.prettyPrint());

            // 7db0ab84-ae29-4c2d-9792-10fa0155b1aa

           // Model_InstanceSnapshot snapshot = Model_InstanceSnapshot.find.byId("7db0ab84-ae29-4c2d-9792-10fa0155b1aa");
           // System.out.println("cloud_file_get_b_program_version: snapshot found with link: " +  snapshot.name);
           // System.out.println("cloud_file_get_b_program_version: File Name: " +  snapshot.getBlob().name);
           // System.out.println("cloud_file_get_b_program_version: link?" +  snapshot.getBlob().link);

            // return redirect(snapshot.getBlob().link);



            Model_Blob blob = Model_Blob.find.query().where().eq("hardware.id", UUID.fromString("e7311716-c638-4555-afc2-0c7ba7993301")).findOne();

            return redirect(blob.link);

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


          //  return ok();
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


            String filename = "NewExcelFile.xls";

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

            for (int i = 0; i < columns_id.length; i++) {

                if (i == 0) sheet.createRow(0);

                sheet.getRow(0).createCell(i);

                System.out.println("SEt to Row 0 cell " + i + " value " + columns_id[i]);
                sheet.getRow(0).getCell(i).setCellValue(columns_id[i]);
            }

            for (int i = 0; i < columns.length; i++) {

                BasicDBObject query = new BasicDBObject();
                query.put("device_id", columns[i]);
                query.put("date", new BasicDBObject("$gt", date_from.getMillis()).append("$lt", date_to.getMillis()));
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

                        if (!map.containsKey(dt1.format(new Date(Long.decode(s))))) {
                            map.put(dt1.format(new Date(Long.decode(s))), new String[10]);
                        }

                        map.get(dt1.format(new Date(Long.decode(s))))[i] = d.get("temperature").toString().replace(".", ",");

                    } catch (Exception e) {

                    }
                }
            }


            int i = 1;

            SortedSet<String> keys = new TreeSet<>(map.keySet());

            for (String key : keys) {

                sheet.createRow(i);

                sheet.getRow(i).createCell(0).setCellValue(key);

                for (int j = 0; j < map.get(key).length; j++) {
                    sheet.getRow(i).createCell(j + 1).setCellValue(map.get(key)[j]);
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
            if (product == null) {
                badRequest("Wrong product id!");
            }

            if (product.owner.contact == null) {
                badRequest("We need contact id!");
            }

            if (product.getExtensionIds().size() == 0) {
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
            for (Model_ProductEvent event : eventsExtension) {
                if (event.event_type == ProductEventType.EXTENSION_CREATED) {
                    event.created = projectCreated;
                    event.update();
                }

                if (event.event_type == ProductEventType.EXTENSION_ACTIVATED) {
                    event.created = Date.from(projectCreated.toInstant().plusMillis(100));
                    event.update();
                }
            }

            List<Model_ProductEvent> eventsProduct = Model_ProductEvent.find.query().where()
                    .eq("product.id", product.id)
                    .and()
                    .eq("reference", null)
                    .findList();
            for (Model_ProductEvent event : eventsProduct) {
                if (event.event_type == ProductEventType.PRODUCT_CREATED) {
                    event.created = Date.from(projectCreated.toInstant().minusMillis(100));
                    event.update();
                }
            }

            Instant productCreatedFirstMidnight = LocalDateTime.ofInstant(projectCreated.toInstant(), ZoneId.of("UTC")).toLocalDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant from = projectCreated.toInstant();
            Instant to = productCreatedFirstMidnight;
            for (int i = 0; i < 20; i++) {
                Model_ExtensionFinancialEvent financialEvent = new Model_ExtensionFinancialEvent();
                financialEvent.product_extension = productExtension;
                financialEvent.event_start = Date.from(from);
                financialEvent.event_end = Date.from(to);
                financialEvent.consumption = "{\"minutes\": " + (((to.toEpochMilli() - from.toEpochMilli()) / 60000)) + "}";
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
            if (product == null) {
                badRequest("Wrong product id!");
            }

            if (product.owner.contact == null) {
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

            invoice.status = InvoiceStatus.UNCONFIRMED;
            invoice.update();

            invoice.saveEvent(invoice.created, ProductEventType.INVOICE_CREATED, "{status: " + invoice.status + "}");

            return ok();
        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test7() {
        try {

            // ZÍSKAT aktivní SIM
            //TM_Sim_Status status = Controller_Things_Mobile.sim_status(882360002156971L);
            // System.out.print("Credit:: \n"  + status.prettyPrint());


            // System.out.print("Vyvolám synchronizaci Sim karet ");
            // Odstartovat synchronizaci
            // new Job_ThingsMobile_SimListOnly_Synchronize().execute(null);

            // sleep(5000);

            // System.out.print("Stahování statistik");
            new Job_ThingsMobile_SimData_Synchronize().execute(null);

            return ok();

        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }


    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test8() {
        try {


            // new Job_ThingsMobile_SimData_Synchronize().execute(null);

/*
            BasicDBObject query = new BasicDBObject();
            query.put("deleted", false);

            MongoCursor<Document> cursor =  MongoDB.get_collection("batch-registration-authority").find(query).iterator();

            List<Model_HardwareBatch> batches = new ArrayList<>();
            while (cursor.hasNext()) {

                String string_json = cursor.next().toJson();
                ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);
                Model_HardwareBatch batch = formFactory.formFromJsonWithValidation(Model_HardwareBatch.class, json);
                batches.add(batch);
            }


            System.out.println("Kolik máme batches: " + batches.size());


            for(Model_HardwareBatch help : batches) {

                ModelMongo_Hardware_BatchCollection batch = new ModelMongo_Hardware_BatchCollection();
                batch.revision = help.revision;
                batch.production_batch = help.production_batch;


                DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
                Date date = format.parse(help.date_of_assembly);

                batch.date_of_assembly = date.getTime();
                batch.pcb_manufacture_name = help.pcb_manufacture_name;
                batch.pcb_manufacture_id = help.pcb_manufacture_id;
                batch.assembly_manufacture_name = help.assembly_manufacture_name;
                batch.assembly_manufacture_id = help.assembly_manufacture_id;
                batch.customer_product_name = help.customer_product_name;
                batch.customer_company_name = help.customer_company_name;
                batch.customer_company_made_description = help.customer_company_made_description;
                batch.mac_address_start = help.mac_address_start;
                batch.mac_address_end = help.mac_address_end;
                batch.latest_used_mac_address = help.latest_used_mac_address;
                batch.ean_number = help.ean_number;
                batch.description = help.description;
                batch.compiler_target_name = help.compiler_target_name;

                // Uložení objektu do DB
                batch.save();

            }


            // 3. cc6b3643-652a-40c5-88ee-04cff043afa5    "First production collection"     5bd5dd5423548a6f3082b428
            // 2. 26d189c5-b61f-4565-a8f7-5a043a73963e    "21- Test Collection"             5bd5dd5423548a6f3082b427
            // 1. abd218dc-14ca-4d2e-a731-66f71ed41245    "01 production"                   5bd5dd5423548a6f3082b426


            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://production-byzance-cosmos:PbimpRkWXhUrGBwRtLaR19B6NbffCgzklSfSVtHThFzMn6keUENJ9Hm50TZZgtqVOGesgbtCWLaC3yd6ENhoew==@production-byzance-cosmos.documents.azure.com:10255/?ssl=true&replicaSet=globaldb"));

            // Připojení na konkrétní Databázi clienta
            MongoDatabase database_hardware_registration = mongoClient.getDatabase("hardware-registration-authority-database");

            BasicDBObject query_entity = new BasicDBObject();
            MongoCursor<Document> cursor_entity = database_hardware_registration.getCollection("hardware-registration-authority").find(query_entity).iterator();


            List<Model_HardwareRegistrationEntity> entities = new ArrayList<>();
            while (cursor_entity.hasNext()) {

                String string_json = cursor_entity.next().toJson();
                ObjectNode json = (ObjectNode) new ObjectMapper().readTree(string_json);
                Model_HardwareRegistrationEntity batch = formFactory.formFromJsonWithValidation(Model_HardwareRegistrationEntity.class, json);
                entities.add(batch);
            }


            System.out.println("Kolik máme entities: " + entities.size());


            for(Model_HardwareRegistrationEntity help : entities) {

                ModelMongo_Hardware_RegistrationEntity registration_of_hardware = new ModelMongo_Hardware_RegistrationEntity();

                if (help.production_batch_id.equals("cc6b3643-652a-40c5-88ee-04cff043afa5")  ) {
                    registration_of_hardware.production_batch_id = new ObjectId("5bd5dd5423548a6f3082b428");
                }
                
                else if(help.production_batch_id.equals("26d189c5-b61f-4565-a8f7-5a043a73963e")  ) {
                    registration_of_hardware.production_batch_id = new ObjectId("5bd5dd5423548a6f3082b427");
                }
                else if(help.production_batch_id.equals("abd218dc-14ca-4d2e-a731-66f71ed41245")  ) {
                    registration_of_hardware.production_batch_id = new ObjectId("5bd5dd5423548a6f3082b426");
                }


                if(ModelMongo_Hardware_RegistrationEntity.getbyFull_id(help.full_id) != null) continue;

                registration_of_hardware.mac_address = help.mac_address;
                registration_of_hardware.full_id = help.full_id;

                registration_of_hardware.hash_for_adding = help.hash_for_adding;
                registration_of_hardware.personal_name = help.personal_name;

                registration_of_hardware.hardware_type_compiler_target_name =  help.hardware_type_compiler_target_name;
                registration_of_hardware.created = Long.parseLong(help.created);
                registration_of_hardware.mqtt_username = help.mqtt_username;
                registration_of_hardware.mqtt_password = help.mqtt_password;
                registration_of_hardware.save();

            }

*/

            return ok();

        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

}