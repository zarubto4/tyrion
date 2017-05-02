package models;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Model_LoggyError extends Model {

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
    public Model_LoggyError(String id, String summary, String description) {
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
    public static Finder<String,Model_LoggyError> find = new Finder<>(Model_LoggyError.class);
}
