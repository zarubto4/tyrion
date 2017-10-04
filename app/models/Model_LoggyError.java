package models;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@ApiModel(value = "LoggyError", description = "Model of LoggyError")
@Table(name="LoggyError")
public class Model_LoggyError extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id                                 public String id;
    @Column(columnDefinition = "TEXT")  public String summary;
    @Column(columnDefinition = "TEXT")  public String description;
    @Column(columnDefinition = "TEXT")  public String stack_trace;
    @Column(columnDefinition = "TEXT")  public String cause;
                                        public String youtrack_url;

                                        public Long repetition;

                                        public Date created;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_LoggyError(String id, String summary, String description, String stack_trace, String cause) {
        this.id = id;
        this.summary = summary;
        this.description = description;
        this.stack_trace = stack_trace;
        this.cause = cause;
        this.repetition = 1L;
        this.created = new Date();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore                                      public boolean read_permission()  {  return Controller_Security.get_person().has_permission("LoggyError_read");}
    @JsonProperty @ApiModelProperty(required = true) public boolean edit_permission()  {  return Controller_Security.get_person().has_permission("LoggyError_edit");}
    @JsonProperty @ApiModelProperty(required = true) public boolean delete_permission(){  return Controller_Security.get_person().has_permission("LoggyError_delete");}

    public enum permissions{LoggyError_read, LoggyError_edit, LoggyError_delete,}

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Model_LoggyError> find = new Finder<>(Model_LoggyError.class);
}
