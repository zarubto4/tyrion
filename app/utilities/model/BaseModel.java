package utilities.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers._BaseController;
import exceptions.InvalidBodyException;
import io.ebean.Model;
import io.ebean.ValuePair;
import io.ebean.annotation.SoftDelete;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.swagger.annotations.ApiModelProperty;
import models.Model_HomerServer;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.Server;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.cache.Cached;
import utilities.logger.Logger;
import utilities.models_update_echo.EchoHandler;
import utilities.permission.Action;
import utilities.permission.JsonPermission;
import websocket.interfaces.WS_Homer;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

@JsonFilter("permission")
@MappedSuperclass
public abstract class BaseModel extends Model implements JsonSerializable {

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

/* GENERAL OBJECT CACHE ------------------------------------------------------------------------------------------------*/

   @JsonIgnore @Transient
    public IDCache idCache(){

        if(idCache == null) {
            idCache = new IDCache();
        }

        return idCache;
    }

    // It must be always private!
    private IDCache idCache;

    // Private Class not acceptable from other Tyrion Components
    public class IDCache {

        private HashMap<Class, List<UUID>> cacheMap = new HashMap<>();

        public void add(Class c, List<UUID> ids){
            try {
                if (ids != null) {
                    if (!cacheMap.containsKey(c)) {
                        cacheMap.put(c, ids);
                    } else {
                        if (cacheMap.get(c) != null) {

                            List<UUID> list = cacheMap.get(c);
                            for (UUID id : ids){
                                if (!list.contains(id)) {
                                    list.add(id);
                                }
                            }

                        } else {
                            cacheMap.put(c, ids);
                        }

                    }
                }
            }catch (Exception e){
                // Nothing
            }
        }

        public void add(Class c, UUID id){
            try {

                if (id != null) {

                    if (!cacheMap.containsKey(c)) {
                       // System.out.println("IDCache:: not contains KEy");

                        // Create List ArraList <- its not possible to use  Collections.singletonList(id))
                        // * @throws UnsupportedOperationException if the <tt>add</tt> operation
                        // *  is not supported by singletonList
                        List<UUID> list = new ArrayList<>();
                        list.add(id);

                        cacheMap.put(c, list);
                    } else {

                        if(!cacheMap.get(c).contains(id)) {
                            cacheMap.get(c).add(id);
                        }

                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                // Nothing
            }
        }

        public void remove(Class c, List<UUID> ids){
            try {
                if(ids != null) {
                    if (cacheMap.containsKey(c)) {
                        cacheMap.get(c).removeAll(ids);
                    }
                }
            } catch (Exception e){
                // Nothing
            }
        }

        public void remove(Class c, UUID id){
            try {
                if(id != null)
                if(cacheMap.containsKey(c)){
                    System.out.println("BaseModel Remove - Class " + c.getSimpleName() + " id " + id);
                    System.out.println("BaseModel Remove - Content " + cacheMap.get(c) );
                    System.out.println("BaseModel Remove - Content size " + cacheMap.get(c).size() );
                    cacheMap.get(c).remove(id);
                    System.out.println("BaseModel Remove - Content " + cacheMap.get(c) );
                }
            } catch (Exception e){
                logger.internalServerError(e);
            }
        }

        public void removeAll(Class c){
            try {
                cacheMap.remove(c);
            } catch (Exception e) {
                logger.internalServerError(e);
            }
        }

        public List<UUID> gets(Class c){
            return cacheMap.getOrDefault(c, null);
        }

        public UUID get(Class c){
            try {
                if (cacheMap.containsKey(c)) {
                    List<UUID> list = cacheMap.get(c);
                    if (!list.isEmpty()) {
                        return list.get(0);
                    }
                }

                return null;
            } catch (Exception e) {
                return null;
            }
        }

        public JsonNode get_cached_json(){
            this.cacheMap = new HashMap<>();
            cached_json_object_for_rest = null;
        }
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

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

    /**
     * Binds Json data to this form - that is, handles form submission.
     * Special Method with Response to Websocket
     * @param clazz
     * @param jsonNode
     * @param <T>
     * @return a copy of this form filled with the new data
     */
    public static  <T> T formFromJsonWithValidation(Model_HomerServer server, Class<T> clazz, JsonNode jsonNode) throws InvalidBodyException, IOException {
        return Server.formFactory.formFromJsonWithValidation(server, clazz, jsonNode);
    }

    /**
     * Binds Json data to this form - that is, handles form submission.
     * Special Method with Response to Websocket
     * @param clazz
     * @param jsonNode
     * @param <T>
     * @return a copy of this form filled with the new data
     */
    public static  <T> T formFromJsonWithValidation(WS_Homer server, Class<T> clazz, JsonNode jsonNode) throws InvalidBodyException, IOException {
        return Server.formFactory.formFromJsonWithValidation(server, clazz, jsonNode);
    }

/* COMMON METHODS ------------------------------------------------------------------------------------------------------*/

    @Override
    public void save() {

        logger.debug("save - saving new {}", this.getClass().getSimpleName());

        // Check Permission - only if user is logged!
        if (its_person_operation()) {
            save_author();
        }

        boolean isNew = this.id == null;

        if (this.created == null) {
            this.created = new Date();
        }
        if (this.updated == null) {
            this.updated = new Date();
        }

        super.save();

        new Thread(this::cache).start(); // Caches the object

        logger.trace("save - saved {} to DB, id: {}", this.getClass().getSimpleName(), this.id);

        // Send the echo update
        if (isNew) {
            this.echoParent();
        } else {
            this.echo();
        }
    }

    @Override
    public void update() {
        logger.debug("update - updating {}, id: {}", this.getClass().getSimpleName(), this.id);
        this.invalidate();
        this.updated = new Date();
        super.update();

        new Thread(this::cache).start();
    }

    @Override
    public boolean delete() {
        logger.debug("delete - soft deleting {}, id: {}", this.getClass().getSimpleName(), this.id);
        this.invalidate();
        super.delete();

        new Thread(this::evict).start(); // Evict the object from cache
        this.echoParent(); // Send echo update of parent object

        return true;
    }

    @Override
    public boolean deletePermanent() {
        logger.debug("deletePermanent - permanently deleting {}, id: {}", this.getClass().getSimpleName(), this.id);
        return super.deletePermanent();
    }

    @Override
    public void refresh() {
        super.refresh();
        this.idCache().clean();
        this.cache();
        this.idCache().clean();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    /**
     * Method finds the cache field in the class
     * and if present it puts or replaces the value in the cache.
     * TODO measure performance impact LEVEL: HARD  TIME: LONGTERM
     */
    @SuppressWarnings("unchecked")
    private void cache() {
        long start = System.currentTimeMillis();
        Class<? extends BaseModel> cls = this.getClass();

        logger.debug("cache - finding cache finder for {}", cls.getSimpleName());

        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectCache.class) && field.getType().equals(CacheFinder.class)) {
                try {

                    logger.debug("cache - found cache finder field");

                    CacheFinder<BaseModel> cacheFinder = (CacheFinder<BaseModel>) field.get(null);
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
        Class<? extends BaseModel> cls = this.getClass();

        logger.trace("evict - finding cache finder for {}", cls.getSimpleName());

        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectCache.class) && field.getType().equals(CacheFinder.class)) {
                try {

                    logger.debug("evict - found cache finder field");

                    CacheFinder<?> cacheFinder = (CacheFinder<?>) field.get(null);
                    cacheFinder.evict(this.id);

                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        }
    }

    /**
     * This method check if there were some ORM changes on this object,
     * if so, it invalidates both the old and the new value of the relation
     * and it also invalidates the object itself, so the queries will return
     * right values
     */
    public void invalidate() {
        try {
            if (this instanceof EntityBean) {
                EntityBeanIntercept intercept = ((EntityBean) this)._ebean_intercept();
                boolean ormChanged = false;
                for (Map.Entry<String, ValuePair> entry : intercept.getDirtyValues().entrySet()) {
                    String key = entry.getKey();
                    ValuePair valuePair = entry.getValue();
                    if (valuePair.getOldValue() instanceof BaseModel) {
                        ormChanged = true;
                        logger.trace("invalidate - invalidating changed property: {}", key);
                        Class cls = valuePair.getOldValue().getClass();
                        CacheFinder finder = findCacheFinder(cls);
                        if (finder != null) {
                            finder.invalidate(((BaseModel) valuePair.getOldValue()).id);
                        }
                    }
                    if (valuePair.getNewValue() instanceof BaseModel) {
                        Class cls = valuePair.getNewValue().getClass();
                        CacheFinder finder = findCacheFinder(cls);
                        if (finder != null) {
                            finder.invalidate(((BaseModel) valuePair.getNewValue()).id);
                        }
                    }
                }

                if (ormChanged) {
                    logger.trace("invalidate - orm changed, invalidating itself");
                    this.idCache().clean();
                    CacheFinder finder = findCacheFinder(this.getClass());
                    if (finder != null) {
                        finder.invalidate(this.id);
                    }
                }
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
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


/* Private Support methods  -----------------------------------------------------------------------------------------------*/

    /**
     * Here we can log who do this operation. User or System.
     * Just a idea..
      * @return
     */
    @JsonIgnore public boolean its_person_operation() {
        try {
            return _BaseController.isAuthenticated();
        } catch (Exception e) {
            logger.internalServerError(e);
            return false;
        }
    }


    /**
     * Here we can log who do this operation. User or System.
     * Just a idea..
     * @return
     */
    @JsonIgnore private void save_author() {
        try {

            List<Field> fields = new ArrayList<>();
            getAllFields(fields, this.getClass(), 0);

            for (Field field : fields) {

                if (field.getName().equals("author_id")) {
                    if (field.get(this) == null) {
                        field.set(this, _BaseController.personId());
                        return;
                    }
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    private static CacheFinder findCacheFinder(Class cls) {
        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(InjectCache.class) && field.getType().equals(CacheFinder.class)) {
                try {
                    logger.debug("findCacheFinder - found cache finder field");
                    return (CacheFinder<?>) field.get(null);
                } catch (Exception e) {
                    logger.internalServerError(e);
                }
            }
        }
        return null;
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type, int iteration) {
        if(iteration > 4) return fields;
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass(), ++iteration);
        }

        return fields;
    }

    /**
     * Helper method for checking if property is loaded
     * without accidentally loading it.
     * @param property to check
     * @return true if it is loaded
     */
    protected boolean isLoaded(String property) {
        if (this instanceof EntityBean) {
            EntityBeanIntercept intercept = ((EntityBean) this)._ebean_intercept();
            int index = intercept.findProperty(property);
            if (index > -1) {
                boolean loaded = intercept.isLoadedProperty(index);
                logger.trace("isLoaded - property '{}' is {}", property, loaded ? "loaded" : "not loaded");
                return loaded;
            }
        }

        return true;
    }

/* Permission Contents ----------------------------------------------------------------------------------------------------*/

    @JsonPermission @Transient @ApiModelProperty(readOnly = true, value = "True if user can update this object.")
    public boolean update_permission;

    @JsonPermission(Action.DELETE) @Transient @ApiModelProperty(readOnly = true, value = "True if user can delete this object.")
    public boolean delete_permission;

}

