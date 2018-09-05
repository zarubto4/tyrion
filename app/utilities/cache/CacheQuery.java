package utilities.cache;

import io.ebean.EbeanServer;
import io.ebean.ExpressionFactory;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.querydefn.DefaultOrmQuery;
import org.ehcache.Cache;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class CacheQuery<T extends BaseModel> extends DefaultOrmQuery<T> {

    private static final Logger logger = new Logger(CacheQuery.class);

    private CacheFinder<T> cacheFinder;

    public CacheQuery(CacheFinder<T> cacheFinder, BeanDescriptor<T> desc, EbeanServer server, ExpressionFactory expressionFactory) {
        super(desc, server, expressionFactory);

        this.cacheFinder = cacheFinder;
    }

    @NotNull
    @Override
    public T findOne() {

        Integer hash = this.hashCode();

        logger.trace("findOne - calculated hash: {}", hash);

        Cache<Integer, UUID> queryCache = this.cacheFinder.getQueryCache();
        Cache<UUID, T> cache = this.cacheFinder.getCache();

        if (queryCache.containsKey(hash)) {

            logger.debug("findOne - found cached query");

            UUID key = queryCache.get(hash);
            if (cache.containsKey(key)) {
                logger.debug("findOne - get cached record");
                return cache.get(key);
            } else {
                return this.cacheFinder.byId(key);
            }
        }

        logger.debug("findOne - get from db");
        T entity = super.findOne();
        if (entity == null) {
            logger.trace("findOne - not found");
            throw new Result_Error_NotFound(this.cacheFinder.getEntityType());
        }

        queryCache.put(hash, entity.id);

        if (cache.containsKey(entity.id)) {
            logger.debug("findOne - query not cached, but record itself was cached");
            return cache.get(entity.id);
        }

        cache.put(entity.id, entity);

        return entity;
    }

    @Override
    public int hashCode() {
        StringBuilder builder = new StringBuilder();
        this.getDetail().queryPlanHash(builder);

        int hc = 92821 * builder.toString().hashCode();
        hc = 92821 * hc + this.queryBindHash();
        return hc;
    }
}
