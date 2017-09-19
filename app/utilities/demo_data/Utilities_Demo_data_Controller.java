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
import utilities.login_entities.Secured_API;
import utilities.response.GlobalResult;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Api(value = "Dashboard Private Api", hidden = true)
@Security.Authenticated(Secured_API.class)
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

        result = this.external_servers();
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

            // Nastavím Type of Boards - YODA
            Model_TypeOfBoard typeOfBoard_1 = new Model_TypeOfBoard();
            typeOfBoard_1.name = "IODA G2";
            typeOfBoard_1.description = " Yoda - Master Board with Ethernet and Wifi - second generation";
            typeOfBoard_1.compiler_target_name = "BYZANCE_YODAG2";
            typeOfBoard_1.revision = "12/2015 V1.0 #0000";
            typeOfBoard_1.processor = processor_1;
            typeOfBoard_1.producer = producer;
            typeOfBoard_1.connectible_to_internet = true;
            typeOfBoard_1.features.add(ethernet);
            typeOfBoard_1.features.add(wifi);
            typeOfBoard_1.save();

            // Nastavím Type of Boards - YODA
            Model_TypeOfBoard typeOfBoard_2 = new Model_TypeOfBoard();
            typeOfBoard_2.name = "IODA G3";
            typeOfBoard_2.description = " Ioda - Master Board with Ethernet and Wifi - third generation";
            typeOfBoard_2.compiler_target_name = "BYZANCE_YODAG3";
            typeOfBoard_2.revision = "12/2015 V1.0 #0000";
            typeOfBoard_2.processor = processor_1;
            typeOfBoard_2.producer = producer;
            typeOfBoard_2.connectible_to_internet = true;
            typeOfBoard_2.features.add(ethernet);
            typeOfBoard_2.features.add(wifi);
            typeOfBoard_2.save();


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
            Model_TypeOfBoard yoda_type = Model_TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_YODAG3").findUnique();
            if (yoda_type == null) return GlobalResult.result_badRequest("Create Type of Boards first");


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

            return GlobalResult.result_ok();
        } catch (Exception e) {
            return Server_Logger.result_internalServerError(e, request());
        }
    }

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

            // Pro geeky
            Model_Tariff geek_tariff = new Model_Tariff();
            geek_tariff.order_position = 2;
            geek_tariff.active = true;
            geek_tariff.business_model = Enum_BusinessModel.saas;
            geek_tariff.name = "For true Geeks";
            geek_tariff.description = "Temporary account only for next 3 months";
            geek_tariff.identifier = "geek";

            geek_tariff.color = "green-jungle";

            geek_tariff.credit_for_beginning = 200000L;

            geek_tariff.company_details_required = false;
            geek_tariff.payment_details_required = false;

            geek_tariff.save();
            geek_tariff.refresh();

            // Placená
            Model_Tariff business_tariff = new Model_Tariff();
            business_tariff.order_position = 3;
            business_tariff.active = true;
            business_tariff.business_model = Enum_BusinessModel.saas;
            business_tariff.name = "For true Business";
            business_tariff.description = "Best for true business";
            business_tariff.identifier = "business_1";

            business_tariff.color = "green-jungle";

            business_tariff.credit_for_beginning = (long) 100000;

            business_tariff.company_details_required = true;
            business_tariff.payment_details_required = true;


            business_tariff.save();
            business_tariff.refresh();

            /*
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
            business_tariff_2.order_position = 4;
            business_tariff_2.active = true;
            business_tariff_2.business_model = Enum_BusinessModel.saas;
            business_tariff_2.name = "Enterprise";
            business_tariff_2.description = "You know what you need!";
            business_tariff_2.identifier = "business_2";

            business_tariff_2.color = "green-sharp";

            business_tariff_2.credit_for_beginning = (long) 500000;

            business_tariff_2.company_details_required = true;
            business_tariff_2.payment_details_required = true;

            business_tariff_2.save();
            business_tariff_2.refresh();

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

            */

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