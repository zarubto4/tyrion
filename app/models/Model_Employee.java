package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers._BaseController;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.ParticipantStatus;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel(value = "Employee", description = "Model of Employee")
@Table(name="Employee")
public class Model_Employee extends BaseModel {

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
        return this.get_person();
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

    @Transient @JsonIgnore
    public Model_Person get_person() {

        try {
            return Model_Person.find.byId(get_person_id());
        } catch (Exception e) {
            return null;

        }
    }
/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // TODO rework permissions

    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Employee_crate.name())) return;
        if(person.id.equals(_BaseController.person().id)) return;
        customer.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Employee_read.name())) return;
        if(get_person_id().equals(_BaseController.person().id)) return;
        customer.check_read_permission();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Employee_update.name())) return;
        if(get_person_id().equals(_BaseController.person().id)) return;
        customer.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(_BaseController.person().has_permission(Permission.Employee_delete.name())) return;
        if(get_person_id().equals(_BaseController.person().id)) return;
        customer.check_delete_permission();
    }

    public enum Permission { Employee_crate, Employee_edit, Employee_read, Employee_update, Employee_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Employee.class)
    public static CacheFinder<Model_Employee> find = new CacheFinder<>(Model_Employee.class);
}