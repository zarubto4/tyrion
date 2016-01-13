package webSocket.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;


/**
    Tato třída volaná jako singleton slouží k načtení konfiguračního souboru.

    Možné použití třídy
    PropertiesOfDevice.getInstance.properties.getProperty("požadovaný string z config filu");

    *.getProperty("ServerName"); =>  String ServerName = ProjektTara;
    *.
 */

public class PropertiesOfDevice {

    private static PropertiesOfDevice instance;
    public  static Properties         properties;
    private static InputStream input;


    public static PropertiesOfDevice getInstance(){
        if(instance == null)  instance = new PropertiesOfDevice();
        return instance;
    }

    private PropertiesOfDevice() {
        try {
            properties = new Properties();
            input = new FileInputStream("config.properties");
            properties.load(input);

        }catch (Exception e ){
            System.out.println(e.getMessage());
            System.out.println("Server: EROR: Config File is missing in Project Highest File");
        }
    }

    public static Properties getProperties(){
        if(properties == null) instance = new PropertiesOfDevice();
        return properties;
    }


}
