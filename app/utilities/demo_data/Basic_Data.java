package utilities.demo_data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.blocko.BlockoBlock;
import models.blocko.BlockoBlockVersion;
import models.blocko.TypeOfBlock;
import models.compiler.*;
import models.person.FloatingPersonToken;
import models.person.Person;
import models.project.b_program.B_Pair;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Hw_Group;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.c_program.C_Program;
import models.project.global.Product;
import models.project.global.Project;
import models.project.global.financial.Payment_Details;
import play.api.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.UtilTools;
import utilities.enums.Approval_state;
import utilities.enums.Payment_mode;
import utilities.enums.Product_Type;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class Basic_Data {

    public static void  set_default_objects(){

        if(Producer.find.where().eq("name", "Byzance ltd").findUnique() != null ) {
            System.err.println("Defaultní Objekty jsou nastavené - nelze provést znovu");
            return;
        }

        set_default_object_PRODUCER_AND_BOARDS();
        set_default_object_EXTERNAL_SERVERS();
        set_default_object_BLOCKO();

    }


    public static void set_default_object_PRODUCER_AND_BOARDS() {
        // Nastavím Producer
        Producer producer    = new Producer();
        producer.name        = "Byzance ltd";
        producer.description = "Developed with love from Byzance ltd";
        producer.save();

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
    }


    public static void set_default_object_EXTERNAL_SERVERS(){

        // Nasstavím Homer servery
        Cloud_Homer_Server cloud_server_1 = new Cloud_Homer_Server();
        cloud_server_1.server_name = "Alfa";
        cloud_server_1.destination_address = Server.tyrion_webSocketAddress + "/websocket/homer_server/" + cloud_server_1.server_name;
        cloud_server_1.set_hash_certificate();
        cloud_server_1.save();

        Cloud_Homer_Server cloud_server_2 = new Cloud_Homer_Server();
        cloud_server_2.server_name = "Beta";
        cloud_server_2.destination_address = Server.tyrion_webSocketAddress + "/websocket/homer_server/" + cloud_server_2.server_name;
        cloud_server_2.set_hash_certificate();

        // Nastavím kompilační servery
        Cloud_Compilation_Server compilation_server_1 = new Cloud_Compilation_Server();
        compilation_server_1.server_name = "Alfa";
        compilation_server_1.destination_address = Server.tyrion_webSocketAddress + "/websocket/compilation_server/" + compilation_server_1.server_name;
        compilation_server_1.set_hash_certificate();
        compilation_server_1.save();

        Cloud_Compilation_Server compilation_server_2 = new Cloud_Compilation_Server();
        compilation_server_2.server_name = "ubuntu1";
        compilation_server_2.destination_address = Server.tyrion_webSocketAddress + "/websocket/compilation_server/" + compilation_server_2.server_name;
        compilation_server_2.set_hash_certificate();
        compilation_server_2.save();

    }

    public static void set_default_object_BLOCKO(){

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


    }



    public static void set_default_object_NECO(){




    }

    public static void set_basic_demo_data(){

        try {

            if(Person.find.where().eq("nick_name", "pepa2").findUnique() != null ) {
                System.err.println("Databáze je již naplněna - neprovádím žádné další změny");
                return;
            }

            String uuid = UUID.randomUUID().toString().substring(0,4);

            System.err.println("Vytvářím uživatele s emailem:  test_user@byzance.cz");
            System.err.println("Heslem: 123456789");
            System.err.println("Tokenem: token");

            // Vytvoří osobu
            Person person = new Person();
            person.full_name = "Testovací Pepa";
            person.nick_name = "pepa2";
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
            product.type = Product_Type.alpha;
            product.product_individual_name = "Pepkova velkolepá Alfa";
            product.active = true;  // Produkt jelikož je Aplha je aktivní - Alpha nebo Trial dojedou kvuli omezení času
            product.mode = Payment_mode.free;
            product.paid_until_the_day = new GregorianCalendar(2016, 12, 30).getTime();
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
            project_1.project_name = "První velkolepý projekt";
            project_1.project_description = "Toto je Pepkův velkolepý testovací projekt primárně určen pro testování Blocko Programu, kde už má zaregistrovaný testovací HW";
            project_1.save();

            Project project_2 = new Project();
            project_2.product = product;
            project_2.ownersOfProject.add(person);
            project_2.project_name = "Druhý prázdný testovací projekt";
            project_2.project_description = "Toto je Pepkův testovací projekt, kde nic ještě není";
            project_2.save();

            Project project_3 = new Project();
            project_3.product = product;
            project_3.ownersOfProject.add(person);
            project_3.project_name = "Třetí prázdný testovací projekt";
            project_3.project_description = "Toto je Pepkův třetí super testovací projekt, kde nic ještě není a ten blázen se musel dlouhosáhle rozepsat v description??? To jako vážně? Jste na to připravený v designu???? ?";
            project_3.save();

            // Zaregistruji pod ně Yody



            // Zaregistruji Yody
            Board board_yoda_1 = new Board();
            board_yoda_1.id = "002600513533510B34353732";
            board_yoda_1.personal_description = "Martinův Yoda";
            board_yoda_1.type_of_board = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_YODAG2").findUnique();
            board_yoda_1.date_of_create = new Date();
            board_yoda_1.project = project_1;
            board_yoda_1.save();

            Board board_yoda_2 = new Board();
            board_yoda_2.id = "003E00523533510B34353732";
            board_yoda_2.personal_description = "Tomův Yoda";
            board_yoda_2.type_of_board =TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_YODAG2").findUnique();
            board_yoda_2.date_of_create = new Date();
            board_yoda_2.project = project_1;
            board_yoda_2.save();

            Board board_yoda_3 = new Board();
            board_yoda_3.id = "004C00523533510B34353732";
            board_yoda_3.personal_description = "Yoda s WIFI";
            board_yoda_3.type_of_board = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_YODAG2").findUnique();
            board_yoda_3.date_of_create = new Date();
            board_yoda_3.project = project_1;
            board_yoda_3.save();

            Board board_yoda_4 = new Board();
            board_yoda_4.id = "002300513533510B34353732";
            board_yoda_4.personal_description = "Viktorův Yoda";
            board_yoda_4.type_of_board = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_YODAG2").findUnique();
            board_yoda_4.date_of_create = new Date();
            board_yoda_4.project = project_1;
            board_yoda_4.save();

            //project_1.boards.add(  Board.find.byId("002600513533510B34353732") );   // Davidův

            // Zaregistuji Devices
            Board wireless_1 = new Board();
            wireless_1.id = "EEEEEEEEEE_999999999";
            wireless_1.personal_description = "Bezdrátový device 9";
            wireless_1.type_of_board = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_WRLSKITG2").findUnique();
            wireless_1.date_of_create = new Date();
            wireless_1.project = project_1;
            wireless_1.save();

            Board wireless_2 = new Board();
            wireless_2.id = "EEEEEEEEEE_999999998";
            wireless_2.personal_description = "Bezdrátový device 8";
            wireless_2.type_of_board = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_WRLSKITG2").findUnique();
            wireless_2.date_of_create = new Date();
            wireless_2.project = project_1;
            wireless_2.save();

            Board wireless_3 = new Board();
            wireless_3.id = "EEEEEEEEEE_999999997";
            wireless_3.personal_description = "Bezdrátový device 7";
            wireless_3.type_of_board = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_WRLSKITG2").findUnique();
            wireless_3.date_of_create = new Date();
            wireless_3.project = project_1;
            wireless_3.save();


            Board bus_1 = new Board();
            bus_1.id = "BBBBBBBBBB_999999999";
            bus_1.personal_description = "Bus device 9";
            bus_1.type_of_board = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_BUSKITG2").findUnique();
            bus_1.date_of_create = new Date();
            bus_1.project = project_1;
            bus_1.save();

            Board bus_2 = new Board();
            bus_2.id = "BBBBBBBBBB_999999998";
            bus_2.personal_description = "Bus device 8";
            bus_2.type_of_board = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_BUSKITG2").findUnique();
            bus_2.date_of_create = new Date();
            bus_2.project = project_1;
            bus_2.save();

            Board bus_3 = new Board();
            bus_3.id = "BBBBBBBBBB_999999997";
            bus_3.personal_description = "Bus device 7";
            bus_3.type_of_board = TypeOfBoard.find.where().eq("compiler_target_name", "BYZANCE_BUSKITG2").findUnique();
            bus_3.date_of_create = new Date();
            bus_3.project = project_1;
            bus_3.save();




            // Vytvořím C_Programy YODA
            C_Program c_program_1 = new C_Program();
            c_program_1.date_of_create = new Date();
            c_program_1.program_name = "Blikání s Ledkou Pro Yodu";
            c_program_1.type_of_board = TypeOfBoard.find.where().eq("name", "Yoda G2").findUnique();
            c_program_1.program_description = "Tento program je určen na blikání s ledkou";
            c_program_1.project = project_1;
            c_program_1.save();


            // Vytvořím C_Programy YODA
            C_Program c_program_2 = new C_Program();
            c_program_2.date_of_create = new Date();
            c_program_2.program_name = "Hraní si s tlačítkem Pro Yodu";
            c_program_2.type_of_board = TypeOfBoard.find.where().eq("name", "Yoda G2").findUnique();
            c_program_2.program_description = "Tento program je určen na testování tlačítka na yodovi";
            c_program_2.project = project_1;
            c_program_2.save();


            // Vytvořím C_Programy Bezdrát
            C_Program c_program_3 = new C_Program();
            c_program_3.date_of_create = new Date();
            c_program_3.program_name = "Tlačítko na Bezdrátu";
            c_program_3.type_of_board = TypeOfBoard.find.where().eq("name", "Wireless G2").findUnique();
            c_program_3.program_description = "Tento program je určen na testování tlačítka na bezdrátovém modulu";
            c_program_3.project = project_1;
            c_program_3.save();


            // Vytvořím C_Programy Drát
            C_Program c_program_4 = new C_Program();
            c_program_4.date_of_create = new Date();
            c_program_4.program_name = "Tlačítko na BUS kitu";
            c_program_4.type_of_board = TypeOfBoard.find.where().eq("name", "BUS G2").findUnique();
            c_program_4.program_description = "Tento program je určen na testování tlačítka na BUS modulu";
            c_program_4.project = project_1;
            c_program_4.save();


            // První verze C_Programu pro Yodu c_program_1
            Version_Object version_c_program_1 = new Version_Object();
                version_c_program_1.version_name = "Verze 0.0.1";
                version_c_program_1.version_name = "Když jem poprvé zkoušel blikat ledkou";
                version_c_program_1.c_program = c_program_1;
                version_c_program_1.save();

                // Nahraje do Azure a připojí do verze soubor
                ObjectNode content_1 = Json.newObject();
                content_1.put("main", "#include \\\"mbed.h\\\"\\r\\n\\r\\n// nasledujici 2 radky jsou urceny pouze pro interni debug byzance knihovny\\r\\n/*\\r\\n#include \\\"ByzanceLogger.h\\\"\\r\\n#define MQTT_DEBUG 1\\r\\n*/\\r\\n\\r\\n#include \\\"byzance.h\\\"\\r\\n\\r\\nDigitalOut\\tledRed(LED_RED);\\r\\nDigitalOut\\tledGrn(LED_GREEN);\\r\\nInterruptIn button(USER_BUTTON);\\r\\n\\r\\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\\r\\n\\r\\n/*\\r\\n * digital in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_DIGITAL_IN (led_red, {\\r\\n    ledRed = value;\\r\\n    pc.printf(\\\"DIN1: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n\\r\\nBYZANCE_DIGITAL_IN(led_green, {\\r\\n    ledGrn = value;\\r\\n    pc.printf(\\\"DIN2: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\nBYZANCE_DIGITAL_IN(din555, {\\r\\n    pc.printf(\\\"DIN555: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n/*\\r\\n * analog in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_ANALOG_IN(pwmled1, {\\r\\n    //ledPwm1 = value;\\r\\n    pc.printf(\\\"AIN1: %f \\\\n\\\", value);\\r\\n})\\r\\n\\r\\nBYZANCE_ANALOG_IN(ain10, {\\r\\n    pc.printf(\\\"AIN10: %f \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n/*\\r\\n * message in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_MESSAGE_IN(test1, ByzanceString, ByzanceBool, ByzanceInt, {\\r\\n    pc.printf(\\\"test1\\\\n\\\");\\r\\n    pc.printf(\\\"arg1=%s\\\\n\\\", arg1);\\r\\n    pc.printf(\\\"arg2=%d\\\\n\\\", arg2);\\r\\n    pc.printf(\\\"arg3=%ld\\\\n\\\", arg3);\\r\\n});\\r\\n\\r\\nBYZANCE_MESSAGE_IN(test2, ByzanceFloat, ByzanceFloat, {\\r\\n\\tpc.printf(\\\"test2\\\\n\\\");\\r\\n\\tpc.printf(\\\"arg1=%f\\\\n\\\", arg1);\\r\\n\\tpc.printf(\\\"arg2=%f\\\\n\\\", arg2);\\r\\n});\\r\\n\\r\\n/*\\r\\n * message out registrations\\r\\n */\\r\\n\\r\\nBYZANCE_MESSAGE_OUT(click1, ByzanceString);\\r\\nBYZANCE_MESSAGE_OUT(out3, ByzanceInt, ByzanceInt, ByzanceInt, ByzanceInt);\\r\\nBYZANCE_MESSAGE_OUT(test666, ByzanceString, ByzanceString, ByzanceInt, ByzanceString, ByzanceString, ByzanceFloat, ByzanceBool, ByzanceString);\\r\\n\\r\\n/*\\r\\n * digital out registrations\\r\\n */\\r\\nBYZANCE_DIGITAL_OUT(clicked1);\\r\\n\\r\\n/*\\r\\n * analog out registrations\\r\\n */\\r\\nBYZANCE_ANALOG_OUT(analog1);\\r\\n\\r\\nbool button_clicked = 0;\\r\\nstatic int click_counter;\\r\\nchar buffer[100];\\r\\n\\r\\nvoid button_callback(){\\r\\n\\r\\n\\t// vypíše do terminálu\\r\\n    pc.printf(\\\"Button clicked!\\\\n\\\");\\r\\n\\r\\n    button_clicked = 1;\\r\\n    /*\\r\\n     *\\r\\n     * BLBE JE TO, ZE POKUD SEM DO CALLBACKU DAM KTEROUKOLIV Z NASICH FUNKCI\\r\\n     * PROCESOR ZAMRZNE\\r\\n     * V MAINU FUNKCE FUNGUJI OK\\r\\n     * MUZE TO BYT S TIM, ZE TOTO JE PRERUSENI A V PRERUSENI SE BLBE PRACUJE S TIMERAMA/DELAYEMA A TAK,\\r\\n     * PROTOZE JSOU TO VETSINOU TAKY PRERUSENI\\r\\n    */\\r\\n}\\r\\n\\r\\nint main(int argc, char* argv[])\\r\\n{\\r\\n\\r\\n\\t//ByzanceLogger::init_serial(SERIAL_TX, SERIAL_RX);\\r\\n\\r\\n    pc.baud(115200);\\r\\n\\r\\n    BYZANCE_UNUSED_ARG(argc);\\r\\n    BYZANCE_UNUSED_ARG(argv);\\r\\n\\r\\n    pc.printf(\\\"Compiled on %02d. %02d. %04d - %02d:%02d:%02d\\\\n\\\", __BUILD_DAY__, __BUILD_MONTH__, __BUILD_YEAR_LEN4__, __BUILD_HOUR__, __BUILD_MINUTE__, __BUILD_SECOND__);\\r\\n    pc.printf(\\\"Firmware build ID is %s\\\\n\\\", TOSTRING(__BUILD_ID__));\\r\\n    // verze společné knihovny ByzanceCore\\r\\n    pc.printf(\\\"%s:\\\\t %d.%d.%d;\\\\n\\\", (char*)BYZANCE_NAME, (unsigned int)BYZANCE_VERSION_MAJOR, (unsigned int)BYZANCE_VERSION_MINOR, (unsigned int)BYZANCE_VERSION_SUBMINOR);\\r\\n    //verze specifické knihovny podle typu targetu\\r\\n\\t pc.printf(\\\"%s:\\\\t %d.%d.%d;\\\\n\\\", (char*)BYZANCE_LIB_NAME, (unsigned int)BYZANCE_LIB_VERSION_MAJOR, (unsigned int)BYZANCE_LIB_VERSION_MINOR, (unsigned int)BYZANCE_LIB_VERSION_SUBMINOR);\\r\\n    pc.printf(\\\"Waiting for connection...\\\\n\\\");\\r\\n\\r\\n    // pokud se byzance knihovně nepodaří připojit k serveru (vrátí nenulové číslo),\\r\\n    // tak se resetne procesor a zkusí to znovu\\r\\n\\tif(Byzance::connect()) NVIC_SystemReset();\\r\\n\\tpc.printf(\\\"Succesfully connected to MQTT broker\\\\n\\\");\\r\\n\\r\\n\\t// připojí callback (adresu funkce button callback) při stisknutí tlačítka\\r\\n    button.fall(&button_callback);\\r\\n\\r\\n    //set_time(1467994200);\\r\\n\\r\\n    /*\\r\\n    char* vysledek;\\r\\n\\r\\n    vysledek = setlocale(LC_ALL, \\\"cs_CZ\\\");\\r\\n    if(vysledek==NULL){\\r\\n    \\tpc.printf(\\\"Napicu\\\\n\\\");\\r\\n    \\twait_ms(2000);\\r\\n    }\\r\\n\\r\\n\\r\\n    //char buffer[32];\\r\\n     */\\r\\n\\r\\n\\r\\n    while(true) {\\r\\n\\r\\n/*\\r\\n    \\t  time_t rawtime;\\r\\n    \\t  struct tm * timeinfo;\\r\\n    \\t  char buffer [80];\\r\\n\\r\\n    \\t  struct lconv * lc;\\r\\n\\r\\n    \\t  time ( &rawtime );\\r\\n    \\t  timeinfo = localtime ( &rawtime );\\r\\n\\r\\n    \\t  int twice=0;\\r\\n\\r\\n    \\t  do {\\r\\n    \\t    pc.printf (\\\"Locale is: %s\\\\n\\\", setlocale(LC_ALL,NULL) );\\r\\n\\r\\n    \\t    strftime (buffer,80,\\\"%c\\\",timeinfo);\\r\\n    \\t    pc.printf (\\\"Date is: %s\\\\n\\\",buffer);\\r\\n\\r\\n    \\t    lc = localeconv ();\\r\\n    \\t    pc.printf (\\\"Currency symbol is: %s\\\\n-\\\\n\\\",lc->currency_symbol);\\r\\n\\r\\n    \\t    setlocale (LC_ALL,\\\"\\\");\\r\\n    \\t  } while (!twice++);\\r\\n*/\\r\\n\\r\\n    \\tif(button_clicked)\\r\\n    \\t{\\r\\n    \\t\\tbutton_clicked=0;\\r\\n    \\t    sprintf(buffer, \\\"Click #%d\\\", click_counter);\\r\\n    \\t    click1(buffer);\\r\\n    \\t    analog1(66.55f);\\r\\n    \\t    out3(11, 22, 33, 44);\\r\\n    \\t    test666(\\\"Frantisek\\\", \\\"Dobrota\\\", 666, \\\"rodak\\\", \\\"z blizke\\\", 12346.789, true, \\\"vesnice\\\");\\r\\n    \\t    click_counter++;\\r\\n    \\t}\\r\\n\\r\\n    \\t// bez nejakeho Thread::wait to zatim nefunguje,\\r\\n    \\t// protoze main vlakno (asi) zabere 100% vykonu procesoru\\r\\n    \\t// a nedostane se na byzance knihovnu\\r\\n    \\t// mozna by stacilo main vlaknu snizit prioritu a zvysit prioritu vlaknum v byzance knihovne\\r\\n        Thread::wait(100);\\r\\n    }\\r\\n}\\r\\n");
                content_1.set("user_files", Json.parse("[{\"file_name\":\"Koloběžka\",\"code\":\"//nic\"},{\"file_name\":\"autíčko\",\"code\":\"//nic\"}]"));
                //content_1.put("external_libraries", null);
                UtilTools.uploadAzure_Version(content_1.toString(), "code.json", c_program_1.get_path(), version_c_program_1);
                version_c_program_1.refresh();

            // Druhá verze verze C_Programu pro YODU c_program_1
            Version_Object version_c_program_2 = new Version_Object();
                version_c_program_2.version_name = "Verze 0.0.2";
                version_c_program_2.version_name = "Když jem podruhé a snad finálně zkoušel blikat ledkou";
                version_c_program_2.c_program = c_program_1;
                version_c_program_2.approval_state = Approval_state.pending;
                version_c_program_2.save();

                // Nahraje do Azure a připojí do verze soubor
                ObjectNode content_2 = Json.newObject();
                content_2.put("main", "#include \\\"mbed.h\\\"\\r\\n\\r\\n// nasledujici 2 radky jsou urceny pouze pro interni debug byzance knihovny\\r\\n/*\\r\\n#include \\\"ByzanceLogger.h\\\"\\r\\n#define MQTT_DEBUG 1\\r\\n*/\\r\\n\\r\\n#include \\\"byzance.h\\\"\\r\\n\\r\\nDigitalOut\\tledRed(LED_RED);\\r\\nDigitalOut\\tledGrn(LED_GREEN);\\r\\nInterruptIn button(USER_BUTTON);\\r\\n\\r\\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\\r\\n\\r\\n/*\\r\\n * digital in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_DIGITAL_IN (led_red, {\\r\\n    ledRed = value;\\r\\n    pc.printf(\\\"DIN1: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n\\r\\nBYZANCE_DIGITAL_IN(led_green, {\\r\\n    ledGrn = value;\\r\\n    pc.printf(\\\"DIN2: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\nBYZANCE_DIGITAL_IN(din555, {\\r\\n    pc.printf(\\\"DIN555: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n/*\\r\\n * analog in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_ANALOG_IN(pwmled1, {\\r\\n    //ledPwm1 = value;\\r\\n    pc.printf(\\\"AIN1: %f \\\\n\\\", value);\\r\\n})\\r\\n\\r\\nBYZANCE_ANALOG_IN(ain10, {\\r\\n    pc.printf(\\\"AIN10: %f \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n/*\\r\\n * message in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_MESSAGE_IN(test1, ByzanceString, ByzanceBool, ByzanceInt, {\\r\\n    pc.printf(\\\"test1\\\\n\\\");\\r\\n    pc.printf(\\\"arg1=%s\\\\n\\\", arg1);\\r\\n    pc.printf(\\\"arg2=%d\\\\n\\\", arg2);\\r\\n    pc.printf(\\\"arg3=%ld\\\\n\\\", arg3);\\r\\n});\\r\\n\\r\\nBYZANCE_MESSAGE_IN(test2, ByzanceFloat, ByzanceFloat, {\\r\\n\\tpc.printf(\\\"test2\\\\n\\\");\\r\\n\\tpc.printf(\\\"arg1=%f\\\\n\\\", arg1);\\r\\n\\tpc.printf(\\\"arg2=%f\\\\n\\\", arg2);\\r\\n});\\r\\n\\r\\n/*\\r\\n * message out registrations\\r\\n */\\r\\n\\r\\nBYZANCE_MESSAGE_OUT(click1, ByzanceString);\\r\\nBYZANCE_MESSAGE_OUT(out3, ByzanceInt, ByzanceInt, ByzanceInt, ByzanceInt);\\r\\nBYZANCE_MESSAGE_OUT(test666, ByzanceString, ByzanceString, ByzanceInt, ByzanceString, ByzanceString, ByzanceFloat, ByzanceBool, ByzanceString);\\r\\n\\r\\n/*\\r\\n * digital out registrations\\r\\n */\\r\\nBYZANCE_DIGITAL_OUT(clicked1);\\r\\n\\r\\n/*\\r\\n * analog out registrations\\r\\n */\\r\\nBYZANCE_ANALOG_OUT(analog1);\\r\\n\\r\\nbool button_clicked = 0;\\r\\nstatic int click_counter;\\r\\nchar buffer[100];\\r\\n\\r\\nvoid button_callback(){\\r\\n\\r\\n\\t// vypíše do terminálu\\r\\n    pc.printf(\\\"Button clicked!\\\\n\\\");\\r\\n\\r\\n    button_clicked = 1;\\r\\n    /*\\r\\n     *\\r\\n     * BLBE JE TO, ZE POKUD SEM DO CALLBACKU DAM KTEROUKOLIV Z NASICH FUNKCI\\r\\n     * PROCESOR ZAMRZNE\\r\\n     * V MAINU FUNKCE FUNGUJI OK\\r\\n     * MUZE TO BYT S TIM, ZE TOTO JE PRERUSENI A V PRERUSENI SE BLBE PRACUJE S TIMERAMA/DELAYEMA A TAK,\\r\\n     * PROTOZE JSOU TO VETSINOU TAKY PRERUSENI\\r\\n    */\\r\\n}\\r\\n\\r\\nint main(int argc, char* argv[])\\r\\n{\\r\\n\\r\\n\\t//ByzanceLogger::init_serial(SERIAL_TX, SERIAL_RX);\\r\\n\\r\\n    pc.baud(115200);\\r\\n\\r\\n    BYZANCE_UNUSED_ARG(argc);\\r\\n    BYZANCE_UNUSED_ARG(argv);\\r\\n\\r\\n    pc.printf(\\\"Compiled on %02d. %02d. %04d - %02d:%02d:%02d\\\\n\\\", __BUILD_DAY__, __BUILD_MONTH__, __BUILD_YEAR_LEN4__, __BUILD_HOUR__, __BUILD_MINUTE__, __BUILD_SECOND__);\\r\\n    pc.printf(\\\"Firmware build ID is %s\\\\n\\\", TOSTRING(__BUILD_ID__));\\r\\n    // verze společné knihovny ByzanceCore\\r\\n    pc.printf(\\\"%s:\\\\t %d.%d.%d;\\\\n\\\", (char*)BYZANCE_NAME, (unsigned int)BYZANCE_VERSION_MAJOR, (unsigned int)BYZANCE_VERSION_MINOR, (unsigned int)BYZANCE_VERSION_SUBMINOR);\\r\\n    //verze specifické knihovny podle typu targetu\\r\\n\\t pc.printf(\\\"%s:\\\\t %d.%d.%d;\\\\n\\\", (char*)BYZANCE_LIB_NAME, (unsigned int)BYZANCE_LIB_VERSION_MAJOR, (unsigned int)BYZANCE_LIB_VERSION_MINOR, (unsigned int)BYZANCE_LIB_VERSION_SUBMINOR);\\r\\n    pc.printf(\\\"Waiting for connection...\\\\n\\\");\\r\\n\\r\\n    // pokud se byzance knihovně nepodaří připojit k serveru (vrátí nenulové číslo),\\r\\n    // tak se resetne procesor a zkusí to znovu\\r\\n\\tif(Byzance::connect()) NVIC_SystemReset();\\r\\n\\tpc.printf(\\\"Succesfully connected to MQTT broker\\\\n\\\");\\r\\n\\r\\n\\t// připojí callback (adresu funkce button callback) při stisknutí tlačítka\\r\\n    button.fall(&button_callback);\\r\\n\\r\\n    //set_time(1467994200);\\r\\n\\r\\n    /*\\r\\n    char* vysledek;\\r\\n\\r\\n    vysledek = setlocale(LC_ALL, \\\"cs_CZ\\\");\\r\\n    if(vysledek==NULL){\\r\\n    \\tpc.printf(\\\"Napicu\\\\n\\\");\\r\\n    \\twait_ms(2000);\\r\\n    }\\r\\n\\r\\n\\r\\n    //char buffer[32];\\r\\n     */\\r\\n\\r\\n\\r\\n    while(true) {\\r\\n\\r\\n/*\\r\\n    \\t  time_t rawtime;\\r\\n    \\t  struct tm * timeinfo;\\r\\n    \\t  char buffer [80];\\r\\n\\r\\n    \\t  struct lconv * lc;\\r\\n\\r\\n    \\t  time ( &rawtime );\\r\\n    \\t  timeinfo = localtime ( &rawtime );\\r\\n\\r\\n    \\t  int twice=0;\\r\\n\\r\\n    \\t  do {\\r\\n    \\t    pc.printf (\\\"Locale is: %s\\\\n\\\", setlocale(LC_ALL,NULL) );\\r\\n\\r\\n    \\t    strftime (buffer,80,\\\"%c\\\",timeinfo);\\r\\n    \\t    pc.printf (\\\"Date is: %s\\\\n\\\",buffer);\\r\\n\\r\\n    \\t    lc = localeconv ();\\r\\n    \\t    pc.printf (\\\"Currency symbol is: %s\\\\n-\\\\n\\\",lc->currency_symbol);\\r\\n\\r\\n    \\t    setlocale (LC_ALL,\\\"\\\");\\r\\n    \\t  } while (!twice++);\\r\\n*/\\r\\n\\r\\n    \\tif(button_clicked)\\r\\n    \\t{\\r\\n    \\t\\tbutton_clicked=0;\\r\\n    \\t    sprintf(buffer, \\\"Click #%d\\\", click_counter);\\r\\n    \\t    click1(buffer);\\r\\n    \\t    analog1(66.55f);\\r\\n    \\t    out3(11, 22, 33, 44);\\r\\n    \\t    test666(\\\"Frantisek\\\", \\\"Dobrota\\\", 666, \\\"rodak\\\", \\\"z blizke\\\", 12346.789, true, \\\"vesnice\\\");\\r\\n    \\t    click_counter++;\\r\\n    \\t}\\r\\n\\r\\n    \\t// bez nejakeho Thread::wait to zatim nefunguje,\\r\\n    \\t// protoze main vlakno (asi) zabere 100% vykonu procesoru\\r\\n    \\t// a nedostane se na byzance knihovnu\\r\\n    \\t// mozna by stacilo main vlaknu snizit prioritu a zvysit prioritu vlaknum v byzance knihovne\\r\\n        Thread::wait(100);\\r\\n    }\\r\\n}\\r\\n");
                content_2.set("user_files", Json.parse("[{\"file_name\":\"Koloběžka\",\"code\":\"//nic\"},{\"file_name\":\"autíčko\",\"code\":\"//nic\"}]") );
               // content_2.put("external_libraries", "");
                UtilTools.uploadAzure_Version(content_2.toString(), "code.json", c_program_1.get_path(), version_c_program_2);
                version_c_program_2.refresh();



            // První verze  C_Programu pro Wireles c_program_3
            Version_Object version_c_program_3 = new Version_Object();
                version_c_program_3.version_name = "Verze 0.0.1";
                version_c_program_3.version_name = "Když jem podruhé a snad finálně zkoušel blikat ledkou";
                version_c_program_3.c_program = c_program_3;
                version_c_program_3.approval_state = Approval_state.pending;
                version_c_program_3.save();

                // Nahraje do Azure a připojí do verze soubor
                ObjectNode content_3 = Json.newObject();
                content_3.put("main", "#include \\\"mbed.h\\\"\\r\\n\\r\\n// nasledujici 2 radky jsou urceny pouze pro interni debug byzance knihovny\\r\\n/*\\r\\n#include \\\"ByzanceLogger.h\\\"\\r\\n#define MQTT_DEBUG 1\\r\\n*/\\r\\n\\r\\n#include \\\"byzance.h\\\"\\r\\n\\r\\nDigitalOut\\tledRed(LED_RED);\\r\\nDigitalOut\\tledGrn(LED_GREEN);\\r\\nInterruptIn button(USER_BUTTON);\\r\\n\\r\\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\\r\\n\\r\\n/*\\r\\n * digital in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_DIGITAL_IN (led_red, {\\r\\n    ledRed = value;\\r\\n    pc.printf(\\\"DIN1: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n\\r\\nBYZANCE_DIGITAL_IN(led_green, {\\r\\n    ledGrn = value;\\r\\n    pc.printf(\\\"DIN2: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\nBYZANCE_DIGITAL_IN(din555, {\\r\\n    pc.printf(\\\"DIN555: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n/*\\r\\n * analog in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_ANALOG_IN(pwmled1, {\\r\\n    //ledPwm1 = value;\\r\\n    pc.printf(\\\"AIN1: %f \\\\n\\\", value);\\r\\n})\\r\\n\\r\\nBYZANCE_ANALOG_IN(ain10, {\\r\\n    pc.printf(\\\"AIN10: %f \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n/*\\r\\n * message in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_MESSAGE_IN(test1, ByzanceString, ByzanceBool, ByzanceInt, {\\r\\n    pc.printf(\\\"test1\\\\n\\\");\\r\\n    pc.printf(\\\"arg1=%s\\\\n\\\", arg1);\\r\\n    pc.printf(\\\"arg2=%d\\\\n\\\", arg2);\\r\\n    pc.printf(\\\"arg3=%ld\\\\n\\\", arg3);\\r\\n});\\r\\n\\r\\nBYZANCE_MESSAGE_IN(test2, ByzanceFloat, ByzanceFloat, {\\r\\n\\tpc.printf(\\\"test2\\\\n\\\");\\r\\n\\tpc.printf(\\\"arg1=%f\\\\n\\\", arg1);\\r\\n\\tpc.printf(\\\"arg2=%f\\\\n\\\", arg2);\\r\\n});\\r\\n\\r\\n/*\\r\\n * message out registrations\\r\\n */\\r\\n\\r\\nBYZANCE_MESSAGE_OUT(click1, ByzanceString);\\r\\nBYZANCE_MESSAGE_OUT(out3, ByzanceInt, ByzanceInt, ByzanceInt, ByzanceInt);\\r\\nBYZANCE_MESSAGE_OUT(test666, ByzanceString, ByzanceString, ByzanceInt, ByzanceString, ByzanceString, ByzanceFloat, ByzanceBool, ByzanceString);\\r\\n\\r\\n/*\\r\\n * digital out registrations\\r\\n */\\r\\nBYZANCE_DIGITAL_OUT(clicked1);\\r\\n\\r\\n/*\\r\\n * analog out registrations\\r\\n */\\r\\nBYZANCE_ANALOG_OUT(analog1);\\r\\n\\r\\nbool button_clicked = 0;\\r\\nstatic int click_counter;\\r\\nchar buffer[100];\\r\\n\\r\\nvoid button_callback(){\\r\\n\\r\\n\\t// vypíše do terminálu\\r\\n    pc.printf(\\\"Button clicked!\\\\n\\\");\\r\\n\\r\\n    button_clicked = 1;\\r\\n    /*\\r\\n     *\\r\\n     * BLBE JE TO, ZE POKUD SEM DO CALLBACKU DAM KTEROUKOLIV Z NASICH FUNKCI\\r\\n     * PROCESOR ZAMRZNE\\r\\n     * V MAINU FUNKCE FUNGUJI OK\\r\\n     * MUZE TO BYT S TIM, ZE TOTO JE PRERUSENI A V PRERUSENI SE BLBE PRACUJE S TIMERAMA/DELAYEMA A TAK,\\r\\n     * PROTOZE JSOU TO VETSINOU TAKY PRERUSENI\\r\\n    */\\r\\n}\\r\\n\\r\\nint main(int argc, char* argv[])\\r\\n{\\r\\n\\r\\n\\t//ByzanceLogger::init_serial(SERIAL_TX, SERIAL_RX);\\r\\n\\r\\n    pc.baud(115200);\\r\\n\\r\\n    BYZANCE_UNUSED_ARG(argc);\\r\\n    BYZANCE_UNUSED_ARG(argv);\\r\\n\\r\\n    pc.printf(\\\"Compiled on %02d. %02d. %04d - %02d:%02d:%02d\\\\n\\\", __BUILD_DAY__, __BUILD_MONTH__, __BUILD_YEAR_LEN4__, __BUILD_HOUR__, __BUILD_MINUTE__, __BUILD_SECOND__);\\r\\n    pc.printf(\\\"Firmware build ID is %s\\\\n\\\", TOSTRING(__BUILD_ID__));\\r\\n    // verze společné knihovny ByzanceCore\\r\\n    pc.printf(\\\"%s:\\\\t %d.%d.%d;\\\\n\\\", (char*)BYZANCE_NAME, (unsigned int)BYZANCE_VERSION_MAJOR, (unsigned int)BYZANCE_VERSION_MINOR, (unsigned int)BYZANCE_VERSION_SUBMINOR);\\r\\n    //verze specifické knihovny podle typu targetu\\r\\n\\t pc.printf(\\\"%s:\\\\t %d.%d.%d;\\\\n\\\", (char*)BYZANCE_LIB_NAME, (unsigned int)BYZANCE_LIB_VERSION_MAJOR, (unsigned int)BYZANCE_LIB_VERSION_MINOR, (unsigned int)BYZANCE_LIB_VERSION_SUBMINOR);\\r\\n    pc.printf(\\\"Waiting for connection...\\\\n\\\");\\r\\n\\r\\n    // pokud se byzance knihovně nepodaří připojit k serveru (vrátí nenulové číslo),\\r\\n    // tak se resetne procesor a zkusí to znovu\\r\\n\\tif(Byzance::connect()) NVIC_SystemReset();\\r\\n\\tpc.printf(\\\"Succesfully connected to MQTT broker\\\\n\\\");\\r\\n\\r\\n\\t// připojí callback (adresu funkce button callback) při stisknutí tlačítka\\r\\n    button.fall(&button_callback);\\r\\n\\r\\n    //set_time(1467994200);\\r\\n\\r\\n    /*\\r\\n    char* vysledek;\\r\\n\\r\\n    vysledek = setlocale(LC_ALL, \\\"cs_CZ\\\");\\r\\n    if(vysledek==NULL){\\r\\n    \\tpc.printf(\\\"Napicu\\\\n\\\");\\r\\n    \\twait_ms(2000);\\r\\n    }\\r\\n\\r\\n\\r\\n    //char buffer[32];\\r\\n     */\\r\\n\\r\\n\\r\\n    while(true) {\\r\\n\\r\\n/*\\r\\n    \\t  time_t rawtime;\\r\\n    \\t  struct tm * timeinfo;\\r\\n    \\t  char buffer [80];\\r\\n\\r\\n    \\t  struct lconv * lc;\\r\\n\\r\\n    \\t  time ( &rawtime );\\r\\n    \\t  timeinfo = localtime ( &rawtime );\\r\\n\\r\\n    \\t  int twice=0;\\r\\n\\r\\n    \\t  do {\\r\\n    \\t    pc.printf (\\\"Locale is: %s\\\\n\\\", setlocale(LC_ALL,NULL) );\\r\\n\\r\\n    \\t    strftime (buffer,80,\\\"%c\\\",timeinfo);\\r\\n    \\t    pc.printf (\\\"Date is: %s\\\\n\\\",buffer);\\r\\n\\r\\n    \\t    lc = localeconv ();\\r\\n    \\t    pc.printf (\\\"Currency symbol is: %s\\\\n-\\\\n\\\",lc->currency_symbol);\\r\\n\\r\\n    \\t    setlocale (LC_ALL,\\\"\\\");\\r\\n    \\t  } while (!twice++);\\r\\n*/\\r\\n\\r\\n    \\tif(button_clicked)\\r\\n    \\t{\\r\\n    \\t\\tbutton_clicked=0;\\r\\n    \\t    sprintf(buffer, \\\"Click #%d\\\", click_counter);\\r\\n    \\t    click1(buffer);\\r\\n    \\t    analog1(66.55f);\\r\\n    \\t    out3(11, 22, 33, 44);\\r\\n    \\t    test666(\\\"Frantisek\\\", \\\"Dobrota\\\", 666, \\\"rodak\\\", \\\"z blizke\\\", 12346.789, true, \\\"vesnice\\\");\\r\\n    \\t    click_counter++;\\r\\n    \\t}\\r\\n\\r\\n    \\t// bez nejakeho Thread::wait to zatim nefunguje,\\r\\n    \\t// protoze main vlakno (asi) zabere 100% vykonu procesoru\\r\\n    \\t// a nedostane se na byzance knihovnu\\r\\n    \\t// mozna by stacilo main vlaknu snizit prioritu a zvysit prioritu vlaknum v byzance knihovne\\r\\n        Thread::wait(100);\\r\\n    }\\r\\n}\\r\\n");
                content_3.set("user_files", Json.parse("[{\"file_name\":\"Koloběžka\",\"code\":\"//nic\"},{\"file_name\":\"autíčko\",\"code\":\"//nic\"}]") );
                // content_2.put("external_libraries", "");
                UtilTools.uploadAzure_Version(content_3.toString(), "code.json", c_program_3.get_path(), version_c_program_3);
                version_c_program_3.refresh();


            // První verze  C_Programu pro BUS c_program_4
            Version_Object version_c_program_4 = new Version_Object();
                version_c_program_4.version_name = "Verze 0.0.1";
                version_c_program_4.version_name = "Když jem podruhé a snad finálně zkoušel blikat ledkou";
                version_c_program_4.c_program = c_program_4;
                version_c_program_4.save();

                // Nahraje do Azure a připojí do verze soubor
                ObjectNode content_4 = Json.newObject();
                content_4.put("main", "#include \\\"mbed.h\\\"\\r\\n\\r\\n// nasledujici 2 radky jsou urceny pouze pro interni debug byzance knihovny\\r\\n/*\\r\\n#include \\\"ByzanceLogger.h\\\"\\r\\n#define MQTT_DEBUG 1\\r\\n*/\\r\\n\\r\\n#include \\\"byzance.h\\\"\\r\\n\\r\\nDigitalOut\\tledRed(LED_RED);\\r\\nDigitalOut\\tledGrn(LED_GREEN);\\r\\nInterruptIn button(USER_BUTTON);\\r\\n\\r\\nSerial pc(SERIAL_TX, SERIAL_RX); // tx, rx\\r\\n\\r\\n/*\\r\\n * digital in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_DIGITAL_IN (led_red, {\\r\\n    ledRed = value;\\r\\n    pc.printf(\\\"DIN1: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n\\r\\nBYZANCE_DIGITAL_IN(led_green, {\\r\\n    ledGrn = value;\\r\\n    pc.printf(\\\"DIN2: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\nBYZANCE_DIGITAL_IN(din555, {\\r\\n    pc.printf(\\\"DIN555: %d \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n/*\\r\\n * analog in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_ANALOG_IN(pwmled1, {\\r\\n    //ledPwm1 = value;\\r\\n    pc.printf(\\\"AIN1: %f \\\\n\\\", value);\\r\\n})\\r\\n\\r\\nBYZANCE_ANALOG_IN(ain10, {\\r\\n    pc.printf(\\\"AIN10: %f \\\\n\\\", value);\\r\\n})\\r\\n\\r\\n/*\\r\\n * message in registrations\\r\\n */\\r\\n\\r\\nBYZANCE_MESSAGE_IN(test1, ByzanceString, ByzanceBool, ByzanceInt, {\\r\\n    pc.printf(\\\"test1\\\\n\\\");\\r\\n    pc.printf(\\\"arg1=%s\\\\n\\\", arg1);\\r\\n    pc.printf(\\\"arg2=%d\\\\n\\\", arg2);\\r\\n    pc.printf(\\\"arg3=%ld\\\\n\\\", arg3);\\r\\n});\\r\\n\\r\\nBYZANCE_MESSAGE_IN(test2, ByzanceFloat, ByzanceFloat, {\\r\\n\\tpc.printf(\\\"test2\\\\n\\\");\\r\\n\\tpc.printf(\\\"arg1=%f\\\\n\\\", arg1);\\r\\n\\tpc.printf(\\\"arg2=%f\\\\n\\\", arg2);\\r\\n});\\r\\n\\r\\n/*\\r\\n * message out registrations\\r\\n */\\r\\n\\r\\nBYZANCE_MESSAGE_OUT(click1, ByzanceString);\\r\\nBYZANCE_MESSAGE_OUT(out3, ByzanceInt, ByzanceInt, ByzanceInt, ByzanceInt);\\r\\nBYZANCE_MESSAGE_OUT(test666, ByzanceString, ByzanceString, ByzanceInt, ByzanceString, ByzanceString, ByzanceFloat, ByzanceBool, ByzanceString);\\r\\n\\r\\n/*\\r\\n * digital out registrations\\r\\n */\\r\\nBYZANCE_DIGITAL_OUT(clicked1);\\r\\n\\r\\n/*\\r\\n * analog out registrations\\r\\n */\\r\\nBYZANCE_ANALOG_OUT(analog1);\\r\\n\\r\\nbool button_clicked = 0;\\r\\nstatic int click_counter;\\r\\nchar buffer[100];\\r\\n\\r\\nvoid button_callback(){\\r\\n\\r\\n\\t// vypíše do terminálu\\r\\n    pc.printf(\\\"Button clicked!\\\\n\\\");\\r\\n\\r\\n    button_clicked = 1;\\r\\n    /*\\r\\n     *\\r\\n     * BLBE JE TO, ZE POKUD SEM DO CALLBACKU DAM KTEROUKOLIV Z NASICH FUNKCI\\r\\n     * PROCESOR ZAMRZNE\\r\\n     * V MAINU FUNKCE FUNGUJI OK\\r\\n     * MUZE TO BYT S TIM, ZE TOTO JE PRERUSENI A V PRERUSENI SE BLBE PRACUJE S TIMERAMA/DELAYEMA A TAK,\\r\\n     * PROTOZE JSOU TO VETSINOU TAKY PRERUSENI\\r\\n    */\\r\\n}\\r\\n\\r\\nint main(int argc, char* argv[])\\r\\n{\\r\\n\\r\\n\\t//ByzanceLogger::init_serial(SERIAL_TX, SERIAL_RX);\\r\\n\\r\\n    pc.baud(115200);\\r\\n\\r\\n    BYZANCE_UNUSED_ARG(argc);\\r\\n    BYZANCE_UNUSED_ARG(argv);\\r\\n\\r\\n    pc.printf(\\\"Compiled on %02d. %02d. %04d - %02d:%02d:%02d\\\\n\\\", __BUILD_DAY__, __BUILD_MONTH__, __BUILD_YEAR_LEN4__, __BUILD_HOUR__, __BUILD_MINUTE__, __BUILD_SECOND__);\\r\\n    pc.printf(\\\"Firmware build ID is %s\\\\n\\\", TOSTRING(__BUILD_ID__));\\r\\n    // verze společné knihovny ByzanceCore\\r\\n    pc.printf(\\\"%s:\\\\t %d.%d.%d;\\\\n\\\", (char*)BYZANCE_NAME, (unsigned int)BYZANCE_VERSION_MAJOR, (unsigned int)BYZANCE_VERSION_MINOR, (unsigned int)BYZANCE_VERSION_SUBMINOR);\\r\\n    //verze specifické knihovny podle typu targetu\\r\\n\\t pc.printf(\\\"%s:\\\\t %d.%d.%d;\\\\n\\\", (char*)BYZANCE_LIB_NAME, (unsigned int)BYZANCE_LIB_VERSION_MAJOR, (unsigned int)BYZANCE_LIB_VERSION_MINOR, (unsigned int)BYZANCE_LIB_VERSION_SUBMINOR);\\r\\n    pc.printf(\\\"Waiting for connection...\\\\n\\\");\\r\\n\\r\\n    // pokud se byzance knihovně nepodaří připojit k serveru (vrátí nenulové číslo),\\r\\n    // tak se resetne procesor a zkusí to znovu\\r\\n\\tif(Byzance::connect()) NVIC_SystemReset();\\r\\n\\tpc.printf(\\\"Succesfully connected to MQTT broker\\\\n\\\");\\r\\n\\r\\n\\t// připojí callback (adresu funkce button callback) při stisknutí tlačítka\\r\\n    button.fall(&button_callback);\\r\\n\\r\\n    //set_time(1467994200);\\r\\n\\r\\n    /*\\r\\n    char* vysledek;\\r\\n\\r\\n    vysledek = setlocale(LC_ALL, \\\"cs_CZ\\\");\\r\\n    if(vysledek==NULL){\\r\\n    \\tpc.printf(\\\"Napicu\\\\n\\\");\\r\\n    \\twait_ms(2000);\\r\\n    }\\r\\n\\r\\n\\r\\n    //char buffer[32];\\r\\n     */\\r\\n\\r\\n\\r\\n    while(true) {\\r\\n\\r\\n/*\\r\\n    \\t  time_t rawtime;\\r\\n    \\t  struct tm * timeinfo;\\r\\n    \\t  char buffer [80];\\r\\n\\r\\n    \\t  struct lconv * lc;\\r\\n\\r\\n    \\t  time ( &rawtime );\\r\\n    \\t  timeinfo = localtime ( &rawtime );\\r\\n\\r\\n    \\t  int twice=0;\\r\\n\\r\\n    \\t  do {\\r\\n    \\t    pc.printf (\\\"Locale is: %s\\\\n\\\", setlocale(LC_ALL,NULL) );\\r\\n\\r\\n    \\t    strftime (buffer,80,\\\"%c\\\",timeinfo);\\r\\n    \\t    pc.printf (\\\"Date is: %s\\\\n\\\",buffer);\\r\\n\\r\\n    \\t    lc = localeconv ();\\r\\n    \\t    pc.printf (\\\"Currency symbol is: %s\\\\n-\\\\n\\\",lc->currency_symbol);\\r\\n\\r\\n    \\t    setlocale (LC_ALL,\\\"\\\");\\r\\n    \\t  } while (!twice++);\\r\\n*/\\r\\n\\r\\n    \\tif(button_clicked)\\r\\n    \\t{\\r\\n    \\t\\tbutton_clicked=0;\\r\\n    \\t    sprintf(buffer, \\\"Click #%d\\\", click_counter);\\r\\n    \\t    click1(buffer);\\r\\n    \\t    analog1(66.55f);\\r\\n    \\t    out3(11, 22, 33, 44);\\r\\n    \\t    test666(\\\"Frantisek\\\", \\\"Dobrota\\\", 666, \\\"rodak\\\", \\\"z blizke\\\", 12346.789, true, \\\"vesnice\\\");\\r\\n    \\t    click_counter++;\\r\\n    \\t}\\r\\n\\r\\n    \\t// bez nejakeho Thread::wait to zatim nefunguje,\\r\\n    \\t// protoze main vlakno (asi) zabere 100% vykonu procesoru\\r\\n    \\t// a nedostane se na byzance knihovnu\\r\\n    \\t// mozna by stacilo main vlaknu snizit prioritu a zvysit prioritu vlaknum v byzance knihovne\\r\\n        Thread::wait(100);\\r\\n    }\\r\\n}\\r\\n");
                content_4.set("user_files", Json.parse("[{\"file_name\":\"Koloběžka\",\"code\":\"//nic\"},{\"file_name\":\"autíčko\",\"code\":\"//nic\"}]") );
                // content_2.put("external_libraries", "");
                UtilTools.uploadAzure_Version(content_4.toString(), "code.json", c_program_4.get_path(), version_c_program_4);
                version_c_program_4.refresh();


            Thread compile_that = new Thread() {
                @Override
                public void run() {
                    try {

                        F.Promise<WSResponse> responsePromise_1 = Play.current().injector().instanceOf(WSClient.class).url(Server.tyrion_serverAddress + "/compilation/c_program/version/compile/" + version_c_program_1.id)
                                .setContentType("undefined").setMethod("PUT").setHeader("X-AUTH-TOKEN", token.authToken).setRequestTimeout(50000).put("");

                        JsonNode result_1 = responsePromise_1.get(50000).asJson();

                        F.Promise<WSResponse> responsePromise_2 = Play.current().injector().instanceOf(WSClient.class).url(Server.tyrion_serverAddress + "/compilation/c_program/version/compile/" + version_c_program_2.id)
                                .setContentType("undefined").setMethod("PUT").setHeader("X-AUTH-TOKEN", token.authToken).setRequestTimeout(50000).put("");

                        JsonNode result_2 = responsePromise_2.get(50000).asJson();

                    }catch (Exception e){
                    }

                }
            };

            compile_that.start();



            B_Program b_program_1 = new B_Program();
            b_program_1.name = "První blocko program";
            b_program_1.program_description = "Blocko program je úžasná věc když funguje... a tady v tomto progtramu už je připravený i HW!!!!";
            b_program_1.dateOfCreate = new Date();
            b_program_1.project = project_1;
            b_program_1.save();


            B_Program b_program_2 = new B_Program();
            b_program_2.name = "Druhý blocko program";
            b_program_2.program_description = "Lorem Ipsum dolorem h ljahsdf lkasjdbflkjsdbf ndsabflhsbdljkhsafglbjsknbflk jnm.gbcvxůkdjnslvůd jfůsjadbf ůdjkůsjkahůk bam.sd, dsaf bhas dbflhjasbdlfjhbdsalhjbf sdlhfb lsdjfblsd jfbsadbfjhblvhjasdljhf ljh lsdhjfg asjhf bkhjfd";
            b_program_2.dateOfCreate = new Date();
            b_program_2.project = project_1;
            b_program_2.save();


             // První verze B_Programu - Pro instanci!
            Version_Object version_b_program_1 = new Version_Object();
            version_b_program_1.version_name = "Blocko Verze č.1";
            version_b_program_1.version_description = "Snažím se tu dělat veklé věci";
            version_b_program_1.date_of_create = new Date();
            version_b_program_1.b_program = b_program_1;


            B_Program_Hw_Group group_1 = new B_Program_Hw_Group();
                // Main Boad - Yoda
                B_Pair main_1 = new B_Pair();
                    main_1.board = board_yoda_1;
                    main_1.c_program_version = version_c_program_1;

                group_1.main_board_pair = main_1;

                // Bezdrát 1
                B_Pair device_1 = new B_Pair();
                    device_1.board = wireless_1;
                    device_1.c_program_version = version_c_program_3;
                    group_1.device_board_pairs.add(device_1);

                // Bezdrát 2
                B_Pair device_2 = new B_Pair();
                    device_2.board = wireless_2;
                    device_2.c_program_version = version_c_program_3;
                    group_1.device_board_pairs.add(device_2);

                // Bus 1
                B_Pair device_3 = new B_Pair();
                    device_3.board = bus_1;
                    device_3.c_program_version = version_c_program_4;
                    group_1.device_board_pairs.add(device_3);


            B_Program_Hw_Group group_2 = new B_Program_Hw_Group();
                // Main Board - Yoda
                B_Pair main_2 = new B_Pair();
                    main_2.board = board_yoda_2;
                    main_2.c_program_version = version_c_program_2;

                group_2.main_board_pair = main_2;

                // Bus 1
                B_Pair device_4 = new B_Pair();
                    device_4.board = bus_2;
                    device_4.c_program_version = version_c_program_4;
                    group_2.device_board_pairs.add(device_4);


                  B_Pair device_5 = new B_Pair();
                      device_5.board = bus_3;
                      device_4.c_program_version = version_c_program_4;
                      group_2.device_board_pairs.add(device_5);

                   B_Pair device_6 = new B_Pair();
                       device_6.board = wireless_3;
                       device_6.c_program_version = version_c_program_2;
                       group_2.device_board_pairs.add(device_6);

            version_b_program_1.b_program_hw_groups.add(group_1);
            version_b_program_1.b_program_hw_groups.add(group_2);
            version_b_program_1.save();

            UtilTools.uploadAzure_Version("Blocko Program zde!", "program.js", b_program_1.get_path() , version_b_program_1);

            version_b_program_1.update();

        }catch (Exception e){
            e.printStackTrace();
        }
    }







}
