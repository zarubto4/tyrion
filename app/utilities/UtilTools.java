package utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import controllers.WebSocketController_Incoming;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import models.grid.Screen_Size_Type;
import models.overflow.HashTag;
import models.overflow.Post;
import models.person.Person;
import models.person.PersonPermission;
import models.person.SecurityRole;
import models.project.m_program.M_Project;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.WebSocket;
import utilities.webSocket.developing.WS_Homer_Cloud;
import utilities.webSocket.developing.WS_Terminal_Local;
import utilities.webSocket.developing.WebSCType;

import java.io.*;
import java.util.*;

/**
 * Pomocná třída, realizující jednoúčelové metody. Zde si odkládám cokoliv, co by bylo v kódu zbytečně duplicitní.
 * Například při create a edit vždy ukládám něco. Vždy parsuji datum.. atd.
 */

public class UtilTools extends Controller {

    static play.Logger.ALogger logger = play.Logger.of("Loggy");

    public static void add_hashTags_to_Post( List<String> hash_tags, Post post){

        for (final String hs : hash_tags) {

            HashTag hash_tag = HashTag.find.byId(hs);

            if(hash_tag == null) {
                hash_tag = new HashTag(hs);
                hash_tag.save();
            }

            if(!post.hashTagsList.contains(hash_tag)) post.hashTagsList.add(hash_tag);
        }
    }

    /**
     *  MEtoda slouží k rekurzivnímu procháázení úrovně adresáře v Azure data storage a mazání jeho obsahu.
     *  Azure data storage je totiž jednoúrovňové datové skladiště! KJde není možné vytvářet složky, v nich složky
     *  a do nich dávat soubory. Jménbo souboru sice má podobu neco/neco2/něco3/soubor.txt ale je to přímá cesta.
     *  Proto když je potřeba promazat něco v něco2 a všechno dál, je nutné nasadit rekurzivní algoritmus, který se
     *  podívá na obsah a vše promazává.
     *
     */
    public static void azureDelete(CloudBlobContainer container, String pocatekMazani) throws Exception {

        for (ListBlobItem blobItem : container.listBlobs( pocatekMazani + "/" )) {


            if (blobItem instanceof CloudBlob) ((CloudBlob) blobItem).deleteIfExists();

            // Break & loop
            String help = blobItem.getUri().toString().substring(0,blobItem.getUri().toString().length() -1);

            azureDelete(container, pocatekMazani +  help.substring( help.lastIndexOf("/") ,help.length()) );
        }
    }


    //TODO tohle asi nebude úplně fungovat
    public static void upload_to_Azure_in_File_from_Json(String containerName, String temporaryDirectory, JsonNode json, String azurePath, Version_Object versionObjectObject) throws Exception{

         new File(temporaryDirectory).mkdirs();

            for (final JsonNode objNode : json) {

                String data = objNode.get("content").asText();
                String file_name = objNode.get("file_name").asText() + ".cs";

                File file = new File(temporaryDirectory +"/" + file_name);
                file.createNewFile();

                //true = append file
                FileWriter fileWritter = new FileWriter(file.getName(), true);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                bufferWritter.write(data);
                bufferWritter.close();

                // Připojuji se a tvořím cestu souboru
                CloudBlobContainer container = Server.blobClient.getContainerReference(containerName);

                CloudBlockBlob blob = container.getBlockBlobReference(azurePath +"/" +file_name);

                blob.upload(new FileInputStream(file), file.length());


                FileRecord fileRecord = new FileRecord();
                fileRecord.file_name = file_name;
                fileRecord.save();

                versionObjectObject.files.add(fileRecord);
                versionObjectObject.update();
            }

        FileUtils.deleteDirectory(new File(temporaryDirectory));
    }

    /**
     * Nahrávání souborů na Azure!
     * @param container_name = jméno kontejneru - tyto kontejnery je nutné vytvořit dopředu v Azure data storage.
     * @param file_content   = první úroveň adresáře v kontejneru (library, groupOfLibrary, private, public atd.)
     * @param file_name      = jméno samotného souboru
     * @param azureStorageLink = Jméno balíčku (náhodně generované)
     * @param azurePackageLink = Jméno druhu objektu (singlelibrary, c_program..) (musí být malými písmeny dle dokumentace Microsoft !!)
     * @param versionObjectObject = číslo verze respektive string verze, která odděluje stejné soubory v jiných verzích
     * @throws Exception
     */
    public static void uploadAzure_Version(String container_name, String file_content, String file_name, String azureStorageLink, String azurePackageLink, Version_Object versionObjectObject) throws Exception{

            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
            CloudBlockBlob blob = container.getBlockBlobReference(azureStorageLink +"/" + azurePackageLink  +"/" + versionObjectObject.azureLinkVersion  +"/" + file_name);

            InputStream is = new ByteArrayInputStream(file_content.getBytes());
            blob.upload(is, -1);

            FileRecord fileRecord = new FileRecord();
            fileRecord.file_name = file_name;
            fileRecord.version_object = versionObjectObject;
            fileRecord.save();

            versionObjectObject.files.add(fileRecord);
            versionObjectObject.update();
    }

    public static File file_get_File_from_Azure(String container_name, String azureStorageLink, String azurePackageLink, Integer azureLinkVersion, String filename)throws Exception{

        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
        CloudBlob blob = container.getBlockBlobReference( azureStorageLink +"/"+ azurePackageLink  +"/"+ azureLinkVersion  +"/"+ filename);

        File fileMain = new File("files/" + azureStorageLink + azurePackageLink + azureLinkVersion + filename);
        // Tento soubor se nesmí zapomínat mazat!!!!
        OutputStream outputStreamMain = new FileOutputStream (fileMain);

        blob.download(outputStreamMain);

        return fileMain;
    }

    public static byte[] loadFile(File file) throws IOException {
        byte[] data = FileUtils.readFileToByteArray(file);
        return  Base64.getEncoder().encode(data);
    }

    public static Map<String, String> getMap_From_querry(Set<Map.Entry<String, String[]>> url){
        Map<String, String> map = new HashMap<>();

        for (Map.Entry<String,String[]> entry : url) {

            final String key = entry.getKey();
            final String value = Arrays.toString(entry.getValue());
            map.put(key, value);
        }
        return  map;
    }

    public static WebSocket<String>  b_program_in_cloud(M_Project m_project, String terminal_id){

        System.out.println("Blocko Program je v Cloudu a to zatím není plně otestované!!");

        String instance_name = m_project.b_program_version.b_program_cloud.blocko_instance_name;
        String server_name   = m_project.b_program_version.b_program_cloud.blocko_server_name;


        WebSCType terminal = new WS_Terminal_Local(terminal_id, WebSocketController_Incoming.incomingConnections_terminals);
        WebSocket<String> ws = terminal.connection();

        if (!WebSocketController_Incoming.cloud_servers.containsKey(server_name)) {

            System.out.println("BLocko program je provozován na serveru, který není připojen...");
            System.out.println("Měl bych ho zařadit terminál do seznamu ztracených připojení");

            if (WebSocketController_Incoming.terminal_lost_connection_homer.containsKey(instance_name)) {
                System.out.println("Ztracené spojení už bylo dávno vytvořeno s cloud programem s jiným prvkem ale pořád nejsem spojen a tak přidávám další hodnotu: " + instance_name);
                WebSocketController_Incoming.terminal_lost_connection_homer.get(instance_name).add(terminal_id);
            } else {
                System.out.println("Ještě žádné ztracené spojení nebylo vytvořeno s " + instance_name + " A tak vytvářím a přidávám první hodnotu");
                ArrayList<String> list = new ArrayList<>(4);
                list.add(terminal_id);
                WebSocketController_Incoming.terminal_lost_connection_homer.put(instance_name, list);
            }
            System.out.println("Chystám se upozornit terminál že Cloud Homer není připojený");
            WebSocketController_Incoming.homer_is_not_connected_yet(terminal);
            System.out.println("Upozornil jsem terminál že Homer není připojený");
            return ws;
        }

        if (!WebSocketController_Incoming.cloud_servers.get(server_name).containsKey(instance_name)) {
            System.out.println("Konkrétní instance B_programu není na serveru zprovozněna!");
            System.out.println("Měl bych ho zprovoznit? Ještě asi nejsem na to připraven"); // TODO - zprovoznit připojení

            return WebSocket.reject( badRequest());
        }

        System.out.println("Budu propojovat s Homererem v cloudu protože je připojený a instance běží: ");

        WS_Homer_Cloud homer = (WS_Homer_Cloud) WebSocketController_Incoming.cloud_servers.get(server_name).get(instance_name);


        terminal.subscribers.add(homer);
        if (homer.subscribers.isEmpty()) WebSocketController_Incoming.ask_for_receiving(homer);
        homer.subscribers.add(terminal);

        return ws;
    }





    public static void set_Developer_objects(){

        // For Developing
        if(SecurityRole.findByName("SuperAdmin") == null){
            SecurityRole role = new SecurityRole();
            role.permissions.addAll(PersonPermission.find.all());
            role.name = "SuperAdmin";
            role.save();
        }

        if (Person.find.where().eq("mail", "admin@byzance.cz").findUnique() == null)
        {
            logger.warn("Creating first admin account: admin@byzance.cz, password: 123456789");
            Person person = new Person();
            person.full_name = "Admin Byzance";
            person.mailValidated = true;
            person.mail = "admin@byzance.cz";
            person.setSha("123456789");
            person.roles.add(SecurityRole.findByName("SuperAdmin"));

            person.save();
        }


        if( Screen_Size_Type.find.where().eq("name","iPhone6").findUnique() == null){

            Logger.warn("Creating screen size type for developers iPhone`s");
            Screen_Size_Type screen_size_type = new Screen_Size_Type();

            screen_size_type.name = "iPhone6";

            screen_size_type.landscape_height = 375;
            screen_size_type.landscape_width = 667;
            screen_size_type.landscape_square_height = 6;
            screen_size_type.landscape_square_width = 11;
            screen_size_type.landscape_max_screens = 10;
            screen_size_type.landscape_min_screens = 1;

            screen_size_type.portrait_height = 667;
            screen_size_type.portrait_width = 375;
            screen_size_type.portrait_square_height = 11;
            screen_size_type.portrait_square_width = 6;
            screen_size_type.portrait_max_screens = 10;
            screen_size_type.portrait_min_screens = 1;

            screen_size_type.height_lock  = true;
            screen_size_type.width_lock   = true;
            screen_size_type.touch_screen = true;

            screen_size_type.save();

        }

    }

}
