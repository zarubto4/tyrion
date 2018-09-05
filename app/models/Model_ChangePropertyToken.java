package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers._BaseController;
import io.ebean.Finder;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name="ChangePropertyToken")
public class Model_ChangePropertyToken extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_ChangePropertyToken.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String property;
    public String value;

    @OneToOne(fetch = FetchType.LAZY) public Model_Person person;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/
/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public boolean delete() {

        logger.debug("permanent delete :: Delete object Id: {} ", this.id);
        return super.deletePermanent();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/
/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/
/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/
/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    // Create Permission is always JsonIgnore
    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        logger.error("check_read_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        logger.error("check_create_permission: Not Supported");
        if(_BaseController.person().id.equals(person.id)) return;
        throw new Result_Error_NotSupportedException();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        logger.error("check_update_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        logger.error("check_delete_permission: Not Supported");
        if(_BaseController.person().id.equals(person.id)) return;
        throw new Result_Error_NotSupportedException();
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_ChangePropertyToken> find = new Finder<>(Model_ChangePropertyToken.class);
}
