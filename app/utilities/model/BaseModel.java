package utilities.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers._BaseController;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import io.swagger.annotations.ApiModelProperty;
import models.Model_Person;
import org.ehcache.Cache;
import play.libs.Json;
import scala.xml.Null;
import utilities.cache.CacheField;
import utilities.cache.Cached;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions.Result_Error_Unauthorized;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.models_update_echo.EchoHandler;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.transaction.NotSupportedException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
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

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

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
    public void save() {

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

    /**
     * Default update method - Permission is checked inside
     */
    @JsonIgnore @Override
    public void update() {

        logger.debug("update::Update object Id: {}", this.id);

        // Check Permission
        if(its_person_operation()) check_update_permission();

    }

    /**
     * Default delete method - Permission is checked inside
     * Its not removed permanently!
     */
    @Override
    public boolean delete() {
        logger.trace("delete:: - deleting '{}' from DB, id: {}", this.getClass().getSimpleName(), this.id);

        if(its_person_operation()) check_delete_permission();

            this.deleted = true;
        this.removed = new Date();
        super.update();

        new Thread(this::evict).start(); // Evict the object from cache
        this.echoParent(); // Send echo update of parent object

        return true;
    }

    @Override
    public boolean deletePermanent() {
        logger.trace("deletePermanent - permanently deleting '{}' from DB, id: {}", this.getClass().getSimpleName(), this.id);

        if(its_person_operation()) check_delete_permission();

        return super.deletePermanent();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

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


/* Private Support methods  -----------------------------------------------------------------------------------------------*/

    /**
     * Here we can log who do this operation. User or System.
     * Just a idea..
      * @return
     */
    @JsonIgnore private boolean its_person_operation() {
        try {
            return  _BaseController.isAuthenticated();
        } catch (_Base_Result_Exception e){
            return false;
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

            Field field = this.getClass().getDeclaredField("author");
            if (field != null) {
                // Set only if its not set before by some special logic (For example, when we create copies of objects
                if(field.get(Model_Person.class) == null) {
                    field.set(_BaseController.person(), Model_Person.class);
                }
            }

        }catch (Exception e) {
            // Don't log anything!
            return;
        }
    }


/* Permission Contents ----------------------------------------------------------------------------------------------------*/


    @ApiModelProperty(readOnly = true, value = "can be hidden", required = true)
    @JsonProperty
    public boolean update_permission(){
        try{
            check_update_permission();
            return true;
        }catch (_Base_Result_Exception e) {
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
            check_delete_permission();
            return true;
        }catch (_Base_Result_Exception e) {
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


    /**
     * Special Abstract method for all Model_Xxxx where you can find  public XXXXX getById(UUID id){....}
     * @param id
     * @return
     */
    @JsonIgnore
    public boolean check_if_exist(UUID id) {
        try {

            // Set Arguments of Methods
            Class[] cArg = new Class[1];
            cArg[0] = UUID.class;

            Method method = this.getClass().getDeclaredMethod("getById", cArg);
            Object o = method.invoke(this.getClass(), id);

            return o != null;

        } catch (InvocationTargetException e) {

            if(e.getCause().getClass().getSimpleName().equals(Result_Error_NotFound.class.getSimpleName())){
                return false;
            }

            logger.error("check_if_exist:: is not supported on {}, because getById(UUID id) is missing.", this.getClass().getSimpleName());
            throw new Result_Error_NotSupportedException();

        }catch (Exception e){
            // Everytime its InvocationTargetException, but compilator required this one also
            logger.error("check_if_exist:: is not supported on {}, because getById(UUID id) missing.", this.getClass().getSimpleName());
            throw new Result_Error_NotSupportedException();
        }
    }

    @JsonIgnore
    public boolean check_if_exist(String id) {
        return check_if_exist(UUID.fromString(id));
    }
}

