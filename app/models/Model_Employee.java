package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.enums.ParticipantStatus;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions.Result_Error_PermissionDenied;
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
        return this.get_person();
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @Transient @JsonIgnore
    public Model_Person get_person() {

        Model_Person person = Model_Person.find.query().where().eq("employees.id", id).select("id").findOne();
        return Model_Person.getById(person.id);
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_read_permission() throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.Employee_read.name())) return;
        customer.check_read_permission();
    }
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.Employee_crate.name())) return;
        customer.check_update_permission();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.Employee_update.name())) return;
        customer.check_update_permission();
    }

    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if(BaseController.person().has_permission(Permission.Employee_delete.name())) return;
        customer.check_delete_permission();
    }

    public enum Permission { Employee_crate, Employee_edit, Employee_read, Employee_update, Employee_delete }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Employee getById(String id) {
        return getById(UUID.fromString(id));
    }
    
    public static Model_Employee getById(UUID id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Employee> find = new Finder<>(Model_Employee.class);
}