package utilities;

import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.compiler.Cloud_Compilation_Server;
import models.compiler.FileRecord;
import models.compiler.TypeOfBoard;
import models.compiler.Version_Object;
import models.grid.Screen_Size_Type;
import models.overflow.HashTag;
import models.overflow.Post;
import models.person.Person;
import models.person.PersonPermission;
import models.person.SecurityRole;
import play.Logger;
import play.mvc.Controller;

import java.io.*;
import java.util.*;

/**
 * Pomocná třída, realizující jednoúčelové metody. Zde si odkládám cokoliv, co by bylo v kódu zbytečně duplicitní.
 * Například při create a edit vždy ukládám něco. Vždy parsuji datum.. atd.
 */

public class UtilTools extends Controller {

    // Loger
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

    public static FileRecord uploadAzure_Version(String container_name, String file_content, String file_name, String azureStorageLink, String azurePackageLink, Version_Object version_object) throws Exception{
        logger.debug("Azure load: "+ container_name +"/"+ azureStorageLink +"/" + azurePackageLink  +"/" + version_object.azureLinkVersion  +"/" + file_name );

        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
        CloudBlockBlob blob = container.getBlockBlobReference(azureStorageLink +"/" + azurePackageLink  +"/" + version_object.azureLinkVersion  +"/" + file_name);

        InputStream is = new ByteArrayInputStream(file_content.getBytes());
        blob.upload(is, -1);

        FileRecord fileRecord = new FileRecord();
        fileRecord.file_name = file_name;
        fileRecord.version_object = version_object;
        fileRecord.file_path = container_name +"/"+ azureStorageLink +"/" + azurePackageLink  +"/" + version_object.azureLinkVersion  +"/" + file_name;
        fileRecord.save();

        version_object.files.add(fileRecord);
        version_object.update();

        return fileRecord;
    }


    public static FileRecord uploadAzure_Version(String container_name, File file, String file_name, String azureStorageLink, String azurePackageLink, Version_Object version_object) throws Exception{

        if(azureStorageLink == null) throw new Exception("azureStorageLink == null");
        if(azurePackageLink == null) throw new Exception("azurePackageLink == null");
        if(version_object == null)   throw new Exception("version_object == null");

        logger.debug("Azure load: "+ container_name +"/"+ azureStorageLink +"/" + azurePackageLink  +"/" + version_object.azureLinkVersion  +"/" + file_name );

        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
        CloudBlockBlob blob = container.getBlockBlobReference(azureStorageLink +"/" + azurePackageLink  +"/" + version_object.azureLinkVersion  +"/" + file_name);

        InputStream is = new FileInputStream(file);
        blob.upload(is, -1);

        FileRecord fileRecord = new FileRecord();
        fileRecord.file_name = file_name;
        fileRecord.version_object = version_object;
        fileRecord.file_path = container_name +"/"+ azureStorageLink +"/" + azurePackageLink  +"/" + version_object.azureLinkVersion  +"/" + file_name;
        fileRecord.save();

        version_object.files.add(fileRecord);
        version_object.update();

            // Sobor smažu z adresáře
            try {
                file.delete();
            }catch (Exception e){}

        return fileRecord;
    }

    public static File file_get_File_from_Azure(String file_path)throws Exception{

        int slash = file_path.indexOf("/");
        String container_name = file_path.substring(0,slash);
        String real_file_path = file_path.substring(slash+1);

        logger.debug("Azure load path: " + file_path );
        logger.debug("Azure Container: " + container_name);
        logger.debug("Real File  Path: " + real_file_path);

        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name );

        CloudBlob blob = container.getBlockBlobReference(real_file_path );

        File fileMain = new File("files/" + UUID.randomUUID().toString()  );

        // Tento soubor se nesmí zapomínat mazat!!!!
        OutputStream outputStreamMain = new FileOutputStream (fileMain);

        blob.download(outputStreamMain);

        return fileMain;
    }


    public static String get_encoded_binary_string_from_File(File binary_file) throws Exception {

        String encodedBase64 = null;

        FileInputStream fileInputStreamReader = new FileInputStream(binary_file);
        byte[] bytes = new byte[(int)binary_file.length()];
        fileInputStreamReader.read(bytes);
        encodedBase64 = new String(Base64.getEncoder().encode(bytes));

        return encodedBase64;

    }

    public static String get_encoded_binary_string_from_body(String binary_body) throws Exception {

        String encodedBase64 = null;

        encodedBase64 = new String(Base64.getEncoder().encode(binary_body.getBytes()));

        return encodedBase64;

    }


    public static FileRecord create_Binary_file(String file_content, String file_name) throws Exception{
        logger.debug("Azure create_Binary_file: binaryFile/"+ file_name );

        String authToken = UUID.randomUUID().toString();

        CloudBlobContainer container = Server.blobClient.getContainerReference("binary-file");
        CloudBlockBlob blob = container.getBlockBlobReference( authToken +"/" + file_name );

        InputStream is = new ByteArrayInputStream(file_content.getBytes());
        blob.upload(is, -1);

        FileRecord fileRecord = new FileRecord();
        fileRecord.file_name = file_name;
        fileRecord.file_path = "binary-file" +"/" + authToken +"/" + file_name;
        fileRecord.save();


        return fileRecord;
    }

    public static void remove_file_from_Azure(FileRecord file){
        try{

            String container_name = "c-program";
            String azureStorageLink;
            String azurePackageLink;
            String azureLinkVersion;
            String filename;

            Version_Object version = file.version_object;

            azureLinkVersion = version.azureLinkVersion;
            azurePackageLink = version.c_program.azurePackageLink;
            azureStorageLink = version.c_program.azureStorageLink;
            filename = file.file_name;



            CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
            CloudBlob blob = container.getBlockBlobReference( azureStorageLink +"/"+  azurePackageLink+"/"+ azureLinkVersion  +"/"+ filename);
            blob.delete();

        }catch (Exception e){
            e.printStackTrace();
        }
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

    public static void set_Type_of_board(){

        if(TypeOfBoard.find.where().eq("name", "BYZANCE_YODAG2").findList().size() < 1){

            TypeOfBoard typeOfBoard = new TypeOfBoard();
            typeOfBoard.name = "BYZANCE_YODAG2";
            typeOfBoard.connectible_to_internet = true;
            typeOfBoard.description = "testovací deska pro kompilaci";
            typeOfBoard.save();
        }

    }

    public static void set_Homer_Server(){

        if(Cloud_Homer_Server.find.where().eq("server_name", "Alfa").findUnique() == null ){
            Cloud_Homer_Server server = new Cloud_Homer_Server();
            server.server_name = "Alfa";
            server.destination_address = Server.tyrion_webSocketAddress + "/websocket/compilation_server/" + server.server_name;
            server.set_hash_certificate();
            server.save();
        }

        if(Cloud_Homer_Server.find.where().eq("server_name", "Beta").findUnique() == null ){
            Cloud_Homer_Server server = new Cloud_Homer_Server();
            server.server_name = "Beta";
            server.destination_address = Server.tyrion_webSocketAddress + "/websocket/compilation_server/" + server.server_name;
            server.set_hash_certificate();
            server.save();
        }

    }

    public static void set_Compilation_Server(){

        if(Cloud_Compilation_Server.find.where().eq("server_name", "Alfa").findUnique() == null ){
            Cloud_Compilation_Server server = new Cloud_Compilation_Server();
            server.server_name = "Alfa";
            server.destination_address = Server.tyrion_webSocketAddress + "/websocket/compilation_server/" + server.server_name;
            server.set_hash_certificate();
            server.save();
        }

        if(Cloud_Compilation_Server.find.where().eq("server_name", "ubuntu1").findUnique() == null ){
            Cloud_Compilation_Server server = new Cloud_Compilation_Server();
            server.server_name = "ubuntu1";
            server.destination_address = Server.tyrion_webSocketAddress + "/websocket/compilation_server/" + server.server_name;
            server.set_hash_certificate();
            server.save();
        }

    }

    public static void set_Developer_objects(){

        // For Developing
        if(SecurityRole.findByName("SuperAdmin") == null){
            SecurityRole role = new SecurityRole();
            role.person_permissions.addAll(PersonPermission.find.all());
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

        }else{
            // updatuji oprávnění
            Person person = Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            List<PersonPermission> personPermissions = PersonPermission.find.all();

            for(PersonPermission personPermission :  personPermissions) if(!person.person_permissions.contains(personPermission)) person.person_permissions.add(personPermission);
            person.update();
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


    public static String get_String_from_file(File file) {
        try {
            Scanner scanner = new Scanner(file);
            String text = scanner.useDelimiter("\\A").next();
            scanner.close();

            if(! file.delete()) logger.warn("File wasn't removed from folder!");

            return text;

        }catch (Exception e){
            logger.error("Get string from file exception!", e);
            return null;
        }
    }
}
