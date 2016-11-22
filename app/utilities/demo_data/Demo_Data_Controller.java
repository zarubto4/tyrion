package utilities.demo_data;

import io.swagger.annotations.Api;
import models.blocko.BlockoBlock;
import models.blocko.BlockoBlockVersion;
import models.blocko.TypeOfBlock;
import models.compiler.*;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.global.financial.GeneralTariff;
import models.project.global.financial.GeneralTariffLabel;
import models.project.global.financial.GeneralTariff_Extensions;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.enums.Approval_state;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_Admin;
import utilities.response.GlobalResult;

import java.util.Date;


@Api(value = "Dashboard Private Api", hidden = true)
@Security.Authenticated(Secured_Admin.class)
public class Demo_Data_Controller extends Controller {


    /*
        Slouží primárně k vytváření demo dat při vývoji - je nutné pamatovat že změnou struktury objektů
        se nemění struktura vytvářených dat. Což je poměrně pracné na opravu při velkých úpravách.

        Každopádně každý vývojář by měl do struktury doplnit demo data své práce aby se vždy dalo od ní odpíchnout dál.

        Demodata je povolené tvořit jen a pouze v "Developer modu"!!!!!!!!!!
     */

    public Result producers(){
        try {

            // Ochranná zarážka proti znovu vytvoření
            if(Producer.find.where().eq("name", "Byzance ltd").findUnique() != null) return GlobalResult.badRequest("Its Already done!");

            // Nastavím Producer
            Producer producer    = new Producer();
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
            Producer producer = Producer.find.where().eq("name", "Byzance ltd").findUnique();
            if(producer == null) return GlobalResult.badRequest("Create Producer first");
            if(Processor.find.where().eq("processor_name", "ARM STM32 FR17").findUnique() != null) return GlobalResult.badRequest("Its Already done!");

            // Nastavím Processor - YODA
            Processor processor_1      = new Processor();
            processor_1.processor_name = "ARM STM32 FR17";
            processor_1.description    = "VET6 HPABT VQ KOR HP501";
            processor_1.processor_code = "STM32FR17";
            processor_1.speed          = 3000;
            processor_1.save();

            // Nastavím Processor - DRÁT / BEZDRÁT
            Processor processor_2      = new Processor();
            processor_2.processor_name = "ARM STM32F";
            processor_2.description    = "030CCT6 GH26J 93 CHN 611";
            processor_2.processor_code = "STM32F";
            processor_2.speed          = 3000;
            processor_2.save();

            // Nastavím Processor - Rozbočovat
            Processor processor_3      = new Processor();
            processor_3.processor_name = "ARM STM32F070";
            processor_3.description    = "RBT6 GH25T 98 CHN GH 532";
            processor_3.processor_code = "STM32F070";
            processor_3.speed          = 3000;
            processor_3.save();

            
            // Nastavím Type of Boards - YODA
            TypeOfBoard typeOfBoard_1 = new TypeOfBoard();
            typeOfBoard_1.name        = "Yoda G2";
            typeOfBoard_1.description = " Yoda - Master Board with Ethernet and Wifi - second generation";
            typeOfBoard_1.compiler_target_name  = "BYZANCE_YODAG2";
            typeOfBoard_1.revision = "12/2015 V1.0 #0000";
            typeOfBoard_1.processor = processor_1;
            typeOfBoard_1.producer = producer;
            typeOfBoard_1.connectible_to_internet = true;
            typeOfBoard_1.save();

            TypeOfBoard typeOfBoard_2 = new TypeOfBoard();
            typeOfBoard_2.name        = "Wireless G2";
            typeOfBoard_2.description = " Wireless kit second generation";
            typeOfBoard_2.compiler_target_name  = "BYZANCE_WRLSKITG2";
            typeOfBoard_2.revision = "06/2016 V2.0 #0000";
            typeOfBoard_2.processor = processor_2;
            typeOfBoard_2.producer = producer;
            typeOfBoard_2.connectible_to_internet = false;
            typeOfBoard_2.save();

            TypeOfBoard typeOfBoard_3 = new TypeOfBoard();
            typeOfBoard_3.name        = "BUS G2";
            typeOfBoard_3.description = " BUS kit second generation";
            typeOfBoard_3.compiler_target_name  = "BYZANCE_BUSKITG2";
            typeOfBoard_3.revision = "02/2016 V2.0 #0000";
            typeOfBoard_3.processor = processor_2;
            typeOfBoard_3.producer = producer;
            typeOfBoard_3.connectible_to_internet = false;
            typeOfBoard_3.save();

            TypeOfBoard typeOfBoard_4 = new TypeOfBoard();
            typeOfBoard_4.name        = "Quad BUS HUB G1";
            typeOfBoard_4.description = " BUS kit second generation";
            typeOfBoard_4.compiler_target_name  = "BYZANCE_QUADBUSG1";
            typeOfBoard_4.revision = "12/2015 V1.0 #0000";
            typeOfBoard_4.processor = processor_3;
            typeOfBoard_4.producer = producer;
            typeOfBoard_4.connectible_to_internet = false;
            typeOfBoard_4.save();
            
            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result test_boards(){
        try {
            // Ochranná zarážka proti znovu vytvoření
            TypeOfBoard yoda = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_YODAG2").findUnique();
            if(yoda == null) return GlobalResult.badRequest("Create Type of Boards first");

            TypeOfBoard wireles = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_WRLSKITG2").findUnique();
            TypeOfBoard buskit = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_BUSKITG2").findUnique();

            if(Board.find.where().eq("id", "002600513533510B34353732").findUnique() == null) return GlobalResult.badRequest("Its Already done!");

            // Zaregistruji Yody
            Board board_yoda_1 = new Board();
            board_yoda_1.id = "002600513533510B34353732";
            board_yoda_1.personal_description = "Yoda B";
            board_yoda_1.type_of_board = yoda;
            board_yoda_1.date_of_create = new Date();
            board_yoda_1.save();

            Board board_yoda_2 = new Board();
            board_yoda_2.id = "003E00523533510B34353732";
            board_yoda_2.personal_description = "Yoda E";
            board_yoda_2.type_of_board = yoda;
            board_yoda_2.date_of_create = new Date();
            board_yoda_2.save();

            Board board_yoda_3 = new Board();
            board_yoda_3.id = "004C00523533510B34353732";
            board_yoda_3.personal_description = "Yoda C";
            board_yoda_3.type_of_board = yoda;
            board_yoda_3.date_of_create = new Date();
            board_yoda_3.save();

            Board board_yoda_4 = new Board();
            board_yoda_4.id = "002300513533510B34353732";
            board_yoda_4.personal_description = "Yoda A";
            board_yoda_4.type_of_board = yoda;
            board_yoda_4.date_of_create = new Date();
            board_yoda_4.save();

            Board board_yoda_5 = new Board();
            board_yoda_5.id = "wiki";
            board_yoda_5.personal_description = "Yoda D";
            board_yoda_5.type_of_board = yoda;
            board_yoda_5.date_of_create = new Date();
            board_yoda_5.save();

            // Wireless
            Board wireless_1 = new Board();
            wireless_1.id = "001C00074247430D20363439";
            wireless_1.personal_description = "[6]";
            wireless_1.type_of_board = wireles;
            wireless_1.date_of_create = new Date();
            wireless_1.save();

            Board wireless_2 = new Board();
            wireless_2.id = "001200254247430E20363439";
            wireless_2.personal_description = "[7]";
            wireless_2.type_of_board = wireles;
            wireless_2.date_of_create = new Date();
            wireless_2.save();

            Board wireless_3 = new Board();
            wireless_3.id = "001C000A4247430D20363439";
            wireless_3.personal_description = "[10]";
            wireless_3.type_of_board = wireles;
            wireless_3.date_of_create = new Date();
            wireless_3.save();

            Board wireless_4 = new Board();
            wireless_4.id = "001200244247430E20363439";
            wireless_4.personal_description = "[11]";
            wireless_4.type_of_board = wireles;
            wireless_4.date_of_create = new Date();
            wireless_4.save();

            Board wireless_5 = new Board();
            wireless_5.id = "001200264247430E20363439";
            wireless_5.personal_description = "[12]";
            wireless_5.type_of_board = wireles;
            wireless_5.date_of_create = new Date();
            wireless_5.save();

            Board wireless_6 = new Board();
            wireless_6.id = "001C00094247430D20363439";
            wireless_6.personal_description = "[13]";
            wireless_6.type_of_board = wireles;
            wireless_6.date_of_create = new Date();
            wireless_6.save();


            Board wireless_7 = new Board();
            wireless_7.id = "001C00144247430D20363439";
            wireless_7.personal_description = "[14]";
            wireless_7.type_of_board = wireles;
            wireless_7.date_of_create = new Date();
            wireless_7.save();

            Board bus_1 = new Board();
            bus_1.id = "001C00054247430D20363439";
            bus_1.personal_description = "[1]";
            bus_1.type_of_board = buskit;
            bus_1.date_of_create = new Date();
            bus_1.save();

            Board bus_2 = new Board();
            bus_2.id = "001300274247430E20363439";
            bus_2.personal_description = "[2]";
            bus_2.type_of_board = buskit;
            bus_2.date_of_create = new Date();
            bus_2.save();

            Board bus_3 = new Board();
            bus_3.id = "001C00064247430D20363439";
            bus_3.personal_description  = "[3]";
            bus_3.type_of_board = buskit;
            bus_3.date_of_create = new Date();
            bus_3.save();

            Board bus_4 = new Board();
            bus_4.id = "001200224247430E20363439";
            bus_4.personal_description  = "[4]";
            bus_4.type_of_board = buskit;
            bus_4.date_of_create = new Date();
            bus_4.save();

            Board bus_5 = new Board();
            bus_5.id = "001300244247430E20363439";
            bus_5.personal_description  = "[5]";
            bus_5.type_of_board = buskit;
            bus_5.date_of_create = new Date();
            bus_5.save();

            Board bus_6 = new Board();
            bus_6.id = "001C00104247430D20363439";
            bus_6.personal_description  = "[8]";
            bus_6.type_of_board = buskit;
            bus_6.date_of_create = new Date();
            bus_6.save();

            Board bus_7 = new Board();
            bus_7.id = "001C000C4247430D20363439";
            bus_7.personal_description  = "[9]";
            bus_7.type_of_board = buskit;
            bus_7.date_of_create = new Date();
            bus_7.save();

            Board bus_8 = new Board();
            bus_8.id = "001200234247430E20363439";
            bus_8.personal_description  = "[15]";
            bus_8.type_of_board = buskit;
            bus_8.date_of_create = new Date();
            bus_8.save();

            Board bus_9 = new Board();
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
            if(Cloud_Homer_Server.find.where().eq("server_name", "Alfa").findUnique() != null) return GlobalResult.badRequest("Its Already done!");

            // Nasstavím Homer servery
            Cloud_Homer_Server cloud_server_1 = new Cloud_Homer_Server();
            cloud_server_1.server_name  = "Alfa";
            cloud_server_1.destination_address = Server.tyrion_webSocketAddress + "/websocket/homer_server/" + cloud_server_1.server_name;
            cloud_server_1.server_url   = "localhost";
            cloud_server_1.grid_port    = ":8500";
            cloud_server_1.mqtt_port    = ":1883";
            cloud_server_1.webView_port = ":8505";

            cloud_server_1.set_hash_certificate();
            cloud_server_1.save();

            Cloud_Homer_Server cloud_server_2 = new Cloud_Homer_Server();
            cloud_server_2.server_name  = "Taurus";
            cloud_server_2.destination_address = Server.tyrion_webSocketAddress + "/websocket/homer_server/" + cloud_server_2.server_name;
            cloud_server_2.server_url   = "localhost2";
            cloud_server_2.grid_port    = ":8500";
            cloud_server_2.mqtt_port    = ":1883";
            cloud_server_2.webView_port = ":8505";
            cloud_server_2.set_hash_certificate();
            cloud_server_2.save();

            // Nastavím kompilační servery
            Cloud_Compilation_Server compilation_server_1 = new Cloud_Compilation_Server();
            compilation_server_1.server_name = "Alfa";
            compilation_server_1.destination_address = Server.tyrion_webSocketAddress + "/websocket/compilation_server/" + compilation_server_1.server_name;
            compilation_server_1.set_hash_certificate();
            compilation_server_1.save();

            Cloud_Compilation_Server compilation_server_2 = new Cloud_Compilation_Server();
            compilation_server_2.server_name = "Orion";
            compilation_server_2.destination_address = Server.tyrion_webSocketAddress + "/websocket/compilation_server/" + compilation_server_2.server_name;
            compilation_server_2.set_hash_certificate();
            compilation_server_2.save();


            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }

    public Result basic_tariffs(){
        try {


            // Ochranná zarážka proti znovu vytvoření
            if(GeneralTariff.find.where().eq("tariff_name", "Alfa account").findUnique() != null) return GlobalResult.badRequest("Its Already done!");


            // Alfa

            GeneralTariff tariff_1 = new GeneralTariff();
            tariff_1.active = true;
            tariff_1.tariff_name = "Alfa account";
            tariff_1.tariff_description = "Temporary account only for next 3 months";
            tariff_1.identificator = "alpha";

            tariff_1.color            = "blue";
            tariff_1.number_of_free_months = 0;

            tariff_1.required_paid_that = false;

            tariff_1.company_details_required  = false;
            tariff_1.required_payment_mode     = true;
            tariff_1.required_payment_method   = false;

            tariff_1.credit_card_support      = false;
            tariff_1.bank_transfer_support    = false;

            tariff_1.mode_annually    = false;
            tariff_1.mode_credit      = false;
            tariff_1.free_tariff      = true;

            tariff_1.usd = 0.0;
            tariff_1.eur = 0.0;
            tariff_1.czk = 0.0;


                    GeneralTariffLabel label_1 = new GeneralTariffLabel();
                    label_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
                    label_1.icon = "fa-bullhorn";
                    label_1.label = "Super koment bla bla bla";
                    label_1.order_position = 1;
                    tariff_1.labels.add(label_1);

                    GeneralTariffLabel label_2 = new GeneralTariffLabel();
                    label_2.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
                    label_2.icon = "fa-bullhorn";
                    label_2.label = "Super koment 2";
                    label_2.order_position = 2;
                    tariff_1.labels.add(label_2);


                    GeneralTariffLabel label_3 = new GeneralTariffLabel();
                    label_3.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
                    label_3.icon = "fa-bullhorn";
                    label_3.label = "Super koment 3 ";
                    label_3.order_position = 3;
                    tariff_1.labels.add(label_3);


                    GeneralTariffLabel label_4 = new GeneralTariffLabel();
                    label_4.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
                    label_4.icon = "fa-bullhorn";
                    label_4.label = "Super koment 4";
                    label_4.order_position = 4;
                    tariff_1.labels.add(label_4);




                    GeneralTariff_Extensions extensions_1 = new GeneralTariff_Extensions();
                    extensions_1.active = true;
                    extensions_1.color = "gree";
                    extensions_1.description = "testovací extension";
                    extensions_1.order_position = 1;
                    extensions_1.eur = 1.0;
                    extensions_1.czk = 27.0;
                    extensions_1.usd = 1.15;


                    GeneralTariffLabel label_exstension_1 = new GeneralTariffLabel();
                    label_exstension_1.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
                    label_exstension_1.icon = "fa-bullhorn";
                    label_exstension_1.label = "Super koment 4";
                    extensions_1.labels.add(label_exstension_1);

                    GeneralTariffLabel label_exstension_2 = new GeneralTariffLabel();
                    label_exstension_2.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
                    label_exstension_2.icon = "fa-bullhorn";
                    label_exstension_2.label = "Super koment 3213";
                    extensions_1.labels.add(label_exstension_2);

                    tariff_1.extensionses.add(extensions_1);


                    GeneralTariff_Extensions extensions_2 = new GeneralTariff_Extensions();
                    extensions_2.active = true;
                    extensions_2.color = "red";
                    extensions_2.description = "testovací extension";
                    extensions_2.order_position = 1;
                    extensions_2.eur = 2.0;
                    extensions_2.czk = 58.0;
                    extensions_2.usd = 2.15;


                    GeneralTariffLabel label_exstension_3 = new GeneralTariffLabel();
                    label_exstension_3.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
                    label_exstension_3.icon = "fa-bullhorn";
                    label_exstension_3.label = "Super koment 4";
                    extensions_2.labels.add(label_exstension_3);

                    GeneralTariffLabel label_exstension_4 = new GeneralTariffLabel();
                    label_exstension_4.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
                    label_exstension_4.icon = "fa-bullhorn";
                    label_exstension_4.label = "Super koment 3213";
                    extensions_2.labels.add(label_exstension_4);

                    tariff_1.extensionses.add(extensions_2);

            tariff_1.save();

            // Pro geeky

            GeneralTariff geek_tariff = new GeneralTariff();
            geek_tariff.active = true;
            geek_tariff.tariff_name = "For true Geeks";
            geek_tariff.tariff_description = "Temporary account only for next 3 months";
            geek_tariff.identificator = "geek";

            geek_tariff.color            = "green";

            geek_tariff.required_paid_that = false;
            geek_tariff.number_of_free_months = 1;

            geek_tariff.company_details_required  = false;
            geek_tariff.required_payment_mode     = true;
            geek_tariff.required_payment_method   = false;

            geek_tariff.credit_card_support      = true;
            geek_tariff.bank_transfer_support    = true;

            geek_tariff.mode_annually    = false;
            geek_tariff.mode_credit      = false;
            geek_tariff.free_tariff      = true;

            geek_tariff.usd = 4.99;
            geek_tariff.eur = 4.99;
            geek_tariff.czk = 129.99;


            GeneralTariffLabel label_5 = new GeneralTariffLabel();
            label_5.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_5.icon = "fa-bullhorn";
            label_5.label = "Super koment bla bla bla";
            label_5.order_position = 1;
            geek_tariff.labels.add(label_5);

            GeneralTariffLabel label_6 = new GeneralTariffLabel();
            label_6.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_6.icon = "fa-bullhorn";
            label_6.label = "Super koment 2ln ljhljk ljk hllkjhlkj";
            label_6.order_position = 2;
            geek_tariff.labels.add(label_6);


            GeneralTariffLabel label_7 = new GeneralTariffLabel();
            label_7.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_7.icon = "fa-bullhorn";
            label_7.label = "Super koment 3 ";
            label_7.order_position = 3;
            geek_tariff.labels.add(label_7);


            GeneralTariffLabel label_8 = new GeneralTariffLabel();
            label_8.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_8.icon = "fa-bullhorn";
            label_8.label = "Super koment 4";
            label_8.order_position = 4;
            geek_tariff.labels.add(label_8);

            geek_tariff.save();


            // Placená

            GeneralTariff business_tariff = new GeneralTariff();
            business_tariff.active = true;
            business_tariff.tariff_name = "For true Business";
            business_tariff.tariff_description = "Best for true business";
            business_tariff.identificator = "business_1";

            business_tariff.color            = "yellow";

            business_tariff.required_paid_that = false;
            business_tariff.number_of_free_months = 1;

            business_tariff.company_details_required  = true;
            business_tariff.required_payment_mode     = true;
            business_tariff.required_payment_method   = true;

            business_tariff.credit_card_support      = true;
            business_tariff.bank_transfer_support    = true;

            business_tariff.mode_annually    = true;
            business_tariff.mode_credit      = true;
            business_tariff.free_tariff      = false;

            business_tariff.usd = 39.99;
            business_tariff.eur = 35.99;
            business_tariff.czk = 3399.99;


            GeneralTariffLabel label_9 = new GeneralTariffLabel();
            label_9.description = "První měsíc zdarma";
            label_9.icon = "fa-bullhorn";
            label_9.label = "Super koment bla bla bla";
            label_9.order_position = 1;
            business_tariff.labels.add(label_9);

            GeneralTariffLabel label_10 = new GeneralTariffLabel();
            label_10.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_10.icon = "fa-bullhorn";
            label_10.label = "Super koment 2ln ljhljk ljk hllkjhlkj";
            label_10.order_position = 2;
            business_tariff.labels.add(label_10);


            GeneralTariffLabel label_11 = new GeneralTariffLabel();
            label_11.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_11.icon = "fa-bullhorn";
            label_11.label = "Super koment 3 ";
            label_11.order_position = 31;
            business_tariff.labels.add(label_11);

            GeneralTariffLabel label_12 = new GeneralTariffLabel();
            label_12.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_12.icon = "fa-bullhorn";
            label_12.label = "Super koment 4";
            label_12.order_position = 4;
            business_tariff.labels.add(label_12);

            GeneralTariffLabel label_13 = new GeneralTariffLabel();
            label_13.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_13.icon = "fa-bullhorn";
            label_13.label = "Super koment 4";
            label_13.order_position = 5;
            business_tariff.labels.add(label_13);

            GeneralTariffLabel label_14 = new GeneralTariffLabel();
            label_14.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_14.icon = "fa-bullhorn";
            label_14.label = "Super koment 4";
            label_14.order_position = 6;
            business_tariff.labels.add(label_14);

            GeneralTariffLabel label_15 = new GeneralTariffLabel();
            label_15.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_15.icon = "fa-bullhorn";
            label_15.label = "Super koment 4";
            label_15.order_position = 7;
            business_tariff.labels.add(label_15);

            business_tariff.save();

            GeneralTariff business_tariff_2 = new GeneralTariff();
            business_tariff_2.active = true;
            business_tariff_2.tariff_name = "Enterprise";
            business_tariff_2.tariff_description = "You know what you need!";
            business_tariff_2.identificator = "business_2";

            business_tariff_2.color            = "red";

            business_tariff_2.required_paid_that = true;
            business_tariff_2.number_of_free_months = 0;

            business_tariff_2.company_details_required  = true;
            business_tariff_2.required_payment_mode     = true;
            business_tariff_2.required_payment_method   = true;

            business_tariff_2.credit_card_support      = true;
            business_tariff_2.bank_transfer_support    = true;

            business_tariff_2.mode_annually    = true;
            business_tariff_2.mode_credit      = true;
            business_tariff_2.free_tariff      = false;

            business_tariff_2.usd = 1399.99;
            business_tariff_2.eur = 1199.99;
            business_tariff_2.czk = 29999.99;


            GeneralTariffLabel label_16 = new GeneralTariffLabel();
            label_16.description = "Už prvního měsíce je to placený";
            label_16.icon = "fa-bullhorn";
            label_16.label = "Super koment bla bla bla";
            label_16.order_position = 1;
            business_tariff_2.labels.add(label_16);

            GeneralTariffLabel label_17 = new GeneralTariffLabel();
            label_17.description = "Tento tarif je na odzkoušení kreditky";
            label_17.icon = "fa-bullhorn";
            label_17.label = "Super koment 2ln ljhljk ljk hllkjhlkj";
            label_16.order_position = 2;
            business_tariff_2.labels.add(label_17);


            GeneralTariffLabel label_18 = new GeneralTariffLabel();
            label_18.description = "kreditkaaa!!!";
            label_18.icon = "fa-bullhorn";
            label_18.label = "Super koment 3 ";
            label_18.order_position = 3;
            business_tariff_2.labels.add(label_18);

            GeneralTariffLabel label_19 = new GeneralTariffLabel();
            label_19.description = "Kreditkaaaaa";
            label_19.icon = "fa-bullhorn";
            label_19.label = "Super koment 4";
            label_19.order_position = 4;
            business_tariff.labels.add(label_19);

            GeneralTariffLabel label_20 = new GeneralTariffLabel();
            label_20.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_20.icon = "fa-bullhorn";
            label_20.label = "Super koment 4";
            label_20.order_position = 5;
            business_tariff_2.labels.add(label_20);

            GeneralTariffLabel label_21 = new GeneralTariffLabel();
            label_21.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_21.icon = "fa-bullhorn";
            label_21.label = "Super koment 4";
            label_21.order_position = 6;
            business_tariff_2.labels.add(label_21);

            GeneralTariffLabel label_22 = new GeneralTariffLabel();
            label_22.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_22.icon = "fa-bullhorn";
            label_22.label = "Super koment 4";
            label_22.order_position = 7;
            business_tariff_2.labels.add(label_22);



            GeneralTariff_Extensions extensions_3 = new GeneralTariff_Extensions();
            extensions_3.active = true;
            extensions_3.color = "gree";
            extensions_3.description = "testovací extension";
            extensions_3.order_position = 1;
            extensions_3.eur = 1.0;
            extensions_3.czk = 27.0;
            extensions_3.usd = 1.15;


            GeneralTariffLabel label_exstension_5 = new GeneralTariffLabel();
            label_exstension_5.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_exstension_5.icon = "fa-bullhorn";
            label_exstension_5.label = "Super koment 4";
            extensions_3.labels.add(label_exstension_5);

            GeneralTariffLabel label_exstension_6 = new GeneralTariffLabel();
            label_exstension_6.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_exstension_6.icon = "fa-bullhorn";
            label_exstension_6.label = "Super koment 3213";
            extensions_3.labels.add(label_exstension_6);

            business_tariff_2.extensionses.add(extensions_3);


            GeneralTariff_Extensions extensions_6 = new GeneralTariff_Extensions();
            extensions_6.active = true;
            extensions_6.color = "red";
            extensions_6.description = "testovací extension";
            extensions_6.order_position = 1;
            extensions_6.eur = 2.0;
            extensions_6.czk = 58.0;
            extensions_6.usd = 2.15;


            GeneralTariffLabel label_exstension_7 = new GeneralTariffLabel();
            label_exstension_7.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_exstension_7.icon = "fa-bullhorn";
            label_exstension_7.label = "Super koment 4";
            extensions_6.labels.add(label_exstension_7);

            GeneralTariffLabel label_exstension_8 = new GeneralTariffLabel();
            label_exstension_8.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_exstension_8.icon = "fa-bullhorn";
            label_exstension_8.label = "Super koment 3213";
            extensions_6.labels.add(label_exstension_8);

            business_tariff_2.extensionses.add(extensions_6);



            GeneralTariff_Extensions extensions_7 = new GeneralTariff_Extensions();
            extensions_7.active = true;
            extensions_7.color = "blue";
            extensions_7.description = "testovací extension";
            extensions_7.order_position = 1;
            extensions_7.eur = 20.0;
            extensions_7.czk = 5430.0;
            extensions_7.usd = 2001.15;


            GeneralTariffLabel label_exstension_9 = new GeneralTariffLabel();
            label_exstension_9.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_exstension_9.icon = "fa-bullhorn";
            label_exstension_9.label = "Super koment 4";
            extensions_7.labels.add(label_exstension_9);

            GeneralTariffLabel label_exstension_10 = new GeneralTariffLabel();
            label_exstension_10.description = "Všechno bude dobré, uvidíte!! Toto je dlouhý komentář, který se zobrazí po najetí myškou";
            label_exstension_10.icon = "fa-bullhorn";
            label_exstension_10.label = "Super koment 3213";
            extensions_7.labels.add(label_exstension_10);

            business_tariff_2.extensionses.add(extensions_7);

            business_tariff_2.save();


            GeneralTariff ilegal_tariff = new GeneralTariff();
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
            if(TypeOfBlock.find.where().eq("name", "Social Sites Blocks").findUnique() != null) return GlobalResult.badRequest("Its Already done!");

            TypeOfBlock typeOfBlock_1 = new TypeOfBlock();
            typeOfBlock_1.name =  "Social Sites Blocks";
            typeOfBlock_1.general_description = "Sociální bločky pro Facebook, Twitter a další";
            typeOfBlock_1.save();

            TypeOfBlock typeOfBlock_2 = new TypeOfBlock();
            typeOfBlock_2.name =  "Logic Blocks";
            typeOfBlock_2.general_description = "Základní logické bločky na principu booleovy algebry";
            typeOfBlock_2.save();

            TypeOfBlock typeOfBlock_3 = new TypeOfBlock();
            typeOfBlock_3.name =  "Api Blocks";
            typeOfBlock_3.general_description = "Bločky pro Externí API";
            typeOfBlock_3.save();

            TypeOfBlock typeOfBlock_4 = new TypeOfBlock();
            typeOfBlock_4.name =  "Times Blocks";
            typeOfBlock_4.general_description = "Bločky s časovou konstantou";
            typeOfBlock_4.save();


            //**************************************************************************************************************


            //1
            BlockoBlock blockoBlock_1_1 = new BlockoBlock();
            blockoBlock_1_1.producer = Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_1_1.name = "Facebook Post";
            blockoBlock_1_1.general_description = "m.n,a sldjkfbnlskjd bjsdnf jkbsjndafio bjkvc,mxnymf můiwljhkn bfm,mn.adsjlůxkbcvnymn klnaf m,mnbjlů§k nbasldfb,n jkl.lkn nmsgl,můfjk br,mn.fl kbmfkllykbv vkůljmyn,d.mckůlxůklxbvnm,dsf m.ylp§foigkljsadůjfndmsvoija kdsfvůljnkjb fkljgfbvclasgfbnlfagkbkcnlsgkfklndgdk an dsja";
            blockoBlock_1_1.type_of_block = typeOfBlock_1;
            blockoBlock_1_1.save();

            BlockoBlock blockoBlock_1_2 = new BlockoBlock();
            blockoBlock_1_2.producer = Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_1_2.name = "Twitter tweet";
            blockoBlock_1_2.general_description = "Lorem ipsum di lasjdhflkj dshaflj  sadfsdfas dfsadf sad gsfgsdf sadfsd fas";
            blockoBlock_1_2.type_of_block = typeOfBlock_1;
            blockoBlock_1_2.save();

            BlockoBlock blockoBlock_1_3 = new BlockoBlock();
            blockoBlock_1_3.producer = Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_1_3.name = "Google+";
            blockoBlock_1_3.general_description = "Google+ Function dsafkjb bjbsadlkjbf kblasdf adsf";
            blockoBlock_1_3.type_of_block = typeOfBlock_1;
            blockoBlock_1_3.save();


            //2
            BlockoBlock blockoBlock_2_1 = new BlockoBlock();
            blockoBlock_2_1.producer = Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_2_1.name = "OR";
            blockoBlock_2_1.general_description = "Logic function OR";
            blockoBlock_2_1.type_of_block = typeOfBlock_2;
            blockoBlock_2_1.save();

            BlockoBlock blockoBlock_2_2 = new BlockoBlock();
            blockoBlock_2_2.producer = Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_2_2.name = "AND";
            blockoBlock_2_2.general_description = "Logic function AND";
            blockoBlock_2_2.type_of_block = typeOfBlock_2;
            blockoBlock_2_2.save();

            BlockoBlock blockoBlock_2_3 = new BlockoBlock();
            blockoBlock_2_3.producer = Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_2_3.name = "XOR";
            blockoBlock_2_3.general_description = "Logic function XOR";
            blockoBlock_2_3.type_of_block = typeOfBlock_2;
            blockoBlock_2_3.save();


            //3
            BlockoBlock blockoBlock_3_1 = new BlockoBlock();
            blockoBlock_3_1.producer = Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_3_1.name = "POST";
            blockoBlock_3_1.general_description = "Basic REST-API REQUEST POST";
            blockoBlock_3_1.type_of_block = typeOfBlock_3;
            blockoBlock_3_1.save();

            BlockoBlock blockoBlock_3_2 = new BlockoBlock();
            blockoBlock_3_2.producer = Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_3_2.name = "GET";
            blockoBlock_3_2.general_description = "Basic REST-API REQUEST GET";
            blockoBlock_3_2.type_of_block = typeOfBlock_3;
            blockoBlock_3_2.save();

            BlockoBlock blockoBlock_3_3 = new BlockoBlock();
            blockoBlock_3_3.producer = Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_3_3.name = "PUT";
            blockoBlock_3_3.general_description = "Basic REST-API REQUEST PUT";
            blockoBlock_3_3.type_of_block = typeOfBlock_3;
            blockoBlock_3_3.save();

            BlockoBlock blockoBlock_3_4 = new BlockoBlock();
            blockoBlock_3_4.producer = Producer.find.where().eq("name", "Byzance ltd").findUnique();
            blockoBlock_3_4.name = "DELETE";
            blockoBlock_3_4.general_description = "Basic REST-API REQUEST DELETE";
            blockoBlock_3_4.type_of_block = typeOfBlock_3;
            blockoBlock_3_4.save();


            //**************************************************************************************************************


            // Version scheme

            BlockoBlockVersion scheme = new BlockoBlockVersion();
            scheme.version_name = "version_scheme";
            scheme.version_description = "This is a BlockoBlockVersion scheme. When BlockoBlock is made, its first version will look like this.";
            scheme.approval_state = Approval_state.approved;
            scheme.date_of_create = new Date();
            scheme.design_json = "{ This is a design JSON scheme. }";
            scheme.logic_json = "{ This is a logic JSON scheme. }";
            scheme.save();

            // 1_1
            BlockoBlockVersion version_1_1_1 = new BlockoBlockVersion();
            version_1_1_1.blocko_block = blockoBlock_1_1;
            version_1_1_1.date_of_create = new Date();
            version_1_1_1.logic_json = "{}";
            version_1_1_1.version_description = "První update";
            version_1_1_1.version_name = "1.0.1";
            version_1_1_1.approval_state = Approval_state.approved;
            version_1_1_1.save();

            BlockoBlockVersion version_1_1_2 = new BlockoBlockVersion();
            version_1_1_2.blocko_block = blockoBlock_1_1;
            version_1_1_2.date_of_create = new Date();
            version_1_1_2.logic_json = "{}";
            version_1_1_2.version_description = "První update";
            version_1_1_2.version_name = "1.0.1";
            version_1_1_2.approval_state = Approval_state.approved;
            version_1_1_2.save();

            // 1_2
            BlockoBlockVersion version_1_2_1 = new BlockoBlockVersion();
            version_1_2_1.blocko_block = blockoBlock_1_2;
            version_1_2_1.date_of_create = new Date();
            version_1_2_1.logic_json = "{}";
            version_1_2_1.version_description = "První update";
            version_1_2_1.version_name = "1.0.1";
            version_1_2_1.approval_state = Approval_state.approved;
            version_1_2_1.save();

            BlockoBlockVersion version_1_2_2 = new BlockoBlockVersion();
            version_1_2_2.blocko_block = blockoBlock_1_2;
            version_1_2_2.date_of_create = new Date();
            version_1_2_2.logic_json = "{}";
            version_1_2_2.version_description = "První update";
            version_1_2_2.version_name = "1.0.1";
            version_1_2_2.approval_state = Approval_state.approved;
            version_1_2_2.save();

            // 1_3
            BlockoBlockVersion version_1_3_1 = new BlockoBlockVersion();
            version_1_3_1.blocko_block = blockoBlock_1_3;
            version_1_3_1.date_of_create = new Date();
            version_1_3_1.logic_json = "{}";
            version_1_3_1.version_description = "První update";
            version_1_3_1.version_name = "1.0.1";
            version_1_3_1.approval_state = Approval_state.approved;
            version_1_3_1.save();

            BlockoBlockVersion version_1_3_2 = new BlockoBlockVersion();
            version_1_3_2.blocko_block = blockoBlock_1_3;
            version_1_3_2.date_of_create = new Date();
            version_1_3_2.logic_json = "{}";
            version_1_3_2.version_description = "První update";
            version_1_3_2.version_name = "1.0.1";
            version_1_3_2.approval_state = Approval_state.approved;
            version_1_3_2.save();

            // 2_1
            BlockoBlockVersion version_2_1_1 = new BlockoBlockVersion();
            version_2_1_1.blocko_block = blockoBlock_2_1;
            version_2_1_1.date_of_create = new Date();
            version_2_1_1.logic_json = "{}";
            version_2_1_1.version_description = "První update";
            version_2_1_1.version_name = "1.0.1";
            version_2_1_1.approval_state = Approval_state.approved;
            version_2_1_1.save();

            BlockoBlockVersion version_2_1_2 = new BlockoBlockVersion();
            version_2_1_2.blocko_block = blockoBlock_2_1;
            version_2_1_2.date_of_create = new Date();
            version_2_1_2.logic_json = "{}";
            version_2_1_2.version_description = "Prvnsafd -a.kshm fn.,mbs gjknbm akdfsm,.cxy ndfam,nkvxclůavcx namxyklnvdfsam ,cvklůdfsmv.lyům ,klnvyůmc,.í update";
            version_2_1_2.version_name = "1.0.2";
            version_2_1_2.approval_state = Approval_state.approved;
            version_2_1_2.save();

            // 2_2
            BlockoBlockVersion version_2_2_1 = new BlockoBlockVersion();
            version_2_2_1.blocko_block = blockoBlock_2_2;
            version_2_2_1.date_of_create = new Date();
            version_2_2_1.logic_json = "{}";
            version_2_2_1.version_description = "První update";
            version_2_2_1.version_name = "1.0.1";
            version_2_2_1.approval_state = Approval_state.approved;
            version_2_2_1.save();

            BlockoBlockVersion version_2_2_2 = new BlockoBlockVersion();
            version_2_2_2.blocko_block = blockoBlock_2_2;
            version_2_2_2.date_of_create = new Date();
            version_2_2_2.logic_json = "{}";
            version_2_2_2.version_description = "Druhý update";
            version_2_2_2.version_name = "1.0.2";
            version_2_2_2.approval_state = Approval_state.approved;
            version_2_2_2.save();

            BlockoBlockVersion version_2_2_3= new BlockoBlockVersion();
            version_2_2_3.blocko_block = blockoBlock_2_2;
            version_2_2_3.date_of_create = new Date();
            version_2_2_3.logic_json = "{}";
            version_2_2_3.version_description = "Třetí update";
            version_2_2_3.version_name = "1.0.3";
            version_2_2_3.approval_state = Approval_state.approved;
            version_2_2_3.save();

            BlockoBlockVersion version_2_2_4 = new BlockoBlockVersion();
            version_2_2_4.blocko_block = blockoBlock_2_2;
            version_2_2_4.date_of_create = new Date();
            version_2_2_4.logic_json = "{}";
            version_2_2_4.version_description = "Čtvrtý  update";
            version_2_2_4.version_name = "1.0.4";
            version_2_2_4.approval_state = Approval_state.approved;
            version_2_2_4.save();

            // 2_3
            BlockoBlockVersion version_2_3_1 = new BlockoBlockVersion();
            version_2_3_1.blocko_block = blockoBlock_2_3;
            version_2_3_1.date_of_create = new Date();
            version_2_3_1.logic_json = "{}";
            version_2_3_1.version_description = "První update";
            version_2_3_1.version_name = "Na poprvé";
            version_2_3_1.approval_state = Approval_state.approved;
            version_2_3_1.save();

            BlockoBlockVersion version_2_3_2 = new BlockoBlockVersion();
            version_2_3_2.blocko_block = blockoBlock_2_3;
            version_2_3_2.date_of_create = new Date();
            version_2_3_2.logic_json = "{}";
            version_2_3_2.version_description = "První update";
            version_2_3_2.version_name = "Na podruhé";
            version_2_3_2.approval_state = Approval_state.approved;
            version_2_3_2.save();


            // 3_1
            BlockoBlockVersion version_3_1_1 = new BlockoBlockVersion();
            version_3_1_1.blocko_block = blockoBlock_3_1;
            version_3_1_1.date_of_create = new Date();
            version_3_1_1.logic_json = "{}";
            version_3_1_1.version_description = "První update";
            version_3_1_1.version_name = "Verze 1";
            version_3_1_1.approval_state = Approval_state.approved;
            version_3_1_1.save();

            BlockoBlockVersion version_3_1_2 = new BlockoBlockVersion();
            version_3_1_2.blocko_block = blockoBlock_3_1;
            version_3_1_2.date_of_create = new Date();
            version_3_1_2.logic_json = "{}";
            version_3_1_2.version_description = "Druhý velkopeý asdklbfj aslaksbdfjlkbalskbdf lkjbafs lkjbafslbkjafslkjba sdflkbjasf update";
            version_3_1_2.version_name = "Verze 2";
            version_3_1_2.approval_state = Approval_state.approved;
            version_3_1_2.save();

            // 3_2
            BlockoBlockVersion version_3_2_1 = new BlockoBlockVersion();
            version_3_2_1.blocko_block = blockoBlock_3_2;
            version_3_2_1.date_of_create = new Date();
            version_3_2_1.logic_json = "{}";
            version_3_2_1.version_description = "První update";
            version_3_2_1.version_name = "1.0.1";
            version_3_2_1.approval_state = Approval_state.approved;
            version_3_2_1.save();

            BlockoBlockVersion version_3_2_2 = new BlockoBlockVersion();
            version_3_2_2.blocko_block = blockoBlock_3_2;
            version_3_2_2.date_of_create = new Date();
            version_3_2_2.logic_json = "{}";
            version_3_2_2.version_description = "První update";
            version_3_2_2.version_name = "1.0.2";
            version_3_2_2.approval_state = Approval_state.approved;
            version_3_2_2.save();

            BlockoBlockVersion version_3_2_3 = new BlockoBlockVersion();
            version_3_2_3.blocko_block = blockoBlock_3_2;
            version_3_2_3.date_of_create = new Date();
            version_3_2_3.logic_json = "{}";
            version_3_2_3.version_description = "První update";
            version_3_2_3.version_name = "1.1.3";
            version_3_2_3.approval_state = Approval_state.approved;
            version_3_2_3.save();

            // 3_3
            BlockoBlockVersion version_3_3_1 = new BlockoBlockVersion();
            version_3_3_1.blocko_block = blockoBlock_3_3;
            version_3_3_1.date_of_create = new Date();
            version_3_3_1.logic_json = "{}";
            version_3_3_1.version_description = "První update";
            version_3_3_1.version_name = "1.0.1";
            version_3_3_1.approval_state = Approval_state.approved;
            version_3_3_1.save();

            BlockoBlockVersion version_3_3_2 = new BlockoBlockVersion();
            version_3_3_2.blocko_block = blockoBlock_3_3;
            version_3_3_2.date_of_create = new Date();
            version_3_3_2.logic_json = "{}";
            version_3_3_2.version_description = "Druhý update";
            version_3_3_2.version_name = "1.0.2";
            version_3_3_2.approval_state = Approval_state.approved;
            version_3_3_2.save();

            // 3_4
            BlockoBlockVersion version_3_4_1 = new BlockoBlockVersion();
            version_3_4_1.blocko_block = blockoBlock_3_4;
            version_3_4_1.date_of_create = new Date();
            version_3_4_1.logic_json = "{}";
            version_3_4_1.version_description = "První update";
            version_3_4_1.version_name = "1.0.1";
            version_3_4_1.approval_state = Approval_state.approved;
            version_3_4_1.save();

            BlockoBlockVersion version_3_4_2 = new BlockoBlockVersion();
            version_3_4_2.blocko_block = blockoBlock_3_4;
            version_3_4_2.date_of_create = new Date();
            version_3_4_2.logic_json = "{}";
            version_3_4_2.version_description = "Druhý update";
            version_3_4_2.version_name = "1.0.2";
            version_3_4_2.approval_state = Approval_state.approved;
            version_3_4_2.save();


            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }


    public Result person_test_user(){
        try {


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


            return GlobalResult.result_ok();
        }catch (Exception e){
            return Loggy.result_internalServerError(e, request());
        }
    }






}
