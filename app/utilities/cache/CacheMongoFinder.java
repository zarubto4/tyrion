package utilities.cache;

import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import org.bson.types.ObjectId;
import org.ehcache.Cache;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import utilities.Server;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.*;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongodb.morphia.*;
import org.mongodb.morphia.DeleteOptions;
import org.mongodb.morphia.InsertOptions;
import org.mongodb.morphia.UpdateOptions;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryFactory;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import play.mvc.PathBindable;
import utilities.Server;
import utilities.errors.Exceptions.Result_Error_DatabaseError;
import utilities.errors.Exceptions._Base_Result_Exception;
import org.ehcache.Cache;
import utilities.logger.Logger;

import java.util.*;

import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.model._Abstract_MongoModel;

import java.util.List;

public class CacheMongoFinder<T extends _Abstract_MongoModel> {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(CacheFinder.class);

    /* VALUE  -------------------------------------------------------------------------------------------------------------*/

    /**
     * The name of the EbeanServer, null for the default server.
     */
    private final Datastore main_data_store;

    /**
     * The entity bean type.
     */
    private final Class<T> entityType;

    private Cache<ObjectId, T> cache;
    private Cache<Integer, ObjectId> queryCache;

    /* CONSTRUCTOR  -------------------------------------------------------------------------------------------------------*/

    public CacheMongoFinder(Class<T > cls) {
        this.entityType = cls;
        this.main_data_store = Server.getMainMongoDatabase();
    }

    public T byId(String id) throws _Base_Result_Exception {
        return this.byId(new ObjectId(id));
    }

    /**
     * Retrieves an entity by ID.
     * <p>
     */
    public T byId(ObjectId id) throws _Base_Result_Exception  {

        logger.debug("byId - start search: ({}) id: {} get from cache", this.entityType.getSimpleName(), id);

        if (cache.containsKey(id)) {
            logger.debug("byId - ({}) id: {} get from cache", this.entityType.getSimpleName(), id);
            return this.retrieve(id);
        }

        logger.debug("byId - ({}) id: {} get from db", this.entityType.getSimpleName(), id);


        T entity = bySingleArgument("id", id);

        if (entity == null) {
            logger.debug("byId - ({}) id: {} not found", this.entityType.getSimpleName(), id);
            throw new Result_Error_NotFound(this.entityType);
        }

        if (entity.its_person_operation()) {
            entity.check_read_permission();
        }


        cache.put(id, entity);
        return entity;

    }

    /**
     * Easy Query to find something by single key and value, tipicaly its possible to use that
     * for Name, Email, ID etc.
     * Be aware that ObjectId is converted to String!
     * @param key       -  it must be a String value without spaces or diacritics! We used Snake case conventions!
     * @param value     -  Object like boolean, long, integer etc.
     * @return
     * @throws _Base_Result_Exception
     */
    public T bySingleArgument(String key, Object value) throws _Base_Result_Exception {
        try {
            return this.main_data_store.find(entityType).field(key).equal(value).order("created").get();
        } catch (Exception e) {
            return null;
        }
    }


    /* DATASTORE Override  -------------------------------------------------------------------------------------------------------*/

    /**
     * Saves an entity (Object) and updates the @Id field
     *
     * @param entity the entity to save
     * @param <T>    the type of the entity
     * @return the keys of the entity
     */
    public <T> Key<T> save(T entity) {
        return this.main_data_store.save(entity);
    }

    /**
     * Deletes the given entity (by @Id)
     *
     * @param entity the entity to delete
     * @param <T>    the type to delete
     * @return results of the delete
     */
    public <T> WriteResult delete(T entity) {
        return this.main_data_store.delete(entity);
    }

    /**
     * Find Query start here for Mothpiha
     * @return
     */
    public Query<T> find(){
        return this.main_data_store.find(entityType);
    }


    /**
     * Returns a new query bound to the collection (a specific {@link DBCollection})
     * @return the query
     */
    public Query<T> query() {
        return this.main_data_store.createQuery(entityType);
    }

    /**
     * Find all instances by type
     * @return
     */
    public List<T> all(){
        return this.main_data_store.find(entityType).asList();
    }

    /*##  Cache ######################################################################################################################################################################################################## */

    public void setCache(Cache<ObjectId, T> cache) {
        this.cache = cache;
    }

    public Cache<ObjectId, T> getCache() {
        return this.cache;
    }

    public void setQueryCache(Cache<Integer, ObjectId> cache) {
        this.queryCache = cache;
    }

    public Cache<Integer, ObjectId> getQueryCache() {
        return this.queryCache;
    }

    public Class<T> getEntityType() {
        return this.entityType;
    }

    public void cache(ObjectId key, T value) {
        logger.trace("cache - ({}) caching by key: {}", this.entityType.getSimpleName(), key);
        cache.put(key, value);
    }

    public T retrieve(ObjectId key) {
        logger.trace("retrieve - ({}) retrieving by key: {}", this.entityType.getSimpleName(), key);
        T entity = cache.get(key);
        if (entity != null && entity.its_person_operation()) {
            entity.check_read_permission();
        }
        return entity;

    }

    public void evict(ObjectId key) {
        logger.trace("evict - ({}) removing by key: {}", this.entityType.getSimpleName(), key);
        cache.remove(key);
    }


    /*##  MongoCollection ######################################################################################################################################################################################################## */



}
