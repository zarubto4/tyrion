package utilities.cache;

import org.ehcache.Cache;
import utilities.model.BaseModel;

import java.util.UUID;

public interface ModelCache<T extends BaseModel> {

    void setCache(Cache<UUID, T> cache);

    Cache<UUID, T> getCache();

    void setQueryCache(Cache<Integer, UUID> cache);

    Cache<Integer, UUID> getQueryCache();

    void cache(UUID key, T value);

    void evict(UUID key);
}
