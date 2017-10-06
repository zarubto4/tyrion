package utilities.demo_data;

import com.avaje.ebean.Model;
import io.swagger.annotations.Api;
import models.*;
import play.Application;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.enums.*;
import utilities.logger.Class_Logger;
import utilities.logger.Server_Logger;
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Api(value = "Dashboard Private Api", hidden = true)
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
        result = this.type_of_board();
        result = this.external_servers();
        result = this.basic_tariffs();
        result = this.person_test_user();
        result = this.garfield();

        return result;
    }

    public Result garfield() {
        try{

            System.out.println("garfield()");

            if (Model_Garfield.find.where().eq("name", "Byzance 1").findUnique() != null)
                return GlobalResult.result_badRequest("Its Already done!");

            Model_Garfield garfield = new Model_Garfield();

            garfield.name = "Garfield";
            garfield.description = "Test Garfield";
            garfield.hardware_tester_id = "G1_1";
            garfield.print_label_id_1 =  279211;  // 12 mm
            garfield.print_label_id_2 =  279211;  // 24 mm
            garfield.print_sticker_id =  279211;  // 65 mm


            Model_TypeOfBoard typeOfBoard = Model_TypeOfBoard.find.where().eq("name", "IODA G3").findUnique();
            Model_Producer producer = Model_Producer.find.where().eq("name", "Byzance ltd").findUnique();


            garfield.type_of_board_id = typeOfBoard.id;
            garfield.producer_id = producer.id.toString();

            garfield.save();

            return GlobalResult.result_ok();

        }catch (Exception e){
            return Server_Logger.result_internalServerError(e, request());
        }
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

            // Nastavím Type of Boards - YODA
            Model_TypeOfBoard typeOfBoard_2 = new Model_TypeOfBoard();
            typeOfBoard_2.name = "IODA G3";
            typeOfBoard_2.description = " Ioda - Master Board with Ethernet and Wifi - third generation";
            typeOfBoard_2.compiler_target_name = "BYZANCE_YODAG3E";
            typeOfBoard_2.revision = "12/2015 V1.0 #0000";
            typeOfBoard_2.processor = processor_1;
            typeOfBoard_2.producer = producer;
            typeOfBoard_2.connectible_to_internet = true;
            typeOfBoard_2.features.add(ethernet);
            typeOfBoard_2.features.add(wifi);
            typeOfBoard_2.save();


            // Vytvoříme defaultní C_Program pro snížení počtu kroků pro nastavení desky
            Model_CProgram c_program_2 = new Model_CProgram();
            c_program_2.name =  typeOfBoard_2.name + " default program";
            c_program_2.description = "Default program for this device type";
            c_program_2.type_of_board_default = typeOfBoard_2;
            c_program_2.type_of_board =  typeOfBoard_2;
            c_program_2.publish_type  = Enum_Publishing_type.default_main_program;
            c_program_2.save();

            typeOfBoard_2.refresh();

            // Vytvoříme testovací C_Program pro snížení počtu kroků pro nastavení desky
            Model_CProgram c_program_test_2 = new Model_CProgram();
            c_program_test_2.name =  typeOfBoard_2.name + " test program";
            c_program_test_2.description = "Test program for this device type";
            c_program_test_2.type_of_board_test = typeOfBoard_2;
            c_program_test_2.type_of_board =  typeOfBoard_2;
            c_program_test_2.publish_type  = Enum_Publishing_type.default_test_program;
            c_program_test_2.save();

            typeOfBoard_2.refresh();


            // Prototype Collection from Pragoboard
            Model_TypeOfBoard_Batch batch_proto = new Model_TypeOfBoard_Batch();
            batch_proto.type_of_board = typeOfBoard_2;
            batch_proto.revision = "Test Private Collection";
            batch_proto.production_batch = "First Try";
            batch_proto.date_of_assembly = "12.6.2017";
            batch_proto.pcb_manufacture_name = "PragoBoard s.r.o.";
            batch_proto.pcb_manufacture_id = "25615149";
            batch_proto.assembly_manufacture_name = "Byzance IoT Solution s.r.o";
            batch_proto.assembly_manufacture_id = "Not Know";
            batch_proto.customer_product_name = "YODA G3 - Ethernet";
            batch_proto.customer_company_name = "Byzance LTD";
            batch_proto.customer_company_made_description = "Best Company in World!";
            batch_proto.mac_address_start = 210006720901120L;
            batch_proto.mac_address_end = 210006720901129L;
            batch_proto.ean_number = 210006720901124L;
            batch_proto.save();

            // Prototype Collection from PCB Benešov
            Model_TypeOfBoard_Batch batch_test = new Model_TypeOfBoard_Batch();
            batch_test.type_of_board = typeOfBoard_2;
            batch_test.revision = "VF250717";
            batch_test.production_batch = "1000001 - Test Collection";
            batch_test.date_of_assembly = "27.9.2017";
            batch_test.pcb_manufacture_name = "PCB Benešov a.s";
            batch_test.pcb_manufacture_id = "45147698";
            batch_test.assembly_manufacture_name = "TTC TELEKOMUNIKACE, s.r.o.";
            batch_test.assembly_manufacture_id = "41194403";
            batch_test.customer_product_name = "YODA G3 - Ethernet";
            batch_test.customer_company_name = "Byzance LTD";
            batch_test.customer_company_made_description = "Best Company in World!";
            batch_test.mac_address_start = 210006720901136L;
            batch_test.mac_address_end   = 210006720901155L;
            batch_test.ean_number        = 210006720901139L;
            batch_test.save();

            // Prototype Collection from PCB Benešov
            // Prototype Collection from PCB Benešov
            Model_TypeOfBoard_Batch batch_final_first = new Model_TypeOfBoard_Batch();
            batch_final_first.type_of_board = typeOfBoard_2;
            batch_final_first.revision = "VF250717";
            batch_final_first.production_batch = "1000001 - Test Collection";
            batch_final_first.date_of_assembly = "27.9.2017";
            batch_final_first.pcb_manufacture_name = "PCB Benešov a.s";
            batch_final_first.pcb_manufacture_id = "45147698";
            batch_final_first.assembly_manufacture_name = "TTC TELEKOMUNIKACE, s.r.o.";
            batch_final_first.assembly_manufacture_id = "41194403";
            batch_final_first.customer_product_name = "YODA G3 - Ethernet";
            batch_final_first.customer_company_name = "Byzance LTD";
            batch_final_first.customer_company_made_description = "Best Company in World!";
            batch_final_first.mac_address_start = 210006720901136L;
            batch_final_first.mac_address_end   = 210006720901155L;
            batch_final_first.ean_number        = 210006720901139L;
            // batch_final_first.save(); Odkomentovat s finální produkcí


            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }


    //------------------------------------------------------------------------------------------------------------------

    public Result external_servers() {
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
            cloud_server_1.hash_certificate = "aaaaaaaaaaaaaaa";
            cloud_server_1.connection_identificator = "bbbbbbbbbbbbbbb";
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

            // Nastavím kompilační servery
            Model_CompilationServer compilation_server_1 = new Model_CompilationServer();
            compilation_server_1.personal_server_name = "Perseus";
            compilation_server_1.hash_certificate = "test";
            compilation_server_1.connection_identificator = "test";
            compilation_server_1.save();

            Model_CompilationServer compilation_server_2 = new Model_CompilationServer();
            compilation_server_2.personal_server_name = "Pegas";
            compilation_server_2.save();

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
            tariff_1.business_model = Enum_BusinessModel.alpha;
            tariff_1.name = "Alfa account";
            tariff_1.description = "Unlimited account for testing";
            tariff_1.identifier = "alpha";

            tariff_1.color = "blue";
            tariff_1.credit_for_beginning = 0L;

            tariff_1.company_details_required = false;
            tariff_1.payment_details_required = false;

            tariff_1.save();
            tariff_1.refresh();

            Model_ProductExtension extensions_1 = new Model_ProductExtension();
            extensions_1.name = "Extension 1";
            extensions_1.description = "description extension 1";
            extensions_1.type = Enum_ExtensionType.project;
            extensions_1.active = true;
            extensions_1.removed = false;
            extensions_1.color = "blue-madison";
            extensions_1.tariff_included = tariff_1;
            extensions_1.configuration = "{\"price\":1000,\"count\":100}";
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

            return GlobalResult.result_ok();

        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }
}