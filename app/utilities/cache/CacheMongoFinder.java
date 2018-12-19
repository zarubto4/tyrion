package utilities.cache;

import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import exceptions.NotFoundException;
import org.bson.types.ObjectId;
import org.ehcache.Cache;
import xyz.morphia.Datastore;
import xyz.morphia.Key;
import xyz.morphia.aggregation.AggregationPipeline;
import xyz.morphia.query.Query;
import utilities.Server;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;

import java.util.List;

public class CacheMongoFinder<T extends _Abstract_MongoModel> implements ModelCache<ObjectId, T> {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(CacheFinder.class);

/* VALUE  -------------------------------------------------------------------------------------------------------------*/

    private Datastore datastore;

    /**
     * The entity bean type.
     */
    private final Class<T> entityType;

    private Cache<ObjectId, T> cache;
    private Cache<Integer, ObjectId> queryCache;

/* CONSTRUCTOR  -------------------------------------------------------------------------------------------------------*/

    public CacheMongoFinder(Class<T > cls) {
        this.entityType = cls;
    }

    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public T byId(String id) throws NotFoundException {
        return this.byId(new ObjectId(id));
    }

    /**
     * Retrieves an entity by ID.
     * <p>
     */
    public T byId(ObjectId id) throws NotFoundException  {

        if (cache.containsKey(id)) {
            logger.debug("byId - ({}) id: {} get from cache", this.entityType.getSimpleName(), id);
            return cache.get(id);
        }

        logger.debug("byId - ({}) id: {} get from db", this.entityType.getSimpleName(), id);

        T entity = bySingleArgument("id", id);

        if (entity == null) {
            logger.debug("byId - ({}) id: {} not found", this.entityType.getSimpleName(), id);
            throw new NotFoundException(this.entityType);
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
     */
    public T bySingleArgument(String key, Object value) {
        try {
            return this.datastore.find(entityType).field(key).equal(value).order("created").get();
        } catch (Exception e) {
            return null;
        }
    }

/* DATASTORE Override  -------------------------------------------------------------------------------------------------*/

    /**
     * Saves an entity (Object) and updates the @Id field
     *
     * @param entity the entity to save
     * @param <T>    the type of the entity
     * @return the keys of the entity
     */
    public <T> Key<T> save(T entity) {
        return this.datastore.save(entity);
    }

    /**
     * Deletes the given entity (by @Id)
     *
     * @param entity the entity to delete
     * @param <T>    the type to delete
     * @return results of the delete
     */
    public <T> WriteResult delete(T entity) {
        return this.datastore.delete(entity);
    }

    /**
     * Find Query start here for Mothpiha
     * @return
     */
    public Query<T> find(){
        return this.datastore.find(entityType);
    }

    /**
     * Returns a new query bound to the collection (a specific {@link DBCollection})
     * @return the query
     */
    public Query<T> query() {
        return this.datastore.createQuery(entityType);
    }

    public AggregationPipeline createAggregation() {
        return this.datastore.createAggregation(entityType);
    }

    /**
     * Find all instances by type
     * @return
     */
    public List<T> all(){
        return this.datastore.find(entityType).asList();
    }

    /**
     * @return the mapped collection for the collection
     */
    public DBCollection getCollection() {
        return this.datastore.getCollection(entityType);
    }



/* CACHE ---------------------------------------------------------------------------------------------------------------*/

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

    public void evict(ObjectId key) {
        logger.trace("evict - ({}) removing by key: {}", this.entityType.getSimpleName(), key);
        cache.remove(key);
    }
}
