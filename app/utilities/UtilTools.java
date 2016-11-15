package utilities;

import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import models.compiler.FileRecord;
import models.compiler.Version_Object;
import models.overflow.HashTag;
import models.overflow.Post;
import models.person.FloatingPersonToken;
import models.person.Person;
import models.person.PersonPermission;
import models.person.SecurityRole;
import play.mvc.Controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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


    public static boolean controll_vat_number(String vat_number){

            // Jestli je přítomné VAT number - musí dojít ke kontrole validity Vat number!
            switch (vat_number.substring(0,2)){

                case "BE" : { return true; }
                case "BG" : { return true;  }
                case "CZ" : {return true;  }
                case "DK" : {return true; }
                case "EE" : {return true; }
                case "FI" : {return true;  }
                case "FR" : {return true;  }
                case "IE" : {return true; }
                case "IT" : {return true;  }
                case "CY" : {return true;  }
                case "LT" : {return true;  }
                case "LV" : {return true;  }
                case "LU" : {return true; }
                case "HU" : {return true;  }
                case "MT" : {return true;  }
                case "DE" : {return true;  }
                case "NL" : {return true;  }
                case "PT" : {return true;  }
                case "AT" : {return true; }
                case "RO" : {return true;  }
                case "EL" : {return true;  }
                case "SK" : {return true;  }
                case "SI" : {return true;  }
                case "GB" : {return true;  }
                case "ES" : {return true;  }
                case "SE" : {return true;  }
                default: {
                 return false;
                }
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

    public static FileRecord uploadAzure_Version(String file_content, String file_name, String file_path, Version_Object version_object) throws Exception{

        logger.debug("Azure load: "+ file_path + version_object.get_path() + "/" + file_name );


        int slash = file_path.indexOf("/");
        String container_name = file_path.substring(0,slash);
        String real_file_path = file_path.substring(slash+1);
        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name );

        CloudBlockBlob blob = container.getBlockBlobReference( real_file_path + version_object.get_path() + "/" + file_name);

        InputStream is = new ByteArrayInputStream(file_content.getBytes());
        blob.upload(is, -1);

        FileRecord fileRecord = new FileRecord();
        fileRecord.file_name = file_name;
        fileRecord.version_object = version_object;
        fileRecord.file_path =  file_path   + version_object.get_path() + "/" + file_name;
        fileRecord.save();

        version_object.files.add(fileRecord);
        version_object.update();

        return fileRecord;
    }

    public static FileRecord uploadAzure_Version( File file, String file_name, String file_path, Version_Object version_object) throws Exception{

        logger.debug("Azure load: "+ file_path + version_object.get_path() + "/" + file_name);


        int slash = file_path.indexOf("/");
        String container_name = file_path.substring(0,slash);
        String real_file_path = file_path.substring(slash+1);
        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name );

        CloudBlockBlob blob = container.getBlockBlobReference( real_file_path + version_object.get_path() + "/" + file_name);

        InputStream is = new FileInputStream(file);
        blob.upload(is, -1);

        FileRecord fileRecord = new FileRecord();
        fileRecord.file_name = file_name;
        fileRecord.version_object = version_object;
        fileRecord.file_path =   file_path + version_object.get_path()+ "/" + file_name;
        fileRecord.save();

        version_object.files.add(fileRecord);
        version_object.update();

            // Sobor smažu z adresáře
            try {
                file.delete();
            }catch (Exception e){}

        return fileRecord;
    }

    public static FileRecord uploadAzure_File(File file, String file_name, String file_path) throws Exception{

        logger.debug("Azure load: "+ file_path);


        int slash = file_path.indexOf("/");
        String container_name = file_path.substring(0,slash);
        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);

        CloudBlockBlob blob = container.getBlockBlobReference(file_name);

        InputStream is = new FileInputStream(file);
        blob.upload(is, -1);

        FileRecord fileRecord = new FileRecord();
        fileRecord.file_name = file_name;
        fileRecord.file_path = file_path;
        fileRecord.save();

        return fileRecord;
    }


    public static String get_encoded_binary_string_from_File(File binary_file) throws Exception {

        FileInputStream fileInputStreamReader = new FileInputStream(binary_file);
        byte[] bytes = new byte[(int)binary_file.length()];
        fileInputStreamReader.read(bytes);
        String encodedBase64 = new String(Base64.getEncoder().encode(bytes));

        return encodedBase64;

    }

    public static String get_encoded_binary_string_from_body(byte[] bytes) throws Exception {
        String encodedBase64 = new String(Base64.getEncoder().encode( bytes ));
        return encodedBase64;
    }


    public static FileRecord create_Binary_file(String file_path, String file_content, String file_name) throws Exception{

        logger.debug("Azure create_Binary_file: " + file_path +"/"+ file_name );

        int slash = file_path.indexOf("/");
        String container_name = file_path.substring(0,slash);
        String real_file_path = file_path.substring(slash+1);

        logger.debug("Azure save path: " + file_path );
        logger.debug("Azure Container: " + container_name);
        logger.debug("Real File  Path: " + real_file_path);

        CloudBlobContainer container = Server.blobClient.getContainerReference(container_name);
        CloudBlockBlob blob = container.getBlockBlobReference( real_file_path +"/" + file_name );

        InputStream is = new ByteArrayInputStream(file_content.getBytes());
        blob.upload(is, -1);

        FileRecord fileRecord = new FileRecord();
        fileRecord.file_name = file_name;
        fileRecord.file_path = file_path + "/" + file_name;
        fileRecord.save();

        return fileRecord;
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
            logger.warn("Creating first admin account: admin@byzance.cz, password: 123456789, token: token");
            Person person = new Person();
            person.full_name = "Admin Byzance";
            person.mailValidated = true;
            person.nick_name = "Syndibád";
            person.mail = "admin@byzance.cz";
            person.setSha("123456789");
            person.roles.add(SecurityRole.findByName("SuperAdmin"));

            person.save();

            FloatingPersonToken floatingPersonToken = new FloatingPersonToken();
            floatingPersonToken.set_basic_values();
            floatingPersonToken.person = person;
            floatingPersonToken.user_agent = "Unknown browser";
            floatingPersonToken.save();

        }else{
            // updatuji oprávnění
            Person person = Person.find.where().eq("mail", "admin@byzance.cz").findUnique();
            List<PersonPermission> personPermissions = PersonPermission.find.all();

            for(PersonPermission personPermission :  personPermissions) if(!person.person_permissions.contains(personPermission)) person.person_permissions.add(personPermission);
            person.update();
        }

    }

}
