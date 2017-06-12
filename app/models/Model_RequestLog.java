package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import utilities.logger.Class_Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

@Entity
@ApiModel(value = "RequestLog", description = "Model of RequestLog")
public class Model_RequestLog extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_RequestLog.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                        @Id public String id;
    @Column(unique = true)  public String request;
                            public Long call_count;

                @JsonIgnore public Date date_of_create;


/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save: Creating new Object");

        date_of_create = new Date();

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override
    public void update() {

        terminal_logger.debug("update: Update object value: {}",  this.id);

        super.update();

    }

    @JsonIgnore @Override
    public void delete() {

        terminal_logger.error("delete: This object is not legitimate to remove. ");
        super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_RequestLog> find = new Model.Finder<>(Model_RequestLog.class);

}
