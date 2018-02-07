package utilities.cache;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.UUID;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
public @interface CacheField {

    /**
     * Value type that will be cached.
     * @return value
     */
    Class<?> value();

    /**
     * Type that will be used as a key for stored object.
     * @return key value
     */
    Class<?> keyType() default UUID.class;

    /**
     * Time to idle in seconds.
     * If the entity is not touched for the given time, it will be evicted.
     * @return time to idle in seconds
     */
    long timeToIdle() default 600;

    /**
     * Maximal count of elements held in cache.
     * @return maximal count
     */
    long maxElements() default 100;

    /**
     * Name alias for the cache.
     * @return name
     */
    String name() default "";
}
