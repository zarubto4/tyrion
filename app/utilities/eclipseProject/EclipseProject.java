package utilities.eclipseProject;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;

public class EclipseProject {

    private static SecureRandom random = new SecureRandom();

    public static void createFullnewProject(){
        try {

            String generatedName = "name";
            String nameOfProject = "YodaMaster";
            String libraryName = "stm32l1xx";

            while (true) {
                generatedName = new BigInteger(60, random).toString(32);

                File f = new File("files/" + generatedName);

                if(!f.exists() && !f.isDirectory()) break;
            }

            //1) Vytvořím strukturu složek s náhodně vygenerovaným názvem
            new File("files/" + generatedName).mkdir();

            //2) -> do složky dám složku drivers
            new File("files/" + generatedName + "/drivers").mkdir();
            new File("files/" + generatedName + "/project").mkdir();
            new File("files/" + generatedName + "/project/" + nameOfProject).mkdir();

            new File("files/" + generatedName + "/project/" + nameOfProject + "/Inc").mkdir();
            new File("files/" + generatedName + "/project/" + nameOfProject + "/Src").mkdir();
            new File("files/" + generatedName + "/project/" + nameOfProject + "/SW4STM32").mkdir();
            new File("files/" + generatedName + "/project/" + nameOfProject + "/SW4STM32" + "/" + nameOfProject + " Configuration").mkdir();

            //3) -> vytvořím všechny soubory (JSON)
            new File("files/" +generatedName+"/project/"+nameOfProject+ "/Inc" +"/main.h").createNewFile();
            new File("files/" +generatedName+"/project/"+nameOfProject+ "/Inc" +"/ffconf.h").createNewFile();
            new File("files/" +generatedName+"/project/"+nameOfProject+ "/Inc/"+ libraryName + "_hal_conf.h").createNewFile();
            new File("files/" +generatedName+"/project/"+nameOfProject+ "/Inc/"+ libraryName + "_it.h").createNewFile();

            new File("files/" +generatedName+"/project/"+nameOfProject+ "/Src/" +"main.c").createNewFile();
            new File("files/" +generatedName+"/project/"+nameOfProject+ "/Src/" +libraryName + "_hal_msp.c").createNewFile();
            new File("files/" +generatedName+"/project/"+nameOfProject+ "/Src/" +libraryName + "_it.c").createNewFile();

            String f_adress = "files/" +generatedName+"/project/"+nameOfProject+"/" + "SW4STM32/"+ nameOfProject +" Configuration";
            new File(f_adress).mkdir();

            new File(f_adress + "/.cdproject").createNewFile();
            new File(f_adress + "/.DS_Store" ).createNewFile();
            new File(f_adress + "/.project"  ).createNewFile();

            //4) -> vytvořím složku se jménem projektu (JSON)

            //5) -> Podle struktury vytvořím soubory a nahraju do složky projektu

            //6) -> Bytvořím .project a .cdproject (XML) -> Podle JSON

        }catch (Exception e){

        }
    }


    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }

    public void copyProjectFromRepo(){

    }




}


