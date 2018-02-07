package utilities.financial;

import models.Model_Person;
import models.Model_Product;
import models.Model_ProductExtension;
import models.Model_Project;
import utilities.enums.ExtensionType;
import utilities.financial.extensions.configurations.Configuration_Project;
import utilities.financial.extensions.configurations.Configuration_RestApi;
import utilities.logger.Logger;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Used to check if user is able to perform some action from financial point of view.
 */
public class FinancialPermission {

    private static final Logger terminal_logger = new Logger(FinancialPermission.class);

    /**
     * Checks permission for given action.
     * Counts all extensions and decides if the user is able to do the action.
     * @param product Given product to check.
     * @param action String action that is being performed.
     * @return Boolean true if user is allowed to do it, otherwise false.
     */
    public static boolean check(Model_Product product, String action) {

        switch (action) {

            case "project": {

                List<Model_ProductExtension> filtered = product.extensions.stream().filter(extension -> extension.type == ExtensionType.project).collect(Collectors.toList());

                int available = 0;

                for (Model_ProductExtension extension : filtered) {
                    available += ((Configuration_Project)extension.getConfiguration()).count;
                }

                return Model_Project.find.query().where().eq("product.id", product.id).findCount() < available;
            }

            default: return false;
        }
    }

    /**
     * This method serves to check whether user can do RestApi request from Blocko instance.
     * Counts all RestApi extensions in product.
     * The summary is a number of available requests user can do from one instance.
     * If there are not any extensions the user can do 30 requests.
     * If parameter "object" is model person, it means that requests are made from test environment in Becki,
     * so 50 available requests are returned.
     * @param object Model person or product to check the permission for.
     * @param instance_id Currently unused param.
     * @return Long count of available requests user can do from Blocko instance.
     */
    public static Long checkRestApiRequest(Object object, UUID instance_id) {

        Model_Product product = null;
        //final String custom_id;

        if (object instanceof Model_Product) {

            product = (Model_Product) object;
            //custom_id = instance_id;

        } else if (object instanceof Model_Person) {

            //custom_id = ((Model_Person) object).id;
        } else {
            terminal_logger.internalServerError(new Exception("Object is instance of an unknown class."));
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

            List<Model_ProductExtension> filtered = product.extensions.stream().filter(extension -> extension.type == ExtensionType.rest_api).collect(Collectors.toList());

            terminal_logger.debug("checkRestApiRequest: filtered extensions");

            if (filtered.isEmpty()) {

                available = 30L;

                terminal_logger.debug("checkRestApiRequest: no extension, 30 available requests");
            } else {

                // TODO může se cacheovat
                for (Model_ProductExtension extension : filtered) {
                    available += ((Configuration_RestApi)extension.getConfiguration()).available_requests;
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
