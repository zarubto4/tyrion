package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers._BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.errors.Exceptions.Result_Error_NotSupportedException;
import utilities.errors.Exceptions._Base_Result_Exception;
import utilities.logger.Logger;
import utilities.model.NamedModel;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.beans.Transient;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Log",
        value = "Log")
@Table(name="Log")
public class Model_Log extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Log.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                            public String type; // "tyrion", "homer"
      @JsonIgnore @OneToOne public Model_Blob file;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    @JsonIgnore @Override
    public boolean delete() { // TODO better

        Model_Blob file = this.file;

        this.file = null;
        this.update();

        file.refresh();
        file.delete();

        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient @Override public void check_read_permission()   throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.Log_delete.name())) return;
        logger.error("check_read_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }
    @JsonIgnore @Transient @Override public void check_create_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.Log_delete.name())) return;
        logger.error("check_create_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }
    @JsonIgnore @Transient @Override public void check_update_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.Log_delete.name())) return;
        logger.error("check_update_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }
    @JsonIgnore @Transient @Override public void check_delete_permission() throws _Base_Result_Exception {
        if (_BaseController.person().has_permission(Permission.Log_delete.name())) return;
        logger.error("check_delete_permission: Not Supported");
        throw new Result_Error_NotSupportedException();
    }

    public enum Permission {Log_create, Log_read, Log_update, Log_edit, Log_delete}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Log getById(UUID id) throws _Base_Result_Exception {
        Model_Log board = find.byId(id);
        if (board == null) throw new Result_Error_NotFound(Model_Log.class);

        // Check Permission
        if(board.its_person_operation()) {
            board.check_read_permission();
        }

        return board;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<UUID, Model_Log> find = new Finder<>(Model_Log.class);

}
