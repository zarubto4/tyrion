package models.notification;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.NotificationController;
import io.swagger.annotations.ApiModelProperty;
import models.person.Person;
import play.libs.Json;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Notification extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;


    @Enumerated(EnumType.STRING)    public NotificationController.Notification_level level;     // Důležitost (podbarvení zprávy)
    @Enumerated(EnumType.STRING)    public NotificationController.Notification_type type;       // Typ zprávy

    @Column(columnDefinition = "TEXT")  public String content;                                 // Obsah v podobě Json.toString().
                                        public boolean confirmation_required;
                         @JsonIgnore    public boolean confirmed;
                                        public boolean read;


    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085") public Date created;
    @JsonIgnore @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)  public Person person;

 /* JSON PROPERTY METHOD ------------------------------------------------------------------------------------------------*/

    @JsonProperty(value = "Its not possible document that throw swagger. Please visit documentation.byzance.cz")
    public JsonNode notification_body(){
        return Json.parse(content);
    }

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void set_read(){
            read = true;
            this.update();
    }

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Notification> find = new Finder<>(Notification.class);
}
