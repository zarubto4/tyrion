package utilities.demo_data;

import com.typesafe.config.Config;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.swagger.annotations.Api;
import models.*;
import play.Environment;
import play.libs.ws.WSClient;
import play.mvc.Result;
import utilities.enums.BusinessModel;
import utilities.enums.ExtensionType;
import utilities.enums.HomerType;
import utilities.enums.ProgramType;
import exceptions.NotFoundException;
import utilities.logger.Logger;
import utilities.logger.YouTrack;
import utilities.permission.PermissionService;
import utilities.scheduler.SchedulerService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;


@Api(value = "Dashboard Private Api", hidden = true)
public class Utilities_Demo_data_Controller extends _BaseController {

// LOGGER ##############################################################################################################

    private static final Logger terminal_logger = new Logger(Utilities_Demo_data_Controller.class);



// CONTROLLER CONFIGURATION ############################################################################################

    @javax.inject.Inject
    public Utilities_Demo_data_Controller(Environment environment, WSClient ws, _BaseFormFactory formFactory, YouTrack youTrack, Config config, SchedulerService scheduler, PermissionService permissionService) {
        super(environment, ws, formFactory, youTrack, config, scheduler, permissionService);
    }

// CONTROLLER CONTENT ##################################################################################################

    public Result test() {
        try {

            terminal_logger.trace("test:: Demo_Data_Controller :: test :: start");

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    /*
        Slouží primárně k vytváření demo dat při vývoji - je nutné pamatovat že změnou struktury objektů
        se nemění struktura vytvářených dat. Což je poměrně pracné na opravu při velkých úpravách.

        Každopádně každý vývojář by měl do struktury doplnit demo data své práce aby se vždy dalo od ní odpíchnout dál.

        Demodata je povolené tvořit jen a pouze v "Developer modu"!!!!!!!!!!
     */
    public Result all_for_becki() {

        this.producers();
        this.hardwareType();
        this.external_servers();
        this.basic_tariffs();
        this.person_test_user();
        this.garfield();

        return ok();
    }

    public Result garfield() {
        try {

            terminal_logger.trace("garfield:: garfield()");

            try {
                Model_Garfield.find.query().where().eq("name", "Garfield").findOne();
            } catch (NotFoundException e) {
                Model_Garfield garfield = new Model_Garfield();

                garfield.name = "Garfield";
                garfield.description = "Test Garfield";
                garfield.hardware_tester_id = "G1_1";
                garfield.print_label_id_1 =  279211;  // 12 mm
                garfield.print_label_id_2 =  279211;  // 24 mm
                garfield.print_sticker_id =  279211;  // 65 mm

                Model_HardwareType hardwareType = Model_HardwareType.find.query().where().eq("name", "IODA G3").findOne();
                Model_Producer producer = Model_Producer.find.query().where().eq("name", "Byzance ltd").findOne();

                garfield.hardware_type_id = hardwareType.id;
                garfield.producer_id = producer.id;

                garfield.save();
            }

            return ok();

        } catch (Exception e) {
            return controllerServerError(e);
        }
    }



    public Result producers() {
        try {

            try {
                Model_Producer.find.query().where().eq("name", "Byzance ltd").findOne();
            } catch (NotFoundException e) {
                Model_Producer producer = new Model_Producer();
                producer.name = "Byzance ltd";
                producer.description = "Developed with love from Byzance";
                producer.save();
            }

            return ok();
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    public Result hardwareType() {
        try {

            try {
                Model_Processor.find.query().where().eq("name", "ARM STM32 FR17").findOne();
                return ok();
            } catch (NotFoundException e) {
                // Nothing
            }

            Model_HardwareFeature features_i2c = new Model_HardwareFeature();
            features_i2c.name = "i2c";
            features_i2c.save();

            Model_HardwareFeature wifi = new Model_HardwareFeature();
            wifi.name = "wifi";
            wifi.save();

            Model_HardwareFeature ethernet = new Model_HardwareFeature();
            ethernet.name = "ethernet";
            ethernet.save();

            Model_HardwareFeature bus = new Model_HardwareFeature();
            bus.name = "bus";
            bus.save();

            Model_HardwareFeature wireless = new Model_HardwareFeature();
            wireless.name = "wireless";
            wireless.save();

            // Ochranná zarážka proti znovu vytvoření
            Model_Producer producer = Model_Producer.find.query().where().eq("name", "Byzance ltd").findOne();

            // Nastavím Processor - YODA
            Model_Processor processor_1 = new Model_Processor();
            processor_1.name = "ARM STM32 FR17";
            processor_1.description = "VET6 HPABT VQ KOR HP501";
            processor_1.processor_code = "STM32FR17";
            processor_1.speed = 3000;
            processor_1.save();

            // Nastavím Type of Hardware - YODA
            Model_HardwareType hardwareType = new Model_HardwareType();
            hardwareType.name = "IODA G3";
            hardwareType.description = " Ioda - Master Board with Ethernet and Wifi - third generation";
            hardwareType.compiler_target_name = "BYZANCE_IODAG3E";
            hardwareType.processor = processor_1;
            hardwareType.producer = producer;
            hardwareType.connectible_to_internet = true;

            if(hardwareType.features == null) hardwareType.features = new ArrayList<>();
            hardwareType.features.add(ethernet);
            hardwareType.features.add(wifi);
            hardwareType.save();


            // Vytvoříme defaultní C_Program pro snížení počtu kroků pro nastavení desky
            Model_CProgram cProgram = new Model_CProgram();
            cProgram.name =  hardwareType.name + " default program";
            cProgram.description = "Default program for this device type";
            cProgram.hardware_type_default = hardwareType;
            cProgram.hardware_type = hardwareType;
            cProgram.publish_type = ProgramType.DEFAULT_MAIN;
            cProgram.save();

            hardwareType.refresh();

            // Vytvoříme testovací C_Program pro snížení počtu kroků pro nastavení desky
            Model_CProgram c_program_test_2 = new Model_CProgram();
            c_program_test_2.name =  hardwareType.name + " test program";
            c_program_test_2.description = "Test program for this device type";
            c_program_test_2.hardware_type_test = hardwareType;
            c_program_test_2.hardware_type = hardwareType;
            c_program_test_2.publish_type = ProgramType.DEFAULT_TEST;
            c_program_test_2.save();

            hardwareType.refresh();


            return ok();
        } catch (Exception e) {
            return internalServerError(e);
        }
    }


    //------------------------------------------------------------------------------------------------------------------

    public Result external_servers() {
        try {

            try {
                Model_HomerServer.find.query().where().eq("name", "Alfa").findOne();
                return ok();
            } catch (NotFoundException e) {
                // Nothing
            }

            // Nasstavím Homer servery
            Model_HomerServer cloud_server_1 = new Model_HomerServer();
            cloud_server_1.name = "Alfa";
            cloud_server_1.server_url = "localhost";
            cloud_server_1.grid_port = 8503;
            cloud_server_1.mqtt_port = 1881;
            cloud_server_1.web_view_port = 8502;
            cloud_server_1.hardware_logger_port = 8505;
            cloud_server_1.server_type = HomerType.MAIN;
            cloud_server_1.connection_identifier = "aaaaaaaaaaaaaaa";
            cloud_server_1.hash_certificate = "bbbbbbbbbbbbbbb";
            cloud_server_1.save();

            Model_HomerServer cloud_server_2 = new Model_HomerServer();
            cloud_server_2.name = "Hydra";
            cloud_server_2.server_url = "localhost";
            cloud_server_2.grid_port = 8503;
            cloud_server_2.mqtt_port = 1881;
            cloud_server_2.web_view_port = 8502;
            cloud_server_2.hardware_logger_port = 8505;
            cloud_server_2.server_type = HomerType.BACKUP;
            cloud_server_2.save();


            Model_HomerServer cloud_server_3 = new Model_HomerServer();
            cloud_server_3.name = "Andromeda";
            cloud_server_3.server_url = "localhost";
            cloud_server_3.grid_port = 8503;
            cloud_server_3.mqtt_port = 1881;
            cloud_server_3.web_view_port = 8502;
            cloud_server_3.hardware_logger_port = 8505;
            cloud_server_3.server_type = HomerType.PUBLIC;
            cloud_server_3.save();

            Model_HomerServer cloud_server_4 = new Model_HomerServer();
            cloud_server_4.name = "Gemini";
            cloud_server_4.server_url = "localhost4";
            cloud_server_4.grid_port = 8503;
            cloud_server_4.mqtt_port = 1881;
            cloud_server_4.web_view_port = 8502;
            cloud_server_4.hardware_logger_port = 8505;
            cloud_server_4.server_type = HomerType.PUBLIC;
            cloud_server_4.save();

            // Nastavím kompilační servery
            Model_CompilationServer compilation_server_1 = new Model_CompilationServer();
            compilation_server_1.personal_server_name = "Perseus";
            compilation_server_1.hash_certificate = "test";
            compilation_server_1.connection_identifier = "test";
            compilation_server_1.save();

            Model_CompilationServer compilation_server_2 = new Model_CompilationServer();
            compilation_server_2.personal_server_name = "Pegas";
            compilation_server_2.save();

            return ok();
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    public Result basic_tariffs() {
        try {

            try {
                Model_Tariff.find.query().where().eq("name", "Alfa account").findOne();
                return ok();
            } catch (NotFoundException e) {
                // Nothing
            }

            Model_TariffExtension extensions_1 = new Model_TariffExtension();
            extensions_1.name = "Extension 1";
            extensions_1.description = "description extension 1";
            extensions_1.type = ExtensionType.DATABASE;
            extensions_1.active = true;
            extensions_1.deleted = false;
            extensions_1.color = "blue-madison";
            extensions_1.configuration = "{\"minutePrice\":0.001}";
            extensions_1.consumption = "{\"minutes\": 86400}";
            extensions_1.save();

            Model_TariffExtension extensions_2 = new Model_TariffExtension();
            extensions_2.name = "Extension 2";
            extensions_2.description = "description extension 2";
            extensions_2.type = ExtensionType.LOG;
            extensions_2.active = true;
            extensions_2.deleted = false;
            extensions_2.color = "blue-chambray";
            extensions_2.configuration = "{\"price\":0.4,\"count\":2}";
            extensions_2.consumption = "{}";
            extensions_2.save();

            // Alfa
            Model_Tariff tariff_1 = new Model_Tariff();
            tariff_1.order_position = 1;
            tariff_1.active = true;
            tariff_1.business_model = BusinessModel.SAAS;
            tariff_1.name = "Alfa account";
            tariff_1.description = "Unlimited account for testing";
            tariff_1.identifier = "alpha";
            tariff_1.credit_for_beginning = new BigDecimal(1000000);

            tariff_1.color = "blue";
            tariff_1.credit_for_beginning = BigDecimal.ZERO;

            tariff_1.owner_details_required = false;
            tariff_1.payment_details_required = false;

            tariff_1.extensions_included.add(extensions_1);
            tariff_1.extensions_recommended.add(extensions_2);

            tariff_1.save();

            return ok();
        } catch (Exception e) {
            return internalServerError(e);
        }
    }

    public Result person_test_user() {
        try {

            try {
                Model_Person.find.query().where().eq("nick_name", "Pepíno").findOne();
                return ok();
            } catch (NotFoundException e) {
                // Nothing
            }

            System.err.println("Vytvářím uživatele s emailem:  test_user@byzance.cz");
            System.err.println("Heslem: 123456789");
            System.err.println("Tokenem: token");

            // Vytvoří osobu
            Model_Person person = new Model_Person();
            person.first_name = "Pačmund";
            person.last_name = "Pepa";
            person.nick_name = "Pepíno";
            person.email = "test_user@byzance.cz";
            person.frozen = false;
            person.validated = true;
            person.setPassword("123456789");
            person.save();

            Model_AuthorizationToken token = new Model_AuthorizationToken();
            token.person = person;
            token.token = UUID.randomUUID();
            token.setDate();
            token.save();

            return ok();

        } catch (Exception e) {
            return internalServerError(e);
        }
    }
}