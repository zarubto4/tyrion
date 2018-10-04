package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.enums.ParticipantStatus;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.model.UnderCustomer;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Employee", description = "Model of Employee")
@Table(name="Employee")
public class Model_Employee extends BaseModel implements Permissible, UnderCustomer {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Employee.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                public ParticipantStatus state;
         @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) public Model_Person person;
         @JsonIgnore @ManyToOne public Model_Customer customer;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty
    public Model_Person person() {
        try{
            return this.getPerson();
        } catch (_Base_Result_Exception e){
            //nothing
            return null;
        }catch (Exception e){
            logger.internalServerError(e);
            return null;
        }
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore
    public UUID get_person_id() throws _Base_Result_Exception {

        if (idCache().get(Model_Person.class) == null) {
            idCache().add(Model_Person.class, (UUID) Model_Person.find.query().where().eq("employees.id", id).select("id").findSingleAttribute());
        }

        return idCache().get(Model_Person.class);
    }

    @JsonIgnore
    public Model_Person getPerson() {
        return isLoaded("person") ? person : Model_Person.find.query().where().eq("employees.id", id).findOne();
    }

    @JsonIgnore @Override
    public Model_Customer getCustomer() {
        return isLoaded("customer") ? customer : Model_Customer.find.query().where().eq("employees.id", id).findOne();
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.EMPLOYEE;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Employee.class)
    public static CacheFinder<Model_Employee> find = new CacheFinder<>(Model_Employee.class);
}