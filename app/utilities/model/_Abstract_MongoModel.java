package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers._BaseController;
import exceptions.InvalidBodyException;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import play.data.validation.Constraints;
import play.libs.Json;
import utilities.Server;
import utilities.cache.InjectCache;
import utilities.cache.CacheMongoFinder;
import utilities.logger.Logger;
import utilities.permission.Action;
import utilities.permission.JsonPermission;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;

@Indexes({
        @Index(
                fields = {
                        // In this case, we have more anotation types with same name
                        @org.mongodb.morphia.annotations.Field("id"),
                }
        )
})
public abstract class _Abstract_MongoModel implements JsonSerializable {

/* LOGGER --------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(_Abstract_MongoModel.class);

/* COMMON VALUES -------------------------------------------------------------------------------------------------------*/

    // Public
    @Id
    @Property("id")
    @JsonIgnore
    public ObjectId id;

    @Constraints.Required
    @Property("created")
    @ApiModelProperty(required = true, value = "unixTime", readOnly = true, dataType = "integer", example = "1536424319")
    public long created;

    @Constraints.Required
    @Property("updated")
    @ApiModelProperty(required = true, value = "unixTime", readOnly = true, dataType = "integer", example = "1536424319")
    public long updated;

    @JsonIgnore
    @ApiModelProperty(required = true, value = "unixTime", readOnly = true, dataType = "integer", example = "1536424319")
    public long removed;

    @JsonIgnore
    public boolean deleted; // Default value is false in save()


    @JsonIgnore
    public UUID author_id; // Default value is false in save()


/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @Constraints.Required
    @ApiModelProperty(required = true, value = " ID", readOnly = true, dataType = "String", example = "5b508290-a026-410c-bdbc-6cdf99f48043")
    @JsonProperty()
    public String id(){
        return id.toString();
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/


    /**
     * Converts this model to JSON
     * @return JSON representation of this model
     */
    public ObjectNode json() {

        /*

        // Výrzané zrychlení kdy je cachován už rovnou celý objekt v json podobě,
        // bohužel se musí udělat opravdu hluboké testování na veškšrou kombinatoriku protože při každé změně je nutné udělat clean tohoto jsonu
        if(cache().get_cached_json() != null) {
            return cache().get_cached_json();
        }

        cache().set_cached_json( Json.toJson(this));
        return cache().get_cached_json();
        */

        return (ObjectNode) Json.toJson(this);

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
    @JsonIgnore
    public String prettyPrint() {
        return this.getClass() + ":\n" + Json.prettyPrint(json());
    }

    /**
     * Shortcuts for automatic validation and parsing of incoming JSON to MODEL class
     * @param clazz
     * @param <T>
     * @return
     */
    @JsonIgnore
    public static <T> T formFromJsonWithValidation(Class<T> clazz, JsonNode jsonNode) throws InvalidBodyException {
        return Server.formFactory.formFromJsonWithValidation(clazz, jsonNode);
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/


    @JsonIgnore public void save() {

        // Someone call Save when object is already created in database

        if( this.id != null) {
            this.update();
            return;
        }

        this.id = new ObjectId();

        // Set Time
        if (this.created == 0) {
            this.created = Instant.now().getEpochSecond();
        }
        if (this.updated == 0) {
            this.updated = Instant.now().getEpochSecond();
        }
        // new Thread(this::cache).start(); // Caches the object

        if (its_person_operation()) {
            save_author();
        }

        getFinder().save(this);
        new Thread(this::cache).start(); // Caches the object
    }

    @JsonIgnore public void update() {

        // Set Time
        this.updated = Instant.now().getEpochSecond();

        // Save Document do Mongo Database
        getFinder().save(this);

        new Thread(this::cache).start();
    }

    /**
     * Its not Real Delete from Database, but only set as deleted with one common boolean value!
     * For Real Remove use {@link #deletePermanent() } instead
     */
    @JsonIgnore public void delete() {

        // Set Time
        this.removed = Instant.now().getEpochSecond();
        this.deleted = true;

        // Not Remove, but update!
        getFinder().save(this);

        // Evict the object from cache
        new Thread(this::evict).start();
    }

    /**
     * Permanently remove from Mongo DB,
     * its not possible to resque that without backup on database!!!
     * Do it only if you know, what you are doing!
     */
    @JsonIgnore public void deletePermanent() {

        // Remove Permanently
        getFinder().delete(this);
        new Thread(this::evict).start();
    }


    /**
     * Method finds the cache field in the class
     * and if present it puts or replaces the value in the cache.
     * TODO measure performance impact LEVEL: HARD  TIME: LONGTERM
     */
    @SuppressWarnings("unchecked")
    private void cache() {
        long start = System.currentTimeMillis();
        Class<? extends _Abstract_MongoModel> cls = this.getClass();

        logger.trace("cache - finding cache finder for {}", cls.getSimpleName());

        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectCache.class) && field.getType().equals(CacheMongoFinder.class)) {
                try {

                    logger.debug("cache - found cache finder field");

                    CacheMongoFinder<_Abstract_MongoModel> cacheFinder = (CacheMongoFinder<_Abstract_MongoModel>) field.get(null);
                    cacheFinder.cache(this.id, this);

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
     * TODO measure performance impact LEVEL: HARD  TIME: LONGTERM
     */
    @SuppressWarnings("unchecked")
    private void evict() {
        long start = System.currentTimeMillis();
        Class<? extends _Abstract_MongoModel> cls = this.getClass();

        logger.trace("evict - finding cache finder for {}", cls.getSimpleName());

        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectCache.class) && field.getType().equals(CacheMongoFinder.class)) {
                try {

                    logger.debug("evict - found cache finder field");

                    CacheMongoFinder<?> cacheFinder = (CacheMongoFinder<?>) field.get(null);
                    cacheFinder.evict(this.id);

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        }
    }


    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


    /* Private Support methods  -----------------------------------------------------------------------------------------------*/

    /**
     * Here we can log who do this operation. User or System.
     * Just a idea..
     * @return
     */
    @JsonIgnore public boolean its_person_operation() {
        try {
            return  _BaseController.isAuthenticated();
        }catch (Exception e){
            logger.internalServerError(e);
            return false;
        }
    }

    /**
     * Here we can log who do this operation. User or System.
     * Just a idea..
     * @return
     */
    @JsonIgnore
    private void save_author() {
        try {

            List<Field> fields = new ArrayList<>();
            getAllFields(fields, this.getClass(), 0);

            for(Field field : fields) {
                if(field.getName().equals("author")) {
                    if (field.get(this) == null) {
                        field.set(this, _BaseController.person().id);
                        return;
                    }
                }

            }

        }catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    @JsonIgnore
    private static List<Field> getAllFields(List<Field> fields, Class<?> type, int iteration) {
        if(iteration > 4) return fields;
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass(), ++iteration);
        }
        return fields;
    }

/* Permission Contents ----------------------------------------------------------------------------------------------------*/

    @JsonPermission
    @Transient
    @ApiModelProperty(readOnly = true, value = "True if user can update this object.")
        public boolean update_permission;

    @JsonPermission(Action.DELETE) @Transient
    @ApiModelProperty(readOnly = true, value = "True if user can delete this object.")
    public boolean delete_permission;

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public abstract CacheMongoFinder<?> getFinder();

}
