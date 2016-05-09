package models.notification;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;
import utilities.notification.Notification_level;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Notification extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
    @Enumerated(EnumType.STRING)    public Notification_level level;
                                    public String message;
                                    public boolean confirmation_required;
                     @JsonIgnore    public boolean confirmed;
                                    public boolean read;

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true,
    value = "UNIX time stamp", example = "1458315085")
    public Date created;

    @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE)  public Person person;


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_read(){
            read = true;
            this.update();
    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Notification> find = new Finder<>(Notification.class);
}
