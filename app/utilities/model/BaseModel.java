package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.logger.Logger;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseModel extends Model {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(BaseModel.class);

/* COMMON VALUES -------------------------------------------------------------------------------------------------------*/

    @Id
    public UUID id;

    @ApiModelProperty(required = true, readOnly = true, dataType = "integer", example = "1466163478925")
    public Date created;

    @ApiModelProperty(required = true, readOnly = true, dataType = "integer", example = "1466163478925")
    public Date updated;

    @JsonIgnore
    public Date removed;

    @JsonIgnore @SoftDelete
    public boolean deleted;

/* COMMON METHODS ------------------------------------------------------------------------------------------------------*/

    @Override
    public void save() {

        boolean isNew = this.id == null;

        if (this.created == null) {
            this.created = new Date();
        }

        this.updated = new Date();

        super.save();

        new Thread(this::cache).start(); // Caches the object

        logger.trace("save - saved '{}' to DB, id: {}", this.getClass().getSimpleName(), this.id);

        // Send the echo update
        if (isNew) {
            this.echoParent();
        } else {
            this.echo();
        }
    }

    @Override
    public boolean delete() {
        logger.trace("delete - deleting '{}' from DB, id: {}", this.getClass().getSimpleName(), this.id);
        new Thread(this::evict).start(); // Evict the object from cache
        this.echoParent(); // Send echo update of parent object
        return super.delete();
    }

    @Override
    public boolean deletePermanent() {
        logger.trace("deletePermanent - permanently deleting '{}' from DB, id: {}", this.getClass().getSimpleName(), this.id);
        return super.deletePermanent();
    }

    /**
     * Converts this model to JSON
     * @return JSON representation of this model
     */
    public JsonNode json() {
        return Json.toJson(this);
    }

    /**
     * Converts this model to JSON and than stringify
     * @return string from JSON representation
     */
    public String string() {
        return json().toString();
    }

    /**
     * Converts this model to printable string
     * @return formatted string
     */
    public String prettyPrint() {
        return this.getClass() + ":\n" + Json.prettyPrint(json());
    }

    /**
     * Method finds the cache field in the class
     * and if present it puts or replaces the value in the cache.
     * TODO measure performance impact
     */
    @SuppressWarnings("unchecked")
    public void cache() {
        long start = System.currentTimeMillis();
        Class<? extends BaseModel> cls = this.getClass();
        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(CacheField.class)) {
                try {
                    CacheField annotation = field.getAnnotation(CacheField.class);
                    if (annotation.value() == cls) {
                        Cache<UUID, BaseModel> cache = (Cache<UUID, BaseModel>) field.get(null);
                        if (cache.containsKey(this.id)) {
                            cache.replace(this.id, this);
                        } else {
                            cache.put(this.id, this);
                        }
                        logger.trace("cache - finding cache took {} ms", System.currentTimeMillis() - start);
                        break;
                    }
                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        }
    }

    /**
     * This method should be called when object is no longer fresh
     * and it should be removed from the cache. It finds the cache field
     * and if present it removes this object from it.
     * TODO measure performance impact
     */
    @SuppressWarnings("unchecked")
    public void evict() {
        long start = System.currentTimeMillis();
        Class<? extends BaseModel> cls = this.getClass();
        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(CacheField.class)) {
                try {
                    CacheField annotation = field.getAnnotation(CacheField.class);
                    if (annotation.value() == cls) {
                        Cache<UUID, BaseModel> cache = (Cache<UUID, BaseModel>) field.get(null);
                        cache.remove(this.id);
                        logger.trace("evict - finding cache took {} ms", System.currentTimeMillis() - start);
                        break;
                    }
                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        }
    }

    /**
     * This method should be called when object is no longer fresh
     * and inner caches should be reloaded. It sets null all fields
     * that are only cached. TODO measure performance impact
     */
    public void invalidate() {
        long start = System.currentTimeMillis();
        Class<? extends BaseModel> cls = this.getClass();
        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(Cached.class)) {
                try {
                    field.set(this, null); // Set null
                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        }
        logger.trace("invalidate - operation took {} ms", System.currentTimeMillis() - start);
    }

    /**
     * If this object is under some project, this method will echo
     * the change of this object to all portals viewing the given project.
     */
    public void echo() {
        if (this instanceof Echo) {
            Echo echo = (Echo) this;
            if (echo.hasProject()) {
                new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(echo))).start();
            }
        }
    }

    /**
     * If this object is under some project, this method will echo
     * the change of parent object to all portals viewing the given project.
     */
    public void echoParent() {
        if (this instanceof Echo) {
            Echo echo = (Echo) this;
            if (echo.hasProject()) {
                new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(echo.getParent()))).start();
            }
        }
    }

/* ABSTRACT METHODS ----------------------------------------------------------------------------------------------------*/
/*
    public abstract boolean create_permission();
    public abstract boolean read_permission();
    public abstract boolean update_permission();
    public abstract boolean delete_permission();
*/
}

