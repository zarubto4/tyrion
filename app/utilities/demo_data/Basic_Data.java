package utilities.demo_data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.compiler.*;
import models.person.FloatingPersonToken;
import models.person.Person;
import models.person.PersonPermission;
import models.person.SecurityRole;
import models.project.b_program.B_Pair;
import models.project.b_program.B_Program;
import models.project.b_program.B_Program_Hw_Group;
import models.project.c_program.C_Program;
import models.project.global.Product;
import models.project.global.Project;
import models.project.global.financial.GeneralTariff;
import models.project.global.financial.Payment_Details;
import models.project.m_program.M_Program;
import models.project.m_program.M_Project;
import play.api.Play;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utilities.Server;
import utilities.enums.Approval_state;
import utilities.enums.Currency;
import utilities.enums.Payment_method;
import utilities.enums.Payment_mode;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class Basic_Data {

    public static void  set_default_objects(){
    }

    public static void set_basic_demo_data(){

        try {

            if(Person.find.where().eq("nick_name", "Pepíno").findUnique() != null ) {
                System.err.println("Databáze je již naplněna - neprovádím žádné další změny");
                return;
            }

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
                version_c_program_1.version_name = "Když jem poprvé zkoušel blikat ledkou";
                version_c_program_1.c_program = c_program_1;
                version_c_program_1.save();

                // Nahraje do Azure a připojí do verze soubor
                ObjectNode content_1 = Json.newObject();
                content_1.put("main", "/****************************************\\r\\n * Popis programu                       *\\r\\n ****************************************\\r\\n *\\r\\n * Zaregistruju si 2 tla\\u010D\\u00EDtka - Up a Down.\\r\\n * To jsou moje digit\\u00E1ln\\u00ED vstupy.\\r\\n * Zaregistruju si Dv\\u011B LED diody, to jsou mjo dva digit\\u00E1ln\\u00ED v\\u00FDstupy.\\r\\n * \\r\\n * Pokud stisknu tla\\u010D\\u00EDtko Up, po\\u0161le se informace do Blocka.\\r\\n * Pokud stisknu tla\\u010D\\u00EDtko Down, po\\u0161le se informace do Blocka.\\r\\n * V Blocku mus\\u00ED b\\u00FDt naprogramovan\\u00E9, co se stane.\\r\\n *\\r\\n * D\\u00E1le si inicializuju u\\u017Eivatelsk\\u00E9 tla\\u010D\\u00EDtko na desce.\\r\\n * Toto z\\u00E1m\\u011Brn\\u011B neregistruju do Blocka, ale slou\\u017E\\u00ED mi jenom lok\\u00E1ln\\u011B.\\r\\n * Takt\\u00E9\\u017E si zaregistruju message out zp\\u00E1vu.\\r\\n * Zpr\\u00E1vu nav\\u00E1\\u017Eu uvnit\\u0159 yody na tla\\u010D\\u00EDtko.\\r\\n * Ve zpr\\u00E1v\\u011B se po stisknut\\u00ED tla\\u010D\\u00EDtka ode\\u0161le do Blocka po\\u010Det stisknut\\u00ED tla\\u010D\\u00EDtka jako string.\\r\\n *\\r\\n * D\\u00E1le, pokud p\\u0159ijde z blocka digital IN, tak to rozsv\\u00EDt\\u00ED\\/zhasne zelenou ledku na desce.\\r\\n *\\r\\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\\u011Bjak\\u00E1 zpr\\u00E1va p\\u0159ijde, vyp\\u00ED\\u0161u ji do termin\\u00E1lu.\\r\\n *\\r\\n *\\/\\r\\n\\r\\n\\/*\\r\\n * na za\\u010D\\u00E1tku v\\u017Edy mus\\u00ED b\\u00FDt tento \\u0159\\u00E1dek\\r\\n *\\/\\r\\n#include \\\"byzance.h\\\"\\r\\n\\r\\n\\/*\\r\\n * inicializuju si LEDky (na Pinech)\\r\\n *\\/\\r\\nDigitalOut\\tledRed(X05);\\r\\nDigitalOut\\tledGrn(X04);\\r\\n\\r\\n\\r\\n\\/*\\r\\n * inicializuju si USR tla\\u010D\\u00EDtko (na desce)\\r\\n * co se stane po stisku tla\\u010D\\u00EDtka mus\\u00EDm o\\u0161et\\u0159it v jeho callbacku\\r\\n * callback si zaregistruju v k\\u00F3du funkc\\u00ED btnUsr.fall(&nazev_funkce);\\r\\n *\\r\\n *\\/\\r\\nInterruptIn btnUsr(USER_BUTTON);\\r\\n\\r\\n\\/*\\r\\n * inicializuju si vlastn\\u00ED tla\\u010D\\u00EDtka\\r\\n * co se stane po stisku tla\\u010D\\u00EDtka mus\\u00EDm o\\u0161et\\u0159it v jeho callbacku\\r\\n * callback si zaregistruju v k\\u00F3du funkc\\u00ED\\r\\n * btnUp.fall(&nazev_funkce);\\r\\n * btnDown.fall(&nazev_funkce);\\r\\n *\\r\\n * InterruptIn je default pull down, tak\\u017Ee se pin mus\\u00ED p\\u0159ipojit proti VCC.\\r\\n *\\/\\r\\nInterruptIn btnUp(X00);\\r\\nInterruptIn btnDown(X02);\\r\\n\\r\\n\\/*\\r\\n * inicializuju si s\\u00E9riovou linku\\r\\n *\\/\\r\\nSerial pc(SERIAL_TX, SERIAL_RX); \\/\\/ tx, rx\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si digit\\u00E1ln\\u00ED vstupy\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_DIGITAL_IN (led_green, {\\r\\n    ledGrn = value;\\r\\n    pc.printf(\\\"led_green: %d \\\\n\\\", value);\\r\\n}) \\r\\n\\r\\n\\r\\nBYZANCE_DIGITAL_IN (led_red, {\\r\\n    ledRed = value;\\r\\n    pc.printf(\\\"led_red: %d \\\\n\\\", value);\\r\\n}) \\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si analogov\\u00E9 vstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\n\\/*\\r\\nBYZANCE_ANALOG_IN(led_pwm, {\\r\\n    ledTom = value;\\r\\n    pc.printf(\\\"led_pwm: %f \\\\n\\\", value);\\r\\n})\\r\\n*\\/\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si message vstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\n\\r\\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\\r\\n    pc.printf(\\\"message_in=%s\\\\n\\\", arg1);\\r\\n});\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si message v\\u00FDstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka Z desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si digit\\u00E1ln\\u00ED v\\u00FDstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka Z desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\\r\\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\\r\\n\\r\\n\\/*\\r\\n * Prom\\u011Bnn\\u00E9 pot\\u0159ebn\\u00E9 pro program.\\r\\n *\\/\\r\\nvolatile bool button_usr_clicked\\t\\t= 0;\\r\\nvolatile bool button_up_state\\t\\t\\t= 0;\\r\\nvolatile bool button_up_last_state\\t\\t= 0;\\r\\nvolatile bool button_down_state\\t\\t\\t= 0;\\r\\nvolatile bool button_down_last_state \\t= 0;\\r\\n\\r\\nint button_usr_counter = 0;\\r\\n\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku USR tla\\u010D\\u00EDtka.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_usr_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button USR clicked.\\\\n\\\");\\r\\n\\tbutton_usr_clicked = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka UP.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_up_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button UP clicked.\\\\n\\\");\\r\\n\\tbutton_up_state = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka UP.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_up_rise_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button UP released.\\\\n\\\");\\r\\n\\tbutton_up_state = 0;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka DOWN.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_down_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button DOWN clicked.\\\\n\\\");\\r\\n\\tbutton_down_state = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka DOWN.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_down_rise_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button DOWN released.\\\\n\\\");\\r\\n\\tbutton_down_state = 0;\\r\\n}\\r\\n\\r\\nint main(int argc, char* argv[]){\\r\\n\\r\\n\\r\\n\\t\\/*\\r\\n\\t * nastav\\u00EDm si baud rychlost s\\u00E9riov\\u00E9 linky\\r\\n\\t *\\/\\r\\n    pc.baud(115200);\\r\\n    \\r\\n    ByzanceLogger::init_serial( SERIAL_TX, SERIAL_RX );\\r\\n\\tByzanceLogger::set_level( LOGGER_LEVEL_INFO );\\r\\n\\tByzanceLogger::enable_prefix(false);\\r\\n\\t\\r\\n    \\/*\\r\\n     * Inicializace Byzance knihovny\\r\\n     *\\/\\r\\n    Byzance::init();\\r\\n    pc.printf(\\\"Byzance initialized\\\\n\\\");\\r\\n\\r\\n    \\/*\\r\\n     * P\\u0159ipojen\\u00ED na Byzance servery.\\r\\n     *\\/\\r\\n\\r\\n    Byzance::connect();\\r\\n\\tpc.printf(\\\"Succesfully connected to MQTT broker\\\\n\\\");\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku tla\\u010D\\u00EDtka USR\\r\\n\\t *\\/\\r\\n    btnUsr.fall(&button_usr_fall_callback);\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku extern\\u011B p\\u0159ipojen\\u00E9ho tla\\u010D\\u00EDtka UP\\r\\n\\t *\\/\\r\\n    btnUp.fall(&button_up_fall_callback);\\r\\n    btnUp.rise(&button_up_rise_callback);\\r\\n    btnUp.mode(PullUp); \\/\\/ toto musi byt za attachnuti callbacku\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku extern\\u011B p\\u0159ipojen\\u00E9ho tla\\u010D\\u00EDtka UP\\r\\n\\t *\\/\\r\\n    btnDown.fall(&button_down_fall_callback);\\r\\n    btnDown.rise(&button_down_rise_callback);\\r\\n    btnDown.mode(PullUp); \\/\\/ toto musi byt za attachnuti callbacku\\r\\n\\r\\n    \\/*\\r\\n     * b\\u011Bh programu\\r\\n     *\\/\\r\\n    while(true) {\\r\\n\\r\\n    \\t\\/*\\r\\n    \\t * prom\\u011Bnnou, co jsem naplnil v callbacku USR tla\\u010D\\u00EDtka si p\\u0159e\\u010Dtu\\r\\n    \\t * a pokud nab\\u00FDv\\u00E1 nenulov\\u00E9 hodnoty, provedu funkce,\\r\\n    \\t * co maj\\u00ED nastat po zm\\u00E1\\u010Dknut\\u00ED tla\\u010D\\u00EDtka\\r\\n    \\t *\\/\\r\\n    \\tif(button_usr_clicked)\\r\\n    \\t{\\r\\n    \\t\\tbutton_usr_clicked=0;\\r\\n    \\t\\tbutton_usr_counter++;\\r\\n\\r\\n    \\t\\tchar buffer[100];\\r\\n    \\t\\tsprintf(buffer, \\\"Pocet stisknuti = %d\\\\n\\\", button_usr_counter);\\r\\n    \\t\\tpc.printf(buffer);\\r\\n\\r\\n    \\t\\t\\/*\\r\\n    \\t\\t * Toto je funkce, kterou jsem si p\\u0159ed startem programu zaregistroval\\r\\n    \\t\\t * tak\\u017Ee bude vid\\u011Bt v Blocku.\\r\\n    \\t\\t *\\/\\r\\n    \\t\\tmessage_out_counter(buffer);\\r\\n    \\t}\\r\\n\\r\\n    \\t\\/*\\r\\n    \\t * prom\\u011Bnnou, co jsem naplnil v callbacku USR tla\\u010D\\u00EDtka si p\\u0159e\\u010Dtu\\r\\n    \\t * pokud se zm\\u011Bnila oproti p\\u0159edchoz\\u00ED kontrole, stisknul\\/pustil jsem tla\\u010D\\u00EDtko\\r\\n    \\t *\\/\\r\\n    \\tif(button_up_state!=button_up_last_state)\\r\\n    \\t{\\r\\n    \\t\\tbutton_up_last_state = button_up_state;\\r\\n    \\t\\tledGrn = 1;\\r\\n    \\t\\twait_ms(20);\\r\\n    \\t\\tledGrn = 0;\\r\\n\\r\\n   \\t\\t\\tdig_out_btn_up(button_up_state);\\r\\n\\r\\n   \\t\\t\\tpc.printf(\\\"button_up_clicked = %d\\\\n\\\", button_up_state);\\r\\n    \\t}\\r\\n\\r\\n    \\tif(button_down_state!=button_down_last_state)\\r\\n    \\t{\\r\\n    \\t\\tbutton_down_last_state = button_down_state;\\r\\n    \\t\\tledRed = 1;\\r\\n    \\t\\twait_ms(20);\\r\\n    \\t\\tledRed = 0;\\r\\n\\r\\n   \\t\\t\\tdig_out_btn_down(button_down_state);\\r\\n            \\/\\/bla bla\\r\\n   \\t\\t\\tpc.printf(\\\"button_down_clicked = %d\\\\n\\\", button_down_state);\\r\\n    \\t}\\r\\n\\r\\n        Thread::wait(100);\\r\\n    }\\r\\n}");
                content_1.set("user_files", Json.parse("[{\"file_name\":\"Koloběžka\",\"code\":\"//nic\"},{\"file_name\":\"autíčko\",\"code\":\"//nic\"}]"));
                //content_1.put("external_libraries", null);
                FileRecord.uploadAzure_Version(content_1.toString(), "code.json", c_program_1.get_path(), version_c_program_1);
                version_c_program_1.refresh();

            // Druhá verze verze C_Programu pro YODU c_program_1
            Version_Object version_c_program_1_2 = new Version_Object();
                version_c_program_1_2.version_name = "Verze 0.0.2";
                version_c_program_1_2.version_name = "Když jem podruhé a snad finálně zkoušel blikat ledkou";
                version_c_program_1_2.c_program = c_program_1;
                version_c_program_1_2.save();

                // Nahraje do Azure a připojí do verze soubor
                ObjectNode content_1_2 = Json.newObject();
                content_1_2.put("main", "\\/****************************************\\r\\n * Popis programu                       *\\r\\n ****************************************\\r\\n *\\r\\n * Zaregistruju si 2 tla\\u010D\\u00EDtka - Up a Down.\\r\\n * To jsou moje digit\\u00E1ln\\u00ED vstupy.\\r\\n * Zaregistruju si Dv\\u011B LED diody, to jsou mjo dva digit\\u00E1ln\\u00ED v\\u00FDstupy.\\r\\n * \\r\\n * Pokud stisknu tla\\u010D\\u00EDtko Up, po\\u0161le se informace do Blocka.\\r\\n * Pokud stisknu tla\\u010D\\u00EDtko Down, po\\u0161le se informace do Blocka.\\r\\n * V Blocku mus\\u00ED b\\u00FDt naprogramovan\\u00E9, co se stane.\\r\\n *\\r\\n * D\\u00E1le si inicializuju u\\u017Eivatelsk\\u00E9 tla\\u010D\\u00EDtko na desce.\\r\\n * Toto z\\u00E1m\\u011Brn\\u011B neregistruju do Blocka, ale slou\\u017E\\u00ED mi jenom lok\\u00E1ln\\u011B.\\r\\n * Takt\\u00E9\\u017E si zaregistruju message out zp\\u00E1vu.\\r\\n * Zpr\\u00E1vu nav\\u00E1\\u017Eu uvnit\\u0159 yody na tla\\u010D\\u00EDtko.\\r\\n * Ve zpr\\u00E1v\\u011B se po stisknut\\u00ED tla\\u010D\\u00EDtka ode\\u0161le do Blocka po\\u010Det stisknut\\u00ED tla\\u010D\\u00EDtka jako string.\\r\\n *\\r\\n * D\\u00E1le, pokud p\\u0159ijde z blocka digital IN, tak to rozsv\\u00EDt\\u00ED\\/zhasne zelenou ledku na desce.\\r\\n *\\r\\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\\u011Bjak\\u00E1 zpr\\u00E1va p\\u0159ijde, vyp\\u00ED\\u0161u ji do termin\\u00E1lu.\\r\\n *\\r\\n *\\/\\r\\n\\r\\n\\/*\\r\\n * na za\\u010D\\u00E1tku v\\u017Edy mus\\u00ED b\\u00FDt tento \\u0159\\u00E1dek\\r\\n *\\/\\r\\n#include \\\"byzance.h\\\"\\r\\n\\r\\n\\/*\\r\\n * inicializuju si LEDky (na Pinech)\\r\\n *\\/\\r\\nDigitalOut\\tledRed(X05);\\r\\nDigitalOut\\tledGrn(X04);\\r\\n\\r\\n\\r\\n\\/*\\r\\n * inicializuju si USR tla\\u010D\\u00EDtko (na desce)\\r\\n * co se stane po stisku tla\\u010D\\u00EDtka mus\\u00EDm o\\u0161et\\u0159it v jeho callbacku\\r\\n * callback si zaregistruju v k\\u00F3du funkc\\u00ED btnUsr.fall(&nazev_funkce);\\r\\n *\\r\\n *\\/\\r\\nInterruptIn btnUsr(USER_BUTTON);\\r\\n\\r\\n\\/*\\r\\n * inicializuju si vlastn\\u00ED tla\\u010D\\u00EDtka\\r\\n * co se stane po stisku tla\\u010D\\u00EDtka mus\\u00EDm o\\u0161et\\u0159it v jeho callbacku\\r\\n * callback si zaregistruju v k\\u00F3du funkc\\u00ED\\r\\n * btnUp.fall(&nazev_funkce);\\r\\n * btnDown.fall(&nazev_funkce);\\r\\n *\\r\\n * InterruptIn je default pull down, tak\\u017Ee se pin mus\\u00ED p\\u0159ipojit proti VCC.\\r\\n *\\/\\r\\nInterruptIn btnUp(X00);\\r\\nInterruptIn btnDown(X02);\\r\\n\\r\\n\\/*\\r\\n * inicializuju si s\\u00E9riovou linku\\r\\n *\\/\\r\\nSerial pc(SERIAL_TX, SERIAL_RX); \\/\\/ tx, rx\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si digit\\u00E1ln\\u00ED vstupy\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_DIGITAL_IN (led_green, {\\r\\n    ledGrn = value;\\r\\n    pc.printf(\\\"led_green: %d \\\\n\\\", value);\\r\\n}) \\r\\n\\r\\n\\r\\nBYZANCE_DIGITAL_IN (led_red, {\\r\\n    ledRed = value;\\r\\n    pc.printf(\\\"led_red: %d \\\\n\\\", value);\\r\\n}) \\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si analogov\\u00E9 vstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\n\\/*\\r\\nBYZANCE_ANALOG_IN(led_pwm, {\\r\\n    ledTom = value;\\r\\n    pc.printf(\\\"led_pwm: %f \\\\n\\\", value);\\r\\n})\\r\\n*\\/\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si message vstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\n\\r\\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\\r\\n    pc.printf(\\\"message_in=%s\\\\n\\\", arg1);\\r\\n});\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si message v\\u00FDstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka Z desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si digit\\u00E1ln\\u00ED v\\u00FDstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka Z desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\\r\\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\\r\\n\\r\\n\\/*\\r\\n * Prom\\u011Bnn\\u00E9 pot\\u0159ebn\\u00E9 pro program.\\r\\n *\\/\\r\\nvolatile bool button_usr_clicked\\t\\t= 0;\\r\\nvolatile bool button_up_state\\t\\t\\t= 0;\\r\\nvolatile bool button_up_last_state\\t\\t= 0;\\r\\nvolatile bool button_down_state\\t\\t\\t= 0;\\r\\nvolatile bool button_down_last_state \\t= 0;\\r\\n\\r\\nint button_usr_counter = 0;\\r\\n\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku USR tla\\u010D\\u00EDtka.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_usr_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button USR clicked.\\\\n\\\");\\r\\n\\tbutton_usr_clicked = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka UP.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_up_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button UP clicked.\\\\n\\\");\\r\\n\\tbutton_up_state = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka UP.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_up_rise_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button UP released.\\\\n\\\");\\r\\n\\tbutton_up_state = 0;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka DOWN.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_down_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button DOWN clicked.\\\\n\\\");\\r\\n\\tbutton_down_state = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka DOWN.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_down_rise_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button DOWN released.\\\\n\\\");\\r\\n\\tbutton_down_state = 0;\\r\\n}\\r\\n\\r\\nint main(int argc, char* argv[]){\\r\\n\\r\\n\\r\\n\\t\\/*\\r\\n\\t * nastav\\u00EDm si baud rychlost s\\u00E9riov\\u00E9 linky\\r\\n\\t *\\/\\r\\n    pc.baud(115200);\\r\\n    \\r\\n    ByzanceLogger::init_serial( SERIAL_TX, SERIAL_RX );\\r\\n\\tByzanceLogger::set_level( LOGGER_LEVEL_INFO );\\r\\n\\tByzanceLogger::enable_prefix(false);\\r\\n\\t\\r\\n    \\/*\\r\\n     * Inicializace Byzance knihovny\\r\\n     *\\/\\r\\n    Byzance::init();\\r\\n    pc.printf(\\\"Byzance initialized\\\\n\\\");\\r\\n\\r\\n    \\/*\\r\\n     * P\\u0159ipojen\\u00ED na Byzance servery.\\r\\n     *\\/\\r\\n\\r\\n    Byzance::connect();\\r\\n\\tpc.printf(\\\"Succesfully connected to MQTT broker\\\\n\\\");\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku tla\\u010D\\u00EDtka USR\\r\\n\\t *\\/\\r\\n    btnUsr.fall(&button_usr_fall_callback);\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku extern\\u011B p\\u0159ipojen\\u00E9ho tla\\u010D\\u00EDtka UP\\r\\n\\t *\\/\\r\\n    btnUp.fall(&button_up_fall_callback);\\r\\n    btnUp.rise(&button_up_rise_callback);\\r\\n    btnUp.mode(PullUp); \\/\\/ toto musi byt za attachnuti callbacku\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku extern\\u011B p\\u0159ipojen\\u00E9ho tla\\u010D\\u00EDtka UP\\r\\n\\t *\\/\\r\\n    btnDown.fall(&button_down_fall_callback);\\r\\n    btnDown.rise(&button_down_rise_callback);\\r\\n    btnDown.mode(PullUp); \\/\\/ toto musi byt za attachnuti callbacku\\r\\n\\r\\n    \\/*\\r\\n     * b\\u011Bh programu\\r\\n     *\\/\\r\\n    while(true) {\\r\\n\\r\\n    \\t\\/*\\r\\n    \\t * prom\\u011Bnnou, co jsem naplnil v callbacku USR tla\\u010D\\u00EDtka si p\\u0159e\\u010Dtu\\r\\n    \\t * a pokud nab\\u00FDv\\u00E1 nenulov\\u00E9 hodnoty, provedu funkce,\\r\\n    \\t * co maj\\u00ED nastat po zm\\u00E1\\u010Dknut\\u00ED tla\\u010D\\u00EDtka\\r\\n    \\t *\\/\\r\\n    \\tif(button_usr_clicked)\\r\\n    \\t{\\r\\n    \\t\\tbutton_usr_clicked=0;\\r\\n    \\t\\tbutton_usr_counter++;\\r\\n\\r\\n    \\t\\tchar buffer[100];\\r\\n    \\t\\tsprintf(buffer, \\\"Pocet stisknuti = %d\\\\n\\\", button_usr_counter);\\r\\n    \\t\\tpc.printf(buffer);\\r\\n\\r\\n    \\t\\t\\/*\\r\\n    \\t\\t * Toto je funkce, kterou jsem si p\\u0159ed startem programu zaregistroval\\r\\n    \\t\\t * tak\\u017Ee bude vid\\u011Bt v Blocku.\\r\\n    \\t\\t *\\/\\r\\n    \\t\\tmessage_out_counter(buffer);\\r\\n    \\t}\\r\\n\\r\\n    \\t\\/*\\r\\n    \\t * prom\\u011Bnnou, co jsem naplnil v callbacku USR tla\\u010D\\u00EDtka si p\\u0159e\\u010Dtu\\r\\n    \\t * pokud se zm\\u011Bnila oproti p\\u0159edchoz\\u00ED kontrole, stisknul\\/pustil jsem tla\\u010D\\u00EDtko\\r\\n    \\t *\\/\\r\\n    \\tif(button_up_state!=button_up_last_state)\\r\\n    \\t{\\r\\n    \\t\\tbutton_up_last_state = button_up_state;\\r\\n    \\t\\tledGrn = 1;\\r\\n    \\t\\twait_ms(20);\\r\\n    \\t\\tledGrn = 0;\\r\\n\\r\\n   \\t\\t\\tdig_out_btn_up(button_up_state);\\r\\n\\r\\n   \\t\\t\\tpc.printf(\\\"button_up_clicked = %d\\\\n\\\", button_up_state);\\r\\n    \\t}\\r\\n\\r\\n    \\tif(button_down_state!=button_down_last_state)\\r\\n    \\t{\\r\\n    \\t\\tbutton_down_last_state = button_down_state;\\r\\n    \\t\\tledRed = 1;\\r\\n    \\t\\twait_ms(20);\\r\\n    \\t\\tledRed = 0;\\r\\n\\r\\n   \\t\\t\\tdig_out_btn_down(button_down_state);\\r\\n            \\/\\/bla bla\\r\\n   \\t\\t\\tpc.printf(\\\"button_down_clicked = %d\\\\n\\\", button_down_state);\\r\\n    \\t}\\r\\n\\r\\n        Thread::wait(100);\\r\\n    }\\r\\n}");
                content_1_2.set("user_files", Json.parse("[{\"file_name\":\"Koloběžka\",\"code\":\"//nic\"},{\"file_name\":\"autíčko\",\"code\":\"//nic\"}]") );
               // content_2.put("external_libraries", "");
                FileRecord.uploadAzure_Version(content_1_2.toString(), "code.json", c_program_1.get_path(), version_c_program_1_2);
                version_c_program_1_2.refresh();


            // Druhá verze verze C_Programu pro YODU c_program_1
            Version_Object version_c_program_2 = new Version_Object();
                version_c_program_2.version_name = "Verze 0.0.1";
                version_c_program_2.version_name = "Trala la tra lalala";
                version_c_program_2.c_program = c_program_2;
                version_c_program_2.approval_state = Approval_state.pending;
                version_c_program_2.save();

            // Nahraje do Azure a připojí do verze soubor
            ObjectNode content_2 = Json.newObject();
            content_2.put("main", "\\/****************************************\\r\\n * Popis programu                       *\\r\\n ****************************************\\r\\n *\\r\\n * Zaregistruju si 2 tla\\u010D\\u00EDtka - Up a Down.\\r\\n * To jsou moje digit\\u00E1ln\\u00ED vstupy.\\r\\n * Zaregistruju si Dv\\u011B LED diody, to jsou mjo dva digit\\u00E1ln\\u00ED v\\u00FDstupy.\\r\\n * \\r\\n * Pokud stisknu tla\\u010D\\u00EDtko Up, po\\u0161le se informace do Blocka.\\r\\n * Pokud stisknu tla\\u010D\\u00EDtko Down, po\\u0161le se informace do Blocka.\\r\\n * V Blocku mus\\u00ED b\\u00FDt naprogramovan\\u00E9, co se stane.\\r\\n *\\r\\n * D\\u00E1le si inicializuju u\\u017Eivatelsk\\u00E9 tla\\u010D\\u00EDtko na desce.\\r\\n * Toto z\\u00E1m\\u011Brn\\u011B neregistruju do Blocka, ale slou\\u017E\\u00ED mi jenom lok\\u00E1ln\\u011B.\\r\\n * Takt\\u00E9\\u017E si zaregistruju message out zp\\u00E1vu.\\r\\n * Zpr\\u00E1vu nav\\u00E1\\u017Eu uvnit\\u0159 yody na tla\\u010D\\u00EDtko.\\r\\n * Ve zpr\\u00E1v\\u011B se po stisknut\\u00ED tla\\u010D\\u00EDtka ode\\u0161le do Blocka po\\u010Det stisknut\\u00ED tla\\u010D\\u00EDtka jako string.\\r\\n *\\r\\n * D\\u00E1le, pokud p\\u0159ijde z blocka digital IN, tak to rozsv\\u00EDt\\u00ED\\/zhasne zelenou ledku na desce.\\r\\n *\\r\\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\\u011Bjak\\u00E1 zpr\\u00E1va p\\u0159ijde, vyp\\u00ED\\u0161u ji do termin\\u00E1lu.\\r\\n *\\r\\n *\\/\\r\\n\\r\\n\\/*\\r\\n * na za\\u010D\\u00E1tku v\\u017Edy mus\\u00ED b\\u00FDt tento \\u0159\\u00E1dek\\r\\n *\\/\\r\\n#include \\\"byzance.h\\\"\\r\\n\\r\\n\\/*\\r\\n * inicializuju si LEDky (na Pinech)\\r\\n *\\/\\r\\nDigitalOut\\tledRed(X05);\\r\\nDigitalOut\\tledGrn(X04);\\r\\n\\r\\n\\r\\n\\/*\\r\\n * inicializuju si USR tla\\u010D\\u00EDtko (na desce)\\r\\n * co se stane po stisku tla\\u010D\\u00EDtka mus\\u00EDm o\\u0161et\\u0159it v jeho callbacku\\r\\n * callback si zaregistruju v k\\u00F3du funkc\\u00ED btnUsr.fall(&nazev_funkce);\\r\\n *\\r\\n *\\/\\r\\nInterruptIn btnUsr(USER_BUTTON);\\r\\n\\r\\n\\/*\\r\\n * inicializuju si vlastn\\u00ED tla\\u010D\\u00EDtka\\r\\n * co se stane po stisku tla\\u010D\\u00EDtka mus\\u00EDm o\\u0161et\\u0159it v jeho callbacku\\r\\n * callback si zaregistruju v k\\u00F3du funkc\\u00ED\\r\\n * btnUp.fall(&nazev_funkce);\\r\\n * btnDown.fall(&nazev_funkce);\\r\\n *\\r\\n * InterruptIn je default pull down, tak\\u017Ee se pin mus\\u00ED p\\u0159ipojit proti VCC.\\r\\n *\\/\\r\\nInterruptIn btnUp(X00);\\r\\nInterruptIn btnDown(X02);\\r\\n\\r\\n\\/*\\r\\n * inicializuju si s\\u00E9riovou linku\\r\\n *\\/\\r\\nSerial pc(SERIAL_TX, SERIAL_RX); \\/\\/ tx, rx\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si digit\\u00E1ln\\u00ED vstupy\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_DIGITAL_IN (led_green, {\\r\\n    ledGrn = value;\\r\\n    pc.printf(\\\"led_green: %d \\\\n\\\", value);\\r\\n}) \\r\\n\\r\\n\\r\\nBYZANCE_DIGITAL_IN (led_red, {\\r\\n    ledRed = value;\\r\\n    pc.printf(\\\"led_red: %d \\\\n\\\", value);\\r\\n}) \\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si analogov\\u00E9 vstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\n\\/*\\r\\nBYZANCE_ANALOG_IN(led_pwm, {\\r\\n    ledTom = value;\\r\\n    pc.printf(\\\"led_pwm: %f \\\\n\\\", value);\\r\\n})\\r\\n*\\/\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si message vstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\n\\r\\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\\r\\n    pc.printf(\\\"message_in=%s\\\\n\\\", arg1);\\r\\n});\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si message v\\u00FDstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka Z desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si digit\\u00E1ln\\u00ED v\\u00FDstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka Z desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\\r\\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\\r\\n\\r\\n\\/*\\r\\n * Prom\\u011Bnn\\u00E9 pot\\u0159ebn\\u00E9 pro program.\\r\\n *\\/\\r\\nvolatile bool button_usr_clicked\\t\\t= 0;\\r\\nvolatile bool button_up_state\\t\\t\\t= 0;\\r\\nvolatile bool button_up_last_state\\t\\t= 0;\\r\\nvolatile bool button_down_state\\t\\t\\t= 0;\\r\\nvolatile bool button_down_last_state \\t= 0;\\r\\n\\r\\nint button_usr_counter = 0;\\r\\n\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku USR tla\\u010D\\u00EDtka.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_usr_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button USR clicked.\\\\n\\\");\\r\\n\\tbutton_usr_clicked = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka UP.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_up_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button UP clicked.\\\\n\\\");\\r\\n\\tbutton_up_state = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka UP.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_up_rise_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button UP released.\\\\n\\\");\\r\\n\\tbutton_up_state = 0;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka DOWN.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_down_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button DOWN clicked.\\\\n\\\");\\r\\n\\tbutton_down_state = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka DOWN.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_down_rise_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button DOWN released.\\\\n\\\");\\r\\n\\tbutton_down_state = 0;\\r\\n}\\r\\n\\r\\nint main(int argc, char* argv[]){\\r\\n\\r\\n\\r\\n\\t\\/*\\r\\n\\t * nastav\\u00EDm si baud rychlost s\\u00E9riov\\u00E9 linky\\r\\n\\t *\\/\\r\\n    pc.baud(115200);\\r\\n    \\r\\n    ByzanceLogger::init_serial( SERIAL_TX, SERIAL_RX );\\r\\n\\tByzanceLogger::set_level( LOGGER_LEVEL_INFO );\\r\\n\\tByzanceLogger::enable_prefix(false);\\r\\n\\t\\r\\n    \\/*\\r\\n     * Inicializace Byzance knihovny\\r\\n     *\\/\\r\\n    Byzance::init();\\r\\n    pc.printf(\\\"Byzance initialized\\\\n\\\");\\r\\n\\r\\n    \\/*\\r\\n     * P\\u0159ipojen\\u00ED na Byzance servery.\\r\\n     *\\/\\r\\n\\r\\n    Byzance::connect();\\r\\n\\tpc.printf(\\\"Succesfully connected to MQTT broker\\\\n\\\");\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku tla\\u010D\\u00EDtka USR\\r\\n\\t *\\/\\r\\n    btnUsr.fall(&button_usr_fall_callback);\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku extern\\u011B p\\u0159ipojen\\u00E9ho tla\\u010D\\u00EDtka UP\\r\\n\\t *\\/\\r\\n    btnUp.fall(&button_up_fall_callback);\\r\\n    btnUp.rise(&button_up_rise_callback);\\r\\n    btnUp.mode(PullUp); \\/\\/ toto musi byt za attachnuti callbacku\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku extern\\u011B p\\u0159ipojen\\u00E9ho tla\\u010D\\u00EDtka UP\\r\\n\\t *\\/\\r\\n    btnDown.fall(&button_down_fall_callback);\\r\\n    btnDown.rise(&button_down_rise_callback);\\r\\n    btnDown.mode(PullUp); \\/\\/ toto musi byt za attachnuti callbacku\\r\\n\\r\\n    \\/*\\r\\n     * b\\u011Bh programu\\r\\n     *\\/\\r\\n    while(true) {\\r\\n\\r\\n    \\t\\/*\\r\\n    \\t * prom\\u011Bnnou, co jsem naplnil v callbacku USR tla\\u010D\\u00EDtka si p\\u0159e\\u010Dtu\\r\\n    \\t * a pokud nab\\u00FDv\\u00E1 nenulov\\u00E9 hodnoty, provedu funkce,\\r\\n    \\t * co maj\\u00ED nastat po zm\\u00E1\\u010Dknut\\u00ED tla\\u010D\\u00EDtka\\r\\n    \\t *\\/\\r\\n    \\tif(button_usr_clicked)\\r\\n    \\t{\\r\\n    \\t\\tbutton_usr_clicked=0;\\r\\n    \\t\\tbutton_usr_counter++;\\r\\n\\r\\n    \\t\\tchar buffer[100];\\r\\n    \\t\\tsprintf(buffer, \\\"Pocet stisknuti = %d\\\\n\\\", button_usr_counter);\\r\\n    \\t\\tpc.printf(buffer);\\r\\n\\r\\n    \\t\\t\\/*\\r\\n    \\t\\t * Toto je funkce, kterou jsem si p\\u0159ed startem programu zaregistroval\\r\\n    \\t\\t * tak\\u017Ee bude vid\\u011Bt v Blocku.\\r\\n    \\t\\t *\\/\\r\\n    \\t\\tmessage_out_counter(buffer);\\r\\n    \\t}\\r\\n\\r\\n    \\t\\/*\\r\\n    \\t * prom\\u011Bnnou, co jsem naplnil v callbacku USR tla\\u010D\\u00EDtka si p\\u0159e\\u010Dtu\\r\\n    \\t * pokud se zm\\u011Bnila oproti p\\u0159edchoz\\u00ED kontrole, stisknul\\/pustil jsem tla\\u010D\\u00EDtko\\r\\n    \\t *\\/\\r\\n    \\tif(button_up_state!=button_up_last_state)\\r\\n    \\t{\\r\\n    \\t\\tbutton_up_last_state = button_up_state;\\r\\n    \\t\\tledGrn = 1;\\r\\n    \\t\\twait_ms(20);\\r\\n    \\t\\tledGrn = 0;\\r\\n\\r\\n   \\t\\t\\tdig_out_btn_up(button_up_state);\\r\\n\\r\\n   \\t\\t\\tpc.printf(\\\"button_up_clicked = %d\\\\n\\\", button_up_state);\\r\\n    \\t}\\r\\n\\r\\n    \\tif(button_down_state!=button_down_last_state)\\r\\n    \\t{\\r\\n    \\t\\tbutton_down_last_state = button_down_state;\\r\\n    \\t\\tledRed = 1;\\r\\n    \\t\\twait_ms(20);\\r\\n    \\t\\tledRed = 0;\\r\\n\\r\\n   \\t\\t\\tdig_out_btn_down(button_down_state);\\r\\n            \\/\\/bla bla\\r\\n   \\t\\t\\tpc.printf(\\\"button_down_clicked = %d\\\\n\\\", button_down_state);\\r\\n    \\t}\\r\\n\\r\\n        Thread::wait(100);\\r\\n    }\\r\\n}");
            content_2.set("user_files", Json.parse("[{\"file_name\":\"Koloběžka\",\"code\":\"//nic\"},{\"file_name\":\"autíčko\",\"code\":\"//nic\"}]") );
            // content_2.put("external_libraries", "");
            FileRecord.uploadAzure_Version(content_2.toString(), "code.json", c_program_3.get_path(), version_c_program_2);
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
                content_3.put("main", "\\/****************************************\\r\\n * Popis programu                       *\\r\\n ****************************************\\r\\n *\\r\\n * Zaregistruju si 2 tla\\u010D\\u00EDtka - Up a Down.\\r\\n * To jsou moje digit\\u00E1ln\\u00ED vstupy.\\r\\n * Zaregistruju si Dv\\u011B LED diody, to jsou mjo dva digit\\u00E1ln\\u00ED v\\u00FDstupy.\\r\\n * \\r\\n * Pokud stisknu tla\\u010D\\u00EDtko Up, po\\u0161le se informace do Blocka.\\r\\n * Pokud stisknu tla\\u010D\\u00EDtko Down, po\\u0161le se informace do Blocka.\\r\\n * V Blocku mus\\u00ED b\\u00FDt naprogramovan\\u00E9, co se stane.\\r\\n *\\r\\n * D\\u00E1le si inicializuju u\\u017Eivatelsk\\u00E9 tla\\u010D\\u00EDtko na desce.\\r\\n * Toto z\\u00E1m\\u011Brn\\u011B neregistruju do Blocka, ale slou\\u017E\\u00ED mi jenom lok\\u00E1ln\\u011B.\\r\\n * Takt\\u00E9\\u017E si zaregistruju message out zp\\u00E1vu.\\r\\n * Zpr\\u00E1vu nav\\u00E1\\u017Eu uvnit\\u0159 yody na tla\\u010D\\u00EDtko.\\r\\n * Ve zpr\\u00E1v\\u011B se po stisknut\\u00ED tla\\u010D\\u00EDtka ode\\u0161le do Blocka po\\u010Det stisknut\\u00ED tla\\u010D\\u00EDtka jako string.\\r\\n *\\r\\n * D\\u00E1le, pokud p\\u0159ijde z blocka digital IN, tak to rozsv\\u00EDt\\u00ED\\/zhasne zelenou ledku na desce.\\r\\n *\\r\\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\\u011Bjak\\u00E1 zpr\\u00E1va p\\u0159ijde, vyp\\u00ED\\u0161u ji do termin\\u00E1lu.\\r\\n *\\r\\n *\\/\\r\\n\\r\\n\\/*\\r\\n * na za\\u010D\\u00E1tku v\\u017Edy mus\\u00ED b\\u00FDt tento \\u0159\\u00E1dek\\r\\n *\\/\\r\\n#include \\\"byzance.h\\\"\\r\\n\\r\\n\\/*\\r\\n * inicializuju si LEDky (na Pinech)\\r\\n *\\/\\r\\nDigitalOut\\tledRed(X05);\\r\\nDigitalOut\\tledGrn(X04);\\r\\n\\r\\n\\r\\n\\/*\\r\\n * inicializuju si USR tla\\u010D\\u00EDtko (na desce)\\r\\n * co se stane po stisku tla\\u010D\\u00EDtka mus\\u00EDm o\\u0161et\\u0159it v jeho callbacku\\r\\n * callback si zaregistruju v k\\u00F3du funkc\\u00ED btnUsr.fall(&nazev_funkce);\\r\\n *\\r\\n *\\/\\r\\nInterruptIn btnUsr(USER_BUTTON);\\r\\n\\r\\n\\/*\\r\\n * inicializuju si vlastn\\u00ED tla\\u010D\\u00EDtka\\r\\n * co se stane po stisku tla\\u010D\\u00EDtka mus\\u00EDm o\\u0161et\\u0159it v jeho callbacku\\r\\n * callback si zaregistruju v k\\u00F3du funkc\\u00ED\\r\\n * btnUp.fall(&nazev_funkce);\\r\\n * btnDown.fall(&nazev_funkce);\\r\\n *\\r\\n * InterruptIn je default pull down, tak\\u017Ee se pin mus\\u00ED p\\u0159ipojit proti VCC.\\r\\n *\\/\\r\\nInterruptIn btnUp(X00);\\r\\nInterruptIn btnDown(X02);\\r\\n\\r\\n\\/*\\r\\n * inicializuju si s\\u00E9riovou linku\\r\\n *\\/\\r\\nSerial pc(SERIAL_TX, SERIAL_RX); \\/\\/ tx, rx\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si digit\\u00E1ln\\u00ED vstupy\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_DIGITAL_IN (led_green, {\\r\\n    ledGrn = value;\\r\\n    pc.printf(\\\"led_green: %d \\\\n\\\", value);\\r\\n}) \\r\\n\\r\\n\\r\\nBYZANCE_DIGITAL_IN (led_red, {\\r\\n    ledRed = value;\\r\\n    pc.printf(\\\"led_red: %d \\\\n\\\", value);\\r\\n}) \\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si analogov\\u00E9 vstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\n\\/*\\r\\nBYZANCE_ANALOG_IN(led_pwm, {\\r\\n    ledTom = value;\\r\\n    pc.printf(\\\"led_pwm: %f \\\\n\\\", value);\\r\\n})\\r\\n*\\/\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si message vstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\n\\r\\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\\r\\n    pc.printf(\\\"message_in=%s\\\\n\\\", arg1);\\r\\n});\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si message v\\u00FDstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka Z desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si digit\\u00E1ln\\u00ED v\\u00FDstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka Z desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\\r\\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\\r\\n\\r\\n\\/*\\r\\n * Prom\\u011Bnn\\u00E9 pot\\u0159ebn\\u00E9 pro program.\\r\\n *\\/\\r\\nvolatile bool button_usr_clicked\\t\\t= 0;\\r\\nvolatile bool button_up_state\\t\\t\\t= 0;\\r\\nvolatile bool button_up_last_state\\t\\t= 0;\\r\\nvolatile bool button_down_state\\t\\t\\t= 0;\\r\\nvolatile bool button_down_last_state \\t= 0;\\r\\n\\r\\nint button_usr_counter = 0;\\r\\n\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku USR tla\\u010D\\u00EDtka.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_usr_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button USR clicked.\\\\n\\\");\\r\\n\\tbutton_usr_clicked = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka UP.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_up_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button UP clicked.\\\\n\\\");\\r\\n\\tbutton_up_state = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka UP.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_up_rise_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button UP released.\\\\n\\\");\\r\\n\\tbutton_up_state = 0;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka DOWN.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_down_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button DOWN clicked.\\\\n\\\");\\r\\n\\tbutton_down_state = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka DOWN.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_down_rise_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button DOWN released.\\\\n\\\");\\r\\n\\tbutton_down_state = 0;\\r\\n}\\r\\n\\r\\nint main(int argc, char* argv[]){\\r\\n\\r\\n\\r\\n\\t\\/*\\r\\n\\t * nastav\\u00EDm si baud rychlost s\\u00E9riov\\u00E9 linky\\r\\n\\t *\\/\\r\\n    pc.baud(115200);\\r\\n    \\r\\n    ByzanceLogger::init_serial( SERIAL_TX, SERIAL_RX );\\r\\n\\tByzanceLogger::set_level( LOGGER_LEVEL_INFO );\\r\\n\\tByzanceLogger::enable_prefix(false);\\r\\n\\t\\r\\n    \\/*\\r\\n     * Inicializace Byzance knihovny\\r\\n     *\\/\\r\\n    Byzance::init();\\r\\n    pc.printf(\\\"Byzance initialized\\\\n\\\");\\r\\n\\r\\n    \\/*\\r\\n     * P\\u0159ipojen\\u00ED na Byzance servery.\\r\\n     *\\/\\r\\n\\r\\n    Byzance::connect();\\r\\n\\tpc.printf(\\\"Succesfully connected to MQTT broker\\\\n\\\");\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku tla\\u010D\\u00EDtka USR\\r\\n\\t *\\/\\r\\n    btnUsr.fall(&button_usr_fall_callback);\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku extern\\u011B p\\u0159ipojen\\u00E9ho tla\\u010D\\u00EDtka UP\\r\\n\\t *\\/\\r\\n    btnUp.fall(&button_up_fall_callback);\\r\\n    btnUp.rise(&button_up_rise_callback);\\r\\n    btnUp.mode(PullUp); \\/\\/ toto musi byt za attachnuti callbacku\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku extern\\u011B p\\u0159ipojen\\u00E9ho tla\\u010D\\u00EDtka UP\\r\\n\\t *\\/\\r\\n    btnDown.fall(&button_down_fall_callback);\\r\\n    btnDown.rise(&button_down_rise_callback);\\r\\n    btnDown.mode(PullUp); \\/\\/ toto musi byt za attachnuti callbacku\\r\\n\\r\\n    \\/*\\r\\n     * b\\u011Bh programu\\r\\n     *\\/\\r\\n    while(true) {\\r\\n\\r\\n    \\t\\/*\\r\\n    \\t * prom\\u011Bnnou, co jsem naplnil v callbacku USR tla\\u010D\\u00EDtka si p\\u0159e\\u010Dtu\\r\\n    \\t * a pokud nab\\u00FDv\\u00E1 nenulov\\u00E9 hodnoty, provedu funkce,\\r\\n    \\t * co maj\\u00ED nastat po zm\\u00E1\\u010Dknut\\u00ED tla\\u010D\\u00EDtka\\r\\n    \\t *\\/\\r\\n    \\tif(button_usr_clicked)\\r\\n    \\t{\\r\\n    \\t\\tbutton_usr_clicked=0;\\r\\n    \\t\\tbutton_usr_counter++;\\r\\n\\r\\n    \\t\\tchar buffer[100];\\r\\n    \\t\\tsprintf(buffer, \\\"Pocet stisknuti = %d\\\\n\\\", button_usr_counter);\\r\\n    \\t\\tpc.printf(buffer);\\r\\n\\r\\n    \\t\\t\\/*\\r\\n    \\t\\t * Toto je funkce, kterou jsem si p\\u0159ed startem programu zaregistroval\\r\\n    \\t\\t * tak\\u017Ee bude vid\\u011Bt v Blocku.\\r\\n    \\t\\t *\\/\\r\\n    \\t\\tmessage_out_counter(buffer);\\r\\n    \\t}\\r\\n\\r\\n    \\t\\/*\\r\\n    \\t * prom\\u011Bnnou, co jsem naplnil v callbacku USR tla\\u010D\\u00EDtka si p\\u0159e\\u010Dtu\\r\\n    \\t * pokud se zm\\u011Bnila oproti p\\u0159edchoz\\u00ED kontrole, stisknul\\/pustil jsem tla\\u010D\\u00EDtko\\r\\n    \\t *\\/\\r\\n    \\tif(button_up_state!=button_up_last_state)\\r\\n    \\t{\\r\\n    \\t\\tbutton_up_last_state = button_up_state;\\r\\n    \\t\\tledGrn = 1;\\r\\n    \\t\\twait_ms(20);\\r\\n    \\t\\tledGrn = 0;\\r\\n\\r\\n   \\t\\t\\tdig_out_btn_up(button_up_state);\\r\\n\\r\\n   \\t\\t\\tpc.printf(\\\"button_up_clicked = %d\\\\n\\\", button_up_state);\\r\\n    \\t}\\r\\n\\r\\n    \\tif(button_down_state!=button_down_last_state)\\r\\n    \\t{\\r\\n    \\t\\tbutton_down_last_state = button_down_state;\\r\\n    \\t\\tledRed = 1;\\r\\n    \\t\\twait_ms(20);\\r\\n    \\t\\tledRed = 0;\\r\\n\\r\\n   \\t\\t\\tdig_out_btn_down(button_down_state);\\r\\n            \\/\\/bla bla\\r\\n   \\t\\t\\tpc.printf(\\\"button_down_clicked = %d\\\\n\\\", button_down_state);\\r\\n    \\t}\\r\\n\\r\\n        Thread::wait(100);\\r\\n    }\\r\\n}");
                content_3.set("user_files", Json.parse("[{\"file_name\":\"Koloběžka\",\"code\":\"//nic\"},{\"file_name\":\"autíčko\",\"code\":\"//nic\"}]") );
                // content_2.put("external_libraries", "");
                FileRecord.uploadAzure_Version(content_3.toString(), "code.json", c_program_3.get_path(), version_c_program_3);
                version_c_program_3.refresh();


            // První verze  C_Programu pro BUS c_program_4
            Version_Object version_c_program_4 = new Version_Object();
                version_c_program_4.version_name = "Verze 0.0.1";
                version_c_program_4.version_name = "Když jem podruhé a snad finálně zkoušel blikat ledkou";
                version_c_program_4.c_program = c_program_4;
                version_c_program_4.save();

                // Nahraje do Azure a připojí do verze soubor
                ObjectNode content_4 = Json.newObject();
                content_4.put("main", "\\/****************************************\\r\\n * Popis programu                       *\\r\\n ****************************************\\r\\n *\\r\\n * Zaregistruju si 2 tla\\u010D\\u00EDtka - Up a Down.\\r\\n * To jsou moje digit\\u00E1ln\\u00ED vstupy.\\r\\n * Zaregistruju si Dv\\u011B LED diody, to jsou mjo dva digit\\u00E1ln\\u00ED v\\u00FDstupy.\\r\\n * \\r\\n * Pokud stisknu tla\\u010D\\u00EDtko Up, po\\u0161le se informace do Blocka.\\r\\n * Pokud stisknu tla\\u010D\\u00EDtko Down, po\\u0161le se informace do Blocka.\\r\\n * V Blocku mus\\u00ED b\\u00FDt naprogramovan\\u00E9, co se stane.\\r\\n *\\r\\n * D\\u00E1le si inicializuju u\\u017Eivatelsk\\u00E9 tla\\u010D\\u00EDtko na desce.\\r\\n * Toto z\\u00E1m\\u011Brn\\u011B neregistruju do Blocka, ale slou\\u017E\\u00ED mi jenom lok\\u00E1ln\\u011B.\\r\\n * Takt\\u00E9\\u017E si zaregistruju message out zp\\u00E1vu.\\r\\n * Zpr\\u00E1vu nav\\u00E1\\u017Eu uvnit\\u0159 yody na tla\\u010D\\u00EDtko.\\r\\n * Ve zpr\\u00E1v\\u011B se po stisknut\\u00ED tla\\u010D\\u00EDtka ode\\u0161le do Blocka po\\u010Det stisknut\\u00ED tla\\u010D\\u00EDtka jako string.\\r\\n *\\r\\n * D\\u00E1le, pokud p\\u0159ijde z blocka digital IN, tak to rozsv\\u00EDt\\u00ED\\/zhasne zelenou ledku na desce.\\r\\n *\\r\\n * Nakonec si zaregistruju Message In. Pokud mi z blocka n\\u011Bjak\\u00E1 zpr\\u00E1va p\\u0159ijde, vyp\\u00ED\\u0161u ji do termin\\u00E1lu.\\r\\n *\\r\\n *\\/\\r\\n\\r\\n\\/*\\r\\n * na za\\u010D\\u00E1tku v\\u017Edy mus\\u00ED b\\u00FDt tento \\u0159\\u00E1dek\\r\\n *\\/\\r\\n#include \\\"byzance.h\\\"\\r\\n\\r\\n\\/*\\r\\n * inicializuju si LEDky (na Pinech)\\r\\n *\\/\\r\\nDigitalOut\\tledRed(X05);\\r\\nDigitalOut\\tledGrn(X04);\\r\\n\\r\\n\\r\\n\\/*\\r\\n * inicializuju si USR tla\\u010D\\u00EDtko (na desce)\\r\\n * co se stane po stisku tla\\u010D\\u00EDtka mus\\u00EDm o\\u0161et\\u0159it v jeho callbacku\\r\\n * callback si zaregistruju v k\\u00F3du funkc\\u00ED btnUsr.fall(&nazev_funkce);\\r\\n *\\r\\n *\\/\\r\\nInterruptIn btnUsr(USER_BUTTON);\\r\\n\\r\\n\\/*\\r\\n * inicializuju si vlastn\\u00ED tla\\u010D\\u00EDtka\\r\\n * co se stane po stisku tla\\u010D\\u00EDtka mus\\u00EDm o\\u0161et\\u0159it v jeho callbacku\\r\\n * callback si zaregistruju v k\\u00F3du funkc\\u00ED\\r\\n * btnUp.fall(&nazev_funkce);\\r\\n * btnDown.fall(&nazev_funkce);\\r\\n *\\r\\n * InterruptIn je default pull down, tak\\u017Ee se pin mus\\u00ED p\\u0159ipojit proti VCC.\\r\\n *\\/\\r\\nInterruptIn btnUp(X00);\\r\\nInterruptIn btnDown(X02);\\r\\n\\r\\n\\/*\\r\\n * inicializuju si s\\u00E9riovou linku\\r\\n *\\/\\r\\nSerial pc(SERIAL_TX, SERIAL_RX); \\/\\/ tx, rx\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si digit\\u00E1ln\\u00ED vstupy\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_DIGITAL_IN (led_green, {\\r\\n    ledGrn = value;\\r\\n    pc.printf(\\\"led_green: %d \\\\n\\\", value);\\r\\n}) \\r\\n\\r\\n\\r\\nBYZANCE_DIGITAL_IN (led_red, {\\r\\n    ledRed = value;\\r\\n    pc.printf(\\\"led_red: %d \\\\n\\\", value);\\r\\n}) \\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si analogov\\u00E9 vstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\n\\/*\\r\\nBYZANCE_ANALOG_IN(led_pwm, {\\r\\n    ledTom = value;\\r\\n    pc.printf(\\\"led_pwm: %f \\\\n\\\", value);\\r\\n})\\r\\n*\\/\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si message vstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka DO desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\n\\r\\nBYZANCE_MESSAGE_IN(msg, ByzanceString, {\\r\\n    pc.printf(\\\"message_in=%s\\\\n\\\", arg1);\\r\\n});\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si message v\\u00FDstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka Z desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_MESSAGE_OUT(message_out_counter, ByzanceString);\\r\\n\\r\\n\\/*\\r\\n * Zaregistruju si digit\\u00E1ln\\u00ED v\\u00FDstupy.\\r\\n * (to, co mi p\\u0159ijde z Blocka Z desky)\\r\\n * Budou vid\\u011Bt v Blocku.\\r\\n *\\/\\r\\nBYZANCE_DIGITAL_OUT(dig_out_btn_up);\\r\\nBYZANCE_DIGITAL_OUT(dig_out_btn_down);\\r\\n\\r\\n\\/*\\r\\n * Prom\\u011Bnn\\u00E9 pot\\u0159ebn\\u00E9 pro program.\\r\\n *\\/\\r\\nvolatile bool button_usr_clicked\\t\\t= 0;\\r\\nvolatile bool button_up_state\\t\\t\\t= 0;\\r\\nvolatile bool button_up_last_state\\t\\t= 0;\\r\\nvolatile bool button_down_state\\t\\t\\t= 0;\\r\\nvolatile bool button_down_last_state \\t= 0;\\r\\n\\r\\nint button_usr_counter = 0;\\r\\n\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku USR tla\\u010D\\u00EDtka.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_usr_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button USR clicked.\\\\n\\\");\\r\\n\\tbutton_usr_clicked = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka UP.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_up_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button UP clicked.\\\\n\\\");\\r\\n\\tbutton_up_state = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka UP.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_up_rise_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button UP released.\\\\n\\\");\\r\\n\\tbutton_up_state = 0;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka DOWN.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_down_fall_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button DOWN clicked.\\\\n\\\");\\r\\n\\tbutton_down_state = 1;\\r\\n}\\r\\n\\r\\n\\/*\\r\\n * Callback, kter\\u00FD bude vyvol\\u00E1n po stisku extern\\u00EDho tla\\u010D\\u00EDtka DOWN.\\r\\n * Tento callback si registruju v u\\u017Eivatelsk\\u00E9m k\\u00F3du.\\r\\n *\\/\\r\\nvoid button_down_rise_callback(){\\r\\n\\tpc.printf((const char*)\\\"Button DOWN released.\\\\n\\\");\\r\\n\\tbutton_down_state = 0;\\r\\n}\\r\\n\\r\\nint main(int argc, char* argv[]){\\r\\n\\r\\n\\r\\n\\t\\/*\\r\\n\\t * nastav\\u00EDm si baud rychlost s\\u00E9riov\\u00E9 linky\\r\\n\\t *\\/\\r\\n    pc.baud(115200);\\r\\n    \\r\\n    ByzanceLogger::init_serial( SERIAL_TX, SERIAL_RX );\\r\\n\\tByzanceLogger::set_level( LOGGER_LEVEL_INFO );\\r\\n\\tByzanceLogger::enable_prefix(false);\\r\\n\\t\\r\\n    \\/*\\r\\n     * Inicializace Byzance knihovny\\r\\n     *\\/\\r\\n    Byzance::init();\\r\\n    pc.printf(\\\"Byzance initialized\\\\n\\\");\\r\\n\\r\\n    \\/*\\r\\n     * P\\u0159ipojen\\u00ED na Byzance servery.\\r\\n     *\\/\\r\\n\\r\\n    Byzance::connect();\\r\\n\\tpc.printf(\\\"Succesfully connected to MQTT broker\\\\n\\\");\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku tla\\u010D\\u00EDtka USR\\r\\n\\t *\\/\\r\\n    btnUsr.fall(&button_usr_fall_callback);\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku extern\\u011B p\\u0159ipojen\\u00E9ho tla\\u010D\\u00EDtka UP\\r\\n\\t *\\/\\r\\n    btnUp.fall(&button_up_fall_callback);\\r\\n    btnUp.rise(&button_up_rise_callback);\\r\\n    btnUp.mode(PullUp); \\/\\/ toto musi byt za attachnuti callbacku\\r\\n\\r\\n\\t\\/*\\r\\n\\t * p\\u0159ipoj\\u00ED callback, do kter\\u00E9ho program sko\\u010D\\u00ED po stisku extern\\u011B p\\u0159ipojen\\u00E9ho tla\\u010D\\u00EDtka UP\\r\\n\\t *\\/\\r\\n    btnDown.fall(&button_down_fall_callback);\\r\\n    btnDown.rise(&button_down_rise_callback);\\r\\n    btnDown.mode(PullUp); \\/\\/ toto musi byt za attachnuti callbacku\\r\\n\\r\\n    \\/*\\r\\n     * b\\u011Bh programu\\r\\n     *\\/\\r\\n    while(true) {\\r\\n\\r\\n    \\t\\/*\\r\\n    \\t * prom\\u011Bnnou, co jsem naplnil v callbacku USR tla\\u010D\\u00EDtka si p\\u0159e\\u010Dtu\\r\\n    \\t * a pokud nab\\u00FDv\\u00E1 nenulov\\u00E9 hodnoty, provedu funkce,\\r\\n    \\t * co maj\\u00ED nastat po zm\\u00E1\\u010Dknut\\u00ED tla\\u010D\\u00EDtka\\r\\n    \\t *\\/\\r\\n    \\tif(button_usr_clicked)\\r\\n    \\t{\\r\\n    \\t\\tbutton_usr_clicked=0;\\r\\n    \\t\\tbutton_usr_counter++;\\r\\n\\r\\n    \\t\\tchar buffer[100];\\r\\n    \\t\\tsprintf(buffer, \\\"Pocet stisknuti = %d\\\\n\\\", button_usr_counter);\\r\\n    \\t\\tpc.printf(buffer);\\r\\n\\r\\n    \\t\\t\\/*\\r\\n    \\t\\t * Toto je funkce, kterou jsem si p\\u0159ed startem programu zaregistroval\\r\\n    \\t\\t * tak\\u017Ee bude vid\\u011Bt v Blocku.\\r\\n    \\t\\t *\\/\\r\\n    \\t\\tmessage_out_counter(buffer);\\r\\n    \\t}\\r\\n\\r\\n    \\t\\/*\\r\\n    \\t * prom\\u011Bnnou, co jsem naplnil v callbacku USR tla\\u010D\\u00EDtka si p\\u0159e\\u010Dtu\\r\\n    \\t * pokud se zm\\u011Bnila oproti p\\u0159edchoz\\u00ED kontrole, stisknul\\/pustil jsem tla\\u010D\\u00EDtko\\r\\n    \\t *\\/\\r\\n    \\tif(button_up_state!=button_up_last_state)\\r\\n    \\t{\\r\\n    \\t\\tbutton_up_last_state = button_up_state;\\r\\n    \\t\\tledGrn = 1;\\r\\n    \\t\\twait_ms(20);\\r\\n    \\t\\tledGrn = 0;\\r\\n\\r\\n   \\t\\t\\tdig_out_btn_up(button_up_state);\\r\\n\\r\\n   \\t\\t\\tpc.printf(\\\"button_up_clicked = %d\\\\n\\\", button_up_state);\\r\\n    \\t}\\r\\n\\r\\n    \\tif(button_down_state!=button_down_last_state)\\r\\n    \\t{\\r\\n    \\t\\tbutton_down_last_state = button_down_state;\\r\\n    \\t\\tledRed = 1;\\r\\n    \\t\\twait_ms(20);\\r\\n    \\t\\tledRed = 0;\\r\\n\\r\\n   \\t\\t\\tdig_out_btn_down(button_down_state);\\r\\n            \\/\\/bla bla\\r\\n   \\t\\t\\tpc.printf(\\\"button_down_clicked = %d\\\\n\\\", button_down_state);\\r\\n    \\t}\\r\\n\\r\\n        Thread::wait(100);\\r\\n    }\\r\\n}");
                content_4.set("user_files", Json.parse("[{\"file_name\":\"Koloběžka\",\"code\":\"//nic\"},{\"file_name\":\"autíčko\",\"code\":\"//nic\"}]"));
                // content_2.put("external_libraries", "");
                FileRecord.uploadAzure_Version(content_4.toString(), "code.json", c_program_4.get_path(), version_c_program_4);
                version_c_program_4.refresh();


            Thread compile_that = new Thread() {
                @Override
                public void run() {
                    try {

                        // Zkompiluji - ale dám dostatečnou časovou rezervu pro kompilátor
                        sleep(3000);

                        try{

                            F.Promise<WSResponse> responsePromise_1 = Play.current().injector().instanceOf(WSClient.class).url(Server.tyrion_serverAddress + "/c_program/version/compile/" + version_c_program_1.id)
                                .setContentType("undefined").setMethod("PUT").setHeader("X-AUTH-TOKEN", token.authToken).setRequestTimeout(50000).put("");

                            JsonNode result_1 = responsePromise_1.get(50000).asJson();
                            System.out.print("Result 1 " + result_1.toString());

                        }catch (Exception e){
                            e.printStackTrace();
                        }



                        try{

                            F.Promise<WSResponse> responsePromise_1_2 = Play.current().injector().instanceOf(WSClient.class).url(Server.tyrion_serverAddress + "/c_program/version/compile/" + version_c_program_1_2.id)
                                    .setContentType("undefined").setMethod("PUT").setHeader("X-AUTH-TOKEN", token.authToken).setRequestTimeout(50000).put("");

                            JsonNode result_1_2 = responsePromise_1_2.get(50000).asJson();
                            System.out.print("Result 1_2 " + result_1_2.toString());

                        }catch (Exception e){
                            e.printStackTrace();
                        }


                        try{

                            F.Promise<WSResponse> responsePromise_2 = Play.current().injector().instanceOf(WSClient.class).url(Server.tyrion_serverAddress + "/c_program/version/compile/" + version_c_program_2.id)
                                    .setContentType("undefined").setMethod("PUT").setHeader("X-AUTH-TOKEN", token.authToken).setRequestTimeout(50000).put("");

                            JsonNode result_2 = responsePromise_2.get(50000).asJson();
                            System.out.print("Result 2 " + result_2.toString());

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                         try{

                            F.Promise<WSResponse> responsePromise_3 = Play.current().injector().instanceOf(WSClient.class).url(Server.tyrion_serverAddress + "/c_program/version/compile/" + version_c_program_3.id)
                                    .setContentType("undefined").setMethod("PUT").setHeader("X-AUTH-TOKEN", token.authToken).setRequestTimeout(50000).put("");

                            JsonNode result_3 = responsePromise_3.get(50000).asJson();
                            System.out.print("Result 3 " + result_3.toString());

                        }catch (Exception e){
                            e.printStackTrace();
                        }


                        try{

                             F.Promise<WSResponse> responsePromise_4 = Play.current().injector().instanceOf(WSClient.class).url(Server.tyrion_serverAddress + "/c_program/version/compile/" + version_c_program_4.id)
                                .setContentType("undefined").setMethod("PUT").setHeader("X-AUTH-TOKEN", token.authToken).setRequestTimeout(50000).put("");

                            JsonNode result_4 = responsePromise_4.get(50000).asJson();
                            System.out.print("Result 4 " + result_4.toString());

                        }catch (Exception e){
                            e.printStackTrace();
                        }


                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            };

            System.err.println("Zapínám žádost o kompilace");
            //compile_that.start();


     // První verze B_Programu - Pro instanc Yoda E a Ci!
            B_Program b_program_1 = new B_Program();
            b_program_1.name = "První blocko program";
            b_program_1.description = "Blocko program je úžasná věc když funguje... a tady v tomto progtramu už je připravený i HW!!!!";
            b_program_1.date_of_create = new Date();
            b_program_1.project = project_1;
            b_program_1.save();


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
            b_program_2.name = "Druhý blocko program - Určený pro Yodu B ";
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
            b_program_3.name = "Druhý blocko program - Určený pro Yodu A ";
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

            /*
            // Instance 3 - Voktorův Yoda
            B_Program b_program_4 = new B_Program();
                b_program_4.name = "Čtvrtý blocko program - Určený pro Yodu D";
                b_program_4.description = "Tento program má sloužit Davidovi";
                b_program_4.date_of_create = new Date();
                b_program_4.project = project_1;
                b_program_4.instance = new Homer_Instance();
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
            */

            Thread uploud_instances = new Thread() {
                @Override
                public void run() {
                    try {

                        // Nahraju instance ??? - Ale pak je tu problém nehotových C_programů.... :(
                        System.err.println("Zapínám vlákno nahrávání instnací na server");
                        sleep(4000);

                        System.out.println("Zapínám nahrávání instnací do serveru");

                        try{

                            F.Promise<WSResponse> responsePromise_1 = Play.current().injector().instanceOf(WSClient.class).url(Server.tyrion_serverAddress + "/b_program/uploadToCloud/" + version_b_program_1.id)
                                    .setContentType("undefined").setMethod("PUT").setHeader("X-AUTH-TOKEN", token.authToken).setRequestTimeout(5000).put("");
                            JsonNode result_1 = responsePromise_1.get(5000).asJson();

                            System.out.print("Result 1 " + result_1.toString());

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        try{

                            F.Promise<WSResponse> responsePromise_2 = Play.current().injector().instanceOf(WSClient.class).url(Server.tyrion_serverAddress + "/b_program/uploadToCloud/" + version_b_program_2.id)
                                    .setContentType("undefined").setMethod("PUT").setHeader("X-AUTH-TOKEN", token.authToken).setRequestTimeout(5000).put("");
                            JsonNode result_2 = responsePromise_2.get(5000).asJson();

                            System.out.print("Result 2 " + result_2.toString());

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        try{

                            F.Promise<WSResponse> responsePromise_3 = Play.current().injector().instanceOf(WSClient.class).url(Server.tyrion_serverAddress + "/b_program/uploadToCloud/" + version_b_program_3.id)
                                    .setContentType("undefined").setMethod("PUT").setHeader("X-AUTH-TOKEN", token.authToken).setRequestTimeout(5000).put("");
                            JsonNode result_3 = responsePromise_3.get(5000).asJson();

                            System.out.print("Result 3 " + result_3.toString());

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        /*

                        F.Promise<WSResponse> responsePromise_4 = Play.current().injector().instanceOf(WSClient.class).url(Server.tyrion_serverAddress + "/project/b_program/uploadToCloud/" + version_c_program_4.id)
                                .setContentType("undefined").setMethod("PUT").setHeader("X-AUTH-TOKEN", token.authToken).setRequestTimeout(50000).put("");
                        JsonNode result_4 = responsePromise_4.get(50000).asJson();

                        System.out.print("Result 4 " + result_4.toString());

                        */


                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            };

            System.err.println("Nahrávám instnace na server");
            uploud_instances.start();


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





        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void set_Developer_objects(){

        // For Developing
        if(SecurityRole.findByName("SuperAdmin") == null){
            SecurityRole role = new SecurityRole();
            role.person_permissions.addAll(PersonPermission.find.all());
            role.name = "SuperAdmin";
            role.save();
        }

        if (Person.find.where().eq("mail", "admin@byzance.cz").findUnique() == null)
        {
            System.err.println("Creating first admin account: admin@byzance.cz, password: 123456789, token: token");
            Person person = new Person();
            person.full_name = "Admin Byzance";
            person.mailValidated = true;
            person.nick_name = "Syndibád";
            person.mail = "admin@byzance.cz";
            person.setSha("123456789");
            person.roles.add(SecurityRole.findByName("SuperAdmin"));

            person.save();

            FloatingPersonToken floatingPersonToken = new FloatingPersonToken();
            floatingPersonToken.set_basic_values();
            floatingPersonToken.person = person;
            floatingPersonToken.user_agent = "Unknown browser";
            floatingPersonToken.save();

        }else{
            // updatuji oprávnění
            Person person = Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            List<PersonPermission> personPermissions = PersonPermission.find.all();

            for(PersonPermission personPermission :  personPermissions) if(!person.person_permissions.contains(personPermission)) person.person_permissions.add(personPermission);
            person.update();
        }

    }





}
