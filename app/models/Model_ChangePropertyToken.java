package models;

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

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/
/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/
/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/
/* PERMISSION Description ----------------------------------------------------------------------------------------------*/
/* PERMISSION ----------------------------------------------------------------------------------------------------------*/
/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_ChangePropertyToken getById(String id) {
        return getById(UUID.fromString(id));
    }

    public static Model_ChangePropertyToken getById(UUID id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_ChangePropertyToken> find = new Finder<>(Model_ChangePropertyToken.class);
}
