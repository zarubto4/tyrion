package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import tyrex.util.Configuration;

public class Application extends Controller {

    public Result index(){
        String accept_language = request().getHeader(ACCEPT_LANGUAGE);

        return ok(" Vše je ok  \n Jazyková mutace je " + accept_language);
    }

    public Result test1() throws Exception{

        String azureConnection = Configuration.getProperty("azureConnectionLight");
        System.out.println( azureConnection );


        // Retrieve storage account from connection-string.
       // CloudStorageAccount storageAccount = CloudStorageAccount.parse(azureConnection);

        // Create the blob client.
      //  CloudBlobClient blobClient = storageAccount.createCloudBlobClient();


        //storageAccount.

        /*
        try
        {


            // Get a reference to a container.
            // The container name must be lower case
            CloudBlobContainer container = blobClient.getContainerReference("mycontainer");

            // Create the container if it does not exist.
            container.createIfNotExists();




        // Create a permissions object.
        BlobContainerPermissions containerPermissions = new BlobContainerPermissions();

        // Include public access in the permissions object.
        containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);

        // Set the permissions on the container.
        container.uploadPermissions(containerPermissions);




        }
        catch (Exception e)
        {
            // Output the stack trace.
            e.printStackTrace();
        }

        */
        return null;
    }



}


