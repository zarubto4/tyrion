package utilities;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import play.Configuration;

public class A_GlobalValue {

    public static CloudStorageAccount storageAccount;
    public static CloudBlobClient blobClient;

    public static void onStart() throws Exception{

        String azureConnection = Configuration.root().getString("azureConnectionLight");
        storageAccount = CloudStorageAccount.parse(azureConnection);
        blobClient = storageAccount.createCloudBlobClient();

    }




}
