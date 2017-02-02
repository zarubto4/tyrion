package utilities.demo_data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import models.blocko.Model_BlockoBlock;
import models.blocko.Model_BlockoBlockVersion;
import models.blocko.Model_TypeOfBlock;
import models.compiler.*;
import models.grid.Model_GridWidget;
import models.grid.Model_GridWidgetVersion;
import models.grid.Model_TypeOfWidget;
import models.person.Model_FloatingPersonToken;
import models.person.Model_Person;
import models.project.b_program.Model_BPair;
import models.project.b_program.Model_BProgram;
import models.project.b_program.Model_BProgramHwGroup;
import models.project.b_program.instnace.Model_HomerInstanceRecord;
import models.project.b_program.servers.Model_HomerServer;
import models.project.c_program.Model_CProgram;
import models.project.global.Model_Product;
import models.project.global.Model_Project;
import models.project.global.Model_ProjectParticipant;
import models.project.global.financial.*;
import models.project.m_program.Model_MProgram;
import models.project.m_program.Model_MProject;
import models.project.m_program.Model_MProjectProgramSnapShot;
import org.apache.commons.io.IOUtils;
import play.Application;
import play.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.*;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_Admin;
import utilities.response.GlobalResult;

import javax.inject.Inject;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;


@Api(value = "Dashboard Private Api", hidden = true)
@Security.Authenticated(Secured_Admin.class)
public class Demo_Data_Controller extends Controller {

    @Inject
    Application application;

    public Result test(){
        try {

            System.out.println("Demo_Data_Controller :: test :: start");

            byte[] bootloader_content = IOUtils.toByteArray(Play.application().resourceAsStream("/demo_data/demo_bootloader.bin"));

            Model_BootLoader boot_loader = new Model_BootLoader();
            boot_loader.name = "BootLoader Test";
            boot_loader.version_identificator = "1.0.2";
            boot_loader.description = " V žádném případě nevypalujte tento bootloader do HW - není aktuální a asi to není ani bootloader!!!";
            boot_loader.date_of_create = new Date();
            boot_loader.save();

            Model_FileRecord filerecord  =  Model_FileRecord.create_Binary_file(boot_loader.get_path(), Model_FileRecord.get_encoded_binary_string_from_body(bootloader_content) , "bootloader.bin" );
            boot_loader.file = filerecord;
            filerecord.boot_loader = boot_loader;
            filerecord.update();
            boot_loader.update();


            System.out.println("Demo_Data_Controller :: test ::  Vše v pořádku:: ");


            return GlobalResult.result_ok();


        }catch (Exception e){
            System.out.println("Demo_Data_Controller :: test :: "+ "Došlo k problémům!!!!");

            e.printStackTrace();
            return Loggy.result_internalServerError(e, request());
        }
    }

    /*
        Slouží primárně k vytváření demo dat při vývoji - je nutné pamatovat že změnou struktury objektů
        se nemění struktura vytvářených dat. Což je poměrně pracné na opravu při velkých úpravách.

        Každopádně každý vývojář by měl do struktury doplnit demo data své práce aby se vždy dalo od ní odpíchnout dál.

        Demodata je povolené tvořit jen a pouze v "Developer modu"!!!!!!!!!!
     */
    public Result all_for_becki(){

        Result result = this.producers();           if(result.status() != 200 ) return result;
        result =   this.type_of_board();            if(result.status() != 200 ) return result;
        result =   this.test_boards();              if(result.status() != 200 ) return result;
        result =   this.extendension_servers();     if(result.status() != 200 ) return result;
        result =   this.basic_tariffs();            if(result.status() != 200 ) return result;
        result =   this.blocko_demo_data();         if(result.status() != 200 ) return result;
        result =   this.grid_demo_data();           if(result.status() != 200 ) return result;
        result =   this.c_program_configuration();  if(result.status() != 200 ) return result;

        result =  this.person_test_user();            if(result.status() != 200 ) return result;
        result =  this.person_project_and_programs(); if(result.status() != 200 ) return result;
        result =  this.person_instancies();           if(result.status() != 200 ) return result;

        return result;
    }

    public Result producers(){
        try {

            // Ochranná zarážka proti znovu vytvoření
            if(Model_Producer.find.where().eq("name", "Byzance ltd").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

            // Nastavím Producer
            Model_Producer producer    = new Model_Producer();
            producer.name        = "Byzance ltd";
            producer.description = "Developed with love from Byzance";
            producer.save();

            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result type_of_board(){
        try {

            // Ochranná zarážka proti znovu vytvoření
            Model_Producer producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            if(producer == null) return GlobalResult.result_BadRequest("Create Producer first");
            if(Model_Processor.find.where().eq("processor_name", "ARM STM32 FR17").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

            // Nastavím Processor - YODA
            Model_Processor processor_1      = new Model_Processor();
            processor_1.processor_name = "ARM STM32 FR17";
            processor_1.description    = "VET6 HPABT VQ KOR HP501";
            processor_1.processor_code = "STM32FR17";
            processor_1.speed          = 3000;
            processor_1.save();

            // Nastavím Processor - DRÁT / BEZDRÁT
            Model_Processor processor_2      = new Model_Processor();
            processor_2.processor_name = "ARM STM32F";
            processor_2.description    = "030CCT6 GH26J 93 CHN 611";
            processor_2.processor_code = "STM32F";
            processor_2.speed          = 3000;
            processor_2.save();

            // Nastavím Processor - Rozbočovat
            Model_Processor processor_3      = new Model_Processor();
            processor_3.processor_name = "ARM STM32F070";
            processor_3.description    = "RBT6 GH25T 98 CHN GH 532";
            processor_3.processor_code = "STM32F070";
            processor_3.speed          = 3000;
            processor_3.save();


            byte[] bootloader_content = IOUtils.toByteArray(Play.application().resourceAsStream("/demo_data/demo_bootloader.bin"));


            // Nastavím Type of Boards - YODA
            Model_TypeOfBoard typeOfBoard_1 = new Model_TypeOfBoard();
            typeOfBoard_1.name        = "Yoda G2";
            typeOfBoard_1.description = " Yoda - Master Board with Ethernet and Wifi - second generation";
            typeOfBoard_1.compiler_target_name  = "BYZANCE_YODAG2";
            typeOfBoard_1.revision = "12/2015 V1.0 #0000";
            typeOfBoard_1.processor = processor_1;
            typeOfBoard_1.producer = producer;
            typeOfBoard_1.connectible_to_internet = true;
            typeOfBoard_1.save();


            Model_BootLoader boot_loader_1 = new Model_BootLoader();
            boot_loader_1.name = "BootLoader Test Yoda G2";
            boot_loader_1.version_identificator = "1.2.41";
            boot_loader_1.description = " V žádném případě nevypalujte tento bootloader do HW - není aktuální a asi to není ani bootloader!!!";
            boot_loader_1.date_of_create = new Date();
            boot_loader_1.type_of_board = typeOfBoard_1;
            boot_loader_1.main_type_of_board = typeOfBoard_1;
            boot_loader_1.save();


            Model_FileRecord filerecord_1  =  Model_FileRecord.create_Binary_file(boot_loader_1.get_path(), Model_FileRecord.get_encoded_binary_string_from_body(bootloader_content) , "bootloader.bin" );
            boot_loader_1.file = filerecord_1;
            filerecord_1.boot_loader = boot_loader_1;
            filerecord_1.update();
            boot_loader_1.update();





            Model_TypeOfBoard typeOfBoard_2 = new Model_TypeOfBoard();
            typeOfBoard_2.name        = "Wireless G2";
            typeOfBoard_2.description = " Wireless kit second generation";
            typeOfBoard_2.compiler_target_name  = "BYZANCE_WRLSKITG2";
            typeOfBoard_2.revision = "06/2016 V2.0 #0000";
            typeOfBoard_2.processor = processor_2;
            typeOfBoard_2.producer = producer;
            typeOfBoard_2.connectible_to_internet = false;
            typeOfBoard_2.save();

            Model_BootLoader boot_loader_2 = new Model_BootLoader();
            boot_loader_2.name = "BootLoader Test Wireless G2";
            boot_loader_2.version_identificator = "1.0.12";
            boot_loader_2.description = " V žádném případě nevypalujte tento bootloader do HW - není aktuální a asi to není ani bootloader!!!";
            boot_loader_2.date_of_create = new Date();
            boot_loader_2.type_of_board = typeOfBoard_2;
            boot_loader_2.main_type_of_board = typeOfBoard_2;
            boot_loader_2.save();


            Model_FileRecord filerecord_2  =  Model_FileRecord.create_Binary_file(boot_loader_2.get_path(), Model_FileRecord.get_encoded_binary_string_from_body(bootloader_content) , "bootloader.bin" );
            boot_loader_2.file = filerecord_2;
            filerecord_2.boot_loader = boot_loader_2;
            filerecord_2.update();
            boot_loader_2.update();


            Model_TypeOfBoard typeOfBoard_3 = new Model_TypeOfBoard();
            typeOfBoard_3.name        = "BUS G2";
            typeOfBoard_3.description = " BUS kit second generation";
            typeOfBoard_3.compiler_target_name  = "BYZANCE_BUSKITG2";
            typeOfBoard_3.revision = "02/2016 V2.0 #0000";
            typeOfBoard_3.processor = processor_2;
            typeOfBoard_3.producer = producer;
            typeOfBoard_3.connectible_to_internet = false;
            typeOfBoard_3.save();

            Model_BootLoader boot_loader_3 = new Model_BootLoader();
            boot_loader_3.name = "BootLoader Test Wireless G2";
            boot_loader_3.version_identificator = "9.10.1";
            boot_loader_3.description = " V žádném případě nevypalujte tento bootloader do HW - není aktuální a asi to není ani bootloader!!!";
            boot_loader_3.date_of_create = new Date();
            boot_loader_3.type_of_board = typeOfBoard_3;
            boot_loader_3.main_type_of_board = typeOfBoard_3;
            boot_loader_3.save();


            Model_FileRecord filerecord_3  =  Model_FileRecord.create_Binary_file(boot_loader_3.get_path(), Model_FileRecord.get_encoded_binary_string_from_body(bootloader_content) , "bootloader.bin" );
            boot_loader_3.file = filerecord_3;
            filerecord_3.boot_loader = boot_loader_3;
            filerecord_3.update();
            boot_loader_3.update();


            Model_TypeOfBoard typeOfBoard_4 = new Model_TypeOfBoard();
            typeOfBoard_4.name        = "Quad BUS HUB G1";
            typeOfBoard_4.description = " BUS kit second generation";
            typeOfBoard_4.compiler_target_name  = "BYZANCE_QUADBUSG1";
            typeOfBoard_4.revision = "12/2015 V1.0 #0000";
            typeOfBoard_4.processor = processor_3;
            typeOfBoard_4.producer = producer;
            typeOfBoard_4.connectible_to_internet = false;
            typeOfBoard_4.save();


            Model_BootLoader boot_loader_4 = new Model_BootLoader();
            boot_loader_4.name = "BootLoader Test Wireless G2";
            boot_loader_4.version_identificator = "8.0.1";
            boot_loader_4.description = " V žádném případě nevypalujte tento bootloader do HW - není aktuální a asi to není ani bootloader!!!";
            boot_loader_4.date_of_create = new Date();
            boot_loader_4.type_of_board = typeOfBoard_4;
            boot_loader_4.main_type_of_board = typeOfBoard_4;
            boot_loader_4.save();


            Model_FileRecord filerecord_4  =  Model_FileRecord.create_Binary_file(boot_loader_4.get_path(), Model_FileRecord.get_encoded_binary_string_from_body(bootloader_content) , "bootloader.bin" );
            boot_loader_4.file = filerecord_4;
            filerecord_4.boot_loader = boot_loader_4;
            filerecord_4.update();
            boot_loader_4.update();

            
            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result test_boards(){
        try {
            // Ochranná zarážka proti znovu vytvoření
            Model_TypeOfBoard yoda = Model_TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_YODAG2").findUnique();
            if(yoda == null) return GlobalResult.result_BadRequest("Create Type of Boards first");

            Model_TypeOfBoard wireles = Model_TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_WRLSKITG2").findUnique();
            Model_TypeOfBoard buskit = Model_TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_BUSKITG2").findUnique();

            if(Model_Board.find.where().eq("id", "002600513533510B34353732").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

            // Zaregistruji Yody
            Model_Board board_yoda_1 = new Model_Board();
            board_yoda_1.id = "002600513533510B34353732";
            board_yoda_1.personal_description = "Yoda B";
            board_yoda_1.type_of_board = yoda;
            board_yoda_1.date_of_create = new Date();
            board_yoda_1.save();

            Model_Board board_yoda_2 = new Model_Board();
            board_yoda_2.id = "003E00523533510B34353732";
            board_yoda_2.personal_description = "Yoda E";
            board_yoda_2.type_of_board = yoda;
            board_yoda_2.date_of_create = new Date();
            board_yoda_2.save();

            Model_Board board_yoda_3 = new Model_Board();
            board_yoda_3.id = "004C00523533510B34353732";
            board_yoda_3.personal_description = "Yoda C";
            board_yoda_3.type_of_board = yoda;
            board_yoda_3.date_of_create = new Date();
            board_yoda_3.save();

            Model_Board board_yoda_4 = new Model_Board();
            board_yoda_4.id = "002300513533510B34353732";
            board_yoda_4.personal_description = "Yoda A";
            board_yoda_4.type_of_board = yoda;
            board_yoda_4.date_of_create = new Date();
            board_yoda_4.save();

            Model_Board board_yoda_5 = new Model_Board();
            board_yoda_5.id = "wiki";
            board_yoda_5.personal_description = "Yoda D";
            board_yoda_5.type_of_board = yoda;
            board_yoda_5.date_of_create = new Date();
            board_yoda_5.save();

            // Wireless
            Model_Board wireless_1 = new Model_Board();
            wireless_1.id = "001C00074247430D20363439";
            wireless_1.personal_description = "[6]";
            wireless_1.type_of_board = wireles;
            wireless_1.date_of_create = new Date();
            wireless_1.save();

            Model_Board wireless_2 = new Model_Board();
            wireless_2.id = "001200254247430E20363439";
            wireless_2.personal_description = "[7]";
            wireless_2.type_of_board = wireles;
            wireless_2.date_of_create = new Date();
            wireless_2.save();

            Model_Board wireless_3 = new Model_Board();
            wireless_3.id = "001C000A4247430D20363439";
            wireless_3.personal_description = "[10]";
            wireless_3.type_of_board = wireles;
            wireless_3.date_of_create = new Date();
            wireless_3.save();

            Model_Board wireless_4 = new Model_Board();
            wireless_4.id = "001200244247430E20363439";
            wireless_4.personal_description = "[11]";
            wireless_4.type_of_board = wireles;
            wireless_4.date_of_create = new Date();
            wireless_4.save();

            Model_Board wireless_5 = new Model_Board();
            wireless_5.id = "001200264247430E20363439";
            wireless_5.personal_description = "[12]";
            wireless_5.type_of_board = wireles;
            wireless_5.date_of_create = new Date();
            wireless_5.save();

            Model_Board wireless_6 = new Model_Board();
            wireless_6.id = "001C00094247430D20363439";
            wireless_6.personal_description = "[13]";
            wireless_6.type_of_board = wireles;
            wireless_6.date_of_create = new Date();
            wireless_6.save();


            Model_Board wireless_7 = new Model_Board();
            wireless_7.id = "001C00144247430D20363439";
            wireless_7.personal_description = "[14]";
            wireless_7.type_of_board = wireles;
            wireless_7.date_of_create = new Date();
            wireless_7.save();

            Model_Board bus_1 = new Model_Board();
            bus_1.id = "001C00054247430D20363439";
            bus_1.personal_description = "[1]";
            bus_1.type_of_board = buskit;
            bus_1.date_of_create = new Date();
            bus_1.save();

            Model_Board bus_2 = new Model_Board();
            bus_2.id = "001300274247430E20363439";
            bus_2.personal_description = "[2]";
            bus_2.type_of_board = buskit;
            bus_2.date_of_create = new Date();
            bus_2.save();

            Model_Board bus_3 = new Model_Board();
            bus_3.id = "001C00064247430D20363439";
            bus_3.personal_description  = "[3]";
            bus_3.type_of_board = buskit;
            bus_3.date_of_create = new Date();
            bus_3.save();

            Model_Board bus_4 = new Model_Board();
            bus_4.id = "001200224247430E20363439";
            bus_4.personal_description  = "[4]";
            bus_4.type_of_board = buskit;
            bus_4.date_of_create = new Date();
            bus_4.save();

            Model_Board bus_5 = new Model_Board();
            bus_5.id = "001300244247430E20363439";
            bus_5.personal_description  = "[5]";
            bus_5.type_of_board = buskit;
            bus_5.date_of_create = new Date();
            bus_5.save();

            Model_Board bus_6 = new Model_Board();
            bus_6.id = "001C00104247430D20363439";
            bus_6.personal_description  = "[8]";
            bus_6.type_of_board = buskit;
            bus_6.date_of_create = new Date();
            bus_6.save();

            Model_Board bus_7 = new Model_Board();
            bus_7.id = "001C000C4247430D20363439";
            bus_7.personal_description  = "[9]";
            bus_7.type_of_board = buskit;
            bus_7.date_of_create = new Date();
            bus_7.save();

            Model_Board bus_8 = new Model_Board();
            bus_8.id = "001200234247430E20363439";
            bus_8.personal_description  = "[15]";
            bus_8.type_of_board = buskit;
            bus_8.date_of_create = new Date();
            bus_8.save();

            Model_Board bus_9 = new Model_Board();
            bus_9.id = "001300214247430E20363439";
            bus_9.personal_description  = "[16]";
            bus_9.type_of_board = buskit;
            bus_9.date_of_create = new Date();
            bus_9.save();




            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result extendension_servers(){
        try {

            // Ochranná zarážka proti znovu vytvoření
            if(Model_HomerServer.find.where().eq("personal_server_name", "Alfa").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

            // Nasstavím Homer servery
            Model_HomerServer cloud_server_1 = new Model_HomerServer();
            cloud_server_1.personal_server_name  = "Alfa";
            cloud_server_1.server_url   = "localhost3";
            cloud_server_1.grid_port    = 8500;
            cloud_server_1.mqtt_port    = 1881;
            cloud_server_1.mqtt_password = "pass";
            cloud_server_1.mqtt_username = "user";
            cloud_server_1.webView_port = 8501;
            cloud_server_1.server_type  = CLoud_Homer_Server_Type.main_server;
            cloud_server_1.save();

            Model_HomerServer cloud_server_2 = new Model_HomerServer();
            cloud_server_2.personal_server_name  = "Hydra";
            cloud_server_2.server_url   = "localhost3";
            cloud_server_2.grid_port    = 8500;
            cloud_server_2.mqtt_port    = 1881;
            cloud_server_2.mqtt_password = "pass";
            cloud_server_2.mqtt_username = "user";
            cloud_server_2.webView_port = 8501;
            cloud_server_2.server_type  = CLoud_Homer_Server_Type.backup_server;
            cloud_server_2.save();


            Model_HomerServer cloud_server_3 = new Model_HomerServer();
            cloud_server_3.personal_server_name  = "Andromeda";
            cloud_server_3.server_url   = "localhost3";
            cloud_server_3.grid_port    = 8500;
            cloud_server_3.mqtt_port    = 1881;
            cloud_server_3.mqtt_password = "pass";
            cloud_server_2.mqtt_username = "user";
            cloud_server_3.webView_port = 8501;
            cloud_server_3.server_type  = CLoud_Homer_Server_Type.public_server;
            cloud_server_3.save();

            Model_HomerServer cloud_server_4 = new Model_HomerServer();
            cloud_server_4.personal_server_name  = "Gemini";
            cloud_server_4.server_url   = "localhost4";
            cloud_server_4.grid_port    = 8500;
            cloud_server_4.mqtt_port    = 1881;
            cloud_server_4.mqtt_password = "pass";
            cloud_server_4.mqtt_username = "user";
            cloud_server_4.webView_port =  8501;
            cloud_server_4.server_type  = CLoud_Homer_Server_Type.public_server;
            cloud_server_4.save();

            // Testovací server
            Model_HomerServer cloud_server_5 = new Model_HomerServer();
            cloud_server_5.unique_identificator = "aaaaaaaaaaaaaaa";
            cloud_server_5.hash_certificate = "bbbbbbbbbbbbbbb";
            cloud_server_5.personal_server_name  = "Developer-Demo";
            cloud_server_5.server_url   = "localhost";
            cloud_server_5.grid_port    = 8500;
            cloud_server_5.mqtt_port    = 1881;
            cloud_server_5.mqtt_password = "pass";
            cloud_server_5.mqtt_username = "User";
            cloud_server_5.webView_port = 8501;
            cloud_server_5.server_type  = CLoud_Homer_Server_Type.test_server;
            cloud_server_5.save();

            // Nastavím kompilační servery
            Model_CompilationServer compilation_server_1 = new Model_CompilationServer();
            compilation_server_1.personal_server_name = "Perseus";
            compilation_server_1.save();

            Model_CompilationServer compilation_server_2 = new Model_CompilationServer();
            compilation_server_2.personal_server_name = "Pegas";
            compilation_server_2.save();


            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result basic_tariffs(){
        try {

            // Ochranná zarážka proti znovu vytvoření
            if(Model_GeneralTariff.find.where().eq("tariff_name", "Alfa account").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");
            
            // Alfa
            Model_GeneralTariff tariff_1 = new Model_GeneralTariff();
            tariff_1.order_position = 1;
            tariff_1.active = true;
            tariff_1.tariff_name = "Alfa account";
            tariff_1.tariff_description = "Temporary account only for next 3 months";
            tariff_1.identificator = "alpha";

            tariff_1.color            = "blue";
            tariff_1.credit_for_beginning = 0.0;

            tariff_1.required_paid_that = false;

            tariff_1.company_details_required  = false;
            tariff_1.required_payment_mode     = true;
            tariff_1.required_payment_method   = false;

            tariff_1.credit_card_support      = false;
            tariff_1.bank_transfer_support    = false;

            tariff_1.mode_annually    = false;
            tariff_1.mode_credit      = false;
            tariff_1.free_tariff      = true;

            tariff_1.price_in_usd = 0.0;

            tariff_1.save();
            tariff_1.refresh();

            Model_GeneralTariffLabel label_1 = new Model_GeneralTariffLabel();
            label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_1.icon = "fa-bullhorn";
            label_1.label = "Super koment bla bla bla";
            label_1.general_tariff = tariff_1;
            label_1.save();

            Model_GeneralTariffLabel label_2 = new Model_GeneralTariffLabel();
            label_2.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_2.icon = "fa-bullhorn";
            label_2.label = "Super koment 2";
            label_2.general_tariff = tariff_1;
            label_2.save();


            Model_GeneralTariffLabel label_3 = new Model_GeneralTariffLabel();
            label_3.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_3.icon = "fa-bullhorn";
            label_3.label = "Super koment 3 ";
            label_3.general_tariff = tariff_1;
            label_3.save();


            Model_GeneralTariffLabel label_4 = new Model_GeneralTariffLabel();
            label_4.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_4.icon = "fa-bullhorn";
            label_4.label = "Super koment 4";
            label_4.general_tariff = tariff_1;
            label_4.save();


            Model_GeneralTariffExtensions extensions_1 = new Model_GeneralTariffExtensions();
            extensions_1.name = "P5ístup do fora - Zdarma";
            extensions_1.active = true;
            extensions_1.color = "blue-madison";
            extensions_1.description = "testovací extension";
            extensions_1.price_in_usd = 1.15;
            extensions_1.general_tariff_included = tariff_1;
            extensions_1.save();


                Model_GeneralTariffLabel label_exstension_1 = new Model_GeneralTariffLabel();
                label_exstension_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
                label_exstension_1.icon = "fa-bullhorn";
                label_exstension_1.label = "Super koment 4";
                label_exstension_1.extensions = extensions_1;
                label_exstension_1.save();


            Model_GeneralTariffExtensions extensions_2 = new Model_GeneralTariffExtensions();
            extensions_2.name = "Super bonus ";
            extensions_2.active = true;
            extensions_2.color = "blue-chambray";
            extensions_2.description = "testovací extension";
            extensions_2.price_in_usd = 2.15;
            extensions_2.general_tariff_optional = tariff_1;
            extensions_2.save();

                Model_GeneralTariffLabel label_exstension_3 = new Model_GeneralTariffLabel();
                label_exstension_3.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
                label_exstension_3.icon = "fa-bullhorn";
                label_exstension_3.label = "Super koment 4";
                label_exstension_3.extensions = extensions_2;
                label_exstension_3.save();

            // Pro geeky

            Model_GeneralTariff geek_tariff = new Model_GeneralTariff();
            tariff_1.order_position = 2;
            geek_tariff.active = true;
            geek_tariff.tariff_name = "For true Geeks";
            geek_tariff.tariff_description = "Temporary account only for next 3 months";
            geek_tariff.identificator = "geek";

            geek_tariff.color            = "green-jungle";

            geek_tariff.required_paid_that = false;
            geek_tariff.credit_for_beginning = 5.00;

            geek_tariff.company_details_required  = false;
            geek_tariff.required_payment_mode     = true;
            geek_tariff.required_payment_method   = false;

            geek_tariff.credit_card_support      = true;
            geek_tariff.bank_transfer_support    = true;

            geek_tariff.mode_annually    = false;
            geek_tariff.mode_credit      = false;
            geek_tariff.free_tariff      = true;

            geek_tariff.price_in_usd = 4.99;

            geek_tariff.save();
            geek_tariff.refresh();


            Model_GeneralTariffLabel label_5 = new Model_GeneralTariffLabel();
            label_5.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_5.icon = "fa-bullhorn";
            label_5.label = "Super koment bla bla bla";
            label_5.general_tariff = geek_tariff;
            label_5.save();

            Model_GeneralTariffLabel label_6 = new Model_GeneralTariffLabel();
            label_6.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_6.icon = "fa-bullhorn";
            label_6.label = "Super koment 2ln ljhljk ljk hllkjhlkj";
            label_6.general_tariff = geek_tariff;
            label_6.save();


            Model_GeneralTariffLabel label_7 = new Model_GeneralTariffLabel();
            label_7.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_7.icon = "fa-bullhorn";
            label_7.label = "Super koment 3 ";
            label_7.general_tariff = geek_tariff;
            label_7.save();


            Model_GeneralTariffLabel label_8 = new Model_GeneralTariffLabel();
            label_8.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_8.icon = "fa-bullhorn";
            label_8.label = "Super koment 4";
            label_8.general_tariff = geek_tariff;
            label_8.save();




            // Placená

            Model_GeneralTariff business_tariff = new Model_GeneralTariff();
            tariff_1.order_position = 3;
            business_tariff.active = true;
            business_tariff.tariff_name = "For true Business";
            business_tariff.tariff_description = "Best for true business";
            business_tariff.identificator = "business_1";

            business_tariff.color            = "green-jungle";

            business_tariff.required_paid_that = false;
            business_tariff.credit_for_beginning = 100.0;

            business_tariff.company_details_required  = true;
            business_tariff.required_payment_mode     = true;
            business_tariff.required_payment_method   = true;

            business_tariff.credit_card_support      = true;
            business_tariff.bank_transfer_support    = true;

            business_tariff.mode_annually    = true;
            business_tariff.mode_credit      = true;
            business_tariff.free_tariff      = false;

            business_tariff.price_in_usd = 72.0;

            business_tariff.save();
            business_tariff.refresh();

            Model_GeneralTariffLabel label_9 = new Model_GeneralTariffLabel();
            label_9.description = "Kredit zdarma Trololo!";
            label_9.icon = "fa-bullhorn";
            label_9.label = "Super koment bla bla bla";
            label_9.general_tariff = business_tariff;
            label_9.save();

            Model_GeneralTariffLabel label_10 = new Model_GeneralTariffLabel();
            label_10.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_10.icon = "fa-bullhorn";
            label_10.label = "Super koment 2ln ljhljk ljk hllkjhlkj";
            label_10.general_tariff = business_tariff;
            label_10.save();


            Model_GeneralTariffLabel label_11 = new Model_GeneralTariffLabel();
            label_11.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_11.icon = "fa-bullhorn";
            label_11.label = "Super koment 3 ";
            label_11.general_tariff = business_tariff;
            label_11.save();

            Model_GeneralTariffLabel label_12 = new Model_GeneralTariffLabel();
            label_12.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_12.icon = "fa-bullhorn";
            label_12.label = "Super koment 4";
            label_12.general_tariff = business_tariff;
            label_12.save();

            Model_GeneralTariffLabel label_13 = new Model_GeneralTariffLabel();
            label_13.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_13.icon = "fa-bullhorn";
            label_13.label = "Super koment 4";
            label_13.general_tariff = business_tariff;
            label_13.save();

            Model_GeneralTariffLabel label_14 = new Model_GeneralTariffLabel();
            label_14.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_14.icon = "fa-bullhorn";
            label_14.label = "Super koment 4";
            label_14.general_tariff = business_tariff;
            label_14.save();

            Model_GeneralTariffLabel label_15 = new Model_GeneralTariffLabel();
            label_15.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_15.icon = "fa-bullhorn";
            label_15.label = "Super koment 4";
            label_15.general_tariff = business_tariff;
            label_15.save();


            Model_GeneralTariffExtensions business_tariff_extensions_5 = new Model_GeneralTariffExtensions();
            business_tariff_extensions_5.name = "Included B1";
            business_tariff_extensions_5.active = true;
            business_tariff_extensions_5.color = "blue-chambray";
            business_tariff_extensions_5.description = "testovací extension";
            business_tariff_extensions_5.price_in_usd = 0.8;
            business_tariff_extensions_5.general_tariff_included = business_tariff;
            business_tariff_extensions_5.save();

            Model_GeneralTariffLabel business_tariff_extensions_5_label_1 = new Model_GeneralTariffLabel();
            business_tariff_extensions_5_label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            business_tariff_extensions_5_label_1.icon = "fa-bullhorn";
            business_tariff_extensions_5_label_1.label = "Super koment 4";
            business_tariff_extensions_5_label_1.extensions = business_tariff_extensions_5;
            business_tariff_extensions_5_label_1.save();

            Model_GeneralTariffExtensions business_tariff_extensions_4 = new Model_GeneralTariffExtensions();
            business_tariff_extensions_4.name = "Included B1";
            business_tariff_extensions_4.active = true;
            business_tariff_extensions_4.color = "blue-chambray";
            business_tariff_extensions_4.description = "testovací extension";
            business_tariff_extensions_4.price_in_usd = 0.8;
            business_tariff_extensions_4.general_tariff_included = business_tariff;
            business_tariff_extensions_4.save();

            Model_GeneralTariffLabel business_tariff_extensions_4_label_1 = new Model_GeneralTariffLabel();
            business_tariff_extensions_4_label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            business_tariff_extensions_4_label_1.icon = "fa-bullhorn";
            business_tariff_extensions_4_label_1.label = "Super koment 4";
            business_tariff_extensions_4_label_1.extensions = business_tariff_extensions_4;
            business_tariff_extensions_4_label_1.save();


            Model_GeneralTariffExtensions business_tariff_extensions_3 = new Model_GeneralTariffExtensions();
            business_tariff_extensions_3.name = "Included B1";
            business_tariff_extensions_3.active = true;
            business_tariff_extensions_3.color = "blue-chambray";
            business_tariff_extensions_3.price_in_usd = 0.8;
            business_tariff_extensions_3.general_tariff_included = business_tariff;
            business_tariff_extensions_3.save();

            Model_GeneralTariffLabel business_tariff_extensions_3_label_1 = new Model_GeneralTariffLabel();
            business_tariff_extensions_3_label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            business_tariff_extensions_3_label_1.icon = "fa-bullhorn";
            business_tariff_extensions_3_label_1.label = "Super koment 4";
            business_tariff_extensions_3_label_1.extensions = business_tariff_extensions_3;
            business_tariff_extensions_3_label_1.save();



            Model_GeneralTariffExtensions business_tariff_extensions_2 = new Model_GeneralTariffExtensions();
            business_tariff_extensions_2.name = "Optional Tarrif B1";
            business_tariff_extensions_2.active = true;
            business_tariff_extensions_2.color = "blue-chambray";
            business_tariff_extensions_2.description = "testovací extension";
            business_tariff_extensions_2.price_in_usd = 0.8;
            business_tariff_extensions_2.general_tariff_optional = business_tariff;
            business_tariff_extensions_2.save();

            Model_GeneralTariffLabel business_tariff_extensions_2_label_2 = new Model_GeneralTariffLabel();
            business_tariff_extensions_2_label_2.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            business_tariff_extensions_2_label_2.icon = "fa-bullhorn";
            business_tariff_extensions_2_label_2.label = "Super koment 4";
            business_tariff_extensions_2_label_2.extensions = business_tariff_extensions_2;
            business_tariff_extensions_2_label_2.save();

            Model_GeneralTariffLabel business_tariff_extensions_2_label_1 = new Model_GeneralTariffLabel();
            business_tariff_extensions_2_label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            business_tariff_extensions_2_label_1.icon = "fa-bullhorn";
            business_tariff_extensions_2_label_1.label = "Super koment 4";
            business_tariff_extensions_2_label_1.extensions = business_tariff_extensions_2;
            business_tariff_extensions_2_label_1.save();



            Model_GeneralTariffExtensions business_tariff_extensions_1= new Model_GeneralTariffExtensions();
            business_tariff_extensions_1.name = "Optional Tarrif B1";
            business_tariff_extensions_1.active = true;
            business_tariff_extensions_1.color = "blue-chambray";
            business_tariff_extensions_1.description = "testovací extension";
            business_tariff_extensions_1.price_in_usd = 0.8;
            business_tariff_extensions_1.general_tariff_optional = business_tariff;
            business_tariff_extensions_1.save();

            Model_GeneralTariffLabel business_tariff_extensions_1_label_1 = new Model_GeneralTariffLabel();
            business_tariff_extensions_1_label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            business_tariff_extensions_1_label_1.icon = "fa-bullhorn";
            business_tariff_extensions_1_label_1.label = "Super koment 4";
            business_tariff_extensions_1_label_1.extensions = business_tariff_extensions_1;
            business_tariff_extensions_1_label_1.save();



            // Další placený

            Model_GeneralTariff business_tariff_2 = new Model_GeneralTariff();
            tariff_1.order_position = 4;
            business_tariff_2.active = true;
            business_tariff_2.tariff_name = "Enterprise";
            business_tariff_2.tariff_description = "You know what you need!";
            business_tariff_2.identificator = "business_2";

            business_tariff_2.color            = "green-sharp";

            business_tariff_2.required_paid_that = true;
            business_tariff_2.credit_for_beginning = 0.0;

            business_tariff_2.company_details_required  = true;
            business_tariff_2.required_payment_mode     = true;
            business_tariff_2.required_payment_method   = true;

            business_tariff_2.credit_card_support      = true;
            business_tariff_2.bank_transfer_support    = true;

            business_tariff_2.mode_annually    = true;
            business_tariff_2.mode_credit      = true;
            business_tariff_2.free_tariff      = false;

            business_tariff_2.price_in_usd = 1399.99;

            business_tariff_2.save();
            business_tariff_2.refresh();


            Model_GeneralTariffLabel label_16 = new Model_GeneralTariffLabel();
            label_16.description = "Už prvního měsíce je to placený";
            label_16.icon = "fa-bullhorn";
            label_16.label = "Super koment bla bla bla";
            label_16.general_tariff = business_tariff_2;
            label_16.save();

            Model_GeneralTariffLabel label_17 = new Model_GeneralTariffLabel();
            label_17.description = "Tento tarif je na odzkoušení kreditky";
            label_17.icon = "fa-bullhorn";
            label_17.label = "Super koment 2ln ljhljk ljk hllkjhlkj";
            label_17.general_tariff = business_tariff_2;
            label_17.save();


            Model_GeneralTariffLabel label_18 = new Model_GeneralTariffLabel();
            label_18.description = "kreditkaaa!!!";
            label_18.icon = "fa-bullhorn";
            label_18.label = "Super koment 3 ";
            label_18.general_tariff = business_tariff_2;
            label_18.save();

            Model_GeneralTariffLabel label_19 = new Model_GeneralTariffLabel();
            label_19.description = "Kreditkaaaaa";
            label_19.icon = "fa-bullhorn";
            label_19.label = "Super koment 4";
            label_19.general_tariff = business_tariff_2;
            label_19.save();


            Model_GeneralTariffExtensions business_tariff2_extensions_5 = new Model_GeneralTariffExtensions();
            business_tariff2_extensions_5.name = "Included Extension 5";
            business_tariff2_extensions_5.active = true;
            business_tariff2_extensions_5.color = "blue-chambray";
            business_tariff2_extensions_5.description = "testovací extension";
            business_tariff2_extensions_5.price_in_usd = 8.0;
            business_tariff2_extensions_5.general_tariff_included = business_tariff_2;
            business_tariff2_extensions_5.save();

            Model_GeneralTariffLabel business_tariff2_extensions_5_label_1 = new Model_GeneralTariffLabel();
            business_tariff2_extensions_5_label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            business_tariff2_extensions_5_label_1.icon = "fa-bullhorn";
            business_tariff2_extensions_5_label_1.label = "Super koment 4";
            business_tariff2_extensions_5_label_1.extensions = business_tariff2_extensions_5;
            business_tariff2_extensions_5_label_1.save();

            Model_GeneralTariffExtensions business_tariff2_extensions_4 = new Model_GeneralTariffExtensions();
            business_tariff2_extensions_4.name = "Included Extension 4";
            business_tariff2_extensions_4.active = true;
            business_tariff2_extensions_4.color = "blue-chambray";
            business_tariff2_extensions_4.description = "testovací extension";
            business_tariff2_extensions_4.price_in_usd = 8.0;
            business_tariff2_extensions_4.general_tariff_included = business_tariff_2;
            business_tariff2_extensions_4.save();

            Model_GeneralTariffLabel business_tariff2_extensions_4_label_1 = new Model_GeneralTariffLabel();
            business_tariff2_extensions_4_label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            business_tariff2_extensions_4_label_1.icon = "fa-bullhorn";
            business_tariff2_extensions_4_label_1.label = "Super koment 4";
            business_tariff2_extensions_4_label_1.extensions = business_tariff2_extensions_4;
            business_tariff2_extensions_4_label_1.save();


            Model_GeneralTariffExtensions business_tariff2_extensions_3 = new Model_GeneralTariffExtensions();
            business_tariff2_extensions_3.name = "Optional Extension 3";
            business_tariff2_extensions_3.active = true;
            business_tariff2_extensions_3.color = "blue-chambray";
            business_tariff2_extensions_3.description = "testovací extension";
            business_tariff2_extensions_3.price_in_usd = 0.8;
            business_tariff2_extensions_3.general_tariff_optional = business_tariff_2;
            business_tariff2_extensions_3.save();


            Model_GeneralTariffLabel business_tariff2_extensions_3_label_1 = new Model_GeneralTariffLabel();
            business_tariff2_extensions_3_label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            business_tariff2_extensions_3_label_1.icon = "fa-bullhorn";
            business_tariff2_extensions_3_label_1.label = "Super koment 4";
            business_tariff2_extensions_3_label_1.extensions = business_tariff2_extensions_3;
            business_tariff2_extensions_3_label_1.save();


            Model_GeneralTariffExtensions business_tariff2_extensions_2 = new Model_GeneralTariffExtensions();
            business_tariff2_extensions_2.name = "Optional Tarrif B1";
            business_tariff2_extensions_2.active = true;
            business_tariff2_extensions_2.color = "blue-chambray";
            business_tariff2_extensions_2.description = "testovací extension";
            business_tariff2_extensions_2.price_in_usd = 0.8;
            business_tariff2_extensions_2.general_tariff_optional = business_tariff_2;
            business_tariff2_extensions_2.save();

            Model_GeneralTariffLabel business_tariff2_extensions_2_label_1 = new Model_GeneralTariffLabel();
            business_tariff2_extensions_2_label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            business_tariff2_extensions_2_label_1.icon = "fa-bullhorn";
            business_tariff2_extensions_2_label_1.label = "Super koment 4";
            business_tariff2_extensions_2_label_1.extensions = business_tariff2_extensions_2;
            business_tariff2_extensions_2_label_1.save();


            Model_GeneralTariffExtensions business_tariff2_extensions_1= new Model_GeneralTariffExtensions();
            business_tariff2_extensions_1.name = "Optional Tarrif B1";
            business_tariff2_extensions_1.active = true;
            business_tariff2_extensions_1.color = "blue-chambray";
            business_tariff2_extensions_1.description = "testovací extension";
            business_tariff2_extensions_1.price_in_usd = 0.8;
            business_tariff2_extensions_1.general_tariff_optional = business_tariff_2;
            business_tariff2_extensions_1.save();

            Model_GeneralTariffLabel business_tariff2_extensions_1_label_1 = new Model_GeneralTariffLabel();
            business_tariff2_extensions_1_label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            business_tariff2_extensions_1_label_1.icon = "fa-bullhorn";
            business_tariff2_extensions_1_label_1.label = "Super koment 4";
            business_tariff2_extensions_1_label_1.extensions = business_tariff2_extensions_1;
            business_tariff2_extensions_1_label_1.save();


            Model_GeneralTariff ilegal_tariff = new Model_GeneralTariff();
            tariff_1.order_position = 5;
            ilegal_tariff.active = false;
            ilegal_tariff.save();

            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result blocko_demo_data(){
        try {

            // Ochranná zarážka proti znovu vytvoření
            if(Model_TypeOfBlock.find.where().eq("name", "Social Sites Blocks").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

            Model_TypeOfBlock typeOfBlock_1 = new Model_TypeOfBlock();
            typeOfBlock_1.name =  "Social Sites Blocks";
            typeOfBlock_1.description = "Sociální bločky pro Facebook, Twitter a další";
            typeOfBlock_1.save();

            Model_TypeOfBlock typeOfBlock_2 = new Model_TypeOfBlock();
            typeOfBlock_2.name =  "Logic Blocks";
            typeOfBlock_2.description = "Základní logické bločky na principu booleovy algebry";
            typeOfBlock_2.save();

            Model_TypeOfBlock typeOfBlock_3 = new Model_TypeOfBlock();
            typeOfBlock_3.name =  "Api Blocks";
            typeOfBlock_3.description = "Bločky pro Externí API";
            typeOfBlock_3.save();

            Model_TypeOfBlock typeOfBlock_4 = new Model_TypeOfBlock();
            typeOfBlock_4.name =  "Times Blocks";
            typeOfBlock_4.description = "Bločky s časovou konstantou";
            typeOfBlock_4.save();


            //**************************************************************************************************************


            //1
            Model_BlockoBlock blockoBlock_1_1 = new Model_BlockoBlock();
            blockoBlock_1_1.producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_1_1.name = "Facebook Post";
            blockoBlock_1_1.description = "m.n,a sldjkfbnlskjd bjsdnf jkbsjndafio bjkvc,mxnymf můiwljhkn bfm,mn.adsjlůxkbcvnymn klnaf m,mnbjlů§k nbasldfb,n jkl.lkn nmsgl,můfjk br,mn.fl kbmfkllykbv vkůljmyn,d.mckůlxůklxbvnm,dsf m.ylp§foigkljsadůjfndmsvoija kdsfvůljnkjb fkljgfbvclasgfbnlfagkbkcnlsgkfklndgdk an dsja";
            blockoBlock_1_1.type_of_block = typeOfBlock_1;
            blockoBlock_1_1.save();

            Model_BlockoBlock blockoBlock_1_2 = new Model_BlockoBlock();
            blockoBlock_1_2.producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_1_2.name = "Twitter tweet";
            blockoBlock_1_2.description = "Lorem ipsum di lasjdhflkj dshaflj  sadfsdfas dfsadf sad gsfgsdf sadfsd fas";
            blockoBlock_1_2.type_of_block = typeOfBlock_1;
            blockoBlock_1_2.save();

            Model_BlockoBlock blockoBlock_1_3 = new Model_BlockoBlock();
            blockoBlock_1_3.producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_1_3.name = "Google+";
            blockoBlock_1_3.description = "Google+ Function dsafkjb bjbsadlkjbf kblasdf adsf";
            blockoBlock_1_3.type_of_block = typeOfBlock_1;
            blockoBlock_1_3.save();


            //2
            Model_BlockoBlock blockoBlock_2_1 = new Model_BlockoBlock();
            blockoBlock_2_1.producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_2_1.name = "OR";
            blockoBlock_2_1.description = "Logic function OR";
            blockoBlock_2_1.type_of_block = typeOfBlock_2;
            blockoBlock_2_1.save();

            Model_BlockoBlock blockoBlock_2_2 = new Model_BlockoBlock();
            blockoBlock_2_2.producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_2_2.name = "AND";
            blockoBlock_2_2.description = "Logic function AND";
            blockoBlock_2_2.type_of_block = typeOfBlock_2;
            blockoBlock_2_2.save();

            Model_BlockoBlock blockoBlock_2_3 = new Model_BlockoBlock();
            blockoBlock_2_3.producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_2_3.name = "XOR";
            blockoBlock_2_3.description = "Logic function XOR";
            blockoBlock_2_3.type_of_block = typeOfBlock_2;
            blockoBlock_2_3.save();


            //3
            Model_BlockoBlock blockoBlock_3_1 = new Model_BlockoBlock();
            blockoBlock_3_1.producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_3_1.name = "POST";
            blockoBlock_3_1.description = "Basic REST-API REQUEST POST";
            blockoBlock_3_1.type_of_block = typeOfBlock_3;
            blockoBlock_3_1.save();

            Model_BlockoBlock blockoBlock_3_2 = new Model_BlockoBlock();
            blockoBlock_3_2.producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_3_2.name = "GET";
            blockoBlock_3_2.description = "Basic REST-API REQUEST GET";
            blockoBlock_3_2.type_of_block = typeOfBlock_3;
            blockoBlock_3_2.save();

            Model_BlockoBlock blockoBlock_3_3 = new Model_BlockoBlock();
            blockoBlock_3_3.producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_3_3.name = "PUT";
            blockoBlock_3_3.description = "Basic REST-API REQUEST PUT";
            blockoBlock_3_3.type_of_block = typeOfBlock_3;
            blockoBlock_3_3.save();

            Model_BlockoBlock blockoBlock_3_4 = new Model_BlockoBlock();
            blockoBlock_3_4.producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_3_4.name = "DELETE";
            blockoBlock_3_4.description = "Basic REST-API REQUEST DELETE";
            blockoBlock_3_4.type_of_block = typeOfBlock_3;
            blockoBlock_3_4.save();


            //**************************************************************************************************************

            // 1_1
            Model_BlockoBlockVersion version_1_1_1 = new Model_BlockoBlockVersion();
            version_1_1_1.blocko_block = blockoBlock_1_1;
            version_1_1_1.date_of_create = new Date();
            version_1_1_1.logic_json = "{}";
            version_1_1_1.design_json = "{}";
            version_1_1_1.version_description = "První update";
            version_1_1_1.version_name = "1.0.1";
            version_1_1_1.approval_state = Approval_state.approved;
            version_1_1_1.save();

            Model_BlockoBlockVersion version_1_1_2 = new Model_BlockoBlockVersion();
            version_1_1_2.blocko_block = blockoBlock_1_1;
            version_1_1_2.date_of_create = new Date();
            version_1_1_2.logic_json = "{}";
            version_1_1_2.design_json = "{}";
            version_1_1_2.version_description = "První update";
            version_1_1_2.version_name = "1.0.1";
            version_1_1_2.approval_state = Approval_state.approved;
            version_1_1_2.save();

            // 1_2
            Model_BlockoBlockVersion version_1_2_1 = new Model_BlockoBlockVersion();
            version_1_2_1.blocko_block = blockoBlock_1_2;
            version_1_2_1.date_of_create = new Date();
            version_1_2_1.logic_json = "{}";
            version_1_2_1.design_json = "{}";
            version_1_2_1.version_description = "První update";
            version_1_2_1.version_name = "1.0.1";
            version_1_2_1.approval_state = Approval_state.approved;
            version_1_2_1.save();

            Model_BlockoBlockVersion version_1_2_2 = new Model_BlockoBlockVersion();
            version_1_2_2.blocko_block = blockoBlock_1_2;
            version_1_2_2.date_of_create = new Date();
            version_1_2_2.logic_json = "{}";
            version_1_2_2.design_json = "{}";
            version_1_2_2.version_description = "První update";
            version_1_2_2.version_name = "1.0.1";
            version_1_2_2.approval_state = Approval_state.approved;
            version_1_2_2.save();

            // 1_3
            Model_BlockoBlockVersion version_1_3_1 = new Model_BlockoBlockVersion();
            version_1_3_1.blocko_block = blockoBlock_1_3;
            version_1_3_1.date_of_create = new Date();
            version_1_3_1.logic_json = "{}";
            version_1_3_1.design_json = "{}";
            version_1_3_1.version_description = "První update";
            version_1_3_1.version_name = "1.0.1";
            version_1_3_1.approval_state = Approval_state.approved;
            version_1_3_1.save();

            Model_BlockoBlockVersion version_1_3_2 = new Model_BlockoBlockVersion();
            version_1_3_2.blocko_block = blockoBlock_1_3;
            version_1_3_2.date_of_create = new Date();
            version_1_3_2.logic_json = "{}";
            version_1_3_2.design_json = "{}";
            version_1_3_2.version_description = "První update";
            version_1_3_2.version_name = "1.0.1";
            version_1_3_2.approval_state = Approval_state.approved;
            version_1_3_2.save();

            // 2_1
            Model_BlockoBlockVersion version_2_1_1 = new Model_BlockoBlockVersion();
            version_2_1_1.blocko_block = blockoBlock_2_1;
            version_2_1_1.date_of_create = new Date();
            version_2_1_1.logic_json = "{}";
            version_2_1_1.design_json = "{}";
            version_2_1_1.version_description = "První update";
            version_2_1_1.version_name = "1.0.1";
            version_2_1_1.approval_state = Approval_state.approved;
            version_2_1_1.save();

            Model_BlockoBlockVersion version_2_1_2 = new Model_BlockoBlockVersion();
            version_2_1_2.blocko_block = blockoBlock_2_1;
            version_2_1_2.date_of_create = new Date();
            version_2_1_2.logic_json = "{}";
            version_2_1_2.design_json = "{}";
            version_2_1_2.version_description = "Prvnsafd -a.kshm fn.,mbs gjknbm akdfsm,.cxy ndfam,nkvxclůavcx namxyklnvdfsam ,cvklůdfsmv.lyům ,klnvyůmc,.í update";
            version_2_1_2.version_name = "1.0.2";
            version_2_1_2.approval_state = Approval_state.approved;
            version_2_1_2.save();

            // 2_2
            Model_BlockoBlockVersion version_2_2_1 = new Model_BlockoBlockVersion();
            version_2_2_1.blocko_block = blockoBlock_2_2;
            version_2_2_1.date_of_create = new Date();
            version_2_2_1.logic_json = "{}";
            version_2_2_1.design_json = "{}";
            version_2_2_1.version_description = "První update";
            version_2_2_1.version_name = "1.0.1";
            version_2_2_1.approval_state = Approval_state.approved;
            version_2_2_1.save();

            Model_BlockoBlockVersion version_2_2_2 = new Model_BlockoBlockVersion();
            version_2_2_2.blocko_block = blockoBlock_2_2;
            version_2_2_2.date_of_create = new Date();
            version_2_2_2.logic_json = "{}";
            version_2_2_2.design_json = "{}";
            version_2_2_2.version_description = "Druhý update";
            version_2_2_2.version_name = "1.0.2";
            version_2_2_2.approval_state = Approval_state.approved;
            version_2_2_2.save();

            Model_BlockoBlockVersion version_2_2_3= new Model_BlockoBlockVersion();
            version_2_2_3.blocko_block = blockoBlock_2_2;
            version_2_2_3.date_of_create = new Date();
            version_2_2_3.logic_json = "{}";
            version_2_2_3.design_json = "{}";
            version_2_2_3.version_description = "Třetí update";
            version_2_2_3.version_name = "1.0.3";
            version_2_2_3.approval_state = Approval_state.approved;
            version_2_2_3.save();

            Model_BlockoBlockVersion version_2_2_4 = new Model_BlockoBlockVersion();
            version_2_2_4.blocko_block = blockoBlock_2_2;
            version_2_2_4.date_of_create = new Date();
            version_2_2_4.logic_json = "{}";
            version_2_2_4.design_json = "{}";
            version_2_2_4.version_description = "Čtvrtý  update";
            version_2_2_4.version_name = "1.0.4";
            version_2_2_4.approval_state = Approval_state.approved;
            version_2_2_4.save();

            // 2_3
            Model_BlockoBlockVersion version_2_3_1 = new Model_BlockoBlockVersion();
            version_2_3_1.blocko_block = blockoBlock_2_3;
            version_2_3_1.date_of_create = new Date();
            version_2_3_1.logic_json = "{}";
            version_2_3_1.design_json = "{}";
            version_2_3_1.version_description = "První update";
            version_2_3_1.version_name = "Na poprvé";
            version_2_3_1.approval_state = Approval_state.approved;
            version_2_3_1.save();

            Model_BlockoBlockVersion version_2_3_2 = new Model_BlockoBlockVersion();
            version_2_3_2.blocko_block = blockoBlock_2_3;
            version_2_3_2.date_of_create = new Date();
            version_2_3_2.logic_json = "{}";
            version_2_3_2.design_json = "{}";
            version_2_3_2.version_description = "První update";
            version_2_3_2.version_name = "Na podruhé";
            version_2_3_2.approval_state = Approval_state.approved;
            version_2_3_2.save();


            // 3_1
            Model_BlockoBlockVersion version_3_1_1 = new Model_BlockoBlockVersion();
            version_3_1_1.blocko_block = blockoBlock_3_1;
            version_3_1_1.date_of_create = new Date();
            version_3_1_1.logic_json = "{}";
            version_3_1_1.design_json = "{}";
            version_3_1_1.version_description = "První update";
            version_3_1_1.version_name = "Verze 1";
            version_3_1_1.approval_state = Approval_state.approved;
            version_3_1_1.save();

            Model_BlockoBlockVersion version_3_1_2 = new Model_BlockoBlockVersion();
            version_3_1_2.blocko_block = blockoBlock_3_1;
            version_3_1_2.date_of_create = new Date();
            version_3_1_2.logic_json = "{}";
            version_3_1_2.design_json = "{}";
            version_3_1_2.version_description = "Druhý velkopeý asdklbfj aslaksbdfjlkbalskbdf lkjbafs lkjbafslbkjafslkjba sdflkbjasf update";
            version_3_1_2.version_name = "Verze 2";
            version_3_1_2.approval_state = Approval_state.approved;
            version_3_1_2.save();

            // 3_2
            Model_BlockoBlockVersion version_3_2_1 = new Model_BlockoBlockVersion();
            version_3_2_1.blocko_block = blockoBlock_3_2;
            version_3_2_1.date_of_create = new Date();
            version_3_2_1.logic_json = "{}";
            version_3_2_1.design_json = "{}";
            version_3_2_1.version_description = "První update";
            version_3_2_1.version_name = "1.0.1";
            version_3_2_1.approval_state = Approval_state.approved;
            version_3_2_1.save();

            Model_BlockoBlockVersion version_3_2_2 = new Model_BlockoBlockVersion();
            version_3_2_2.blocko_block = blockoBlock_3_2;
            version_3_2_2.date_of_create = new Date();
            version_3_2_2.logic_json = "{}";
            version_3_2_2.design_json = "{}";
            version_3_2_2.version_description = "První update";
            version_3_2_2.version_name = "1.0.2";
            version_3_2_2.approval_state = Approval_state.approved;
            version_3_2_2.save();

            Model_BlockoBlockVersion version_3_2_3 = new Model_BlockoBlockVersion();
            version_3_2_3.blocko_block = blockoBlock_3_2;
            version_3_2_3.date_of_create = new Date();
            version_3_2_3.logic_json = "{}";
            version_3_2_3.design_json = "{}";
            version_3_2_3.version_description = "První update";
            version_3_2_3.version_name = "1.1.3";
            version_3_2_3.approval_state = Approval_state.approved;
            version_3_2_3.save();

            // 3_3
            Model_BlockoBlockVersion version_3_3_1 = new Model_BlockoBlockVersion();
            version_3_3_1.blocko_block = blockoBlock_3_3;
            version_3_3_1.date_of_create = new Date();
            version_3_3_1.logic_json = "{}";
            version_3_3_1.design_json = "{}";
            version_3_3_1.version_description = "První update";
            version_3_3_1.version_name = "1.0.1";
            version_3_3_1.approval_state = Approval_state.approved;
            version_3_3_1.save();

            Model_BlockoBlockVersion version_3_3_2 = new Model_BlockoBlockVersion();
            version_3_3_2.blocko_block = blockoBlock_3_3;
            version_3_3_2.date_of_create = new Date();
            version_3_3_2.logic_json = "{}";
            version_3_3_2.design_json = "{}";
            version_3_3_2.version_description = "Druhý update";
            version_3_3_2.version_name = "1.0.2";
            version_3_3_2.approval_state = Approval_state.approved;
            version_3_3_2.save();

            // 3_4
            Model_BlockoBlockVersion version_3_4_1 = new Model_BlockoBlockVersion();
            version_3_4_1.blocko_block = blockoBlock_3_4;
            version_3_4_1.date_of_create = new Date();
            version_3_4_1.logic_json = "{}";
            version_3_4_1.design_json = "{}";
            version_3_4_1.version_description = "První update";
            version_3_4_1.version_name = "1.0.1";
            version_3_4_1.approval_state = Approval_state.approved;
            version_3_4_1.save();

            Model_BlockoBlockVersion version_3_4_2 = new Model_BlockoBlockVersion();
            version_3_4_2.blocko_block = blockoBlock_3_4;
            version_3_4_2.date_of_create = new Date();
            version_3_4_2.logic_json = "{}";
            version_3_4_2.design_json = "{}";
            version_3_4_2.version_description = "Druhý update";
            version_3_4_2.version_name = "1.0.2";
            version_3_4_2.approval_state = Approval_state.approved;
            version_3_4_2.save();


            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result grid_demo_data(){
        try {

            // Ochranná zarážka proti znovu vytvoření
            if(Model_TypeOfWidget.find.where().eq("name", "iOS Widgets").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

            Model_TypeOfWidget typeOfWidget_1 = new Model_TypeOfWidget();
            typeOfWidget_1.name =  "iOS Widgets";
            typeOfWidget_1.description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.";
            typeOfWidget_1.save();

            Model_TypeOfWidget typeOfWidget_2 = new Model_TypeOfWidget();
            typeOfWidget_2.name =  "Android Widgets";
            typeOfWidget_2.description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.";
            typeOfWidget_2.save();


            //**************************************************************************************************************


            //1
            Model_GridWidget gridWidget_1_1 = new Model_GridWidget();
            gridWidget_1_1.name = "Apple";
            gridWidget_1_1.author = Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            gridWidget_1_1.description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.";
            gridWidget_1_1.type_of_widget = typeOfWidget_1;
            gridWidget_1_1.save();

            Model_GridWidget gridWidget_1_2 = new Model_GridWidget();
            gridWidget_1_2.name = "Banana";
            gridWidget_1_2.author = Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            gridWidget_1_2.description = "Lorem ipsum di lasjdhflkj dshaflj  sadfsdfas dfsadf sad gsfgsdf sadfsd fas";
            gridWidget_1_2.type_of_widget = typeOfWidget_1;
            gridWidget_1_2.save();

            Model_GridWidget gridWidget_1_3 = new Model_GridWidget();
            gridWidget_1_3.name = "Orange";
            gridWidget_1_3.author = Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            gridWidget_1_3.description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.";
            gridWidget_1_3.type_of_widget = typeOfWidget_1;
            gridWidget_1_3.save();

            // 2
            Model_GridWidget gridWidget_2_1 = new Model_GridWidget();
            gridWidget_2_1.name = "Nice Widget";
            gridWidget_2_1.author = Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            gridWidget_2_1.description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.";
            gridWidget_2_1.type_of_widget = typeOfWidget_2;
            gridWidget_2_1.save();

            Model_GridWidget gridWidget_2_2 = new Model_GridWidget();
            gridWidget_2_2.name = "Ugly Widget";
            gridWidget_2_2.author = Model_Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            gridWidget_2_2.description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.";
            gridWidget_2_2.type_of_widget = typeOfWidget_2;
            gridWidget_2_2.save();


            //**************************************************************************************************************

            // 1_1
            Model_GridWidgetVersion version_1_1_1 = new Model_GridWidgetVersion();
            version_1_1_1.grid_widget = gridWidget_1_1;
            version_1_1_1.date_of_create = new Date();
            version_1_1_1.logic_json = "{}";
            version_1_1_1.design_json = "{}";
            version_1_1_1.version_description = "První update";
            version_1_1_1.version_name = "1.0.1";
            version_1_1_1.approval_state = Approval_state.approved;
            version_1_1_1.save();

            Model_GridWidgetVersion version_1_1_2 = new Model_GridWidgetVersion();
            version_1_1_2.grid_widget = gridWidget_1_1;
            version_1_1_2.date_of_create = new Date();
            version_1_1_2.logic_json = "{}";
            version_1_1_2.design_json = "{}";
            version_1_1_2.version_description = "První update";
            version_1_1_2.version_name = "1.0.1";
            version_1_1_2.approval_state = Approval_state.approved;
            version_1_1_2.save();

            // 1_2
            Model_GridWidgetVersion version_1_2_1 = new Model_GridWidgetVersion();
            version_1_2_1.grid_widget = gridWidget_1_2;
            version_1_2_1.date_of_create = new Date();
            version_1_2_1.logic_json = "{}";
            version_1_2_1.design_json = "{}";
            version_1_2_1.version_description = "První update";
            version_1_2_1.version_name = "1.0.1";
            version_1_2_1.approval_state = Approval_state.approved;
            version_1_2_1.save();

            Model_GridWidgetVersion version_1_2_2 = new Model_GridWidgetVersion();
            version_1_2_2.grid_widget = gridWidget_1_2;
            version_1_2_2.date_of_create = new Date();
            version_1_2_2.logic_json = "{}";
            version_1_2_2.design_json = "{}";
            version_1_2_2.version_description = "Druhý update";
            version_1_2_2.version_name = "1.0.2";
            version_1_2_2.approval_state = Approval_state.approved;
            version_1_2_2.save();

            // 1_3
            Model_GridWidgetVersion version_1_3_1 = new Model_GridWidgetVersion();
            version_1_3_1.grid_widget = gridWidget_1_3;
            version_1_3_1.date_of_create = new Date();
            version_1_3_1.logic_json = "{}";
            version_1_3_1.design_json = "{}";
            version_1_3_1.version_description = "První update";
            version_1_3_1.version_name = "1.0.1";
            version_1_3_1.approval_state = Approval_state.approved;
            version_1_3_1.save();

            Model_GridWidgetVersion version_1_3_2 = new Model_GridWidgetVersion();
            version_1_3_2.grid_widget = gridWidget_1_3;
            version_1_3_2.date_of_create = new Date();
            version_1_3_2.logic_json = "{}";
            version_1_3_2.design_json = "{}";
            version_1_3_2.version_description = "Druhý update";
            version_1_3_2.version_name = "1.0.2";
            version_1_3_2.approval_state = Approval_state.approved;
            version_1_3_2.save();

            // 2_1
            Model_GridWidgetVersion version_2_1_1 = new Model_GridWidgetVersion();
            version_2_1_1.grid_widget = gridWidget_2_1;
            version_2_1_1.date_of_create = new Date();
            version_2_1_1.logic_json = "{}";
            version_2_1_1.design_json = "{}";
            version_2_1_1.version_description = "První update";
            version_2_1_1.version_name = "1.0.1";
            version_2_1_1.approval_state = Approval_state.approved;
            version_2_1_1.save();

            Model_GridWidgetVersion version_2_1_2 = new Model_GridWidgetVersion();
            version_2_1_2.grid_widget = gridWidget_2_1;
            version_2_1_2.date_of_create = new Date();
            version_2_1_2.logic_json = "{}";
            version_2_1_2.design_json = "{}";
            version_2_1_2.version_description = "Prvnsafd -a.kshm fn.,mbs gjknbm akdfsm,.cxy ndfam,nkvxclůavcx namxyklnvdfsam ,cvklůdfsmv.lyům ,klnvyůmc,.í update";
            version_2_1_2.version_name = "1.0.2";
            version_2_1_2.approval_state = Approval_state.approved;
            version_2_1_2.save();

            // 2_2
            Model_GridWidgetVersion version_2_2_1 = new Model_GridWidgetVersion();
            version_2_2_1.grid_widget = gridWidget_2_2;
            version_2_2_1.date_of_create = new Date();
            version_2_2_1.logic_json = "{}";
            version_2_2_1.design_json = "{}";
            version_2_2_1.version_description = "První update";
            version_2_2_1.version_name = "1.0.1";
            version_2_2_1.approval_state = Approval_state.approved;
            version_2_2_1.save();

            Model_GridWidgetVersion version_2_2_2 = new Model_GridWidgetVersion();
            version_2_2_2.grid_widget = gridWidget_2_2;
            version_2_2_2.date_of_create = new Date();
            version_2_2_2.logic_json = "{}";
            version_2_2_2.design_json = "{}";
            version_2_2_2.version_description = "Druhý update";
            version_2_2_2.version_name = "1.0.2";
            version_2_2_2.approval_state = Approval_state.approved;
            version_2_2_2.save();

            Model_GridWidgetVersion version_2_2_3= new Model_GridWidgetVersion();
            version_2_2_3.grid_widget = gridWidget_2_2;
            version_2_2_3.date_of_create = new Date();
            version_2_2_3.logic_json = "{}";
            version_2_2_3.design_json = "{}";
            version_2_2_3.version_description = "Třetí update";
            version_2_2_3.version_name = "1.0.3";
            version_2_2_3.approval_state = Approval_state.approved;
            version_2_2_3.save();

            Model_GridWidgetVersion version_2_2_4 = new Model_GridWidgetVersion();
            version_2_2_4.grid_widget = gridWidget_2_2;
            version_2_2_4.date_of_create = new Date();
            version_2_2_4.logic_json = "{}";
            version_2_2_4.design_json = "{}";
            version_2_2_4.version_description = "Čtvrtý  update";
            version_2_2_4.version_name = "1.0.4";
            version_2_2_4.approval_state = Approval_state.approved;
            version_2_2_4.save();

            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result c_program_configuration(){
        try {

            // Ochranná zarážka proti znovu vytvoření
            if(Model_CProgram.find.where().eq("name", "Default C_Program for Yoda").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

            Model_TypeOfBoard yoda = Model_TypeOfBoard.find.where().eq("name", "Yoda G2").findUnique();

            Model_CProgram c_program_1 = new Model_CProgram();
            c_program_1.name = "Default C_Program for Yoda";
            c_program_1.type_of_board = yoda;

            c_program_1.description = "For first version of every program";
            c_program_1.date_of_create = new Date();
            c_program_1.default_program_type_of_board = yoda;
            c_program_1.save();

            Model_VersionObject version_object_1 = new Model_VersionObject();
            version_object_1.version_name = "1.0.0";
            version_object_1.version_description = "First default example Program";
            version_object_1.date_of_create = new Date();
            version_object_1.c_program = c_program_1;
            version_object_1.public_version = false;
            version_object_1.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content = Json.newObject();
            content.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content.set("user_files", null);
            content.set("external_libraries", null );

            // Content se nahraje na Azure
            Model_FileRecord.uploadAzure_Version(content.toString(), "code.json" , c_program_1.get_path() ,  version_object_1);
            version_object_1.update();
            version_object_1.compile_program_thread();

            yoda.default_program = c_program_1;
            yoda.default_program.default_main_version = version_object_1;
            version_object_1.default_version_program = yoda.default_program;
            version_object_1.update();
            yoda.update();



            Model_TypeOfBoard wireless = Model_TypeOfBoard.find.where().eq("name", "Wireless G2").findUnique();

            Model_CProgram c_program_2 = new Model_CProgram();
            c_program_2.name = "Default C_Program for Wireless Kit";
            c_program_2.description = "You can used that for prototyping";
            c_program_2.type_of_board = wireless;

            c_program_2.description = "For first version of every program";
            c_program_2.date_of_create = new Date();
            c_program_2.default_program_type_of_board = wireless;
            c_program_2.save();

            Model_VersionObject version_object_2 = new Model_VersionObject();
            version_object_2.version_name = "1.0.0";
            version_object_2.version_description = "First default example Program";
            version_object_2.date_of_create = new Date();
            version_object_2.c_program = c_program_2;
            version_object_2.public_version = false;
            version_object_2.save();


            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_2 = Json.newObject();
            content_2.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_2.set("user_files", null);
            content_2.set("external_libraries", null );

            // Content se nahraje na Azure
            Model_FileRecord.uploadAzure_Version(content.toString(), "code.json" , c_program_2.get_path() ,  version_object_2);
            version_object_2.update();
            version_object_2.compile_program_thread();

            wireless.default_program = c_program_2;
            wireless.default_program.default_main_version = version_object_2;

            version_object_2.default_version_program = wireless.default_program;

            version_object_2.update();
            wireless.update();


            Model_TypeOfBoard busKit = Model_TypeOfBoard.find.where().eq("name", "BUS G2").findUnique();

            Model_CProgram c_program_3 = new Model_CProgram();
            c_program_3.name = "Default C_Program for Bus Kit";
            c_program_3.type_of_board = busKit;

            c_program_3.description = "For first version of every program";
            c_program_3.date_of_create = new Date();
            c_program_3.default_program_type_of_board = busKit;
            c_program_3.save();

            Model_VersionObject version_object_3 = new Model_VersionObject();
            version_object_3.version_name = "1.0.0";
            version_object_3.version_description = "First default example Program";
            version_object_3.date_of_create = new Date();
            version_object_3.c_program = c_program_3;
            version_object_3.public_version = false;
            version_object_3.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_3 = Json.newObject();
            content_3.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_3.set("user_files", null);
            content_3.set("external_libraries", null );

            // Content se nahraje na Azure
            Model_FileRecord.uploadAzure_Version(content.toString(), "code.json" , c_program_3.get_path() ,  version_object_3);
            version_object_3.update();
            version_object_3.compile_program_thread();

            busKit.default_program = c_program_3;
            busKit.default_program.default_main_version = version_object_3;
            version_object_3.default_version_program = busKit.default_program;
            version_object_3.update();
            busKit.update();

            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result person_test_user(){
        try {

            if(Model_Person.find.where().eq("nick_name", "Pepíno").findUnique() != null ) return GlobalResult.result_BadRequest("Its Already done!");

            String uuid = UUID.randomUUID().toString().substring(0,4);

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
            product.general_tariff = Model_GeneralTariff.find.where().eq("identificator","alpha").findUnique();
            product.product_individual_name = "Pepkova velkolepá Alfa";
            product.active = true;  // Produkt jelikož je Aplha je aktivní - Alpha nebo Trial dojedou kvuli omezení času
            product.method = Payment_method.free;
            product.mode = Payment_mode.free;
            product.paid_until_the_day = new GregorianCalendar(2016, 12, 30).getTime();
            Model_PaymentDetails payment_details = new Model_PaymentDetails();
            payment_details.person = person;
            payment_details.company_account = false;
            payment_details.street = "Karlovo náměsí";
            payment_details.street_number = "457";
            payment_details.city = "Praha";
            payment_details.zip_code = "12000";
            payment_details.country = "Czech Republic";
            payment_details.product = product;
            product.payment_details = payment_details;
            product.save();

            for( Model_GeneralTariffExtensions e : Model_GeneralTariff.find.where().eq("identificator","alpha").findUnique().extensions_included ){
                e.products.add(product);
                e.update();
                product.extensions.add(e);
                product.update();
            }

            Model_Invoice invoice = new Model_Invoice();
            invoice.facturoid_invoice_id = 4023423L;
            invoice.invoice_number = "324234234";
            invoice.date_of_create = new Date();
            invoice.product = product;
            invoice.status = Payment_status.paid;
            invoice.method = Payment_method.credit_card;

            Model_InvoiceItem item_1 = new Model_InvoiceItem();
            item_1.currency = Currency.USD;
            item_1.name = "položka 1";
            item_1.quantity = 1L;
            item_1.unit_name = "service";
            item_1.unit_price = 530.00;
            invoice.invoice_items.add(item_1);

            Model_InvoiceItem item_2 = new Model_InvoiceItem();
            item_2.currency = Currency.USD;
            item_2.name = "Databáze";
            item_2.quantity = 5L;
            item_2.unit_name = "GB";
            item_2.unit_price = 99.00;
            invoice.invoice_items.add(item_2);

            invoice.save();

            Model_Invoice invoice_2 = new Model_Invoice();
            invoice_2.facturoid_invoice_id = 4023423L;
            invoice_2.invoice_number = "324234234";
            invoice_2.date_of_create = new Date();
            invoice_2.product = product;
            invoice_2.status = Payment_status.created_waited;
            invoice_2.method = Payment_method.credit_card;

            Model_InvoiceItem item_3 = new Model_InvoiceItem();
            item_3.currency = Currency.USD;
            item_3.name = "položka 1";
            item_3.quantity = 1L;
            item_3.unit_name = "service";
            item_3.unit_price = 530.00;
            invoice_2.invoice_items.add(item_3);

            Model_InvoiceItem item_4 = new Model_InvoiceItem();
            item_4.currency = Currency.USD;
            item_4.name = "Databáze";
            item_4.quantity = 5L;
            item_4.unit_name = "GB";
            item_4.unit_price = 99.00;
            invoice_2.invoice_items.add(item_4);

            invoice_2.save();

            // Vytvořím Projekty
            Model_Project project_1 = new Model_Project();
            project_1.product = product;
            project_1.name = "První velkolepý projekt";
            project_1.description = "Toto je Pepkův velkolepý testovací projekt primárně určen pro testování Blocko Programu, kde už má zaregistrovaný testovací HW";
            project_1.save();
            project_1.refresh();

            Model_ProjectParticipant participant_1 = new Model_ProjectParticipant();
            participant_1.person = person;
            participant_1.project = project_1;
            participant_1.state = Participant_status.owner;
            participant_1.save();
            System.err.println(Json.toJson(participant_1));

            Model_Project project_2 = new Model_Project();
            project_2.product = product;
            project_2.name = "Druhý prázdný testovací projekt";
            project_2.description = "Toto je Pepkův testovací projekt, kde nic ještě není";
            project_2.save();

            Model_ProjectParticipant participant_2 = new Model_ProjectParticipant();
            participant_2.person = person;
            participant_2.project = project_2;
            participant_2.state = Participant_status.owner;
            participant_2.save();

            Model_Project project_3 = new Model_Project();
            project_3.product = product;
            project_3.name = "Třetí prázdný testovací projekt";
            project_3.description = "Toto je Pepkův třetí super testovací projekt, kde nic ještě není a ten blázen se musel dlouhosáhle rozepsat v description??? To jako vážně? Jste na to připravený v designu???? ?";
            project_3.save();

            Model_ProjectParticipant participant_3 = new Model_ProjectParticipant();
            participant_3.person = person;
            participant_3.project = project_3;
            participant_3.state = Participant_status.owner;
            participant_3.save();

            // Zaregistruji pod ně Yody
            project_1.boards.add( Model_Board.find.where().eq("personal_description","Yoda A").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","Yoda B").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","Yoda C").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","Yoda D").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","Yoda E").findUnique());

            Model_Board yoda_1 = Model_Board.find.where().eq("personal_description","Yoda A").findUnique();
            yoda_1.project = project_1;
            project_1.private_instance.boards_in_virtual_instance.add(yoda_1);
            yoda_1.update();

            Model_Board yoda_2 = Model_Board.find.where().eq("personal_description","Yoda B").findUnique();
            yoda_2.project = project_1;
            project_1.private_instance.boards_in_virtual_instance.add(yoda_2);
            yoda_2.update();


            Model_Board yoda_3 = Model_Board.find.where().eq("personal_description","Yoda C").findUnique();
            yoda_3.project = project_1;
            project_1.private_instance.boards_in_virtual_instance.add(yoda_3);
            yoda_3.update();


            Model_Board yoda_4 = Model_Board.find.where().eq("personal_description","Yoda D").findUnique();
            yoda_4.project = project_1;
            project_1.private_instance.boards_in_virtual_instance.add(yoda_4);
            yoda_4.update();

            Model_Board yoda_5 = Model_Board.find.where().eq("personal_description","Yoda E").findUnique();
            yoda_5.project = project_1;
            project_1.private_instance.boards_in_virtual_instance.add(yoda_5);
            yoda_5.update();

            project_1.update();

            // Bezdráty a Bezdráty
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[1]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[2]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[3]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[4]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[5]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[6]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[7]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[8]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[9]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[11]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[12]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[13]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[14]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[15]").findUnique());
            project_1.boards.add( Model_Board.find.where().eq("personal_description","[16]").findUnique());


            Model_Board board_1 = Model_Board.find.where().eq("personal_description","[1]").findUnique();
            board_1.project = project_1;
            board_1.update();

            Model_Board board_2 = Model_Board.find.where().eq("personal_description","[2]").findUnique();
            board_2.project = project_1;
            board_2.update();

            Model_Board board_3 = Model_Board.find.where().eq("personal_description","[3]").findUnique();
            board_3.project = project_1;
            board_3.update();

            Model_Board board_4 = Model_Board.find.where().eq("personal_description","[4]").findUnique();
            board_4.project = project_1;
            board_4.update();

            Model_Board board_5 = Model_Board.find.where().eq("personal_description","[5]").findUnique();
            board_5.project = project_1;
            board_5.update();

            Model_Board board_6 = Model_Board.find.where().eq("personal_description","[6]").findUnique();
            board_6.project = project_1;
            board_6.update();

            Model_Board board_7 = Model_Board.find.where().eq("personal_description","[7]").findUnique();
            board_7.project = project_1;
            board_7.update();

            Model_Board board_8 = Model_Board.find.where().eq("personal_description","[8]").findUnique();
            board_8.project = project_1;
            board_8.update();

            Model_Board board_9 = Model_Board.find.where().eq("personal_description","[9]").findUnique();
            board_9.project = project_1;
            board_9.update();

            Model_Board board_10 = Model_Board.find.where().eq("personal_description","[10]").findUnique();
            board_10.project = project_1;
            board_10.update();

            Model_Board board_11 = Model_Board.find.where().eq("personal_description","[11]").findUnique();
            board_11.project = project_1;
            board_11.update();

            Model_Board board_12 = Model_Board.find.where().eq("personal_description","[12]").findUnique();
            board_12.project = project_1;
            board_12.update();

            Model_Board board_13 = Model_Board.find.where().eq("personal_description","[13]").findUnique();
            board_13.project = project_1;
            board_13.update();

            Model_Board board_14 = Model_Board.find.where().eq("personal_description","[14]").findUnique();
            board_14.project = project_1;
            board_14.update();

            Model_Board board_15 = Model_Board.find.where().eq("personal_description","[15]").findUnique();
            board_15.project = project_1;
            board_15.update();

            Model_Board board_16 = Model_Board.find.where().eq("personal_description","[16]").findUnique();
            board_16.project = project_1;
            board_16.update();


            // Vytvořím C_Programy YODA
            Model_CProgram c_program_1 = new Model_CProgram();
            c_program_1.date_of_create = new Date();
            c_program_1.name = "Defaultní program";
            c_program_1.type_of_board = Model_TypeOfBoard.find.where().eq("name", "Yoda G2").findUnique();
            c_program_1.description = "Tento program je určen na blikání s ledkou";
            c_program_1.project = project_1;
            c_program_1.save();


            // Vytvořím C_Programy YODA
            Model_CProgram c_program_2 = new Model_CProgram();
            c_program_2.date_of_create = new Date();
            c_program_2.name = "Hraní si s tlačítkem Pro Yodu";
            c_program_2.type_of_board = Model_TypeOfBoard.find.where().eq("name", "Yoda G2").findUnique();
            c_program_2.description = "Tento program je určen na testování tlačítka na yodovi";
            c_program_2.project = project_1;
            c_program_2.save();


            // Vytvořím C_Programy Bezdrát
            Model_CProgram c_program_3 = new Model_CProgram();
            c_program_3.date_of_create = new Date();
            c_program_3.name = "Tlačítko s ledkou na Bezdrátu";
            c_program_3.type_of_board = Model_TypeOfBoard.find.where().eq("name", "Wireless G2").findUnique();
            c_program_3.description = "Tento program je určen na testování tlačítka na bezdrátovém modulu";
            c_program_3.project = project_1;
            c_program_3.save();


            // Vytvořím C_Programy Drát
            Model_CProgram c_program_4 = new Model_CProgram();
            c_program_4.date_of_create = new Date();
            c_program_4.name = "Tlačítko s ledkou na BUS kitu";
            c_program_4.type_of_board = Model_TypeOfBoard.find.where().eq("name", "BUS G2").findUnique();
            c_program_4.description = "Tento program je určen na testování tlačítka na BUS modulu";
            c_program_4.project = project_1;
            c_program_4.save();


            // První verze C_Programu pro Yodu c_program_1
            Model_VersionObject version_c_program_1 = new Model_VersionObject();
            version_c_program_1.version_name = "Verze 0.0.1";
            version_c_program_1.version_description = "Když jem poprvé zkoušel blikat ledkou - Yoda";
            version_c_program_1.c_program = c_program_1;
            version_c_program_1.date_of_create = new Date();
            version_c_program_1.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_1 = Json.newObject();
            content_1.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_1.set("user_files", null);
            content_1.set("external_libraries", null);
            Model_FileRecord.uploadAzure_Version(content_1.toString(), "code.json", c_program_1.get_path(), version_c_program_1);
            version_c_program_1.compile_program_procedure();
            version_c_program_1.update();

            // Druhá verze verze C_Programu pro YODU c_program_1
            Model_VersionObject version_c_program_1_2 = new Model_VersionObject();
            version_c_program_1_2.version_name = "Verze 0.0.2";
            version_c_program_1_2.version_description = "Když jem podruhé zkoušel blikat ledkou - Yoda";
            version_c_program_1_2.c_program = c_program_1;
            version_c_program_1_2.date_of_create = new Date();
            version_c_program_1_2.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_1_2 = Json.newObject();
            content_1_2.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_1_2.set("user_files", null);
            content_1_2.set("external_libraries", null);
            Model_FileRecord.uploadAzure_Version(content_1_2.toString(), "code.json", c_program_1.get_path(), version_c_program_1_2);
            version_c_program_1_2.compile_program_procedure();
            version_c_program_1_2.update();


            // Druhá verze verze C_Programu pro YODU c_program_1
            Model_VersionObject version_c_program_2 = new Model_VersionObject();
            version_c_program_2.version_name = "Verze 0.0.1";
            version_c_program_2.version_description = "Když jiná věc pro Yodu poprvé";
            version_c_program_2.c_program = c_program_2;
            version_c_program_2.date_of_create = new Date();
            version_c_program_2.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_2 = Json.newObject();
            content_2.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_2.set("user_files", null);
            content_2.set("external_libraries", null);
            Model_FileRecord.uploadAzure_Version(content_2.toString(), "code.json", c_program_2.get_path(), version_c_program_2);
            version_c_program_2.compile_program_procedure();
            version_c_program_2.update();


            // První verze  C_Programu pro Wireles c_program_3
            Model_VersionObject version_c_program_3 = new Model_VersionObject();
            version_c_program_3.version_name = "Verze 0.0.1";
            version_c_program_3.version_description = "Když jem poprvé Wireles";
            version_c_program_3.c_program = c_program_3;
            version_c_program_3.date_of_create = new Date();
            version_c_program_3.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_3 = Json.newObject();
            content_3.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_3.set("user_files", null);
            content_3.set("external_libraries", null);
            Model_FileRecord.uploadAzure_Version(content_3.toString(), "code.json", c_program_3.get_path(), version_c_program_3);
            version_c_program_3.compile_program_procedure();
            version_c_program_3.update();


            // První verze  C_Programu pro BUS c_program_4
            Model_VersionObject version_c_program_4 = new Model_VersionObject();
            version_c_program_4.version_name = "Verze 0.0.1";
            version_c_program_4.version_description = "Když jem poprvé Drát";
            version_c_program_4.c_program = c_program_4;
            version_c_program_4.date_of_create = new Date();
            version_c_program_4.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_4 = Json.newObject();
            content_4.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_4.set("user_files", null);
            content_4.set("external_libraries", null);
            Model_FileRecord.uploadAzure_Version(content_4.toString(), "code.json", c_program_4.get_path(), version_c_program_4);
            version_c_program_4.compile_program_procedure();
            version_c_program_4.update();

            Model_TypeOfBlock typeOfBlock_1 = new Model_TypeOfBlock();
            typeOfBlock_1.name =  "Pepkovy Bloky 1";
            typeOfBlock_1.description = "Bla bla description that says nothing";
            typeOfBlock_1.project = project_1;
            typeOfBlock_1.save();

            Model_TypeOfBlock typeOfBlock_2 = new Model_TypeOfBlock();
            typeOfBlock_2.name =  "Pepkovy Bloky 2";
            typeOfBlock_2.description = "Bla bla description that says nothing";
            typeOfBlock_2.project = project_1;
            typeOfBlock_2.save();

            Model_BlockoBlock blockoBlock_1_1 = new Model_BlockoBlock();
            blockoBlock_1_1.author = person;
            blockoBlock_1_1.name = "Blocko block";
            blockoBlock_1_1.description = "m.n,a sldjkfbnlskjd bjsdnf jkbsjndafio bjkvc,mxnymf můiwljhkn bfm,mn.adsjlůxkbcvnymn klnaf m,mnbjlů§k nbasldfb,n jkl.lkn nmsgl,můfjk br,mn.fl kbmfkllykbv vkůljmyn,d.mckůlxůklxbvnm,dsf m.ylp§foigkljsadůjfndmsvoija kdsfvůljnkjb fkljgfbvclasgfbnlfagkbkcnlsgkfklndgdk an dsja";
            blockoBlock_1_1.type_of_block = typeOfBlock_1;
            blockoBlock_1_1.save();

            Model_BlockoBlock blockoBlock_1_2 = new Model_BlockoBlock();
            blockoBlock_1_2.author = person;
            blockoBlock_1_2.name = "Empty block";
            blockoBlock_1_2.description = "Lorem ipsum di lasjdhflkj dshaflj  sadfsdfas dfsadf sad gsfgsdf sadfsd fas";
            blockoBlock_1_2.type_of_block = typeOfBlock_1;
            blockoBlock_1_2.save();

            Model_BlockoBlockVersion version_1_1_1 = new Model_BlockoBlockVersion();
            version_1_1_1.blocko_block = blockoBlock_1_1;
            version_1_1_1.date_of_create = new Date();
            version_1_1_1.logic_json = "{}";
            version_1_1_1.design_json = "{}";
            version_1_1_1.version_description = "První verze";
            version_1_1_1.version_name = "1.0.1";
            version_1_1_1.approval_state = Approval_state.approved;
            version_1_1_1.save();

            Model_BlockoBlockVersion version_1_1_2 = new Model_BlockoBlockVersion();
            version_1_1_2.blocko_block = blockoBlock_1_1;
            version_1_1_2.date_of_create = new Date();
            version_1_1_2.logic_json = "{}";
            version_1_1_2.design_json = "{}";
            version_1_1_2.version_description = "Druhá verze";
            version_1_1_2.version_name = "1.0.2";
            version_1_1_2.approval_state = Approval_state.approved;
            version_1_1_2.save();

            Model_TypeOfWidget typeOfWidget_1 = new Model_TypeOfWidget();
            typeOfWidget_1.name =  "Pepkovy Widgety 1";
            typeOfWidget_1.description = "Bla bla description that says nothing";
            typeOfWidget_1.project = project_1;
            typeOfWidget_1.save();

            Model_TypeOfWidget typeOfWidget_2 = new Model_TypeOfWidget();
            typeOfWidget_2.name =  "Pepkovy Widgety 2";
            typeOfWidget_2.description = "Bla bla description that says nothing";
            typeOfWidget_2.project = project_1;
            typeOfWidget_2.save();

            Model_GridWidget gridWidget_1_1 = new Model_GridWidget();
            gridWidget_1_1.author = person;
            gridWidget_1_1.name = "Grid Widget";
            gridWidget_1_1.description = "m.n,a sldjkfbnlskjd bjsdnf jkbsjndafio bjkvc,mxnymf můiwljhkn bfm,mn.adsjlůxkbcvnymn klnaf m,mnbjlů§k nbasldfb,n jkl.lkn nmsgl,můfjk br,mn.fl kbmfkllykbv vkůljmyn,d.mckůlxůklxbvnm,dsf m.ylp§foigkljsadůjfndmsvoija kdsfvůljnkjb fkljgfbvclasgfbnlfagkbkcnlsgkfklndgdk an dsja";
            gridWidget_1_1.type_of_widget = typeOfWidget_1;
            gridWidget_1_1.save();

            Model_GridWidget gridWidget_1_2 = new Model_GridWidget();
            gridWidget_1_2.author = person;
            gridWidget_1_2.name = "Empty widget";
            gridWidget_1_2.description = "Lorem ipsum di lasjdhflkj dshaflj  sadfsdfas dfsadf sad gsfgsdf sadfsd fas";
            gridWidget_1_2.type_of_widget = typeOfWidget_1;
            gridWidget_1_2.save();

            Model_GridWidgetVersion grid_version_1_1_1 = new Model_GridWidgetVersion();
            grid_version_1_1_1.grid_widget = gridWidget_1_1;
            grid_version_1_1_1.date_of_create = new Date();
            grid_version_1_1_1.logic_json = "{}";
            grid_version_1_1_1.design_json = "{}";
            grid_version_1_1_1.version_description = "První verze";
            grid_version_1_1_1.version_name = "1.0.1";
            grid_version_1_1_1.approval_state = Approval_state.approved;
            grid_version_1_1_1.save();

            Model_GridWidgetVersion grid_version_1_1_2 = new Model_GridWidgetVersion();
            grid_version_1_1_2.grid_widget = gridWidget_1_1;
            grid_version_1_1_2.date_of_create = new Date();
            grid_version_1_1_2.logic_json = "{}";
            grid_version_1_1_2.design_json = "{}";
            grid_version_1_1_2.version_description = "Druhá verze";
            grid_version_1_1_2.version_name = "1.0.2";
            grid_version_1_1_2.approval_state = Approval_state.approved;
            grid_version_1_1_2.save();

            Model_MProject m_project = new Model_MProject();
            m_project.project = project_1;
            m_project.name = "Velkolepá kolekce terminálových přístupů";
            m_project.description = "Tak tady si pepa dělá všechny svoje super cool apky!!! Je to fakt mazec!! a V připadě updatu je autoincrement true - což znamená že systém v případě updatu lidem na teminálech updatuje verzi";
            m_project.date_of_create = new Date();
            m_project.save();

            Model_MProgram m_program_1 = new Model_MProgram();
            m_program_1.m_project = m_project;
            m_program_1.date_of_create = new Date();
            m_program_1.description = "Tohle bude peckový program jež spasí svět";
            m_program_1.name = "Program pro Dědu";
            m_program_1.save();


            Model_VersionObject m_program_version_object_1 = new Model_VersionObject();
            m_program_version_object_1.version_description = "Toto je první verze!";
            m_program_version_object_1.version_name = "1.0.0";
            m_program_version_object_1.m_program = m_program_1;
            m_program_version_object_1.public_version = false;
            m_program_version_object_1.qr_token = "qr_token_1";
            m_program_version_object_1.date_of_create = new Date();
            m_program_version_object_1.m_program_virtual_input_output = "{\"analogInputs\":{},\"digitalInputs\":{\"button_1\":{}},\"messageInputs\":{},\"analogOutputs\":{},\"digitalOutputs\":{\"button_1\":{}},\"messageOutputs\":{}}";

            m_program_version_object_1.save();

            ObjectNode content_m_program_version_1 = Json.newObject();
            content_m_program_version_1.put("m_code", "{\"device\":\"mobile\",\"screens\":{\"main\":[{\"widgets\":[{\"type\":\"TimeWidget\",\"boxBoundingBox\":{\"x\":2,\"y\":1,\"height\":1,\"width\":1},\"config\":{\"bgColor\":\"#32995F\",\"bgTransparent\":false}},{\"type\":\"LabelWidget\",\"boxBoundingBox\":{\"x\":5,\"y\":1,\"height\":1,\"width\":1},\"config\":{\"text\":\"The Grid\",\"textSize\":100,\"bgColor\":\"#32995F\",\"bgTransparent\":false}},{\"type\":\"ButtonWidget\",\"boxBoundingBox\":{\"x\":0,\"y\":0,\"height\":5,\"width\":2},\"config\":{\"ioName\":\"button_1\",\"text\":\"Byzance!\",\"textSize\":100,\"bgColor\":\"#32995F\",\"bgTransparent\":false}}]}]}}");

            Model_FileRecord.uploadAzure_Version(content_m_program_version_1.toString(), "m_program.json" , m_program_1.get_path() ,  m_program_version_object_1);
            m_program_version_object_1.update();

            Model_MProgram m_program_2 = new Model_MProgram();
            m_program_2.m_project = m_project;
            m_program_2.date_of_create = new Date();
            m_program_2.description = "Fůůů nářez od babičky";
            m_program_2.name = "Program pro babičku";
            m_program_2.save();

            Model_VersionObject m_program_version_object_2 = new Model_VersionObject();
            m_program_version_object_2.version_description = "Toto je první verze!";
            m_program_version_object_2.version_name = "1.0.0";
            m_program_version_object_2.m_program = m_program_2;
            m_program_version_object_2.public_version = false;
            m_program_version_object_2.qr_token = "qr_token_2";
            m_program_version_object_2.date_of_create = new Date();
            m_program_version_object_2.m_program_virtual_input_output = "{\"analogInputs\":{},\"digitalInputs\":{\"button_1\":{}},\"messageInputs\":{},\"analogOutputs\":{},\"digitalOutputs\":{\"button_1\":{}},\"messageOutputs\":{}}";
            m_program_version_object_2.save();

            ObjectNode content_m_program_version_2 = Json.newObject();
            content_m_program_version_2.put("m_code", "{\"device\":\"mobile\",\"screens\":{\"main\":[{\"widgets\":[{\"type\":\"TimeWidget\",\"boxBoundingBox\":{\"x\":2,\"y\":1,\"height\":1,\"width\":1},\"config\":{\"bgColor\":\"#32995F\",\"bgTransparent\":false}},{\"type\":\"LabelWidget\",\"boxBoundingBox\":{\"x\":5,\"y\":1,\"height\":1,\"width\":1},\"config\":{\"text\":\"The Grid\",\"textSize\":100,\"bgColor\":\"#32995F\",\"bgTransparent\":false}},{\"type\":\"ButtonWidget\",\"boxBoundingBox\":{\"x\":0,\"y\":0,\"height\":5,\"width\":2},\"config\":{\"ioName\":\"button_1\",\"text\":\"Byzance!\",\"textSize\":100,\"bgColor\":\"#32995F\",\"bgTransparent\":false}}]}]}}");

            Model_FileRecord.uploadAzure_Version(content_m_program_version_2.toString(), "m_program.json" , m_program_2.get_path() ,  m_program_version_object_2);
            m_program_version_object_2.update();





            // První verze B_Programu - Pro instanc Yoda E a Ci!
            Model_BProgram b_program_1 = new Model_BProgram();
            b_program_1.name = "První blocko program";
            b_program_1.description = "Blocko program je úžasná věc když funguje... a tady v tomto progtramu už je připravený i HW!!!!";
            b_program_1.date_of_create = new Date();
            b_program_1.project = project_1;
            b_program_1.save();

            Model_VersionObject instace_version = Model_VersionObject.find.where().eq("b_program.name","První blocko program").findUnique();

            Model_VersionObject version_b_program_1 = new Model_VersionObject();
            version_b_program_1.version_name = "Blocko Verze č.1";
            version_b_program_1.version_description = "Snažím se tu dělat veklé věci";
            version_b_program_1.date_of_create = new Date();
            version_b_program_1.b_program = b_program_1;

            version_b_program_1.save();
            version_b_program_1.refresh();


            // M Program
            Model_MProjectProgramSnapShot snap_version_b_program_1 = new Model_MProjectProgramSnapShot();
            snap_version_b_program_1.m_project = m_project;
            snap_version_b_program_1.version_objects_program.add(m_program_version_object_1);
            snap_version_b_program_1.version_objects_program.add(m_program_version_object_2);
            snap_version_b_program_1.instance_versions.add(version_b_program_1);

            snap_version_b_program_1.save();

            // Instance 1 (TOM - YODA E a Yoda C)
            Model_BProgramHwGroup group_1 = new Model_BProgramHwGroup();
            group_1.save();
            group_1.refresh();

            // Main Boad - Yoda
            Model_BPair main_1 = new Model_BPair();
            main_1.board = Model_Board.find.where().eq("personal_description", "Yoda E").findUnique();
            main_1.c_program_version = version_c_program_1;
            main_1.main_board_pair = group_1;
            main_1.save();

            // Bus
            Model_BPair device_16 = new Model_BPair();
            device_16.board = Model_Board.find.where().eq("personal_description", "[16]").findUnique();
            device_16.c_program_version = version_c_program_4;
            device_16.device_board_pair = group_1;
            device_16.save();

            // Bus
            Model_BPair device_15 = new Model_BPair();
            device_15.board = Model_Board.find.where().eq("personal_description", "[15]").findUnique();
            device_15.c_program_version = version_c_program_4;
            device_15.device_board_pair = group_1;
            device_15.save();

            // Bus
            Model_BPair device_2 = new Model_BPair();
            device_2.board = Model_Board.find.where().eq("personal_description", "[2]").findUnique();
            device_2.c_program_version = version_c_program_4;
            device_2.device_board_pair = group_1;
            device_2.save();

            // Bezdrát
            Model_BPair device_13 = new Model_BPair();
            device_13.board = Model_Board.find.where().eq("personal_description", "[13]").findUnique();
            device_13.c_program_version = version_c_program_3;
            device_13.device_board_pair = group_1;
            device_13.save();

            // Bezdrát
            Model_BPair device_14 = new Model_BPair();
            device_14.board = Model_Board.find.where().eq("personal_description", "[14]").findUnique();
            device_14.c_program_version = version_c_program_3;
            device_14.device_board_pair = group_1;
            device_14.save();


            Model_BProgramHwGroup group_2 = new Model_BProgramHwGroup();
            group_2.save();
            group_2.refresh();

            // Main Board - Yoda
            Model_BPair main_2 = new Model_BPair();
            main_2.board = Model_Board.find.where().eq("personal_description", "Yoda C").findUnique();
            main_2.c_program_version = version_c_program_2;
            main_2.main_board_pair = group_2;
            main_2.save();

            version_b_program_1.b_program_hw_groups.add(group_1);
            version_b_program_1.b_program_hw_groups.add(group_2);
            version_b_program_1.update();

            Model_FileRecord.uploadAzure_Version("Blocko Program zde!", "program.js", b_program_1.get_path() , version_b_program_1);


            // Druhý B_Program - Pro instanci Yoda B!
            // Instance 2 - Martinův Yoda
            Model_BProgram b_program_2 = new Model_BProgram();
            b_program_2.name = "Druhý blocko program";
            b_program_2.description = "Tento program má sloužit Martinovi";
            b_program_2.date_of_create = new Date();
            b_program_2.project = project_1;
            b_program_2.save();

            // První verze B_Programu - Pro instanci!
            Model_VersionObject version_b_program_2 = new Model_VersionObject();
            version_b_program_2.version_name = "Blocko Verze č.1";
            version_b_program_2.version_description = "Snažím se tu dělat veklé věci";
            version_b_program_2.date_of_create = new Date();
            version_b_program_2.b_program = b_program_2;

            version_b_program_2.save();
            version_b_program_2.refresh();

            // M Program
            Model_MProjectProgramSnapShot snap_version_b_program_2 = new Model_MProjectProgramSnapShot();
            snap_version_b_program_2.m_project = m_project;
            snap_version_b_program_2.version_objects_program.add(m_program_version_object_1);
            snap_version_b_program_2.version_objects_program.add(m_program_version_object_2);
            snap_version_b_program_2.instance_versions.add(version_b_program_2);

            snap_version_b_program_2.save();

            Model_BProgramHwGroup group_3 = new Model_BProgramHwGroup();
            group_3.save();
            group_3.refresh();

            Model_BPair main_3 = new Model_BPair();
            main_3.board = Model_Board.find.where().eq("personal_description", "Yoda B").findUnique();
            main_3.c_program_version = version_c_program_1;
            main_3.main_board_pair = group_3;
            main_3.save();

            version_b_program_2.b_program_hw_groups.add(group_3);
            version_b_program_2.update();



            // Instance 3 - Viktorův Yoda
            Model_BProgram b_program_3 = new Model_BProgram();
            b_program_3.name = "Třetí blocko program";
            b_program_3.description = "Tento program má sloužit Viktorovi";
            b_program_3.date_of_create = new Date();
            b_program_3.project = project_1;
            b_program_3.save();

            // První verze B_Programu - Pro instanci!
            Model_VersionObject version_b_program_3 = new Model_VersionObject();
            version_b_program_3.version_name = "Blocko Verze č.1";
            version_b_program_3.version_description = "Snažím se tu dělat veklé věci";
            version_b_program_3.date_of_create = new Date();
            version_b_program_3.b_program = b_program_3;
            version_b_program_3.save();
            version_b_program_3.refresh();

            // M Program
            Model_MProjectProgramSnapShot snap_version_b_program_3 = new Model_MProjectProgramSnapShot();
            snap_version_b_program_3.m_project = m_project;
            snap_version_b_program_3.version_objects_program.add(m_program_version_object_1);
            snap_version_b_program_3.version_objects_program.add(m_program_version_object_2);
            snap_version_b_program_3.instance_versions.add(version_b_program_3);
            snap_version_b_program_3.save();


            Model_BProgramHwGroup group_4 = new Model_BProgramHwGroup();
            group_4.save();
            group_4.refresh();

            // Main A - yoda
            Model_BPair main_4 = new Model_BPair();
            main_4.board = Model_Board.find.where().eq("personal_description", "Yoda A").findUnique();
            main_4.c_program_version = version_c_program_1;
            main_4.main_board_pair = group_4;
            main_4.save();


            // BUS
            Model_BPair device_1 = new Model_BPair();
            device_1.board = Model_Board.find.where().eq("personal_description", "[1]").findUnique();
            device_1.c_program_version = version_c_program_4;
            device_1.device_board_pair = group_4;
            device_1.save();


            // BUS
            Model_BPair device_3 = new Model_BPair();
            device_3.board = Model_Board.find.where().eq("personal_description", "[3]").findUnique();
            device_3.c_program_version = version_c_program_4;
            device_3.device_board_pair = group_4;
            device_3.save();


            // BUS
            Model_BPair device_4 = new Model_BPair();
            device_4.board = Model_Board.find.where().eq("personal_description", "[4]").findUnique();
            device_4.c_program_version = version_c_program_4;
            device_4.device_board_pair = group_4;
            device_4.save();

            // BUS
            Model_BPair device_5 = new Model_BPair();
            device_5.board = Model_Board.find.where().eq("personal_description", "[5]").findUnique();
            device_5.c_program_version = version_c_program_4;
            device_5.device_board_pair = group_4;
            device_5.save();


            // BUS
            Model_BPair device_8 = new Model_BPair();
            device_8.board = Model_Board.find.where().eq("personal_description", "[8]").findUnique();
            device_8.c_program_version = version_c_program_4;
            device_8.device_board_pair = group_4;
            device_8.save();

            // BUS
            Model_BPair device_9 = new Model_BPair();
            device_9.board = Model_Board.find.where().eq("personal_description", "[9]").findUnique();
            device_9.c_program_version = version_c_program_4;
            device_9.device_board_pair = group_4;
            device_9.save();


            // Bezdrát
            Model_BPair device_6 = new Model_BPair();
            device_6.board = Model_Board.find.where().eq("personal_description", "[6]").findUnique();
            device_6.c_program_version = version_c_program_3;
            device_6.device_board_pair = group_4;
            device_6.save();

            // Bezdrát
            Model_BPair device_7 = new Model_BPair();
            device_7.board = Model_Board.find.where().eq("personal_description", "[7]").findUnique();
            device_7.c_program_version = version_c_program_3;
            device_7.device_board_pair = group_4;
            device_7.save();

            version_b_program_3.b_program_hw_groups.add(group_4);
            version_b_program_3.update();



            // Instance 4 - Davidův Yoda - Nefuknční Yoda


            // Instance 3 - Voktorův Yoda
            Model_BProgram b_program_4 = new Model_BProgram();
                b_program_4.name = "Čtvrtý blocko program - Určený pro Yodu D";
                b_program_4.description = "Tento program má sloužit Davidovi";
                b_program_4.date_of_create = new Date();
                b_program_4.project = project_1;
                b_program_4.save();

            // První verze B_Programu - Pro instanci!
            Model_VersionObject version_b_program_4 = new Model_VersionObject();
                version_b_program_4.version_name = "Blocko Verze č.1";
                version_b_program_4.version_description = "Snažím se tu dělat veklé věci";
                version_b_program_4.date_of_create = new Date();
                version_b_program_4.b_program = b_program_4;
                version_b_program_4.save();
                version_b_program_4.refresh();

            // M Program
            Model_MProjectProgramSnapShot snap_version_b_program_4 = new Model_MProjectProgramSnapShot();
            snap_version_b_program_4.m_project = m_project;
            snap_version_b_program_4.version_objects_program.add(m_program_version_object_1);
            snap_version_b_program_4.version_objects_program.add(m_program_version_object_2);
            snap_version_b_program_4.instance_versions.add(version_b_program_4);
            snap_version_b_program_4.save();

            Model_BProgramHwGroup group_5 = new Model_BProgramHwGroup();
            group_5.save();
            group_5.refresh();

            Model_BPair main_5 = new Model_BPair();
            main_5.board = Model_Board.find.where().eq("personal_description", "Yoda D").findUnique();
            main_5.c_program_version = version_c_program_1;
            main_5.main_board_pair = group_5;
            main_5.save();

            version_b_program_4.b_program_hw_groups.add(group_5);
            version_b_program_4.update();

            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result person_project_and_programs(){
        try {

            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result person_instancies(){
        try {

            Model_VersionObject instace_version = Model_VersionObject.find.where().eq("b_program.name","První blocko program").findUnique();

            Model_HomerInstanceRecord record = new Model_HomerInstanceRecord();
            record.main_instance_history = instace_version.b_program.instance;
            record.actual_running_instance = instace_version.b_program.instance;
            record.version_object = instace_version;
            record.date_of_created = new Date();
            record.save();

            record.main_instance_history.add_instance_to_server();


            Model_VersionObject instace_version_2 = Model_VersionObject.find.where().eq("b_program.name","Druhý blocko program").findUnique();

            Model_HomerInstanceRecord record_2 = new Model_HomerInstanceRecord();
            record_2.main_instance_history = instace_version_2.b_program.instance;
            record_2.actual_running_instance = instace_version_2.b_program.instance;
            record_2.version_object = instace_version_2;
            record_2.date_of_created = new Date();
            record_2.save();

            record_2.main_instance_history.add_instance_to_server();


            Model_VersionObject instace_version_3 = Model_VersionObject.find.where().eq("b_program.name","Třetí blocko program").findUnique();

            Model_HomerInstanceRecord record_3 = new Model_HomerInstanceRecord();
            record_3.main_instance_history = instace_version_3.b_program.instance;
            record_3.actual_running_instance = instace_version_3.b_program.instance;
            record_3.version_object = instace_version_3;
            record_3.date_of_created = new Date();
            record_3.save();

            record_3.main_instance_history.add_instance_to_server();


            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }






}
