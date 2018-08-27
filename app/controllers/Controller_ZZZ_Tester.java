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
import models.Model_GSM;
import models.Model_HardwareRegistrationEntity;
import models.Model_Person;
import org.apache.poi.ss.usermodel.*;
import org.bson.Document;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import play.Environment;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;

import utilities.gsm_services.things_mobile.statistic_class.DataSim_overview;
import utilities.logger.Logger;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utilities.logger.YouTrack;
import utilities.scheduler.SchedulerController;
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

// CONTROLLER CONTENT ##################################################################################################
    @ApiOperation(value = "Hidden test Method", hidden = true)
     public Result test1() {
         try {



             return ok("Valid version for mode");
         } catch (Exception e) {
             logger.internalServerError(e);
             return badRequest();
         }
     }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test2() {
        try {

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

           return ok();
        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3() {
        try {


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




}