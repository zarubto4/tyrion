package utilities.document_db;


import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.RequestOptions;
import models.Model_Board;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import play.Configuration;
import utilities.Server;
import utilities.logger.Class_Logger;

import java.util.List;

public class DocumentDB {
                                                            // Server.class is intentionally used! Don't change that
    private static final Class_Logger terminal_logger = new Class_Logger(Server.class);

    // Online status - variables
    private static final String ONLINE_STATUS_COLLECTION = "ONLINE_STATUS";
    public static DocumentCollection online_status_collection = null;

    public static void set_no_SQL_collection(){
        set_online_status_collection();
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
    private static void set_online_status_collection(){
        try {

            // RequestOptions definuje maximální počet requestů za vteřinu na Azure Database NO SQL kolekci.
            // Jde o velikost kanálu - a za ten se platí. Není tedy potřeba do začátku mít kanál moc velký.
            // Každá kolekce má svůj vlastní RequestOptions.

            // Najdu collekci - Název kolekce totiž tvoří náhodné UUID
            List<DocumentCollection> collections = Server.documentClient.queryCollections(Server.documentDB_Path, "SELECT * FROM root r WHERE r.id='" + ONLINE_STATUS_COLLECTION + "'", null).getQueryIterable().toList();

            // Zkusím najít a pak vrátit Collekci
            if(!collections.isEmpty()) {

                online_status_collection = collections.get(0);
                terminal_logger.debug("DocumentDB:: set_collection:: Online Status Collection:: Collection has already been created");

            }else {

                RequestOptions request_options_online_status = new RequestOptions();
                request_options_online_status.setOfferThroughput(Configuration.root().getInt("Azure.documentDB." + Server.server_mode.name() + ".RUsReserved" + ONLINE_STATUS_COLLECTION));

                DocumentCollection collection = new DocumentCollection();
                collection.setId(ONLINE_STATUS_COLLECTION);

                Server.documentClient.createCollection(Server.documentDB_Path, collection, request_options_online_status);

                online_status_collection = collection;

                terminal_logger.debug("DocumentDB:: set_collection:: Online Status Collection:: Collection Successfully created");
            }

        } catch ( Exception e ){
            terminal_logger.internalServerError(e);
        }
    }


}
