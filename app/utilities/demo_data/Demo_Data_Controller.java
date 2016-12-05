package utilities.demo_data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import models.blocko.BlockoBlock;
import models.blocko.BlockoBlockVersion;
import models.blocko.TypeOfBlock;
import models.compiler.*;
import models.grid.GridWidget;
import models.grid.GridWidgetVersion;
import models.grid.TypeOfWidget;
import models.person.FloatingPersonToken;
import models.person.Person;
import models.project.b_program.B_Pair;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Hw_Group;
import models.project.b_program.instnace.Homer_Instance_Record;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.c_program.C_Program;
import models.project.global.Product;
import models.project.global.Project;
import models.project.global.financial.GeneralTariff;
import models.project.global.financial.GeneralTariffLabel;
import models.project.global.financial.GeneralTariff_Extensions;
import models.project.global.financial.Payment_Details;
import models.project.m_program.M_Program;
import models.project.m_program.M_Project;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.Server;
import utilities.enums.Approval_state;
import utilities.enums.Currency;
import utilities.enums.Payment_method;
import utilities.enums.Payment_mode;
import utilities.loggy.Loggy;
import utilities.loginEntities.Secured_Admin;
import utilities.response.GlobalResult;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;


@Api(value = "Dashboard Private Api", hidden = true)
@Security.Authenticated(Secured_Admin.class)
public class Demo_Data_Controller extends Controller {


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
        result =   this.grid_demo_data();         if(result.status() != 200 ) return result;
        result =   this.c_program_configuration();  if(result.status() != 200 ) return result;

        result =  this.person_test_user();            if(result.status() != 200 ) return result;
        result =  this.person_project_and_programs(); if(result.status() != 200 ) return result;
        result =  this.person_instancies();           if(result.status() != 200 ) return result;

        return result;
    }

    public Result producers(){
        try {

            // Ochranná zarážka proti znovu vytvoření
            if(Producer.find.where().eq("name", "Byzance ltd").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

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
            if(producer == null) return GlobalResult.result_BadRequest("Create Producer first");
            if(Processor.find.where().eq("processor_name", "ARM STM32 FR17").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

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
            if(yoda == null) return GlobalResult.result_BadRequest("Create Type of Boards first");

            TypeOfBoard wireles = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_WRLSKITG2").findUnique();
            TypeOfBoard buskit = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_BUSKITG2").findUnique();

            if(Board.find.where().eq("id", "002600513533510B34353732").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

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
            if(Cloud_Homer_Server.find.where().eq("server_name", "Alfa").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

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
            if(GeneralTariff.find.where().eq("tariff_name", "Alfa account").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");


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
                    extensions_1.name = "Super tunning";
                    extensions_1.active = true;
                    extensions_1.color = "blue-madison";
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
                    extensions_2.name = "Super bonus ";
                    extensions_2.active = true;
                    extensions_2.color = "blue-chambray";
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

            geek_tariff.color            = "green-jungle";

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

            business_tariff.color            = "green-jungle";

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

            business_tariff_2.color            = "green-sharp";

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
            extensions_3.name = "Mega tunning";
            extensions_3.active = true;
            extensions_3.color = "purple-sharp";
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
            extensions_6.name = "HyperTotal tunning";
            extensions_6.active = true;
            extensions_6.color = "red-flamingo";
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
            extensions_7.name = "Mega tunning";
            extensions_7.active = true;
            extensions_7.color = "YELLOW-CRUSTA";
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
            if(TypeOfBlock.find.where().eq("name", "Social Sites Blocks").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

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

            // 1_1
            BlockoBlockVersion version_1_1_1 = new BlockoBlockVersion();
            version_1_1_1.blocko_block = blockoBlock_1_1;
            version_1_1_1.date_of_create = new Date();
            version_1_1_1.logic_json = "{}";
            version_1_1_1.design_json = "{}";
            version_1_1_1.version_description = "První update";
            version_1_1_1.version_name = "1.0.1";
            version_1_1_1.approval_state = Approval_state.approved;
            version_1_1_1.save();

            BlockoBlockVersion version_1_1_2 = new BlockoBlockVersion();
            version_1_1_2.blocko_block = blockoBlock_1_1;
            version_1_1_2.date_of_create = new Date();
            version_1_1_2.logic_json = "{}";
            version_1_1_2.design_json = "{}";
            version_1_1_2.version_description = "První update";
            version_1_1_2.version_name = "1.0.1";
            version_1_1_2.approval_state = Approval_state.approved;
            version_1_1_2.save();

            // 1_2
            BlockoBlockVersion version_1_2_1 = new BlockoBlockVersion();
            version_1_2_1.blocko_block = blockoBlock_1_2;
            version_1_2_1.date_of_create = new Date();
            version_1_2_1.logic_json = "{}";
            version_1_2_1.design_json = "{}";
            version_1_2_1.version_description = "První update";
            version_1_2_1.version_name = "1.0.1";
            version_1_2_1.approval_state = Approval_state.approved;
            version_1_2_1.save();

            BlockoBlockVersion version_1_2_2 = new BlockoBlockVersion();
            version_1_2_2.blocko_block = blockoBlock_1_2;
            version_1_2_2.date_of_create = new Date();
            version_1_2_2.logic_json = "{}";
            version_1_2_2.design_json = "{}";
            version_1_2_2.version_description = "První update";
            version_1_2_2.version_name = "1.0.1";
            version_1_2_2.approval_state = Approval_state.approved;
            version_1_2_2.save();

            // 1_3
            BlockoBlockVersion version_1_3_1 = new BlockoBlockVersion();
            version_1_3_1.blocko_block = blockoBlock_1_3;
            version_1_3_1.date_of_create = new Date();
            version_1_3_1.logic_json = "{}";
            version_1_3_1.design_json = "{}";
            version_1_3_1.version_description = "První update";
            version_1_3_1.version_name = "1.0.1";
            version_1_3_1.approval_state = Approval_state.approved;
            version_1_3_1.save();

            BlockoBlockVersion version_1_3_2 = new BlockoBlockVersion();
            version_1_3_2.blocko_block = blockoBlock_1_3;
            version_1_3_2.date_of_create = new Date();
            version_1_3_2.logic_json = "{}";
            version_1_3_2.design_json = "{}";
            version_1_3_2.version_description = "První update";
            version_1_3_2.version_name = "1.0.1";
            version_1_3_2.approval_state = Approval_state.approved;
            version_1_3_2.save();

            // 2_1
            BlockoBlockVersion version_2_1_1 = new BlockoBlockVersion();
            version_2_1_1.blocko_block = blockoBlock_2_1;
            version_2_1_1.date_of_create = new Date();
            version_2_1_1.logic_json = "{}";
            version_2_1_1.design_json = "{}";
            version_2_1_1.version_description = "První update";
            version_2_1_1.version_name = "1.0.1";
            version_2_1_1.approval_state = Approval_state.approved;
            version_2_1_1.save();

            BlockoBlockVersion version_2_1_2 = new BlockoBlockVersion();
            version_2_1_2.blocko_block = blockoBlock_2_1;
            version_2_1_2.date_of_create = new Date();
            version_2_1_2.logic_json = "{}";
            version_2_1_2.design_json = "{}";
            version_2_1_2.version_description = "Prvnsafd -a.kshm fn.,mbs gjknbm akdfsm,.cxy ndfam,nkvxclůavcx namxyklnvdfsam ,cvklůdfsmv.lyům ,klnvyůmc,.í update";
            version_2_1_2.version_name = "1.0.2";
            version_2_1_2.approval_state = Approval_state.approved;
            version_2_1_2.save();

            // 2_2
            BlockoBlockVersion version_2_2_1 = new BlockoBlockVersion();
            version_2_2_1.blocko_block = blockoBlock_2_2;
            version_2_2_1.date_of_create = new Date();
            version_2_2_1.logic_json = "{}";
            version_2_2_1.design_json = "{}";
            version_2_2_1.version_description = "První update";
            version_2_2_1.version_name = "1.0.1";
            version_2_2_1.approval_state = Approval_state.approved;
            version_2_2_1.save();

            BlockoBlockVersion version_2_2_2 = new BlockoBlockVersion();
            version_2_2_2.blocko_block = blockoBlock_2_2;
            version_2_2_2.date_of_create = new Date();
            version_2_2_2.logic_json = "{}";
            version_2_2_2.design_json = "{}";
            version_2_2_2.version_description = "Druhý update";
            version_2_2_2.version_name = "1.0.2";
            version_2_2_2.approval_state = Approval_state.approved;
            version_2_2_2.save();

            BlockoBlockVersion version_2_2_3= new BlockoBlockVersion();
            version_2_2_3.blocko_block = blockoBlock_2_2;
            version_2_2_3.date_of_create = new Date();
            version_2_2_3.logic_json = "{}";
            version_2_2_3.design_json = "{}";
            version_2_2_3.version_description = "Třetí update";
            version_2_2_3.version_name = "1.0.3";
            version_2_2_3.approval_state = Approval_state.approved;
            version_2_2_3.save();

            BlockoBlockVersion version_2_2_4 = new BlockoBlockVersion();
            version_2_2_4.blocko_block = blockoBlock_2_2;
            version_2_2_4.date_of_create = new Date();
            version_2_2_4.logic_json = "{}";
            version_2_2_4.design_json = "{}";
            version_2_2_4.version_description = "Čtvrtý  update";
            version_2_2_4.version_name = "1.0.4";
            version_2_2_4.approval_state = Approval_state.approved;
            version_2_2_4.save();

            // 2_3
            BlockoBlockVersion version_2_3_1 = new BlockoBlockVersion();
            version_2_3_1.blocko_block = blockoBlock_2_3;
            version_2_3_1.date_of_create = new Date();
            version_2_3_1.logic_json = "{}";
            version_2_3_1.design_json = "{}";
            version_2_3_1.version_description = "První update";
            version_2_3_1.version_name = "Na poprvé";
            version_2_3_1.approval_state = Approval_state.approved;
            version_2_3_1.save();

            BlockoBlockVersion version_2_3_2 = new BlockoBlockVersion();
            version_2_3_2.blocko_block = blockoBlock_2_3;
            version_2_3_2.date_of_create = new Date();
            version_2_3_2.logic_json = "{}";
            version_2_3_2.design_json = "{}";
            version_2_3_2.version_description = "První update";
            version_2_3_2.version_name = "Na podruhé";
            version_2_3_2.approval_state = Approval_state.approved;
            version_2_3_2.save();


            // 3_1
            BlockoBlockVersion version_3_1_1 = new BlockoBlockVersion();
            version_3_1_1.blocko_block = blockoBlock_3_1;
            version_3_1_1.date_of_create = new Date();
            version_3_1_1.logic_json = "{}";
            version_3_1_1.design_json = "{}";
            version_3_1_1.version_description = "První update";
            version_3_1_1.version_name = "Verze 1";
            version_3_1_1.approval_state = Approval_state.approved;
            version_3_1_1.save();

            BlockoBlockVersion version_3_1_2 = new BlockoBlockVersion();
            version_3_1_2.blocko_block = blockoBlock_3_1;
            version_3_1_2.date_of_create = new Date();
            version_3_1_2.logic_json = "{}";
            version_3_1_2.design_json = "{}";
            version_3_1_2.version_description = "Druhý velkopeý asdklbfj aslaksbdfjlkbalskbdf lkjbafs lkjbafslbkjafslkjba sdflkbjasf update";
            version_3_1_2.version_name = "Verze 2";
            version_3_1_2.approval_state = Approval_state.approved;
            version_3_1_2.save();

            // 3_2
            BlockoBlockVersion version_3_2_1 = new BlockoBlockVersion();
            version_3_2_1.blocko_block = blockoBlock_3_2;
            version_3_2_1.date_of_create = new Date();
            version_3_2_1.logic_json = "{}";
            version_3_2_1.design_json = "{}";
            version_3_2_1.version_description = "První update";
            version_3_2_1.version_name = "1.0.1";
            version_3_2_1.approval_state = Approval_state.approved;
            version_3_2_1.save();

            BlockoBlockVersion version_3_2_2 = new BlockoBlockVersion();
            version_3_2_2.blocko_block = blockoBlock_3_2;
            version_3_2_2.date_of_create = new Date();
            version_3_2_2.logic_json = "{}";
            version_3_2_2.design_json = "{}";
            version_3_2_2.version_description = "První update";
            version_3_2_2.version_name = "1.0.2";
            version_3_2_2.approval_state = Approval_state.approved;
            version_3_2_2.save();

            BlockoBlockVersion version_3_2_3 = new BlockoBlockVersion();
            version_3_2_3.blocko_block = blockoBlock_3_2;
            version_3_2_3.date_of_create = new Date();
            version_3_2_3.logic_json = "{}";
            version_3_2_3.design_json = "{}";
            version_3_2_3.version_description = "První update";
            version_3_2_3.version_name = "1.1.3";
            version_3_2_3.approval_state = Approval_state.approved;
            version_3_2_3.save();

            // 3_3
            BlockoBlockVersion version_3_3_1 = new BlockoBlockVersion();
            version_3_3_1.blocko_block = blockoBlock_3_3;
            version_3_3_1.date_of_create = new Date();
            version_3_3_1.logic_json = "{}";
            version_3_3_1.design_json = "{}";
            version_3_3_1.version_description = "První update";
            version_3_3_1.version_name = "1.0.1";
            version_3_3_1.approval_state = Approval_state.approved;
            version_3_3_1.save();

            BlockoBlockVersion version_3_3_2 = new BlockoBlockVersion();
            version_3_3_2.blocko_block = blockoBlock_3_3;
            version_3_3_2.date_of_create = new Date();
            version_3_3_2.logic_json = "{}";
            version_3_3_2.design_json = "{}";
            version_3_3_2.version_description = "Druhý update";
            version_3_3_2.version_name = "1.0.2";
            version_3_3_2.approval_state = Approval_state.approved;
            version_3_3_2.save();

            // 3_4
            BlockoBlockVersion version_3_4_1 = new BlockoBlockVersion();
            version_3_4_1.blocko_block = blockoBlock_3_4;
            version_3_4_1.date_of_create = new Date();
            version_3_4_1.logic_json = "{}";
            version_3_4_1.design_json = "{}";
            version_3_4_1.version_description = "První update";
            version_3_4_1.version_name = "1.0.1";
            version_3_4_1.approval_state = Approval_state.approved;
            version_3_4_1.save();

            BlockoBlockVersion version_3_4_2 = new BlockoBlockVersion();
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
            if(TypeOfWidget.find.where().eq("name", "iOS Widgets").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

            TypeOfWidget typeOfWidget_1 = new TypeOfWidget();
            typeOfWidget_1.name =  "iOS Widgets";
            typeOfWidget_1.general_description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.";
            typeOfWidget_1.save();

            TypeOfWidget typeOfWidget_2 = new TypeOfWidget();
            typeOfWidget_2.name =  "Android Widgets";
            typeOfWidget_2.general_description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.";
            typeOfWidget_2.save();


            //**************************************************************************************************************


            //1
            GridWidget gridWidget_1_1 = new GridWidget();
            gridWidget_1_1.name = "Apple";
            gridWidget_1_1.author = Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            gridWidget_1_1.general_description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.";
            gridWidget_1_1.type_of_widget = typeOfWidget_1;
            gridWidget_1_1.save();

            GridWidget gridWidget_1_2 = new GridWidget();
            gridWidget_1_2.name = "Banana";
            gridWidget_1_2.author = Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            gridWidget_1_2.general_description = "Lorem ipsum di lasjdhflkj dshaflj  sadfsdfas dfsadf sad gsfgsdf sadfsd fas";
            gridWidget_1_2.type_of_widget = typeOfWidget_1;
            gridWidget_1_2.save();

            GridWidget gridWidget_1_3 = new GridWidget();
            gridWidget_1_3.name = "Orange";
            gridWidget_1_3.author = Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            gridWidget_1_3.general_description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.";
            gridWidget_1_3.type_of_widget = typeOfWidget_1;
            gridWidget_1_3.save();

            // 2
            GridWidget gridWidget_2_1 = new GridWidget();
            gridWidget_2_1.name = "Nice Widget";
            gridWidget_2_1.author = Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            gridWidget_2_1.general_description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.";
            gridWidget_2_1.type_of_widget = typeOfWidget_2;
            gridWidget_2_1.save();

            GridWidget gridWidget_2_2 = new GridWidget();
            gridWidget_2_2.name = "Ugly Widget";
            gridWidget_2_2.author = Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            gridWidget_2_2.general_description = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.";
            gridWidget_2_2.type_of_widget = typeOfWidget_2;
            gridWidget_2_2.save();


            //**************************************************************************************************************

            // 1_1
            GridWidgetVersion version_1_1_1 = new GridWidgetVersion();
            version_1_1_1.grid_widget = gridWidget_1_1;
            version_1_1_1.date_of_create = new Date();
            version_1_1_1.logic_json = "{}";
            version_1_1_1.design_json = "{}";
            version_1_1_1.version_description = "První update";
            version_1_1_1.version_name = "1.0.1";
            version_1_1_1.approval_state = Approval_state.approved;
            version_1_1_1.save();

            GridWidgetVersion version_1_1_2 = new GridWidgetVersion();
            version_1_1_2.grid_widget = gridWidget_1_1;
            version_1_1_2.date_of_create = new Date();
            version_1_1_2.logic_json = "{}";
            version_1_1_2.design_json = "{}";
            version_1_1_2.version_description = "První update";
            version_1_1_2.version_name = "1.0.1";
            version_1_1_2.approval_state = Approval_state.approved;
            version_1_1_2.save();

            // 1_2
            GridWidgetVersion version_1_2_1 = new GridWidgetVersion();
            version_1_2_1.grid_widget = gridWidget_1_2;
            version_1_2_1.date_of_create = new Date();
            version_1_2_1.logic_json = "{}";
            version_1_2_1.design_json = "{}";
            version_1_2_1.version_description = "První update";
            version_1_2_1.version_name = "1.0.1";
            version_1_2_1.approval_state = Approval_state.approved;
            version_1_2_1.save();

            GridWidgetVersion version_1_2_2 = new GridWidgetVersion();
            version_1_2_2.grid_widget = gridWidget_1_2;
            version_1_2_2.date_of_create = new Date();
            version_1_2_2.logic_json = "{}";
            version_1_2_2.design_json = "{}";
            version_1_2_2.version_description = "Druhý update";
            version_1_2_2.version_name = "1.0.2";
            version_1_2_2.approval_state = Approval_state.approved;
            version_1_2_2.save();

            // 1_3
            GridWidgetVersion version_1_3_1 = new GridWidgetVersion();
            version_1_3_1.grid_widget = gridWidget_1_3;
            version_1_3_1.date_of_create = new Date();
            version_1_3_1.logic_json = "{}";
            version_1_3_1.design_json = "{}";
            version_1_3_1.version_description = "První update";
            version_1_3_1.version_name = "1.0.1";
            version_1_3_1.approval_state = Approval_state.approved;
            version_1_3_1.save();

            GridWidgetVersion version_1_3_2 = new GridWidgetVersion();
            version_1_3_2.grid_widget = gridWidget_1_3;
            version_1_3_2.date_of_create = new Date();
            version_1_3_2.logic_json = "{}";
            version_1_3_2.design_json = "{}";
            version_1_3_2.version_description = "Druhý update";
            version_1_3_2.version_name = "1.0.2";
            version_1_3_2.approval_state = Approval_state.approved;
            version_1_3_2.save();

            // 2_1
            GridWidgetVersion version_2_1_1 = new GridWidgetVersion();
            version_2_1_1.grid_widget = gridWidget_2_1;
            version_2_1_1.date_of_create = new Date();
            version_2_1_1.logic_json = "{}";
            version_2_1_1.design_json = "{}";
            version_2_1_1.version_description = "První update";
            version_2_1_1.version_name = "1.0.1";
            version_2_1_1.approval_state = Approval_state.approved;
            version_2_1_1.save();

            GridWidgetVersion version_2_1_2 = new GridWidgetVersion();
            version_2_1_2.grid_widget = gridWidget_2_1;
            version_2_1_2.date_of_create = new Date();
            version_2_1_2.logic_json = "{}";
            version_2_1_2.design_json = "{}";
            version_2_1_2.version_description = "Prvnsafd -a.kshm fn.,mbs gjknbm akdfsm,.cxy ndfam,nkvxclůavcx namxyklnvdfsam ,cvklůdfsmv.lyům ,klnvyůmc,.í update";
            version_2_1_2.version_name = "1.0.2";
            version_2_1_2.approval_state = Approval_state.approved;
            version_2_1_2.save();

            // 2_2
            GridWidgetVersion version_2_2_1 = new GridWidgetVersion();
            version_2_2_1.grid_widget = gridWidget_2_2;
            version_2_2_1.date_of_create = new Date();
            version_2_2_1.logic_json = "{}";
            version_2_2_1.design_json = "{}";
            version_2_2_1.version_description = "První update";
            version_2_2_1.version_name = "1.0.1";
            version_2_2_1.approval_state = Approval_state.approved;
            version_2_2_1.save();

            GridWidgetVersion version_2_2_2 = new GridWidgetVersion();
            version_2_2_2.grid_widget = gridWidget_2_2;
            version_2_2_2.date_of_create = new Date();
            version_2_2_2.logic_json = "{}";
            version_2_2_2.design_json = "{}";
            version_2_2_2.version_description = "Druhý update";
            version_2_2_2.version_name = "1.0.2";
            version_2_2_2.approval_state = Approval_state.approved;
            version_2_2_2.save();

            GridWidgetVersion version_2_2_3= new GridWidgetVersion();
            version_2_2_3.grid_widget = gridWidget_2_2;
            version_2_2_3.date_of_create = new Date();
            version_2_2_3.logic_json = "{}";
            version_2_2_3.design_json = "{}";
            version_2_2_3.version_description = "Třetí update";
            version_2_2_3.version_name = "1.0.3";
            version_2_2_3.approval_state = Approval_state.approved;
            version_2_2_3.save();

            GridWidgetVersion version_2_2_4 = new GridWidgetVersion();
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
            if(C_Program.find.where().eq("name", "Default C_Program for Yoda").findUnique() != null) return GlobalResult.result_BadRequest("Its Already done!");

            TypeOfBoard yoda = TypeOfBoard.find.where().eq("name", "Yoda G2").findUnique();

            C_Program c_program_1 = new C_Program();
            c_program_1.name = "Default C_Program for Yoda";
            c_program_1.type_of_board = yoda;

            c_program_1.description = "For first version of every program";
            c_program_1.date_of_create = new Date();
            c_program_1.default_program_type_of_board = yoda;
            c_program_1.save();

            Version_Object version_object_1 = new Version_Object();
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
            FileRecord.uploadAzure_Version(content.toString(), "code.json" , c_program_1.get_path() ,  version_object_1);
            version_object_1.update();
            version_object_1.compile_program_thread();

            yoda.default_program = c_program_1;
            yoda.default_program.default_main_version = version_object_1;
            version_object_1.default_version_program = yoda.default_program;
            version_object_1.update();
            yoda.update();



            TypeOfBoard wireless = TypeOfBoard.find.where().eq("name", "Wireless G2").findUnique();

            C_Program c_program_2 = new C_Program();
            c_program_2.name = "Default C_Program for Wireless Kit";
            c_program_2.description = "You can used that for prototyping";
            c_program_2.type_of_board = yoda;

            c_program_2.description = "For first version of every program";
            c_program_2.date_of_create = new Date();
            c_program_2.default_program_type_of_board = wireless;
            c_program_2.save();

            Version_Object version_object_2 = new Version_Object();
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
            FileRecord.uploadAzure_Version(content.toString(), "code.json" , c_program_2.get_path() ,  version_object_2);
            version_object_2.update();
            version_object_2.compile_program_thread();

            wireless.default_program = c_program_2;
            wireless.default_program.default_main_version = version_object_2;

            version_object_2.default_version_program = wireless.default_program;

            version_object_2.update();
            wireless.update();


            TypeOfBoard busKit = TypeOfBoard.find.where().eq("name", "BUS G2").findUnique();

            C_Program c_program_3 = new C_Program();
            c_program_3.name = "Default C_Program for Bus Kit";
            c_program_3.type_of_board = busKit;

            c_program_3.description = "For first version of every program";
            c_program_3.date_of_create = new Date();
            c_program_3.default_program_type_of_board = busKit;
            c_program_3.save();

            Version_Object version_object_3 = new Version_Object();
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
            FileRecord.uploadAzure_Version(content.toString(), "code.json" , c_program_3.get_path() ,  version_object_3);
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

            if(Person.find.where().eq("nick_name", "Pepíno").findUnique() != null ) return GlobalResult.result_BadRequest("Its Already done!");

            String uuid = UUID.randomUUID().toString().substring(0,4);

            System.err.println("Vytvářím uživatele s emailem:  test_user@byzance.cz");
            System.err.println("Heslem: 123456789");
            System.err.println("Tokenem: token");

            // Vytvoří osobu
            Person person = new Person();
            person.full_name = "Pačmund Pepa";
            person.nick_name = "Pepíno";
            person.mail = "test_user@byzance.cz";
            person.freeze_account = false;
            person.mailValidated = true;
            person.setSha("123456789");
            person.save();


            FloatingPersonToken token = new FloatingPersonToken();
            token.person = person;
            token.authToken = "token";
            token.setDate();
            token.save();


            // Vytvoří tarif
            Product product = new Product();
            product.general_tariff = GeneralTariff.find.where().eq("identificator","alpha").findUnique();
            product.product_individual_name = "Pepkova velkolepá Alfa";
            product.active = true;  // Produkt jelikož je Aplha je aktivní - Alpha nebo Trial dojedou kvuli omezení času
            product.method = Payment_method.free;
            product.mode = Payment_mode.free;
            product.paid_until_the_day = new GregorianCalendar(2016, 12, 30).getTime();
            product.currency = Currency.CZK;
            Payment_Details payment_details = new Payment_Details();
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


            // Vytvořím Projekty
            Project project_1 = new Project();
            project_1.product = product;
            project_1.ownersOfProject.add(person);
            project_1.name = "První velkolepý projekt";
            project_1.description = "Toto je Pepkův velkolepý testovací projekt primárně určen pro testování Blocko Programu, kde už má zaregistrovaný testovací HW";
            project_1.save();

            Project project_2 = new Project();
            project_2.product = product;
            project_2.ownersOfProject.add(person);
            project_2.name = "Druhý prázdný testovací projekt";
            project_2.description = "Toto je Pepkův testovací projekt, kde nic ještě není";
            project_2.save();

            Project project_3 = new Project();
            project_3.product = product;
            project_3.ownersOfProject.add(person);
            project_3.name = "Třetí prázdný testovací projekt";
            project_3.description = "Toto je Pepkův třetí super testovací projekt, kde nic ještě není a ten blázen se musel dlouhosáhle rozepsat v description??? To jako vážně? Jste na to připravený v designu???? ?";
            project_3.save();

            // Zaregistruji pod ně Yody
            project_1.boards.add( Board.find.where().eq("personal_description","Yoda A").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","Yoda B").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","Yoda C").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","Yoda D").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","Yoda E").findUnique());

            Board yoda_1 = Board.find.where().eq("personal_description","Yoda A").findUnique();
            yoda_1.project = project_1;
            project_1.private_instance.boards_in_virtual_instance.add(yoda_1);
            yoda_1.update();

            Board yoda_2 = Board.find.where().eq("personal_description","Yoda B").findUnique();
            yoda_2.project = project_1;
            project_1.private_instance.boards_in_virtual_instance.add(yoda_2);
            yoda_2.update();


            Board yoda_3 = Board.find.where().eq("personal_description","Yoda C").findUnique();
            yoda_3.project = project_1;
            project_1.private_instance.boards_in_virtual_instance.add(yoda_3);
            yoda_3.update();


            Board yoda_4 = Board.find.where().eq("personal_description","Yoda D").findUnique();
            yoda_4.project = project_1;
            project_1.private_instance.boards_in_virtual_instance.add(yoda_4);
            yoda_4.update();

            Board yoda_5 = Board.find.where().eq("personal_description","Yoda E").findUnique();
            yoda_5.project = project_1;
            project_1.private_instance.boards_in_virtual_instance.add(yoda_5);
            yoda_5.update();

            project_1.update();

            // Bezdráty a Bezdráty
            project_1.boards.add( Board.find.where().eq("personal_description","[1]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[2]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[3]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[4]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[5]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[6]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[7]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[8]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[9]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[11]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[12]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[13]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[14]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[15]").findUnique());
            project_1.boards.add( Board.find.where().eq("personal_description","[16]").findUnique());


            Board board_1 = Board.find.where().eq("personal_description","[1]").findUnique();
            board_1.project = project_1;
            board_1.update();

            Board board_2 = Board.find.where().eq("personal_description","[2]").findUnique();
            board_2.project = project_1;
            board_2.update();

            Board board_3 = Board.find.where().eq("personal_description","[3]").findUnique();
            board_3.project = project_1;
            board_3.update();

            Board board_4 = Board.find.where().eq("personal_description","[4]").findUnique();
            board_4.project = project_1;
            board_4.update();

            Board board_5 = Board.find.where().eq("personal_description","[5]").findUnique();
            board_5.project = project_1;
            board_5.update();

            Board board_6 = Board.find.where().eq("personal_description","[6]").findUnique();
            board_6.project = project_1;
            board_6.update();

            Board board_7 = Board.find.where().eq("personal_description","[7]").findUnique();
            board_7.project = project_1;
            board_7.update();

            Board board_8 = Board.find.where().eq("personal_description","[8]").findUnique();
            board_8.project = project_1;
            board_8.update();

            Board board_9 = Board.find.where().eq("personal_description","[9]").findUnique();
            board_9.project = project_1;
            board_9.update();

            Board board_10 = Board.find.where().eq("personal_description","[10]").findUnique();
            board_10.project = project_1;
            board_10.update();

            Board board_11 = Board.find.where().eq("personal_description","[11]").findUnique();
            board_11.project = project_1;
            board_11.update();

            Board board_12 = Board.find.where().eq("personal_description","[12]").findUnique();
            board_12.project = project_1;
            board_12.update();

            Board board_13 = Board.find.where().eq("personal_description","[13]").findUnique();
            board_13.project = project_1;
            board_13.update();

            Board board_14 = Board.find.where().eq("personal_description","[14]").findUnique();
            board_14.project = project_1;
            board_14.update();

            Board board_15 = Board.find.where().eq("personal_description","[15]").findUnique();
            board_15.project = project_1;
            board_15.update();

            Board board_16 = Board.find.where().eq("personal_description","[16]").findUnique();
            board_16.project = project_1;
            board_16.update();


            // Vytvořím C_Programy YODA
            C_Program c_program_1 = new C_Program();
            c_program_1.date_of_create = new Date();
            c_program_1.name = "Defaultní program";
            c_program_1.type_of_board = TypeOfBoard.find.where().eq("name", "Yoda G2").findUnique();
            c_program_1.description = "Tento program je určen na blikání s ledkou";
            c_program_1.project = project_1;
            c_program_1.save();


            // Vytvořím C_Programy YODA
            C_Program c_program_2 = new C_Program();
            c_program_2.date_of_create = new Date();
            c_program_2.name = "Hraní si s tlačítkem Pro Yodu";
            c_program_2.type_of_board = TypeOfBoard.find.where().eq("name", "Yoda G2").findUnique();
            c_program_2.description = "Tento program je určen na testování tlačítka na yodovi";
            c_program_2.project = project_1;
            c_program_2.save();


            // Vytvořím C_Programy Bezdrát
            C_Program c_program_3 = new C_Program();
            c_program_3.date_of_create = new Date();
            c_program_3.name = "Tlačítko s ledkou na Bezdrátu";
            c_program_3.type_of_board = TypeOfBoard.find.where().eq("name", "Wireless G2").findUnique();
            c_program_3.description = "Tento program je určen na testování tlačítka na bezdrátovém modulu";
            c_program_3.project = project_1;
            c_program_3.save();


            // Vytvořím C_Programy Drát
            C_Program c_program_4 = new C_Program();
            c_program_4.date_of_create = new Date();
            c_program_4.name = "Tlačítko s ledkou na BUS kitu";
            c_program_4.type_of_board = TypeOfBoard.find.where().eq("name", "BUS G2").findUnique();
            c_program_4.description = "Tento program je určen na testování tlačítka na BUS modulu";
            c_program_4.project = project_1;
            c_program_4.save();


            // První verze C_Programu pro Yodu c_program_1
            Version_Object version_c_program_1 = new Version_Object();
            version_c_program_1.version_name = "Verze 0.0.1";
            version_c_program_1.version_description = "Když jem poprvé zkoušel blikat ledkou - Yoda";
            version_c_program_1.c_program = c_program_1;
            version_c_program_1.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_1 = Json.newObject();
            content_1.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_1.set("user_files", null);
            content_1.set("external_libraries", null);
            FileRecord.uploadAzure_Version(content_1.toString(), "code.json", c_program_1.get_path(), version_c_program_1);
            version_c_program_1.compile_program_procedure();
            version_c_program_1.update();

            // Druhá verze verze C_Programu pro YODU c_program_1
            Version_Object version_c_program_1_2 = new Version_Object();
            version_c_program_1_2.version_name = "Verze 0.0.2";
            version_c_program_1_2.version_description = "Když jem podruhé zkoušel blikat ledkou - Yoda";
            version_c_program_1_2.c_program = c_program_1;
            version_c_program_1_2.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_1_2 = Json.newObject();
            content_1_2.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_1_2.set("user_files", null);
            content_1_2.set("external_libraries", null);
            FileRecord.uploadAzure_Version(content_1_2.toString(), "code.json", c_program_1.get_path(), version_c_program_1_2);
            version_c_program_1_2.compile_program_procedure();
            version_c_program_1_2.update();


            // Druhá verze verze C_Programu pro YODU c_program_1
            Version_Object version_c_program_2 = new Version_Object();
            version_c_program_2.version_name = "Verze 0.0.1";
            version_c_program_2.version_description = "Když jiná věc pro Yodu poprvé";
            version_c_program_2.c_program = c_program_2;
            version_c_program_2.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_2 = Json.newObject();
            content_2.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_2.set("user_files", null);
            content_2.set("external_libraries", null);
            FileRecord.uploadAzure_Version(content_2.toString(), "code.json", c_program_2.get_path(), version_c_program_2);
            version_c_program_2.compile_program_procedure();
            version_c_program_2.update();


            // První verze  C_Programu pro Wireles c_program_3
            Version_Object version_c_program_3 = new Version_Object();
            version_c_program_3.version_name = "Verze 0.0.1";
            version_c_program_3.version_description = "Když jem poprvé Wireles";
            version_c_program_3.c_program = c_program_3;
            version_c_program_3.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_3 = Json.newObject();
            content_3.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_3.set("user_files", null);
            content_3.set("external_libraries", null);
            FileRecord.uploadAzure_Version(content_3.toString(), "code.json", c_program_3.get_path(), version_c_program_3);
            version_c_program_3.compile_program_procedure();
            version_c_program_3.update();


            // První verze  C_Programu pro BUS c_program_4
            Version_Object version_c_program_4 = new Version_Object();
            version_c_program_4.version_name = "Verze 0.0.1";
            version_c_program_4.version_description = "Když jem poprvé Drát";
            version_c_program_4.c_program = c_program_4;
            version_c_program_4.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_4 = Json.newObject();
            content_4.put("main", "/****************************************\r\n * Popis programu                       *\r\n ****************************************\r\n *\r\n * Zaregistruju si 2 tla\u010D\u00EDtka - Up a Down.\r\n * To jsou moje digit\u00E1ln\u00ED vstupy.\r\n * Zaregistruju si ledPwm, to je m\u016Fj analogov\u00FD v\u00FDstup.\r\n * Pokud stisknu tla\u010D\u00EDtko Up, po\u0161le se informace do Blocka.\r\n * Pokud stisknu tla\u010D\u00EDtko Down, po\u0161le se informace do Blocka.\r\n * V Blocku mus\u00ED b\u00FDt naprogramovan\u00E9, co se stane.\r\n * Nap\u0159. p\u0159i tla\u010D\u00EDtku Up se zv\u00FD\u0161\u00ED jas LEDky a p\u0159i Down se sn\u00ED\u017E\u00ED.\r\n *\r\n * D\u00E1le si inicializuju u\u017Eivatelsk\u00E9 tla\u010D\u00EDtko na desce.\r\n * Toto z\u00E1m\u011Brn\u011B neregistruju do Blocka, ale slou\u017E\u00ED mi jenom lok\u00E1ln\u011B.\r\n * Takt\u00E9\u017E si zaregistruju message out zp\u00E1vu.\r\n * Zpr\u00E1vu nav\u00E1\u017Eu uvnit\u0159 yody na tla\u010D\u00EDtko.\r\n * Ve zpr\u00E1v\u011B se po stisknut\u00ED tla\u010D\u00EDtka ode\u0161le do Blocka po\u010Det stisknut\u00ED tla\u010D\u00EDtka jako string.\r\n *\r\n * D\u00E1le, pokud p\u0159ijde z blocka digital IN, tak to rozsv\u00EDt\u00ED/zhasne zelenou ledku na desce.\r\n *\r\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\u011Bjak\u00E1 zpr\u00E1va p\u0159ijde, vyp\u00ED\u0161u ji do termin\u00E1lu.\r\n *\r\n */\r\n\r\n/*\r\n * na za\u010D\u00E1tku v\u017Edy mus\u00ED b\u00FDt tento \u0159\u00E1dek\r\n */\r\n#include \"byzance.h\"\r\n\r\n/*\r\n * inicializuju si LEDky (na desce)\r\n */\r\nDigitalOut\tledRed(LED_RED);\r\nDigitalOut\tledGrn(LED_GRN);\r\n\r\n/*\r\n * inicializuju si LEDky (vlastn\u00ED)\r\n */\r\nPwmOut\t\tledTom(X05);\r\n\r\n/*\r\n * inicializuju si USR tla\u010D\u00EDtko (na desce)\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED btnUsr.fall(&nazev_funkce);\r\n *\r\n */\r\nInterruptIn btnUsr(USER_BUTTON);\r\n\r\n/*\r\n * inicializuju si vlastn\u00ED tla\u010D\u00EDtka\r\n * co se stane po stisku tla\u010D\u00EDtka mus\u00EDm o\u0161et\u0159it v jeho callbacku\r\n * callback si zaregistruju v k\u00F3du funkc\u00ED\r\n * btnUp.fall(&nazev_funkce);\r\n * btnDown.fall(&nazev_funkce);\r\n *\r\n * InterruptIn je default pull down, tak\u017Ee se pin mus\u00ED p\u0159ipojit proti VCC.\r\n */\r\nInterruptIn btnUp(X00);\r\nInterruptIn btnDown(X02);\r\n\r\n/*\r\n * inicializuju si s\u00E9riovou linku\r\n */\r\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED vstupy\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_IN (led_green, {\r\n    ledGrn = value;\r\n    pc.printf(\"led_green: %d \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si analogov\u00E9 vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_ANALOG_IN(led_pwm, {\r\n    ledTom = value;\r\n    pc.printf(\"led_pwm: %f \\n\", value);\r\n})\r\n\r\n/*\r\n * Zaregistruju si message vstupy.\r\n * (to, co mi p\u0159ijde z Blocka DO desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\n\r\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\r\n    pc.printf(\"message_in=%s\\n\", arg1);\r\n});\r\n\r\n/*\r\n * Zaregistruju si message v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\r\n\r\n/*\r\n * Zaregistruju si digit\u00E1ln\u00ED v\u00FDstupy.\r\n * (to, co mi p\u0159ijde z Blocka Z desky)\r\n * Budou vid\u011Bt v Blocku.\r\n */\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\r\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\r\n\r\n/*\r\n * Prom\u011Bnn\u00E9 pot\u0159ebn\u00E9 pro program.\r\n */\r\nvolatile bool button_usr_clicked\t\t= 0;\r\nvolatile bool button_up_state\t\t\t= 0;\r\nvolatile bool button_up_last_state\t\t= 0;\r\nvolatile bool button_down_state\t\t\t= 0;\r\nvolatile bool button_down_last_state \t= 0;\r\n\r\nint button_usr_counter = 0;\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku USR tla\u010D\u00EDtka.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_usr_fall_callback(){\r\n\tpc.printf((const char*)\"Button USR clicked.\\n\");\r\n\tbutton_usr_clicked = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_fall_callback(){\r\n\tpc.printf((const char*)\"Button UP clicked.\\n\");\r\n\tbutton_up_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka UP.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_up_rise_callback(){\r\n\tpc.printf((const char*)\"Button UP released.\\n\");\r\n\tbutton_up_state = 0;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_fall_callback(){\r\n\tpc.printf((const char*)\"Button DOWN clicked.\\n\");\r\n\tbutton_down_state = 1;\r\n}\r\n\r\n/*\r\n * Callback, kter\u00FD bude vyvol\u00E1n po stisku extern\u00EDho tla\u010D\u00EDtka DOWN.\r\n * Tento callback si registruju v u\u017Eivatelsk\u00E9m k\u00F3du.\r\n */\r\nvoid button_down_rise_callback(){\r\n\tpc.printf((const char*)\"Button DOWN released.\\n\");\r\n\tbutton_down_state = 0;\r\n}\r\n\r\nint main(int argc, char* argv[]){\r\n\r\n\t/*\r\n\t * nastav\u00EDm si baud rychlost s\u00E9riov\u00E9 linky\r\n\t */\r\n    pc.baud(115200);\r\n\r\n    /*\r\n     * Inicializace Byzance knihovny\r\n     */\r\n    Byzance::init();\r\n    pc.printf(\"Byzance initialized\\n\");\r\n\r\n    /*\r\n     * P\u0159ipojen\u00ED na Byzance servery.\r\n     */\r\n\r\n    Byzance::connect();\r\n\tpc.printf(\"Succesfully connected to MQTT broker\\n\");\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku tla\u010D\u00EDtka USR\r\n\t */\r\n    btnUsr.fall(&button_usr_fall_callback);\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnUp.fall(&button_up_fall_callback);\r\n    btnUp.rise(&button_up_rise_callback);\r\n    btnUp.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n\t/*\r\n\t * p\u0159ipoj\u00ED callback, do kter\u00E9ho program sko\u010D\u00ED po stisku extern\u011B p\u0159ipojen\u00E9ho tla\u010D\u00EDtka UP\r\n\t */\r\n    btnDown.fall(&button_down_fall_callback);\r\n    btnDown.rise(&button_down_rise_callback);\r\n    btnDown.mode(PullUp); // toto musi byt za attachnuti callbacku\r\n\r\n    /*\r\n     * b\u011Bh programu\r\n     */\r\n    while(true) {\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * a pokud nab\u00FDv\u00E1 nenulov\u00E9 hodnoty, provedu funkce,\r\n    \t * co maj\u00ED nastat po zm\u00E1\u010Dknut\u00ED tla\u010D\u00EDtka\r\n    \t */\r\n    \tif(button_usr_clicked)\r\n    \t{\r\n    \t\tbutton_usr_clicked=0;\r\n    \t\tbutton_usr_counter++;\r\n\r\n    \t\tchar buffer[100];\r\n    \t\tsprintf(buffer, \"Pocet stisknuti = %d\\n\", button_usr_counter);\r\n    \t\tpc.printf(buffer);\r\n\r\n    \t\t/*\r\n    \t\t * Toto je funkce, kterou jsem si p\u0159ed startem programu zaregistroval\r\n    \t\t * tak\u017Ee bude vid\u011Bt v Blocku.\r\n    \t\t */\r\n    \t\tmessage_out_counter(buffer);\r\n    \t}\r\n\r\n    \t/*\r\n    \t * prom\u011Bnnou, co jsem naplnil v callbacku USR tla\u010D\u00EDtka si p\u0159e\u010Dtu\r\n    \t * pokud se zm\u011Bnila oproti p\u0159edchoz\u00ED kontrole, stisknul/pustil jsem tla\u010D\u00EDtko\r\n    \t */\r\n    \tif(button_up_state!=button_up_last_state)\r\n    \t{\r\n    \t\tbutton_up_last_state = button_up_state;\r\n    \t\tledGrn = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledGrn = 0;\r\n\r\n   \t\t\tdig_out_btn_up(button_up_state);\r\n\r\n   \t\t\tpc.printf(\"button_up_clicked = %d\\n\", button_up_state);\r\n    \t}\r\n\r\n    \tif(button_down_state!=button_down_last_state)\r\n    \t{\r\n    \t\tbutton_down_last_state = button_down_state;\r\n    \t\tledRed = 1;\r\n    \t\twait_ms(20);\r\n    \t\tledRed = 0;\r\n\r\n   \t\t\tdig_out_btn_down(button_down_state);\r\n\r\n   \t\t\tpc.printf(\"button_down_clicked = %d\\n\", button_down_state);\r\n    \t}\r\n\r\n        Thread::wait(100);\r\n    }\r\n}\r\n");
            content_4.set("user_files", null);
            content_4.set("external_libraries", null);
            FileRecord.uploadAzure_Version(content_4.toString(), "code.json", c_program_4.get_path(), version_c_program_4);
            version_c_program_4.compile_program_procedure();
            version_c_program_4.update();



            // První verze B_Programu - Pro instanc Yoda E a Ci!
            B_Program b_program_1 = new B_Program();
            b_program_1.name = "První blocko program";
            b_program_1.description = "Blocko program je úžasná věc když funguje... a tady v tomto progtramu už je připravený i HW!!!!";
            b_program_1.date_of_create = new Date();
            b_program_1.project = project_1;
            b_program_1.save();

            Version_Object instace_version = Version_Object.find.where().eq("b_program.name","První blocko program").findUnique();

            Version_Object version_b_program_1 = new Version_Object();
            version_b_program_1.version_name = "Blocko Verze č.1";
            version_b_program_1.version_description = "Snažím se tu dělat veklé věci";
            version_b_program_1.date_of_create = new Date();
            version_b_program_1.b_program = b_program_1;


            // Instance 1 (TOM - YODA E a Yoda C)
            B_Program_Hw_Group group_1 = new B_Program_Hw_Group();
            // Main Boad - Yoda
            B_Pair main_1 = new B_Pair();
            main_1.board = Board.find.where().eq("personal_description", "Yoda E").findUnique();
            main_1.c_program_version = version_c_program_1;

            group_1.main_board_pair = main_1;

            // Bus
            B_Pair device_16 = new B_Pair();
            device_16.board = Board.find.where().eq("personal_description", "[16]").findUnique();
            device_16.c_program_version = version_c_program_4;
            group_1.device_board_pairs.add(device_16);

            // Bus
            B_Pair device_15 = new B_Pair();
            device_15.board = Board.find.where().eq("personal_description", "[15]").findUnique();
            device_15.c_program_version = version_c_program_4;
            group_1.device_board_pairs.add(device_15);

            // Bus
            B_Pair device_2 = new B_Pair();
            device_2.board =Board.find.where().eq("personal_description", "[2]").findUnique();
            device_2.c_program_version = version_c_program_4;
            group_1.device_board_pairs.add(device_2);

            // Bezdrát
            B_Pair device_13 = new B_Pair();
            device_13.board =Board.find.where().eq("personal_description", "[13]").findUnique();
            device_13.c_program_version = version_c_program_3;
            group_1.device_board_pairs.add(device_13);

            // Bezdrát
            B_Pair device_14 = new B_Pair();
            device_14.board =Board.find.where().eq("personal_description", "[14]").findUnique();
            device_14.c_program_version = version_c_program_3;
            group_1.device_board_pairs.add(device_14);


            B_Program_Hw_Group group_2 = new B_Program_Hw_Group();
            // Main Board - Yoda
            B_Pair main_2 = new B_Pair();
            main_2.board = Board.find.where().eq("personal_description", "Yoda C").findUnique();
            main_2.c_program_version = version_c_program_2;

            group_2.main_board_pair = main_2;

            version_b_program_1.b_program_hw_groups.add(group_1);
            version_b_program_1.b_program_hw_groups.add(group_2);
            version_b_program_1.save();
            version_b_program_1.refresh();

            FileRecord.uploadAzure_Version("Blocko Program zde!", "program.js", b_program_1.get_path() , version_b_program_1);


            // Druhý B_Program - Pro instanci Yoda B!
            // Instance 2 - Martinův Yoda
            B_Program b_program_2 = new B_Program();
            b_program_2.name = "Druhý blocko program";
            b_program_2.description = "Tento program má sloužit Martinovi";
            b_program_2.date_of_create = new Date();
            b_program_2.project = project_1;
            b_program_2.save();

            // První verze B_Programu - Pro instanci!
            Version_Object version_b_program_2 = new Version_Object();
            version_b_program_2.version_name = "Blocko Verze č.1";
            version_b_program_2.version_description = "Snažím se tu dělat veklé věci";
            version_b_program_2.date_of_create = new Date();
            version_b_program_2.b_program = b_program_2;


            B_Program_Hw_Group group_3 = new B_Program_Hw_Group();

            B_Pair main_3 = new B_Pair();
            main_3.board = Board.find.where().eq("personal_description", "Yoda B").findUnique();
            main_3.c_program_version = version_c_program_1;

            group_3.main_board_pair = main_3;
            version_b_program_2.b_program_hw_groups.add(group_3);
            version_b_program_2.save();
            version_b_program_2.refresh();


            // Instance 3 - Viktorv Yoda
            // Instance 3 - Voktorův Yoda
            B_Program b_program_3 = new B_Program();
            b_program_3.name = "Třetí blocko program";
            b_program_3.description = "Tento program má sloužit Viktorovi";
            b_program_3.date_of_create = new Date();
            b_program_3.project = project_1;
            b_program_3.save();

            // První verze B_Programu - Pro instanci!
            Version_Object version_b_program_3 = new Version_Object();
            version_b_program_3.version_name = "Blocko Verze č.1";
            version_b_program_3.version_description = "Snažím se tu dělat veklé věci";
            version_b_program_3.date_of_create = new Date();
            version_b_program_3.b_program = b_program_3;


            B_Program_Hw_Group group_4 = new B_Program_Hw_Group();

            // Main A - yoda
            B_Pair main_4 = new B_Pair();
            main_4.board = Board.find.where().eq("personal_description", "Yoda A").findUnique();
            main_4.c_program_version = version_c_program_1;
            group_4.main_board_pair = main_4;


            // BUS
            B_Pair device_1 = new B_Pair();
            device_1.board =Board.find.where().eq("personal_description", "[1]").findUnique();
            device_1.c_program_version = version_c_program_4;
            group_4.device_board_pairs.add(device_1);


            // BUS
            B_Pair device_3 = new B_Pair();
            device_3.board =Board.find.where().eq("personal_description", "[3]").findUnique();
            device_3.c_program_version = version_c_program_4;
            group_4.device_board_pairs.add(device_3);


            // BUS
            B_Pair device_4 = new B_Pair();
            device_4.board =Board.find.where().eq("personal_description", "[4]").findUnique();
            device_4.c_program_version = version_c_program_4;
            group_4.device_board_pairs.add(device_4);

            // BUS
            B_Pair device_5 = new B_Pair();
            device_5.board =Board.find.where().eq("personal_description", "[5]").findUnique();
            device_5.c_program_version = version_c_program_4;
            group_4.device_board_pairs.add(device_5);


            // BUS
            B_Pair device_8 = new B_Pair();
            device_8.board =Board.find.where().eq("personal_description", "[8]").findUnique();
            device_8.c_program_version = version_c_program_4;
            group_4.device_board_pairs.add(device_8);

            // BUS
            B_Pair device_9 = new B_Pair();
            device_9.board =Board.find.where().eq("personal_description", "[9]").findUnique();
            device_9.c_program_version = version_c_program_4;
            group_4.device_board_pairs.add(device_9);


            // Bezdrát
            B_Pair device_6 = new B_Pair();
            device_6.board =Board.find.where().eq("personal_description", "[6]").findUnique();
            device_6.c_program_version = version_c_program_3;
            group_4.device_board_pairs.add(device_6);

            // Bezdrát
            B_Pair device_7 = new B_Pair();
            device_7.board =Board.find.where().eq("personal_description", "[7]").findUnique();
            device_7.c_program_version = version_c_program_3;
            group_4.device_board_pairs.add(device_7);

            version_b_program_3.b_program_hw_groups.add(group_4);
            version_b_program_3.save();
            version_b_program_3.refresh();


            // Instance 4 - Davidův Yoda - Nefuknční Yoda


            // Instance 3 - Voktorův Yoda
            B_Program b_program_4 = new B_Program();
                b_program_4.name = "Čtvrtý blocko program - Určený pro Yodu D";
                b_program_4.description = "Tento program má sloužit Davidovi";
                b_program_4.date_of_create = new Date();
                b_program_4.project = project_1;
                b_program_4.save();

            // První verze B_Programu - Pro instanci!
            Version_Object version_b_program_4 = new Version_Object();
                version_b_program_4.version_name = "Blocko Verze č.1";
                version_b_program_4.version_description = "Snažím se tu dělat veklé věci";
                version_b_program_4.date_of_create = new Date();
                version_b_program_4.b_program = b_program_4;

            B_Program_Hw_Group group_5 = new B_Program_Hw_Group();

            B_Pair main_5 = new B_Pair();
            main_5.board = Board.find.where().eq("personal_description", "Yoda D").findUnique();
            main_5.c_program_version = version_c_program_1;

            group_5.main_board_pair = main_5;
            version_b_program_4.b_program_hw_groups.add(group_5);
            version_b_program_4.save();

            M_Project m_project = new M_Project();
            m_project.project = project_1;
            m_project.name = "Velkolepá kolekce terminálových přístupů";
            m_project.description = "Tak tady si pepa dělá všechny svoje super cool apky!!! Je to fakt mazec!! a V připadě updatu je autoincrement true - což znamená že systém v případě updatu lidem na teminálech updatuje verzi";
            m_project.date_of_create = new Date();
            m_project.save();

            M_Program m_program_1 = new M_Program();
            m_program_1.m_project = m_project;
            m_program_1.date_of_create = new Date();
            m_program_1.description = "První verze se snad zdařila!!! Yahoooo!!!!";
            m_program_1.name = "Velký M PRogram";
            m_program_1.save();


            Version_Object m_program_version_object_1 = new Version_Object();
            m_program_version_object_1.version_description = "Toto je první verze!";
            m_program_version_object_1.version_name = "1.0.0";
            m_program_version_object_1.m_program = m_program_1;
            m_program_version_object_1.public_version = false;
            m_program_version_object_1.qr_token = "randooooooooo_uuuuuuid";
            m_program_version_object_1.save();

            ObjectNode content = Json.newObject();
            content.put("m_code", "{}");
            content.put("virtual_input_output", "{}");

            FileRecord.uploadAzure_Version(content.toString(), "m_program.json" , m_program_1.get_path() ,  m_program_version_object_1);
            m_program_version_object_1.save();

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

            Version_Object instace_version = Version_Object.find.where().eq("b_program.name","První blocko program").findUnique();

            Homer_Instance_Record record = new Homer_Instance_Record();
            record.main_instance_history = instace_version.b_program.instance;
            record.actual_running_instance = instace_version.b_program.instance;
            record.version_object = instace_version;
            record.date_of_created = new Date();
            record.save();

            record.main_instance_history.add_instance_to_server();


            Version_Object instace_version_2 = Version_Object.find.where().eq("b_program.name","Druhý blocko program").findUnique();

            Homer_Instance_Record record_2 = new Homer_Instance_Record();
            record_2.main_instance_history = instace_version_2.b_program.instance;
            record_2.actual_running_instance = instace_version_2.b_program.instance;
            record_2.version_object = instace_version_2;
            record_2.date_of_created = new Date();
            record_2.save();

            record_2.main_instance_history.add_instance_to_server();


            Version_Object instace_version_3 = Version_Object.find.where().eq("b_program.name","Třetí blocko program").findUnique();

            Homer_Instance_Record record_3 = new Homer_Instance_Record();
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
