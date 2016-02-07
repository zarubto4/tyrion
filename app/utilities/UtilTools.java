package utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import models.compiler.FileRecord;
import models.compiler.Version;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    public static void azureDelete(CloudBlobContainer container, String pocatekMazani) throws Exception {

        System.out.println("Container name: " + container.getName());
        System.out.println("Mazání " + pocatekMazani);

        for (ListBlobItem blobItem : container.listBlobs( pocatekMazani + "/" )) {


            if (blobItem instanceof CloudBlob) {
                System.out.println( "I am deleting file: " + ((CloudBlob) blobItem).getName() );
                ((CloudBlob) blobItem).deleteIfExists();
            }

            // Break & loop
            String help = blobItem.getUri().toString().substring(0,blobItem.getUri().toString().length() -1);

            azureDelete(container, pocatekMazani +  help.substring( help.lastIndexOf("/") ,help.length()) );
        }
    }


    public static void c_ProgramAzure(String temporaryDirectory, JsonNode json, String azurePath, Version versionObject) throws Exception{

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
                CloudBlobContainer container = GlobalValue.blobClient.getContainerReference("c-program");

                CloudBlockBlob blob = container.getBlockBlobReference(azurePath +"/" +fileName);

                blob.upload(new FileInputStream(file), file.length());


                FileRecord fileRecord = new FileRecord();
                fileRecord.filename = fileName;
                fileRecord.save();

                versionObject.files.add(fileRecord);
                versionObject.update();
            }

        FileUtils.deleteDirectory(new File(temporaryDirectory));
    }



}
