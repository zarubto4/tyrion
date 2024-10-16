package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.cache.CacheFinder;
import utilities.cache.InjectCache;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import javax.persistence.*;
import java.util.UUID;

@Entity
@ApiModel(value = "GridTerminal", description = "Model of GridTerminal")
@Table(name="GridTerminal")
public class Model_GridTerminal extends BaseModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_GridTerminal.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, example = "Mobile, WebBrowser") public String terminal_token;
    @ApiModelProperty(required = false, readOnly = true, example = "Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Mobile/7B405") public String user_agent;
    @ApiModelProperty(required = false, example = "Mobile, WebBrowser")                  public String device_type;
    @ApiModelProperty(required = false, example = "Iphone 5S, Chrome 4")                 public String device_name;

    // public Integer resolution_height;
    // public Integer resolution_width; -resolution_width teoreticky potřebné pro vývojáře Gridu

    @ApiModelProperty(required = false, readOnly = true, value = "Only if Terminal Device is connected with logged Person")
    @JsonIgnore @ManyToOne(cascade = CascadeType.ALL) public Model_Person person;

    // lokální nedořešené oprávnění
    @ApiModelProperty(required = true)  public boolean ws_permission;       // TODO TOM smazat?
    @ApiModelProperty(required = true)  public boolean m_program_access;    // TODO TOM smazat?
    @ApiModelProperty(required = true)  public boolean up_to_date;          // TODO TOM smazat?

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    public static final String private_prefix = "tprt_"; // Token PRiviT
    public static final String public_prefix  = "tpuk_"; // Token PUbliK

    @JsonIgnore @Override
    public void save() {
                                                // Dont change this prefix - its used on another places
         if (person != null) this.terminal_token = private_prefix + UUID.randomUUID().toString() + UUID.randomUUID().toString();  // terminal private token _
         else               this.terminal_token = public_prefix + UUID.randomUUID().toString() + UUID.randomUUID().toString();  // terminal public token _

        super.save();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @InjectCache(Model_GridTerminal.class)
    public static CacheFinder<Model_GridTerminal> find = new CacheFinder<>(Model_GridTerminal.class);
}
