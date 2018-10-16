package utilities.cache;

import io.ebean.Finder;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.querydefn.DefaultOrmQuery;
import org.ehcache.Cache;
import exceptions.NotFoundException;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * This is an extension of normal Ebean Finder. It has its own caching system inside.
 * It handles all caching and retrieving from database, so the client does not know if
 * the record was cached or not. The caching is implemented only for single record queries.
 * @param <T> Type of the stored entity.
 */
public class CacheFinder<T extends BaseModel> extends Finder<UUID, T> implements ModelCache<UUID, T> {

    private static final Logger logger = new Logger(CacheFinder.class);

    private final Class<T> entityType;

    private Cache<UUID, T> cache;
    private Cache<Integer, UUID> queryCache;

    public CacheFinder(Class<T> cls) {
        super(cls);

        this.entityType = cls;
    }

    @Override
    @Nonnull
    public T byId(UUID id) {

        if (cache.containsKey(id)) {
            logger.debug("byId - ({}) id: {} get from cache", this.entityType.getSimpleName(), id);
            return this.cache.get(id);
        }

        logger.debug("byId - ({}) id: {} get from db", this.entityType.getSimpleName(), id);
        T entity = super.byId(id);

        if (entity == null) {
            logger.debug("byId - ({}) id: {} not found", this.entityType.getSimpleName(), id);
            throw new NotFoundException(this.entityType);
        }

        cache.put(id, entity);

        return entity;
    }

    @Override
    public CacheQuery<T> query() {
        DefaultOrmQuery<T> query = (DefaultOrmQuery<T>) super.query();
        return new CacheQuery<>(this, query.getBeanDescriptor(), (SpiEbeanServer) db(), query.getExpressionFactory());
    }

    public void setCache(Cache<UUID, T> cache) {
        this.cache = cache;
    }

    public Cache<UUID, T> getCache() {
        return this.cache;
    }

    public void setQueryCache(Cache<Integer, UUID> cache) {
        this.queryCache = cache;
    }

    public Cache<Integer, UUID> getQueryCache() {
        return this.queryCache;
    }

    public Class<T> getEntityType() {
        return this.entityType;
    }

    public void cache(UUID key, T value) {
        logger.trace("cache - ({}) caching by key: {}", this.entityType.getSimpleName(), key);
        cache.put(key, value);
    }

    public void evict(UUID key) {
        logger.trace("evict - ({}) removing by key: {}", this.entityType.getSimpleName(), key);
        cache.remove(key);
    }
}
