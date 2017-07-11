package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.Controller_Security;
import graphql.schema.GraphQLObjectType;
import io.swagger.annotations.ApiModel;
import utilities.enums.Enum_Garfield_burning_state;
import utilities.logger.Class_Logger;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

@Entity
@ApiModel(description = "Model for MacAdress Controling",
          value = "MacAddressRegisterRecord")
public class Model_MacAddressRegisterRecord extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_MacAddressRegisterRecord.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

            @Id  public String uuid_request_number;

    @JsonIgnore  public String mac_address;         // Requestem přidělená Mac Adressa
    @JsonIgnore  public Date date_of_create;        // Datum vytvoření Request

    @JsonIgnore  public String type_of_board;         // Typ Desky pro kterou byl proveden Request
    @JsonIgnore  public String full_id;               // ID Procesoru - který zasla request
    @JsonIgnore  public String bootloader_id; // Verze Bootloaderu, který byl vypálen
    @JsonIgnore  public String firmware_version_id;   // Verze Firmwaru, který byl vypálen
    @JsonIgnore  public Enum_Garfield_burning_state state;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
        terminal_logger.debug("save :: Creating new Object");
        super.save();
    }

    @JsonIgnore @Override
    public void update() {
        terminal_logger.debug("update :: Update object Id: {}",  this.uuid_request_number);
        super.update();
    }

    @JsonIgnore @Override
    public void delete() {
        terminal_logger.debug("update :: Delete object Id: {} ", this.uuid_request_number);
        super.delete();
    }




/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient  public boolean admin_permission(){  return Controller_Security.get_person().permissions_keys.containsKey("MacAddressRegister"); }

    public enum permissions{MacAddressRegister}

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* GRAPH_QL ------------------------------------------------------------------------------------------------------------*/


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_MacAddressRegisterRecord> find = new Finder<>(Model_MacAddressRegisterRecord.class);


}
