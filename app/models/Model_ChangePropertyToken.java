package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
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
        return super.deletePermanent();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_ChangePropertyToken> find = new Finder<>(Model_ChangePropertyToken.class);
}
