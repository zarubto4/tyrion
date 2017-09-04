package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import utilities.enums.Enum_Participant_status;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_Person_Short_Detail;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@ApiModel(value = "Employee", description = "Model of Employee")
@Table(name="Employee")
public class Model_Employee extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Employee.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                            @Id public UUID id;
                    @JsonIgnore public Date created;
                                public Enum_Participant_status state;
         @JsonIgnore @ManyToOne public Model_Person person;
         @JsonIgnore @ManyToOne public Model_Customer customer;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty
    public Swagger_Person_Short_Detail person(){

        return this.person.get_short_person();
    }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save: Creating new Object");

        created = new Date();

        super.save();
    }

    @JsonIgnore @Override
    public void update() {

        terminal_logger.debug("update: Update object Id = {}",  this.id);

        super.update();
    }

    @JsonIgnore @Override
    public void delete() {

        terminal_logger.debug("delete: Delete object Id = {} ", this.id);

        this.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   public boolean create_permission()    {return true;}
    @JsonProperty public boolean update_permission()    {return true;}
    @JsonProperty public boolean edit_permission()      {return true;}
    @JsonProperty public boolean delete_permission()    {return person.id.equals(Controller_Security.get_person_id()) || customer.isEmployee(Controller_Security.get_person()) || Controller_Security.get_person().has_permission("Employee_delete");}

    public enum permissions{Employee_edit, Employee_read, Employee_update, Employee_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public static Model_Employee get_byId(String id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<String, Model_Employee> find = new Finder<>(Model_Employee.class);
}