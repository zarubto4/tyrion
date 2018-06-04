package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import models.Model_GSM;
import models.Model_HardwareBatch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;
import org.w3c.dom.Element;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.gsm_services.things_mobile.Controller_Things_Mobile;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_List_list;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_Status;
import utilities.gsm_services.things_mobile.help_class.TM_Sim_Status_cdr;
import utilities.gsm_services.things_mobile.statistic_class.DataSim_overview;
import utilities.logger.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_ZZZ_Tester extends Controller {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(Controller_ZZZ_Tester.class);

// CONTROLLER CONFIGURATION ############################################################################################

    // Nothing
    private Config config;

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
    public Result test2(UUID sim_id) {
        try {
            // nalezení sim
            Model_GSM gsm = Model_GSM.getById(sim_id);

            // ověření jestli existuje
            if (gsm == null) {
                return notFound("sim wasn't found");
            }
            DataSim_overview overview = gsm.louskani();

           return ok( Json.toJson(overview) );
        } catch (Exception e) {
            logger.internalServerError(e);
            return badRequest();
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test3() {
        try {

            return ok();
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(e.toString());
        }
    }

    @ApiOperation(value = "Hidden test Method", hidden = true)
    public Result test4() {
        try {



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
                query.put("date", new BasicDBObject("$gt", 1527580800000L).append("$lt", 1527595200000L));
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