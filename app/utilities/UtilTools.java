package utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;

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



}
