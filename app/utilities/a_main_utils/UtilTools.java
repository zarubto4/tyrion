package utilities.a_main_utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import models.compiler.FileRecord;
import models.compiler.Version_Object;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Pomocná třída, realizující jednoúčelové metody. Zde si odkládám cokoliv, co by bylo v kódu zbytečně duplicitní.
 * Například při create a edit vždy ukládám něco. Vždy parsuji datum.. atd.
 */

public class UtilTools {


    public static Date returnDateFromMillis(String millis){
        long milliSeconds= Long.parseLong(millis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return calendar.getTime();
    }

    public static Long returnIntFromString(String string) throws Exception{
       try{
           return Long.getLong(string);
       }catch (Exception e){
           throw  new Exception("Incoming Value " + string + " is not regular number");
       }
    }

    public static List<String> getListFromJson(JsonNode json, String name){
        List<String> list = new ArrayList<>();
        for (final JsonNode objNode : json.get(name)) { list.add(objNode.asText()); }
        return list;
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

    /* TODO dodělat nahrávání souboru, jakmile se opraví PostMan
    public static void uploadAzure_File(String containerName, String temporaryDirectory, JsonNode json, String azurePath, Version_Object versionObjectObject) throws Exception{

         new File(temporaryDirectory).mkdirs();


            for (final JsonNode objNode : json) {

                String data = objNode.get("content").asText();
                String fileName = objNode.get("fileName").asText() + ".cs";

                File file = new File(temporaryDirectory +"/" + fileName);
                file.createNewFile();

                //true = append file
                FileWriter fileWritter = new FileWriter(file.getName(), true);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                bufferWritter.write(data);
                bufferWritter.close();

                // Připojuji se a tvořím cestu souboru
                CloudBlobContainer container = GlobalValue.blobClient.getContainerReference(containerName);

                CloudBlockBlob blob = container.getBlockBlobReference(azurePath +"/" +fileName);

                blob.upload(new FileInputStream(file), file.length());


                FileRecord fileRecord = new FileRecord();
                fileRecord.filename = fileName;
                fileRecord.save();

                versionObjectObject.files.add(fileRecord);
                versionObjectObject.update();
            }

        FileUtils.deleteDirectory(new File(temporaryDirectory));
    }
    */

    /**
     * Nahdávání souborů na Azure!
     * @param container_name = jméno kontejneru - tyto kontejnery je nutné vytvořit dopředu v Azure data storage.
     * @param file_content   = první úroveň adresáře v kontejneru (library, groupOfLibrary, private, public atd.)
     * @param file_name      = jméno samotného souboru
     * @param azureStorageLink = Jméno balíčku (náhodně generované)
     * @param azurePackageLink = Jméno druhu objektu (singlelibrary, c_program..) (musí být malými písmeny dle dokumentace Microsoft !!)
     * @param versionObjectObject = číslo verze respektive string verze, která odděluje stejné soubory v jiných verzích
     * @throws Exception
     */
    public static void uploadAzure_Version(String container_name, String file_content, String file_name, String azureStorageLink, String azurePackageLink, Version_Object versionObjectObject) throws Exception{

            CloudBlobContainer container = GlobalValue.blobClient.getContainerReference(container_name);
            CloudBlockBlob blob = container.getBlockBlobReference(azureStorageLink +"/" + azurePackageLink  +"/" + versionObjectObject.azureLinkVersion  +"/" + file_name);

            InputStream is = new ByteArrayInputStream(file_content.getBytes());
            blob.upload(is, -1);

            FileRecord fileRecord = new FileRecord();
            fileRecord.fileName = file_name;
            fileRecord.version_object = versionObjectObject;
            fileRecord.save();

            versionObjectObject.files.add(fileRecord);
            versionObjectObject.update();
    }

    public static File file_get_File_from_Azure(String container_name, String azureStorageLink, String azurePackageLink, Integer azureLinkVersion, String filename)throws Exception{

        CloudBlobContainer container = GlobalValue.blobClient.getContainerReference(container_name);
        CloudBlob blob = container.getBlockBlobReference( azureStorageLink +"/"+ azurePackageLink  +"/"+ azureLinkVersion  +"/"+ filename);

        File fileMain = new File("files/"+ azureStorageLink + azurePackageLink + azureLinkVersion + filename);
        // Tento soubor se nesmí zapomínat mazat!!!!
        OutputStream outputStreamMain = new FileOutputStream (fileMain);

        blob.download(outputStreamMain);

        return fileMain;
    }
}
