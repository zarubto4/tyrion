package utilities.demo_data;

import io.swagger.annotations.Api;
import models.*;
import play.Application;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.*;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_Admin;
import utilities.response.GlobalResult;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Api(value = "Dashboard Private Api", hidden = true)
@Security.Authenticated(Secured_Admin.class)
public class Utilities_Demo_data_Controller extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Utilities_Demo_data_Controller.class);

    @Inject
    Application application;

    public Result test() {
        try {

            System.out.println("Demo_Data_Controller :: test :: start");

            return  ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    /*
        Slouží primárně k vytváření demo dat při vývoji - je nutné pamatovat že změnou struktury objektů
        se nemění struktura vytvářených dat. Což je poměrně pracné na opravu při velkých úpravách.

        Každopádně každý vývojář by měl do struktury doplnit demo data své práce aby se vždy dalo od ní odpíchnout dál.

        Demodata je povolené tvořit jen a pouze v "Developer modu"!!!!!!!!!!
     */
    public Result all_for_becki() {

        Result result = this.producers();
        if (result.status() != 200) return result;

        result = this.type_of_board();
        if (result.status() != 200) return result;

        result = this.test_boards();
        if (result.status() != 200) return result;

        result = this.extendension_servers();
        if (result.status() != 200) return result;

        result = this.basic_tariffs();
        if (result.status() != 200) return result;

        result = this.person_test_user();
        if (result.status() != 200) return result;

        return result;
    }

    public Result producers() {
        try {

            // Ochranná zarážka proti znovu vytvoření
            if (Model_Producer.find.where().eq("name", "Byzance ltd").findUnique() != null)
                return GlobalResult.result_badRequest("Its Already done!");

            // Nastavím Producer
            Model_Producer producer = new Model_Producer();
            producer.name = "Byzance ltd";
            producer.description = "Developed with love from Byzance";
            producer.save();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result type_of_board() {
        try {


            Model_TypeOfBoardFeatures features_i2c = new Model_TypeOfBoardFeatures();
            features_i2c.name = "i2c";
            features_i2c.save();

            Model_TypeOfBoardFeatures wifi = new Model_TypeOfBoardFeatures();
            wifi.name = "wifi";
            wifi.save();

            Model_TypeOfBoardFeatures ethernet = new Model_TypeOfBoardFeatures();
            ethernet.name = "ethernet";
            ethernet.save();

            Model_TypeOfBoardFeatures bus = new Model_TypeOfBoardFeatures();
            bus.name = "bus";
            bus.save();

            Model_TypeOfBoardFeatures wireless = new Model_TypeOfBoardFeatures();
            wireless.name = "wireless";
            wireless.save();


            // Ochranná zarážka proti znovu vytvoření
            Model_Producer producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            if (producer == null) return GlobalResult.result_badRequest("Create Producer first");
            if (Model_Processor.find.where().eq("processor_name", "ARM STM32 FR17").findUnique() != null)
                return GlobalResult.result_badRequest("Its Already done!");

            // Nastavím Processor - YODA
            Model_Processor processor_1 = new Model_Processor();
            processor_1.processor_name = "ARM STM32 FR17";
            processor_1.description = "VET6 HPABT VQ KOR HP501";
            processor_1.processor_code = "STM32FR17";
            processor_1.speed = 3000;
            processor_1.save();

            // Nastavím Processor - DRÁT / BEZDRÁT
            Model_Processor processor_2 = new Model_Processor();
            processor_2.processor_name = "ARM STM32F";
            processor_2.description = "030CCT6 GH26J 93 CHN 611";
            processor_2.processor_code = "STM32F";
            processor_2.speed = 3000;
            processor_2.save();

            // Nastavím Processor - Rozbočovat
            Model_Processor processor_3 = new Model_Processor();
            processor_3.processor_name = "ARM STM32F070";
            processor_3.description = "RBT6 GH25T 98 CHN GH 532";
            processor_3.processor_code = "STM32F070";
            processor_3.speed = 3000;
            processor_3.save();

            File file = new File(System.getProperty("user.dir") + "/conf/demo_data/yoda.png");


            // Nastavím Type of Boards - YODA
            Model_TypeOfBoard typeOfBoard_1 = new Model_TypeOfBoard();
            typeOfBoard_1.name = "Yoda G2";
            typeOfBoard_1.description = " Yoda - Master Board with Ethernet and Wifi - second generation";
            typeOfBoard_1.compiler_target_name = "BYZANCE_YODAG2";
            typeOfBoard_1.revision = "12/2015 V1.0 #0000";
            typeOfBoard_1.processor = processor_1;
            typeOfBoard_1.producer = producer;
            typeOfBoard_1.connectible_to_internet = true;
            typeOfBoard_1.features.add(ethernet);
            typeOfBoard_1.features.add(wifi);
            typeOfBoard_1.save();


            typeOfBoard_1.azure_picture_link = typeOfBoard_1.get_Container().getName() + "/" + UUID.randomUUID().toString() + ".png";
            typeOfBoard_1.update();

            typeOfBoard_1.picture = Model_FileRecord.uploadAzure_File(file,  typeOfBoard_1.azure_picture_link.substring( typeOfBoard_1.azure_picture_link.indexOf("/") +1) , typeOfBoard_1.azure_picture_link);
            typeOfBoard_1.update();



            Model_TypeOfBoard typeOfBoard_2 = new Model_TypeOfBoard();
            typeOfBoard_2.name = "Wireless G2";
            typeOfBoard_2.description = " Wireless kit second generation";
            typeOfBoard_2.compiler_target_name = "BYZANCE_WRLSKITG2";
            typeOfBoard_2.revision = "06/2016 V2.0 #0000";
            typeOfBoard_2.processor = processor_2;
            typeOfBoard_2.producer = producer;
            typeOfBoard_2.connectible_to_internet = false;
            typeOfBoard_2.features.add(wireless);
            typeOfBoard_2.save();

            typeOfBoard_2.azure_picture_link = typeOfBoard_2.get_Container().getName() + "/" + UUID.randomUUID().toString() + ".png";
            typeOfBoard_2.update();

            typeOfBoard_2.picture = Model_FileRecord.uploadAzure_File(file,  typeOfBoard_2.azure_picture_link.substring( typeOfBoard_2.azure_picture_link.indexOf("/") +1) , typeOfBoard_2.azure_picture_link);
            typeOfBoard_2.update();


            Model_TypeOfBoard typeOfBoard_3 = new Model_TypeOfBoard();
            typeOfBoard_3.name = "BUS G2";
            typeOfBoard_3.description = " BUS kit second generation";
            typeOfBoard_3.compiler_target_name = "BYZANCE_BUSKITG2";
            typeOfBoard_3.revision = "02/2016 V2.0 #0000";
            typeOfBoard_3.processor = processor_2;
            typeOfBoard_3.producer = producer;
            typeOfBoard_3.connectible_to_internet = false;
            typeOfBoard_3.features.add(bus);
            typeOfBoard_3.save();

            typeOfBoard_3.azure_picture_link = typeOfBoard_3.get_Container().getName() + "/" + UUID.randomUUID().toString() + ".png";
            typeOfBoard_3.update();

            typeOfBoard_3.picture = Model_FileRecord.uploadAzure_File(file,  typeOfBoard_3.azure_picture_link.substring( typeOfBoard_3.azure_picture_link.indexOf("/") +1) , typeOfBoard_3.azure_picture_link);
            typeOfBoard_3.update();


            Model_TypeOfBoard typeOfBoard_4 = new Model_TypeOfBoard();
            typeOfBoard_4.name = "Quad BUS HUB G1";
            typeOfBoard_4.description = " BUS kit second generation";
            typeOfBoard_4.compiler_target_name = "BYZANCE_QUADBUSG1";
            typeOfBoard_4.revision = "12/2015 V1.0 #0000";
            typeOfBoard_4.processor = processor_3;
            typeOfBoard_4.producer = producer;
            typeOfBoard_4.connectible_to_internet = false;
            typeOfBoard_4.features.add(bus);
            typeOfBoard_4.save();

            typeOfBoard_4.azure_picture_link = typeOfBoard_4.get_Container().getName() + "/" + UUID.randomUUID().toString() + ".png";
            typeOfBoard_4.update();

            typeOfBoard_4.picture = Model_FileRecord.uploadAzure_File(file,  typeOfBoard_4.azure_picture_link.substring( typeOfBoard_4.azure_picture_link.indexOf("/") +1) , typeOfBoard_4.azure_picture_link);
            typeOfBoard_4.update();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    // Pair objekt určen pro tvorbu demo dat (registrace Deviců)
    class Pair {
        public String id;
        public String name;

        public Pair(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public Result test_boards() {
        try {
            // Ochranná zarážka proti znovu vytvoření
            Model_TypeOfBoard yoda_type = Model_TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_YODAG2").findUnique();
            if (yoda_type == null) return GlobalResult.result_badRequest("Create Type of Boards first");

            Model_TypeOfBoard wireles_type = Model_TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_WRLSKITG2").findUnique();
            Model_TypeOfBoard buskit_type = Model_TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_BUSKITG2").findUnique();

            if (Model_Board.find.where().eq("id", "005300393533510B34353732").findUnique() != null)
                return GlobalResult.result_badRequest("Its Already done!");

            // YODA!!!!!
            List<Pair> yodas = new ArrayList<>();
            yodas.add(new Pair("005300393533510B34353732", "[F]"));
            yodas.add(new Pair("004000393533510B34353732", "[G]"));
            yodas.add(new Pair("004500393533510B34353732", "[H]"));
            yodas.add(new Pair("0022003A3533510B34353732", "[I]"));
            yodas.add(new Pair("003C00393533510B34353732", "[J]"));
            yodas.add(new Pair("0025003A3533510B34353732", "[K]"));
            yodas.add(new Pair("004300393533510B34353732", "[L]"));
            yodas.add(new Pair("001D00453533510B34353732", "[M]"));
            yodas.add(new Pair("0021003A3533510B34353732", "[N]"));
            yodas.add(new Pair("002000453533510B34353732", "[O]"));
            yodas.add(new Pair("0024003A3533510B34353732", "[P]"));
            yodas.add(new Pair("001B00453533510B34353732", "[Q]"));
            yodas.add(new Pair("002300513533510B34353732", "[R]"));
            yodas.add(new Pair("003B00313533510B34353732", "[S]"));
            yodas.add(new Pair("002300453533510B34353732", "[T]"));

            for (Pair yoda_pair : yodas) {
                Model_Board yoda = new Model_Board();
                yoda.id = yoda_pair.id;
                yoda.name = yoda_pair.name;
                yoda.type_of_board = yoda_type;
                yoda.date_of_create = new Date();
                yoda.description = "G2.0";
                yoda.save();
            }


            List<Pair> WrlsKitG2 = new ArrayList<>();
            WrlsKitG2.add(new Pair("001C00074247430D20363439", "[6]"));
            WrlsKitG2.add(new Pair("001200254247430E20363439", "[7]"));
            WrlsKitG2.add(new Pair("001C000A4247430D20363439", "[10]"));
            WrlsKitG2.add(new Pair("001200244247430E20363439", "[11]"));
            WrlsKitG2.add(new Pair("001200264247430E20363439", "[12]"));
            WrlsKitG2.add(new Pair("001C00094247430D20363439", "[13]"));
            WrlsKitG2.add(new Pair("001C00144247430D20363439", "[14]"));
            WrlsKitG2.add(new Pair("000D00105748430E20303039", "[20]"));
            WrlsKitG2.add(new Pair("0030000C5748430E20303039", "[21]"));
            WrlsKitG2.add(new Pair("003000155748430E20303039", "[22]"));
            WrlsKitG2.add(new Pair("000C800C5748430E20303039", "[23]"));
            WrlsKitG2.add(new Pair("002A80045748430E20303039", "[24]"));
            WrlsKitG2.add(new Pair("000D000E5748430E20303039", "[25]"));
            WrlsKitG2.add(new Pair("003000115748430E20303039", "[26]"));
            WrlsKitG2.add(new Pair("0030000E5748430E20303039", "[27]"));
            WrlsKitG2.add(new Pair("002E00155748430E20303039", "[28]"));
            WrlsKitG2.add(new Pair("000D00085748430E20303039", "[29]"));
            WrlsKitG2.add(new Pair("001300254247430E20363439", "[30]"));
            WrlsKitG2.add(new Pair("002A80065748430E20303039", "[31]"));
            WrlsKitG2.add(new Pair("001300264247430E20363439", "[32]"));
            WrlsKitG2.add(new Pair("002D00175748430E20303039", "[33]"));
            WrlsKitG2.add(new Pair("002D000F5748430E20303039", "[34]"));
            WrlsKitG2.add(new Pair("002D00145748430E20303039", "[35]"));
            WrlsKitG2.add(new Pair("0012001D4247430E20363439", "[36]"));
            WrlsKitG2.add(new Pair("003000145748430E20303039", "[37]"));
            WrlsKitG2.add(new Pair("002F00095748430E20303039", "[38]"));
            WrlsKitG2.add(new Pair("0030000F5748430E20303039", "[39]"));
            WrlsKitG2.add(new Pair("002D00155748430E20303039", "[40]"));
            WrlsKitG2.add(new Pair("000D800D5748430E20303039", "[41]"));
            WrlsKitG2.add(new Pair("003000135748430E20303039", "[42]"));
            WrlsKitG2.add(new Pair("002D00185748430E20303039", "[43]"));
            WrlsKitG2.add(new Pair("002F00075748430E20303039", "[44]"));
            WrlsKitG2.add(new Pair("002F00155748430E20303039", "[45]"));
            WrlsKitG2.add(new Pair("001C000E4247430D20363439", "[46]"));

            for (Pair WrlsKitG2_pair : WrlsKitG2) {
                Model_Board wrls = new Model_Board();
                wrls.id = WrlsKitG2_pair.id;
                wrls.name = WrlsKitG2_pair.name;
                wrls.type_of_board = wireles_type;
                wrls.date_of_create = new Date();
                wrls.description = "G2.0";
                wrls.save();
            }


            List<Pair> BusKitG2 = new ArrayList<>();
            BusKitG2.add(new Pair("001C00054247430D20363439", "[1]"));
            BusKitG2.add(new Pair("001300274247430E20363439", "[2]"));
            BusKitG2.add(new Pair("001C00064247430D20363439", "[3]"));
            BusKitG2.add(new Pair("001200224247430E20363439", "[4]"));
            BusKitG2.add(new Pair("001300244247430E20363439", "[5]"));
            BusKitG2.add(new Pair("001C00104247430D20363439", "[8]"));
            BusKitG2.add(new Pair("001C000C4247430D20363439", "[9]"));
            BusKitG2.add(new Pair("001200234247430E20363439", "[15]"));
            BusKitG2.add(new Pair("001300214247430E20363439", "[16]"));
            BusKitG2.add(new Pair("001C00084247430D20363439", "[50]"));
            BusKitG2.add(new Pair("003000125748430E20303039", "[51]"));
            BusKitG2.add(new Pair("002F00175748430E20303039", "[52]"));
            BusKitG2.add(new Pair("0030000B5748430E20303039", "[53]"));
            BusKitG2.add(new Pair("000C800B5748430E20303039", "[54]"));
            BusKitG2.add(new Pair("002B80045748430E20303039", "[55]"));
            BusKitG2.add(new Pair("002F000A5748430E20303039", "[56]"));
            BusKitG2.add(new Pair("002F00165748430E20303039", "[57]"));
            BusKitG2.add(new Pair("000D80075748430E20303039", "[58]"));
            BusKitG2.add(new Pair("002D00165748430E20303039", "[59]"));
            BusKitG2.add(new Pair("001200204247430E20363439", "[60]"));
            BusKitG2.add(new Pair("002F00035748430E20303039", "[61]"));
            BusKitG2.add(new Pair("003000105748430E20303039", "[62]"));
            BusKitG2.add(new Pair("0030000D5748430E20303039", "[63]"));
            BusKitG2.add(new Pair("001C000D4247430D20363439", "[64]"));
            BusKitG2.add(new Pair("003000165748430E20303039", "[65]"));
            BusKitG2.add(new Pair("002D001B5748430E20303039", "[66]"));
            BusKitG2.add(new Pair("002B80065748430E20303039", "[67]"));
            BusKitG2.add(new Pair("002F00065748430E20303039", "[68]"));
            BusKitG2.add(new Pair("002F00185748430E20303039", "[69]"));
            BusKitG2.add(new Pair("002F00185748430E20303039-0", "[70]")); // OPRAVIT
            BusKitG2.add(new Pair("002F00185748430E20303039-1", "[71]")); // OPRAVIT
            BusKitG2.add(new Pair("002F00185748430E20303039-2", "[72]")); // OPRAVIT
            BusKitG2.add(new Pair("001C000F4247430D20363439", "[73]"));
            BusKitG2.add(new Pair("002A80075748430E20303039", "[74]"));
            BusKitG2.add(new Pair("002F00015748430E20303039", "[75]"));
            BusKitG2.add(new Pair("002D00115748430E20303039", "[76]"));

            for (Pair bskpair : BusKitG2) {
                Model_Board bsk = new Model_Board();
                bsk.id = bskpair.id;
                bsk.name = bskpair.name;
                bsk.type_of_board = buskit_type;
                bsk.date_of_create = new Date();
                bsk.description = "G2.0";
                bsk.save();
            }


            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result extendension_servers() {
        try {

            // Ochranná zarážka proti znovu vytvoření
            if (Model_HomerServer.find.where().eq("personal_server_name", "Alfa").findUnique() != null)
                return GlobalResult.result_badRequest("Its Already done!");

            // Nasstavím Homer servery
            Model_HomerServer cloud_server_1 = new Model_HomerServer();
            cloud_server_1.personal_server_name = "Alfa";
            cloud_server_1.server_url = "localhost3";
            cloud_server_1.grid_port = 8500;
            cloud_server_1.mqtt_port = 1881;
            cloud_server_1.mqtt_password = "pass";
            cloud_server_1.mqtt_username = "user";
            cloud_server_1.web_view_port = 8501;
            cloud_server_1.server_type = Enum_Cloud_HomerServer_type.main_server;
            cloud_server_1.save();

            Model_HomerServer cloud_server_2 = new Model_HomerServer();
            cloud_server_2.personal_server_name = "Hydra";
            cloud_server_2.server_url = "localhost3";
            cloud_server_2.grid_port = 8500;
            cloud_server_2.mqtt_port = 1881;
            cloud_server_2.mqtt_password = "pass";
            cloud_server_2.mqtt_username = "user";
            cloud_server_2.web_view_port = 8501;
            cloud_server_2.server_type = Enum_Cloud_HomerServer_type.backup_server;
            cloud_server_2.save();


            Model_HomerServer cloud_server_3 = new Model_HomerServer();
            cloud_server_3.personal_server_name = "Andromeda";
            cloud_server_3.server_url = "localhost3";
            cloud_server_3.grid_port = 8500;
            cloud_server_3.mqtt_port = 1881;
            cloud_server_3.mqtt_password = "pass";
            cloud_server_2.mqtt_username = "user";
            cloud_server_3.web_view_port = 8501;
            cloud_server_3.server_type = Enum_Cloud_HomerServer_type.public_server;
            cloud_server_3.save();

            Model_HomerServer cloud_server_4 = new Model_HomerServer();
            cloud_server_4.personal_server_name = "Gemini";
            cloud_server_4.server_url = "localhost4";
            cloud_server_4.grid_port = 8500;
            cloud_server_4.mqtt_port = 1881;
            cloud_server_4.mqtt_password = "pass";
            cloud_server_4.mqtt_username = "user";
            cloud_server_4.web_view_port = 8501;
            cloud_server_4.server_type = Enum_Cloud_HomerServer_type.public_server;
            cloud_server_4.save();

            // Testovací server
            Model_HomerServer cloud_server_5 = new Model_HomerServer();
            cloud_server_5.unique_identificator = "aaaaaaaaaaaaaaa";
            cloud_server_5.hash_certificate = "bbbbbbbbbbbbbbb";
            cloud_server_5.personal_server_name = "Developer-Demo";
            cloud_server_5.server_url = "localhost";
            cloud_server_5.grid_port = 8500;
            cloud_server_5.mqtt_port = 1881;
            cloud_server_5.mqtt_password = "pass";
            cloud_server_5.mqtt_username = "User";
            cloud_server_5.web_view_port = 8501;
            cloud_server_5.server_type = Enum_Cloud_HomerServer_type.test_server;
            cloud_server_5.save();

            // Nastavím kompilační servery
            Model_CompilationServer compilation_server_1 = new Model_CompilationServer();
            compilation_server_1.personal_server_name = "Perseus";
            compilation_server_1.save();

            Model_CompilationServer compilation_server_2 = new Model_CompilationServer();
            compilation_server_2.personal_server_name = "Pegas";
            compilation_server_2.save();

            Model_CompilationServer compilation_server_3 = new Model_CompilationServer();
            compilation_server_3.personal_server_name = "Test Server";
            compilation_server_3.unique_identificator = "test";
            compilation_server_3.hash_certificate = "testHash";
            compilation_server_3.save();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result basic_tariffs() {
        try {

            // Ochranná zarážka proti znovu vytvoření
            if (Model_Tariff.find.where().eq("name", "Alfa account").findUnique() != null)
                return GlobalResult.result_badRequest("Its Already done!");

            // Alfa
            Model_Tariff tariff_1 = new Model_Tariff();
            tariff_1.order_position = 1;
            tariff_1.active = true;
            tariff_1.name = "Alfa account";
            tariff_1.description = "Temporary account only for next 3 months";
            tariff_1.identifier = "alpha";

            tariff_1.color = "blue";
            tariff_1.credit_for_beginning = (long) 0;

            tariff_1.company_details_required = false;
            tariff_1.payment_details_required = false;

            tariff_1.credit_card_support = false;
            tariff_1.bank_transfer_support = false;

            tariff_1.save();
            tariff_1.refresh();

            Model_TariffLabel label_1 = new Model_TariffLabel();
            label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_1.icon = "fa-bullhorn";
            label_1.label = "Super koment bla bla bla";
            label_1.tariff = tariff_1;
            label_1.save();

            Model_TariffLabel label_2 = new Model_TariffLabel();
            label_2.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_2.icon = "fa-bullhorn";
            label_2.label = "Super koment 2";
            label_2.tariff = tariff_1;
            label_2.save();

            Model_TariffLabel label_3 = new Model_TariffLabel();
            label_3.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_3.icon = "fa-bullhorn";
            label_3.label = "Super koment 3 ";
            label_3.tariff = tariff_1;
            label_3.save();

            Model_TariffLabel label_4 = new Model_TariffLabel();
            label_4.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_4.icon = "fa-bullhorn";
            label_4.label = "Super koment 4";
            label_4.tariff = tariff_1;
            label_4.save();

            Model_ProductExtension extensions_1 = new Model_ProductExtension();
            extensions_1.name = "Extension 1";
            extensions_1.description = "description extension 1";
            extensions_1.type = Enum_ExtensionType.project;
            extensions_1.active = true;
            extensions_1.removed = false;
            extensions_1.color = "blue-madison";
            extensions_1.tariff_included = tariff_1;
            extensions_1.configuration = "{\"price\":1000,\"count\":1}";
            extensions_1.save();

            Model_ProductExtension extensions_2 = new Model_ProductExtension();
            extensions_2.name = "Extension 2";
            extensions_2.description = "description extension 2";
            extensions_2.type = Enum_ExtensionType.log;
            extensions_2.active = true;
            extensions_2.removed = false;
            extensions_2.color = "blue-chambray";
            extensions_2.tariff_optional = tariff_1;
            extensions_2.configuration = "{\"price\":400,\"count\":2}";
            extensions_2.save();

            // Pro geeky
            Model_Tariff geek_tariff = new Model_Tariff();
            tariff_1.order_position = 2;
            geek_tariff.active = true;
            geek_tariff.name = "For true Geeks";
            geek_tariff.description = "Temporary account only for next 3 months";
            geek_tariff.identifier = "geek";

            geek_tariff.color = "green-jungle";

            geek_tariff.credit_for_beginning = (long) 20000;

            geek_tariff.company_details_required = false;
            geek_tariff.payment_details_required = false;

            geek_tariff.credit_card_support = true;
            geek_tariff.bank_transfer_support = true;

            geek_tariff.save();
            geek_tariff.refresh();

            Model_TariffLabel label_5 = new Model_TariffLabel();
            label_5.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_5.icon = "fa-bullhorn";
            label_5.label = "Super koment bla bla bla";
            label_5.tariff = geek_tariff;
            label_5.save();

            Model_TariffLabel label_6 = new Model_TariffLabel();
            label_6.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_6.icon = "fa-bullhorn";
            label_6.label = "Super koment 2ln ljhljk ljk hllkjhlkj";
            label_6.tariff = geek_tariff;
            label_6.save();

            Model_TariffLabel label_7 = new Model_TariffLabel();
            label_7.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_7.icon = "fa-bullhorn";
            label_7.label = "Super koment 3 ";
            label_7.tariff = geek_tariff;
            label_7.save();

            Model_TariffLabel label_8 = new Model_TariffLabel();
            label_8.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_8.icon = "fa-bullhorn";
            label_8.label = "Super koment 4";
            label_8.tariff = geek_tariff;
            label_8.save();

            // Placená
            Model_Tariff business_tariff = new Model_Tariff();
            tariff_1.order_position = 3;
            business_tariff.active = true;
            business_tariff.name = "For true Business";
            business_tariff.description = "Best for true business";
            business_tariff.identifier = "business_1";

            business_tariff.color = "green-jungle";

            business_tariff.credit_for_beginning = (long) 100000;

            business_tariff.company_details_required = true;
            business_tariff.payment_details_required = true;

            business_tariff.credit_card_support = true;
            business_tariff.bank_transfer_support = true;

            business_tariff.save();
            business_tariff.refresh();

            Model_TariffLabel label_9 = new Model_TariffLabel();
            label_9.description = "Kredit zdarma Trololo!";
            label_9.icon = "fa-bullhorn";
            label_9.label = "Super koment bla bla bla";
            label_9.tariff = business_tariff;
            label_9.save();

            Model_TariffLabel label_10 = new Model_TariffLabel();
            label_10.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_10.icon = "fa-bullhorn";
            label_10.label = "Super koment 2ln ljhljk ljk hllkjhlkj";
            label_10.tariff = business_tariff;
            label_10.save();

            Model_TariffLabel label_11 = new Model_TariffLabel();
            label_11.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_11.icon = "fa-bullhorn";
            label_11.label = "Super koment 3 ";
            label_11.tariff = business_tariff;
            label_11.save();

            Model_TariffLabel label_12 = new Model_TariffLabel();
            label_12.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_12.icon = "fa-bullhorn";
            label_12.label = "Super koment 4";
            label_12.tariff = business_tariff;
            label_12.save();

            Model_TariffLabel label_13 = new Model_TariffLabel();
            label_13.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_13.icon = "fa-bullhorn";
            label_13.label = "Super koment 4";
            label_13.tariff = business_tariff;
            label_13.save();

            Model_TariffLabel label_14 = new Model_TariffLabel();
            label_14.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_14.icon = "fa-bullhorn";
            label_14.label = "Super koment 4";
            label_14.tariff = business_tariff;
            label_14.save();

            Model_TariffLabel label_15 = new Model_TariffLabel();
            label_15.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_15.icon = "fa-bullhorn";
            label_15.label = "Super koment 4";
            label_15.tariff = business_tariff;
            label_15.save();

            Model_ProductExtension business_tariff_extensions_4 = new Model_ProductExtension();
            business_tariff_extensions_4.name = "Extension Project";
            business_tariff_extensions_4.description = "description extension sadafdfv";
            business_tariff_extensions_4.type = Enum_ExtensionType.project;
            business_tariff_extensions_4.active = true;
            business_tariff_extensions_4.removed = false;
            business_tariff_extensions_4.color = "blue-chambray";
            business_tariff_extensions_4.tariff_included = business_tariff;
            business_tariff_extensions_4.configuration = "{\"price\":2600,\"count\":4}";
            business_tariff_extensions_4.save();

            Model_ProductExtension business_tariff_extensions_3 = new Model_ProductExtension();
            business_tariff_extensions_3.name = "Extension 1";
            business_tariff_extensions_3.description = "description extension djsdjs";
            business_tariff_extensions_3.type = Enum_ExtensionType.project;
            business_tariff_extensions_3.active = true;
            business_tariff_extensions_3.removed = false;
            business_tariff_extensions_3.color = "blue-chambray";
            business_tariff_extensions_3.tariff_included = business_tariff;
            business_tariff_extensions_3.configuration = "{\"price\":600,\"count\":5}";
            business_tariff_extensions_3.save();

            Model_ProductExtension business_tariff_extensions_2 = new Model_ProductExtension();
            business_tariff_extensions_2.name = "Extension sadas";
            business_tariff_extensions_2.description = "description extasdension 1";
            business_tariff_extensions_2.type = Enum_ExtensionType.project;
            business_tariff_extensions_2.active = true;
            business_tariff_extensions_2.removed = false;
            business_tariff_extensions_2.color = "blue-chambray";
            business_tariff_extensions_2.tariff_optional = business_tariff;
            business_tariff_extensions_2.configuration = "{\"price\":600,\"count\":5}";
            business_tariff_extensions_2.save();

            Model_ProductExtension business_tariff_extensions_1 = new Model_ProductExtension();
            business_tariff_extensions_1.name = "Extension sadasa";
            business_tariff_extensions_1.description = "description extensioasdan 1";
            business_tariff_extensions_1.type = Enum_ExtensionType.project;
            business_tariff_extensions_1.active = true;
            business_tariff_extensions_1.removed = false;
            business_tariff_extensions_1.color = "blue-chambray";
            business_tariff_extensions_1.tariff_optional = business_tariff;
            business_tariff_extensions_1.configuration = "{\"price\":600,\"count\":5}";
            business_tariff_extensions_1.save();

            // Další placený

            Model_Tariff business_tariff_2 = new Model_Tariff();
            tariff_1.order_position = 4;
            business_tariff_2.active = true;
            business_tariff_2.name = "Enterprise";
            business_tariff_2.description = "You know what you need!";
            business_tariff_2.identifier = "business_2";

            business_tariff_2.color = "green-sharp";

            business_tariff_2.credit_for_beginning = (long) 500000;

            business_tariff_2.company_details_required = true;
            business_tariff_2.payment_details_required = true;

            business_tariff_2.credit_card_support = true;
            business_tariff_2.bank_transfer_support = true;

            business_tariff_2.save();
            business_tariff_2.refresh();

            Model_TariffLabel label_16 = new Model_TariffLabel();
            label_16.description = "Už prvního měsíce je to placený";
            label_16.icon = "fa-bullhorn";
            label_16.label = "Super koment bla bla bla";
            label_16.tariff = business_tariff_2;
            label_16.save();

            Model_TariffLabel label_17 = new Model_TariffLabel();
            label_17.description = "Tento tarif je na odzkoušení kreditky";
            label_17.icon = "fa-bullhorn";
            label_17.label = "Super koment 2ln ljhljk ljk hllkjhlkj";
            label_17.tariff = business_tariff_2;
            label_17.save();

            Model_TariffLabel label_18 = new Model_TariffLabel();
            label_18.description = "kreditkaaa!!!";
            label_18.icon = "fa-bullhorn";
            label_18.label = "Super koment 3 ";
            label_18.tariff = business_tariff_2;
            label_18.save();

            Model_TariffLabel label_19 = new Model_TariffLabel();
            label_19.description = "Kreditkaaaaa";
            label_19.icon = "fa-bullhorn";
            label_19.label = "Super koment 4";
            label_19.tariff = business_tariff_2;
            label_19.save();

            Model_ProductExtension business_tariff2_extensions_5 = new Model_ProductExtension();
            business_tariff2_extensions_5.name = "Extension saasa";
            business_tariff2_extensions_5.description = "description extensioasn 1";
            business_tariff2_extensions_5.type = Enum_ExtensionType.project;
            business_tariff2_extensions_5.active = true;
            business_tariff2_extensions_5.removed = false;
            business_tariff2_extensions_5.color = "blue-chambray";
            business_tariff2_extensions_5.tariff_included = business_tariff_2;
            business_tariff2_extensions_5.configuration = "{\"price\":600,\"count\":5}";
            business_tariff2_extensions_5.save();

            Model_ProductExtension business_tariff2_extensions_4 = new Model_ProductExtension();
            business_tariff2_extensions_4.name = "Extension sasa";
            business_tariff2_extensions_4.description = "description extensdan 1";
            business_tariff2_extensions_4.type = Enum_ExtensionType.project;
            business_tariff2_extensions_4.active = true;
            business_tariff2_extensions_4.removed = false;
            business_tariff2_extensions_4.color = "blue-chambray";
            business_tariff2_extensions_4.tariff_included = business_tariff_2;
            business_tariff2_extensions_4.configuration = "{\"price\":600,\"count\":5}";
            business_tariff2_extensions_4.save();

            Model_ProductExtension business_tariff2_extensions_3 = new Model_ProductExtension();
            business_tariff2_extensions_3.name = "Extension dasa";
            business_tariff2_extensions_3.description = "description extensioan 1";
            business_tariff2_extensions_3.type = Enum_ExtensionType.project;
            business_tariff2_extensions_3.active = true;
            business_tariff2_extensions_3.removed = false;
            business_tariff2_extensions_3.color = "blue-chambray";
            business_tariff2_extensions_3.tariff_optional = business_tariff_2;
            business_tariff2_extensions_3.configuration = "{\"price\":600,\"count\":5}";
            business_tariff2_extensions_3.save();

            Model_ProductExtension business_tariff2_extensions_2 = new Model_ProductExtension();
            business_tariff2_extensions_2.name = "Extension ssa";
            business_tariff2_extensions_2.description = "description extenssdan 1";
            business_tariff2_extensions_2.type = Enum_ExtensionType.project;
            business_tariff2_extensions_2.active = true;
            business_tariff2_extensions_2.removed = false;
            business_tariff2_extensions_2.color = "blue-chambray";
            business_tariff2_extensions_2.tariff_optional = business_tariff_2;
            business_tariff2_extensions_2.configuration = "{\"price\":600,\"count\":5}";
            business_tariff2_extensions_2.save();

            Model_ProductExtension business_tariff2_extensions_1 = new Model_ProductExtension();
            business_tariff2_extensions_1.name = "Extension sadaska";
            business_tariff2_extensions_1.description = "description extenssaioasdan 1";
            business_tariff2_extensions_1.type = Enum_ExtensionType.project;
            business_tariff2_extensions_1.active = true;
            business_tariff2_extensions_1.removed = false;
            business_tariff2_extensions_1.color = "blue-chambray";
            business_tariff2_extensions_1.tariff_optional = business_tariff_2;
            business_tariff2_extensions_1.configuration = "{\"price\":600,\"count\":5}";
            business_tariff2_extensions_1.save();

            Model_Tariff illegal_tariff = new Model_Tariff();
            tariff_1.order_position = 5;
            illegal_tariff.active = false;
            illegal_tariff.save();

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

    public Result person_test_user() {
        try {

            if (Model_Person.find.where().eq("nick_name", "Pepíno").findUnique() != null)
                return GlobalResult.result_badRequest("Its Already done!");

            System.err.println("Vytvářím uživatele s emailem:  test_user@byzance.cz");
            System.err.println("Heslem: 123456789");
            System.err.println("Tokenem: token");

            // Vytvoří osobu
            Model_Person person = new Model_Person();
            person.full_name = "Pačmund Pepa";
            person.nick_name = "Pepíno";
            person.mail = "test_user@byzance.cz";
            person.freeze_account = false;
            person.mailValidated = true;
            person.setSha("123456789");
            person.save();

            Model_FloatingPersonToken token = new Model_FloatingPersonToken();
            token.person = person;
            token.authToken = "token";
            token.setDate();
            token.save();

            // Vytvoří tarif
            Model_Product product = new Model_Product();
            product.business_model = Enum_BusinessModel.alpha;
            product.credit = (long) 0;
            product.name = "Pepkova velkolepá Alfa";
            product.active  = true;  // Produkt jelikož je Aplha je aktivní - Alpha nebo Trial dojedou kvuli omezení času
            product.method  = Enum_Payment_method.free;

            Model_Customer customer = new Model_Customer();
            customer.person = person;
            product.customer = customer;

            Model_PaymentDetails payment_details = new Model_PaymentDetails();
            payment_details.full_name = person.full_name;
            payment_details.invoice_email = person.mail;
            payment_details.company_account = false;
            payment_details.street = "Karlovo náměsí";
            payment_details.street_number = "457";
            payment_details.city = "Praha";
            payment_details.zip_code = "12000";
            payment_details.country = "Czech Republic";
            payment_details.product = product;
            product.payment_details = payment_details;
            product.save();

             // Okopíruji všechny aktivní, které má Tarrif už v sobě
            for (Model_ProductExtension ext : Model_Tariff.find.where().eq("identifier","alpha").findUnique().extensions_included){

                if(ext.active) {

                    Model_ProductExtension extension = Model_ProductExtension.copyExtension(ext);
                    extension.product = product;
                    extension.save();
                }
            }

            // Vytvořím Projekty
            Model_Project project_1 = new Model_Project();
            project_1.product = product;
            project_1.name = "První velkolepý projekt";
            project_1.description = "Toto je Pepkův velkolepý testovací projekt primárně určen pro testování Blocko Programu, kde už má zaregistrovaný testovací HW";
            project_1.save();


            Model_ProjectParticipant participant_1 = new Model_ProjectParticipant();
            participant_1.person = person;
            participant_1.project = project_1;
            participant_1.state = Enum_Participant_status.owner;
            participant_1.save();

            // Zaregistruji pod ně Yody
            Model_Board yoda_G  = Model_Board.find.where().eq("name","[Q]").findUnique();
            yoda_G.project = project_1;
            yoda_G.virtual_instance_under_project = project_1.private_instance;
            yoda_G.update();

           for(Model_Board board :project_1.boards  ) {
               board.project = project_1;
               board.save();
           }

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}