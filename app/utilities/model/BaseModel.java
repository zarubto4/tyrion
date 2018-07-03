package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import controllers._BaseController;
import controllers._BaseFormFactory;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import io.swagger.annotations.ApiModelProperty;
import org.ehcache.Cache;
import play.libs.Json;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.errors.Exceptions.*;
import utilities.logger.Logger;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.util.*;

import static java.util.stream.Collectors.toList;

@MappedSuperclass
public abstract class BaseModel  extends Model implements JsonSerializer {

    @Inject public static _BaseFormFactory baseFormFactory; // Its Required to set this in Server.class Component

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
    /**
     *
     */
   @JsonIgnore @Transient
    public IDCache cache(){

        if(idCache == null) {
            idCache = new IDCache();
        }

        return idCache;
    }

    // It must be always private!
    private IDCache idCache;

    // Private Class not acceptable from other Tyrion Components
    public class IDCache {

        private HashMap<Class, List<UUID>> cash_map = new HashMap<>();
        private JsonNode cached_json_object_for_rest = null;

        public void add(Class c, List<UUID> ids){
            try {
                if (ids != null) {
                    if (!cash_map.containsKey(c)) {
                        cash_map.put(c, ids);
                    } else {
                        if (cash_map.get(c) != null) {
                            cash_map.get(c).addAll(0, ids);
                        } else {
                            cash_map.put(c, ids);
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

                    if (!cash_map.containsKey(c)) {
                       // System.out.println("IDCache:: not contains KEy");

                        // Create List ArraList <- its not possible to use  Collections.singletonList(id))
                        // * @throws UnsupportedOperationException if the <tt>add</tt> operation
                        // *  is not supported by singletonList
                        List<UUID> list = new ArrayList<>();
                        list.add(id);

                        cash_map.put(c, list);
                    } else {

                        if(!cash_map.get(c).contains(id)) {
                            cash_map.get(c).add(id);
                        }

                    }
                } else {
                    System.err.println("IDCache: add id is null for class: " + c.getSimpleName() + " its not error. In most cases it's okay ");
                }
            } catch (Exception e){
                e.printStackTrace();
                // Nothing
            }
        }

        public void remove(Class c, List<UUID> ids){
            try {
                if(ids != null) {
                    if (cash_map.containsKey(c)) {
                        cash_map.get(c).removeAll(ids);
                    }
                }
            } catch (Exception e){
                // Nothing
            }
        }

        public void remove(Class c, UUID id){
            try {
                if(id != null)
                if(cash_map.containsKey(c)){
                    System.out.println("BaseModel Remove - Class " + c.getSimpleName() + " id " + id);
                    System.out.println("BaseModel Remove - Content " + cash_map.get(c) );
                    System.out.println("BaseModel Remove - Content size " + cash_map.get(c).size() );
                    cash_map.get(c).remove(id);
                    System.out.println("BaseModel Remove - Content " + cash_map.get(c) );
                }
            } catch (Exception e){
                logger.internalServerError(e);
            }
        }

        public void removeAll(Class c){
            try {

                if (cash_map.containsKey(c)) {
                    cash_map.remove(c);
                }

            } catch (Exception e){
                logger.internalServerError(e);
            }
        }


        public List<UUID> gets(Class c){

                if (cash_map.containsKey(c)) {
                    // return Collections.synchronizedList(cash_map.get(c));
                    List<UUID> clone = cash_map.get(c).stream().collect(toList());
                    return clone;

                } else {
                    return null;
                }

        }

        public UUID get(Class c){
            try {
                if (cash_map.containsKey(c)) {
                    List<UUID> list = cash_map.get(c);
                    if (!list.isEmpty()) {
                        return list.get(0);
                    }
                }

                return null;
            }catch (Exception e) {
                return null;
            }
        }

        public JsonNode get_cached_json(){
           return this.cached_json_object_for_rest;
        }

        public void set_cached_json(JsonNode node){
            this.cached_json_object_for_rest = node;
        }

    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    /**
     * Converts this model to JSON
     * @return JSON representation of this model
     */
    public JsonNode json() {

        /*

        // Výrzané zrychlení kdy je cachován už rovnou celý objekt v json podobě,
        // bohužel se musí udělat opravdu hluboké testování na veškšrou kombinatoriku protože při každé změně je nutné udělat clean tohoto jsonu
        if(cache().get_cached_json() != null) {
            return cache().get_cached_json();
        }

        cache().set_cached_json( Json.toJson(this));
        return cache().get_cached_json();
        */

        return Json.toJson(this);

    }

    /**
     * Converts this model to JSON and than stringify
     * @return string from JSON representation
     *
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


/* COMMON METHODS ------------------------------------------------------------------------------------------------------*/

    /**
     * Default save method - Permission is checked inside
     * System will evytime check permission for this operation. But sometime, its done by system without "logged person" token
     */
    @Override
    public void save() throws _Base_Result_Exception {

        logger.debug("save::Creating new Object");

        // Check Permission - only if user is logged!
        if(its_person_operation()) {
            check_create_permission();
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

        new Thread(this::cacheCleaner).start(); // Caches the object

        logger.trace("save - saved '{}' to DB, id: {}", this.getClass().getSimpleName(), this.id);

        // Send the echo update
        if (isNew) {
            this.echoParent();
        } else {
            this.echo();
        }
    }

    /**
     * Default update method - Permission is checked inside
     */
    @JsonIgnore @Override
    public void update() throws _Base_Result_Exception {
        try {

            logger.debug("update::Update object Id: {}", this.id);

            // Check Permission
            if (its_person_operation()) {
                check_update_permission();
                logger.debug("Permission is ok");
            }

            super.update();
            // this.cacheCleaner();

        } catch (Result_Error_PermissionDenied e) {
            logger.warn("update::Unauthorized UPDATE operation, its required remove everything from Cache");
            throw new Result_Error_PermissionDenied();
        } catch (Exception e){
            logger.error("update::Something is wrong!");
            logger.internalServerError(e);
            throw new Result_Error_PermissionDenied();
        }
    }

    /**
     * Default delete method - Permission is checked inside
     * Its not removed permanently!
     */
    @Override
    public boolean delete() throws _Base_Result_Exception {
        logger.debug("delete:: - deleting '{}' from DB, id: {}", this.getClass().getSimpleName(), this.id);

        if(its_person_operation()) {
            check_delete_permission();
        }

        super.delete();

        new Thread(this::evict).start(); // Evict the object from cache
        this.echoParent(); // Send echo update of parent object

        return true;
    }

    @Override
    public boolean deletePermanent() {
        logger.debug("deletePermanent - permanently deleting '{}' from DB, id: {}", this.getClass().getSimpleName(), this.id);

        if(its_person_operation()) check_delete_permission();

        return super.deletePermanent();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    /**
     * Method finds the cache field in the class
     * and if present it puts or replaces the value in the cache.
     * TODO measure performance impact LEVEL: HARD  TIME: LONGTERM
     */
    @SuppressWarnings("unchecked")
    private void cacheCleaner() {
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
     * TODO measure performance impact LEVEL: HARD  TIME: LONGTERM
     */
    @SuppressWarnings("unchecked")
    private void evict() {
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
     * that are only cached. TODO measure performance impact LEVEL: HARD  TIME: LONGTERM
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
    @JsonIgnore private void save_author() {
        try {

            List<Field> fields = new ArrayList<>();
            getAllFields(fields, this.getClass(), 0);


            for(Field field : fields) {

                if(field.getName().equals("author_id")) {
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

    public static List<Field> getAllFields(List<Field> fields, Class<?> type, int iteration) {
        if(iteration > 4) return fields;
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass(), ++iteration);
        }

        return fields;
    }


/* Permission Contents ----------------------------------------------------------------------------------------------------*/


    @ApiModelProperty(readOnly = true, value = "can be hidden", required = true)
    @JsonProperty
    public boolean update_permission(){
        try{

            if(!its_person_operation()) {
               return true;
            }

            check_update_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, true);
            return true;
        }catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_update_" + id, false);
            return false;
        }catch (Exception e){
            logger.internalServerError(e);
            return false;
        }
    }

    @ApiModelProperty(readOnly = true, value = "can be hidden", required = true)
    @JsonProperty
    public boolean delete_permission(){
        try{

            if(!its_person_operation()) {
                return true;
            }

            check_delete_permission();
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, true);
            return true;
        }catch (_Base_Result_Exception e) {
            _BaseController.person().cache_permission(this.getClass().getSimpleName() + "_delete_" + id, false);
            return false;
        }catch (Exception e){
            logger.internalServerError(e);
            return false;
        }
    }

/* ABSTRACT METHODS ----------------------------------------------------------------------------------------------------*/

    /*
     * Required for all Models in Database.
     * You can used this one, or Override this
     *
     */
    @JsonIgnore public abstract void check_create_permission() throws _Base_Result_Exception;
    @JsonIgnore public abstract void check_read_permission()   throws _Base_Result_Exception;
    @JsonIgnore public abstract void check_update_permission() throws _Base_Result_Exception;
    @JsonIgnore public abstract void check_delete_permission() throws _Base_Result_Exception;

}

