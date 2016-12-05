package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.UUID;


@Entity
public class Grid_Terminal extends Model {

    @Id
    @ApiModelProperty(required = true, readOnly = true, example = "Mobile, WebBrowser")
    public String terminal_token;

    @ApiModelProperty(required = false, readOnly = true)
    public String user_agent;

    @ApiModelProperty(required = true, example = "Mobile, WebBrowser")
    public String device_type;

    @ApiModelProperty(required = true, example = "Iphone 5S, Chrome 4")
    public String device_name;

    // public Integer resolution_hight;
    // public Integer resolution_weight; -resolution_weight teoreticky potřebné pro vývojáře Gridu 

    @ApiModelProperty(required = false, readOnly = true, value = "Only if Terminal Device is connected with logged Person")
    @JsonIgnore @ManyToOne(cascade = CascadeType.ALL) public Person person;

    @JsonIgnore  public Date date_of_create;
    @JsonIgnore  public Date date_of_last_update;


    // lokální nedořešené oprávnění
    @ApiModelProperty(required = true)  public boolean ws_permission;
    @ApiModelProperty(required = true)  public boolean m_program_access;
    @ApiModelProperty(required = true)  public boolean up_to_date;


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {
         if(person != null) this.terminal_token = "private_grid_token_" + UUID.randomUUID().toString() + UUID.randomUUID().toString();
         else               this.terminal_token = "public_grid_token_" + UUID.randomUUID().toString() + UUID.randomUUID().toString();

        super.save();
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Grid_Terminal> find = new Model.Finder<>(Grid_Terminal.class);
}
