package utilities.a_main_utils;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import controllers.CompilationLibrariesController;
import controllers.PermissionController;
import play.Configuration;
import play.Logger;
import utilities.webSocket.ClientThreadChecker;

public class GlobalValue {

    public static CloudStorageAccount storageAccount;
    public static CloudBlobClient blobClient;
    public static String serverAddress;

    public static void set_Server() throws Exception{

        /**
         * 1)
         * Nastavení, která globální proměná se bude používat. Vychází z application.conf podle toho zda je Server.production
         * Server Address = například http://localhost:9000/ pokud vývojář vyvíjí systém u sebe na počítači a potřebuje si ho testovat
         * skrze PostMan, nebo http://tyrion.byzance.cz
         *
         * Předpokládá se, že v rámci výpočetních úspor by bylo vhodnější mít pevné řetězce v objektech to jest nahradit
         * --- >@JsonProperty public String  versions()  { return GlobalValue.serverAddress + "/project/blockoBlock/versions/"  + this.id;}
         * --- >@JsonProperty public String  versions()  { return "http//www.byzance.cz/project/blockoBlock/versions/"  + this.id;}
         *
         * Zatím, se zdá vhodnější varianta přepínání v configuračním souboru. Tomáš Záruba 15.2.16
         */
        if( Configuration.root().getBoolean("Server.developerMode"))   serverAddress = Configuration.root().getString("Server.localhost");
        else                                                           serverAddress = Configuration.root().getString("Server.production");

        /**
         * 2)
         * Nastavení Azure připojení
         * jelikož v době vývoje nebylo možné realizovat různá připojení, bylo nutné zajistit pouze jedno připojení v počátku
         */
        String azureConnection = Configuration.root().getString("Azure.azureConnectionSecret");
        storageAccount = CloudStorageAccount.parse(azureConnection);
        blobClient = storageAccount.createCloudBlobClient();

    }


    public static void set_Blocko_Server_Connection(){

        if (Configuration.root().getBoolean("Servers.blocko.server1.run")) {

            Logger.warn("Starting Main Thread for Blocko Server1 ");

            ClientThreadChecker clientThreadChecker = new ClientThreadChecker()
                    .setIDentificator(Configuration.root().getString("Servers.blocko.server1.name"))
                    .setPeriodReconnectionTime(Configuration.root().getInt("Servers.blocko.server1.periodicTime"))
                    .setReconnection(true)
                    .setServerAddress(Configuration.root().getString("Servers.blocko.server1.url"))
                    .connectToServer();
        }

    }


    /**
     * Metoda slouží k zavolání hlavníchm neměnných metod v controllerech,
     * kde se evidují přístupové klíče jednotlivých metod controlleru.
     *
     * Každý controller by měl mít svůj seznam oprávnění.
     * @throws Exception
     */
    public static void setPermission() throws Exception{

        CompilationLibrariesController.set_System_Permission();
        PermissionController.set_System_Permission();

    }






}
