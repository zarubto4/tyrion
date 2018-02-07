package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
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

    @JsonIgnore   public boolean create_permission()    {return true;}
    @JsonProperty public boolean update_permission()    {return true;}
    @JsonProperty public boolean edit_permission()      {return true;}
    @JsonProperty public boolean delete_permission()    {return true;}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Customer getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_Customer getById(UUID id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_Customer> find = new Finder<>(Model_Customer.class);
}