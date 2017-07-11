package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import utilities.logger.Class_Logger;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value = "Customer", description = "Model of Customer")
public class Model_Customer extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_Customer.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                                                                @Id public String id;
                                                        @JsonIgnore public Date created;
                                                        @JsonIgnore public boolean removed_by_user;
                                                        @JsonIgnore public boolean company;

         @OneToOne(mappedBy = "customer",cascade = CascadeType.ALL) public Model_PaymentDetails payment_details;
                                                         @OneToOne public Model_Person person;

       @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL) public List<Model_Product>  products  = new ArrayList<>();
       @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL) public List<Model_Employee> employees = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");

        created = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (find.byId(this.id) == null) break;
        }

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

        this.removed_by_user = true;
        this.update();
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
    @JsonProperty public boolean delete_permission()    {return true;}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore public static Model_Customer get_byId(String id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<String, Model_Customer> find = new Finder<>(Model_Customer.class);
}