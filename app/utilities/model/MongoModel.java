package utilities.model;


import com.mongodb.client.MongoCollection;
import org.bson.Document;
import utilities.document_mongo_db.MongoDB;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

public abstract class MongoModel extends _Swagger_Abstract_Default {

    public abstract String get_collection_name();

    /**
     * Jelikož je spojení na Mongo databázi nespolehlivé a dochází k výpadku po x minutách,
     * a metoda nové inicializace pokaždé, když přišel request taky nebyla dobrá (trvá to cca 2 - 5 sekund navázat spojení)
     * Rozhodlo se přejít k vláknu, které vždy vytvoří novou inicializaci a dosadí do pole
     * @param collection_name
     * @return
     */
    public static MongoCollection<Document> collection(String collection_name) {
        return MongoDB.get_collection(collection_name);
    }

}
