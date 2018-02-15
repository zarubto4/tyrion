package utilities.document_db;

import com.google.inject.Inject;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.RequestOptions;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import play.Configuration;
import play.libs.ws.WSClient;
import utilities.Server;
import utilities.logger.Logger;
import utilities.scheduler.SchedulerController;

import java.util.List;

public class DocumentDB {

// LOGGER ##############################################################################################################

    private static final Logger logger = new Logger(DocumentDB.class);

// CONTROLLER CONFIGURATION ############################################################################################
    private static Config config;


    @Inject
    public DocumentDB(Config config) {
        DocumentDB.config = config;
    }

///###################################################################################################################*/

    // Online status - variables
    private static final String ONLINE_STATUS_COLLECTION = "ONLINE_STATUS";

    private static final String BLOCKO_REQUEST_COLLECTION = "BLOCKO_REQUEST";
    public static DocumentCollection blocko_request_collection = null;

    public static void init() {
        set_online_status_collection();
        //set_blocko_request_collection();
    }


    /**
     * Set up a collection that collects logs for hardware and server connections and disconnections.
     * Collection has 3 required parameters and 1 option parameter ::
     *
     * REQUIRED:: collection_type           ::  String -> CLASS_NAME (Model_Board, Model_HomerServer etd.)
     * REQUIRED:: document_type             ::  String -> Type (connection_record, disconnection_record, backup_set, change_version)
     * OPTIONAL:: document_type_sub_type    ::  String -> Sub_type for DM_XXX_XXX objects in utilities/document_db/document_objects
     * REQUIRED:: id                        ::  String -> UUID.getUUID().toString() recommended by Mongo
     * REQUIRED:: server_version            ::  String -> Server.server_version
     */
    private static void set_online_status_collection() {
        try {

            // RequestOptions definuje maximální počet requestů za vteřinu na Azure Database NO SQL kolekci.
            // Jde o velikost kanálu - a za ten se platí. Není tedy potřeba do začátku mít kanál moc velký.
            // Každá kolekce má svůj vlastní RequestOptions.

            // Najdu collekci - Název kolekce totiž tvoří náhodné UUID
            List<DocumentCollection> collections = Server.documentClient.queryCollections(Server.documentDB_Path, "SELECT * FROM root r WHERE r.id='" + ONLINE_STATUS_COLLECTION + "'", null).getQueryIterable().toList();

            // Zkusím najít a pak vrátit Collekci
            if (!collections.isEmpty()) {

                Server.online_status_collection = collections.get(0);
                logger.debug("set_online_status_collection - collection has already been created");

            } else {

                RequestOptions request_options_online_status = new RequestOptions();
                request_options_online_status.setOfferThroughput(config.getInt("documentDB." + Server.mode.name() + ".RUsReserved" + ONLINE_STATUS_COLLECTION));

                DocumentCollection collection = new DocumentCollection();
                collection.setId(ONLINE_STATUS_COLLECTION);

                Server.documentClient.createCollection(Server.documentDB_Path, collection, request_options_online_status);

                Server.online_status_collection = collection;

                logger.debug("set_online_status_collection - collection successfully created");
            }

        } catch ( Exception e ) {
            logger.internalServerError(e);
        }
    }

    private static void set_blocko_request_collection() {
        try {

            // RequestOptions definuje maximální počet requestů za vteřinu na Azure Database NO SQL kolekci.
            // Jde o velikost kanálu - a za ten se platí. Není tedy potřeba do začátku mít kanál moc velký.
            // Každá kolekce má svůj vlastní RequestOptions.

            // Najdu collekci - Název kolekce totiž tvoří náhodné UUID
            List<DocumentCollection> collections = Server.documentClient.queryCollections(Server.documentDB_Path, "SELECT * FROM root r WHERE r.id='" + BLOCKO_REQUEST_COLLECTION + "'", null).getQueryIterable().toList();

            // Zkusím najít a pak vrátit Collekci
            if (!collections.isEmpty()) {

                blocko_request_collection = collections.get(0);
                logger.debug("set_blocko_request_collection - collection has already been created");

            } else {

                RequestOptions request_options_blocko_request = new RequestOptions();
                request_options_blocko_request.setOfferThroughput(config.getInt("documentDB." + Server.mode.name() + ".RUsReserved" + BLOCKO_REQUEST_COLLECTION));

                DocumentCollection collection = new DocumentCollection();
                collection.setId(BLOCKO_REQUEST_COLLECTION);

                Server.documentClient.createCollection(Server.documentDB_Path, collection, request_options_blocko_request);

                blocko_request_collection = collection;

                logger.debug("set_blocko_request_collection - collection successfully created");
            }

        } catch ( Exception e ) {
            logger.internalServerError(e);
        }
    }
}
