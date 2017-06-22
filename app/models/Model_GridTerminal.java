package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Class_Logger;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.UUID;


@Entity
@ApiModel(value = "Grid_Terminal", description = "Model of Grid_Terminal")
public class Model_GridTerminal extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_GridTerminal.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id
    @ApiModelProperty(required = true, readOnly = true, example = "Mobile, WebBrowser") public String terminal_token;
    @ApiModelProperty(required = false, readOnly = true)                                public String user_agent;
    @ApiModelProperty(required = true, example = "Mobile, WebBrowser")                  public String device_type;
    @ApiModelProperty(required = true, example = "Iphone 5S, Chrome 4")                 public String device_name;

    // public Integer resolution_height;
    // public Integer resolution_width; -resolution_width teoreticky potřebné pro vývojáře Gridu

    @ApiModelProperty(required = false, readOnly = true, value = "Only if Terminal Device is connected with logged Person")
    @JsonIgnore @ManyToOne(cascade = CascadeType.ALL) public Model_Person person;

    @JsonIgnore  public Date date_of_create;
    @JsonIgnore  public Date date_of_last_update;


    // lokální nedořešené oprávnění
    @ApiModelProperty(required = true)  public boolean ws_permission;       // TODO TOM smazat?
    @ApiModelProperty(required = true)  public boolean m_program_access;    // TODO TOM smazat?
    @ApiModelProperty(required = true)  public boolean up_to_date;          // TODO TOM smazat?

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


    public static final String private_prefix = "tprt_";
    public static final String public_prefix  = "tprt_";

    @JsonIgnore @Override
    public void save() {
                                                   // Dont change this prefix - its used on another places
         if(person != null) this.terminal_token = private_prefix + UUID.randomUUID().toString() + UUID.randomUUID().toString();  // terminal private token _
         else               this.terminal_token = public_prefix + UUID.randomUUID().toString() + UUID.randomUUID().toString();  // terminal public token _

        super.save();
    }



    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object Id: {}",  this.terminal_token);
        super.update();
    }

    @JsonIgnore @Override public void delete() {

        terminal_logger.debug("update :: Delete object Id: {} ", this.terminal_token);
        super.delete();

    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Model_GridTerminal> find = new Model.Finder<>(Model_GridTerminal.class);
}
