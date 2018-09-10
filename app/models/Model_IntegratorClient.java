package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.beans.Transient;
import java.util.UUID;

@Entity
@ApiModel(value = "IntegratorClient", description = "Model of Client of an Integrator (Customer)")
@Table(name="IntegratorClient")
public class Model_IntegratorClient extends BaseModel {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_IntegratorClient.class);

    /* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @JsonIgnore @OneToOne(mappedBy = "integrator_client")  public Model_Product product;

                @OneToOne public Model_Contact contact;

    /* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    /* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    /* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

    /* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

    /* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

    /* PERMISSION Description ----------------------------------------------------------------------------------------------*/

    /* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // TODO
    @JsonIgnore @Transient
    @Override public void check_read_permission()   throws _Base_Result_Exception {
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

    public static Model_IntegratorClient getById(UUID id) throws _Base_Result_Exception {

        Model_IntegratorClient client = Model_IntegratorClient.find.byId(id);
        if (client == null) throw new Result_Error_NotFound(Model_IntegratorClient.class);
        // Check Permission
        if(client.its_person_operation()) {
            client.check_read_permission();
        }
        return client;
    }

    /* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_IntegratorClient> find = new Finder<>(Model_IntegratorClient.class);
}
