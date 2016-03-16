package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.persons.Person;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;


@Entity
public class Grid_Terminal extends Model {

    @Id
    @ApiModelProperty(required = true, readOnly = true, example = "Mobile, WebBrowser")
    public String terminal_id;


    @ApiModelProperty(required = true, example = "Mobile, WebBrowser")
    public String device_type;

    @ApiModelProperty(required = true, example = "Iphone 5S, Chrome 4")
    public String device_name;

    @ApiModelProperty(required = false, readOnly = true, value = "Only if Device is connected with logged Person")
    @JsonIgnore public Person person;

    //***** Private ****************************************************************************************************

    @JsonIgnore
    public void set_terminal_id() {
        while(true){ // I need Unique Value
            this.terminal_id  = UUID.randomUUID().toString();
            if (Grid_Terminal.find.where().eq("qr_token", this.terminal_id ).findUnique() == null) break;
        }
    }

    public static Model.Finder<String,Grid_Terminal> find = new Model.Finder<>(Grid_Terminal.class);
}
