package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Customer", description = "Model of Customer")
@Table(name="Customer")
public class Model_Customer extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Customer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                        @JsonIgnore public String fakturoid_subject_id;

         @OneToOne(mappedBy = "customer",cascade = CascadeType.ALL) public Model_PaymentDetails payment_details;

       @JsonIgnore @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL) public List<Model_Product>  products  = new ArrayList<>();
                   @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY) public List<Model_Employee> employees = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    public List<Model_Employee> getEmployees() {
        return  Model_Employee.find.query().where().eq("customer.id", this.id).findList();
    }

    @JsonIgnore
    public boolean isEmployee(Model_Person person) {
        try {

            if (employees.isEmpty()) {
                return Model_Person.find.query().where().eq("employees.customer.id", this.id).findOne() != null;
            }

            return employees.stream().anyMatch(e -> e.get_person().id.equals(person.id));

        } catch (Exception e) {
            logger.internalServerError(e);
            return false;
        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // TODO Oprávnění - Poměrně složité na řešení - odloženo na neurčito - Řešitel bude lexa - Financial je jeho.
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        //
    }
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        //
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        //
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        //
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Customer getById(String id)throws _Base_Result_Exception {
        return getById(UUID.fromString(id));
    }

    public static Model_Customer getById(UUID id) throws _Base_Result_Exception {

        Model_Customer customer = Model_Customer.find.byId(id);
        if (customer == null) throw new Result_Error_NotFound(Model_Customer.class);
        // Check Permission
        if(customer.its_person_operation()) {
            customer.check_read_permission();
        }
        return customer;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Customer> find = new Finder<>(Model_Customer.class);
}