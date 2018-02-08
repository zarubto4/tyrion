package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.enums.ParticipantStatus;
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

    @JsonIgnore   public boolean create_permission()    {return true;}
    @JsonProperty public boolean update_permission()    {return true;}
    @JsonProperty public boolean edit_permission()      {return true;}
    @JsonProperty public boolean delete_permission()    {return person.id.equals(BaseController.personId()) || customer.isEmployee(BaseController.person()) || BaseController.person().has_permission("Employee_delete");}

    public enum Permission { Employee_edit, Employee_read, Employee_update, Employee_delete }

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