package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;


@Entity
public class Grid_Terminal extends Model {

    @Id
    @ApiModelProperty(required = true, readOnly = true, example = "Mobile, WebBrowser")
    public String terminal_id;

    @ApiModelProperty(required = false, readOnly = true)
    public String user_agent;

    @ApiModelProperty(required = true, example = "Mobile, WebBrowser")
    public String device_type;

    @ApiModelProperty(required = true, example = "Iphone 5S, Chrome 4")
    public String device_name;

    @ApiModelProperty(required = false, readOnly = true, value = "Only if Device is connected with logged Person")
    @JsonIgnore public Person person;

    @JsonIgnore  public Date date_of_create;
    @JsonIgnore  public Date date_of_last_update;


    // lokální nedořešené oprávnění
    public Boolean ws_permission;
    public Boolean m_program_access;
    public Boolean up_to_date;


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public void set_terminal_id() {
        while(true){ // I need Unique Value
            this.terminal_id  = UUID.randomUUID().toString();
            if (Grid_Terminal.find.where().eq("terminal_id", this.terminal_id ).findUnique() == null) break;
        }
    }


/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Model.Finder<String,Grid_Terminal> find = new Model.Finder<>(Grid_Terminal.class);
}
