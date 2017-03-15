package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import utilities.enums.Burning_state;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;

@Entity
@ApiModel(description = "Model for MacAdress Controling",
          value = "MacAddressRegisterRecord")
public class Model_MacAddressRegisterRecord extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

            @Id  public String uuid_request_number;

    @JsonIgnore  public String mac_address;         // Requestem přidělená Mac Adressa
    @JsonIgnore  public Date date_of_create;        // Datum vytvoření Request

    @JsonIgnore  public String type_of_board;         // Typ Desky pro kterou byl proveden Request
    @JsonIgnore  public String full_id;               // ID Procesoru - který zasla request
    @JsonIgnore  public String bootloader_id; // Verze Bootloaderu, který byl vypálen
    @JsonIgnore  public String firmware_version_id;   // Verze Firmwaru, který byl vypálen
    @JsonIgnore  public Burning_state state;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/




/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient  public boolean admin_permission(){  return Controller_Security.getPerson().has_permission("MacAddressRegister"); }

    public enum permissions{MacAddressRegister}


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String, Model_MacAddressRegisterRecord> find = new Finder<>(Model_MacAddressRegisterRecord.class);


}
