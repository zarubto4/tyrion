package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@ApiModel(description = "Model of Log",
        value = "Log")
@Table(name="Log")
public class Model_Log extends Model{

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

                        @Id public String id;
                            public String name;
                            public Date created;
                            public String type; // "tyrion", "homer"

      @JsonIgnore @OneToOne public Model_FileRecord file;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_Log.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override
    public void delete() {

        Model_FileRecord file = this.file;

        this.file = null;
        this.update();

        file.refresh();
        file.delete();

        super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_Log> find = new Model.Finder<>(Model_Log.class);

}
