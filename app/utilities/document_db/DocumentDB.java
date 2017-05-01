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
                                                            // Server.class is Is intentionally used
    private static final Class_Logger terminal_logger = new Class_Logger(Server.class);

    public static void set_no_SQL_collection(){
        set_model_board();
    }

    private static void set_model_board(){
        try {

            // RequestOptions definuje maximální počet requestů za vteřinu na Azure Database NO SQL kolekci.
            // Jde o velikost kanálu - a za ten se platí. Není tedy potřeba do začátku mít kanál moc velký.
            // Každá kolekce má svůj vlastní RequestOptions. 
            RequestOptions request_options_Model_Board = new RequestOptions();
            request_options_Model_Board.setOfferThroughput(Configuration.root().getInt("Azure.documentDB." + Server.server_mode.name() + ".RUsReserved" + Model_Board.COLLECTION_MAME));

            // Najdu collekci - Název kolekce totiž tvoří náhodné UUID
            DocumentCollection collection = Server.documentClient.queryCollections(Server.documentDB_Path, "SELECT * FROM root r WHERE r.id='" + Model_Board.COLLECTION_MAME + "'", null).getQueryIterable().toList().get(0);

            // Zkusím najít a pak vrátit Collekci
            if(collection != null) {

                Model_Board.collection = collection;
                terminal_logger.debug("DocumentDB:: set_collection:: Model_Board:: Collection already created");

            }else {

                collection.setId(Model_Board.COLLECTION_MAME);

                Server.documentClient.createCollection(Server.documentDB_Path, collection, request_options_Model_Board);

                Model_Board.collection = collection;

                terminal_logger.debug("DocumentDB:: set_collection:: Model_Board:: Collection Successfully created");
            }

        } catch ( Exception e ){
            terminal_logger.internalServerError(e);
        }
    }


}
