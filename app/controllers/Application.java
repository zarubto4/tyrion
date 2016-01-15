package controllers;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.*;
import play.Configuration;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Application extends Controller {

    public Result index(){
        String accept_language = request().getHeader(ACCEPT_LANGUAGE);

        return ok(" Vše je ok  \n Jazyková mutace je " + accept_language);
    }

    public Result test1() throws Exception{

        // Jméno připojení
        String azureConnection = Configuration.root().getString("azureConnectionLight");

        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(azureConnection);

        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
        CloudBlobContainer container1 = blobClient.getContainerReference("container");

        // Create the container if it does not exist.
        container1.createIfNotExists();

        // Create a permissions object.
        BlobContainerPermissions containerPermissions = new BlobContainerPermissions();

        // Include public access in the permissions object.
        containerPermissions.setPublicAccess(BlobContainerPublicAccessType.BLOB);

        // Set the permissions on the container.
        container1.uploadPermissions(containerPermissions);

        // Retrieve storage account from connection-string.
        // Define the path to a local file.
        final String filePath = "files/blablabla.txt";

        CloudBlockBlob blob1 = container1.getBlockBlobReference("ver1/blablabla.txt");
        CloudBlockBlob blob2 = container1.getBlockBlobReference("ver2/blablabla.txt");
        CloudBlockBlob blob3 = container1.getBlockBlobReference("ver3/blablabla.txt");
        CloudBlockBlob blob4 = container1.getBlockBlobReference("blablabla.txt");

        File source1 = new File(filePath);

        blob1.upload(new FileInputStream(source1), source1.length());
        blob2.upload(new FileInputStream(source1), source1.length());
        blob3.upload(new FileInputStream(source1), source1.length());
        blob4.upload(new FileInputStream(source1), source1.length());


        // Loop over blobs within the container and output the URI to each of them.
        for (ListBlobItem blobItem : container1.listBlobs()) {
            System.out.println(blobItem.getUri());
        }


        // Loop through each blob item in the container.
        for (ListBlobItem blobItem : container1.listBlobs()) {
           System.out.println("URI " + blobItem.getUri());
           System.out.println("getStorageUri " + blobItem.getStorageUri() );
            System.out.println("getContainer Name " + blobItem.getContainer().getName() );

            // If the item is a blob, not a virtual directory.
            if (blobItem instanceof CloudBlob) {
                // Download the item and save it to a file with the same name.
                CloudBlob blob12 = (CloudBlob) blobItem;
                blob12.download(new FileOutputStream("files/blabla/" + ((CloudBlob) blobItem).getName() ));
            }

        }



        return ok();
    }



}


