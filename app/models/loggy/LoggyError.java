package models.loggy;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class LoggyError extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id                                 public String id;
    @Column(columnDefinition = "TEXT")  public String summary;
    @Column(columnDefinition = "TEXT")  public String description;
                                        public String youtrack_url;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void setYoutrack_url(String url) {
        youtrack_url = url;
    }

    @JsonIgnore
    public LoggyError(String id, String summary, String description) {
        this.id = id;
        this.summary = summary;
        this.description = description;
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,LoggyError> find = new Finder<>(LoggyError.class);
}
