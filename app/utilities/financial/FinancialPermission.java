package utilities.financial;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClientException;
import models.Model_Person;
import models.Model_Product;
import models.Model_ProductExtension;
import models.Model_Project;
import play.libs.Json;
import utilities.Server;
import utilities.document_db.DocumentDB;
import utilities.document_db.document_objects.DM_RestApiRequestRecord;
import utilities.enums.Enum_ExtensionType;
import utilities.logger.Class_Logger;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class FinancialPermission {

    private static final Class_Logger terminal_logger = new Class_Logger(FinancialPermission.class);

    public static boolean check(Model_Product product, String action){

        switch (action) {

            case "Project": {

                List<Model_ProductExtension> filtered = product.extensions.stream().filter(extension -> extension.type == Enum_ExtensionType.Project).collect(Collectors.toList());

                int available = 0;

                for (Model_ProductExtension extension : filtered) {
                    available += extension.count();
                }

                return Model_Project.find.where().eq("product.id", product.id).findRowCount() < available;
            }

            default: return false;
        }
    }

    public static Long checkRestApiRequest(Object object, String instance_id){

        Model_Product product = null;
        //final String custom_id;

        if (object instanceof Model_Product) {

            product = (Model_Product) object;
            //custom_id = instance_id;

        } else if (object instanceof Model_Person) {

            //custom_id = ((Model_Person) object).id;
        } else {
            terminal_logger.internalServerError("checkRestApiRequest:", new Exception("Object is instance of an unknown class."));
            return 0L;
        }

        //terminal_logger.debug("checkRestApiRequest: new request");

        /*
        List<Document> documents = Server.documentClient.queryDocuments(DocumentDB.blocko_request_collection.getSelfLink(),"SELECT * FROM root r WHERE r.custom_id='" + custom_id + "'", null).getQueryIterable().toList();

        if (documents.size() > 0) {

            DM_RestApiRequestRecord record;

            if (documents.size() > 1) {

                terminal_logger.debug("checkRestApiRequest: more than 1 record, finding latest record");
                record = documents.stream().max(Comparator.comparingLong(document -> document.toObject(DM_RestApiRequestRecord.class).time)).get().toObject(DM_RestApiRequestRecord.class);

            } else {

                record = documents.get(0).toObject(DM_RestApiRequestRecord.class);
            }

            terminal_logger.debug("checkRestApiRequest: custom_id: {}", record.custom_id);

            if (record.time > new Date().getTime() - 60000) {

                terminal_logger.debug("checkRestApiRequest: no available requests, return 0");
                return 0L;
            }

            for (Document document : documents) {
                try {

                    terminal_logger.debug("checkRestApiRequest: deleting record");

                    Server.documentClient.deleteDocument(document.getSelfLink(),null);

                } catch (DocumentClientException e) {
                    terminal_logger.internalServerError("checkRestApiRequest:", e);
                }
            }
        }
        */

        terminal_logger.debug("checkRestApiRequest: check previous request");

        Long available = 0L;

        if (product != null) {

            List<Model_ProductExtension> filtered = product.extensions.stream().filter(extension -> extension.type == Enum_ExtensionType.RestApi).collect(Collectors.toList());

            terminal_logger.debug("checkRestApiRequest: filtered extensions");

            if (filtered.isEmpty()) {

                available = 30L;

                terminal_logger.debug("checkRestApiRequest: no extension, 30 available requests");
            } else {

                // TODO může se cacheovat
                for (Model_ProductExtension extension : filtered) {
                    available += extension.count();
                }

                terminal_logger.debug("checkRestApiRequest: counted available requests");
            }
        } else {
            available = 50L;
        }

        /*
        new Thread( () -> {
            try {
                Server.documentClient.createDocument(DocumentDB.blocko_request_collection.getSelfLink(), DM_RestApiRequestRecord.make_request(custom_id), null, true);
                terminal_logger.debug("checkRestApiRequest: saving request record");
            } catch (DocumentClientException e) {
                terminal_logger.internalServerError("checkRestApiRequest:",e);
            }
        }).start();
        */

        terminal_logger.debug("checkRestApiRequest: return available requests");

        return available;
    }
}
