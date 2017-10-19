package models;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.mvc.Http;
import utilities.Server;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@ApiModel(value = "ServerError", description = "Model of ServerError")
@Table(name="ServerError")
public class Model_ServerError extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id                                 public UUID id;
    @Column(columnDefinition = "TEXT")  public String summary;
    @Column(columnDefinition = "TEXT")  public String description;
                                        public String type;
    @Column(columnDefinition = "TEXT")  public String message;
    @Column(columnDefinition = "TEXT")  public String stack_trace;

                                        public String request;
                                        public String person;
                                        public String tyrion;
                                        public Long repetition;
                                        public Date created;

                                        public String cause_type;
    @Column(columnDefinition = "TEXT")  public String cause_message;
    @Column(columnDefinition = "TEXT")  public String cause_stack_trace;

                                        public String youtrack_url;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public Model_ServerError(Throwable exception, String origin, Http.RequestHeader request) {
        this.type = exception.getClass().getName();
        this.message = exception.getMessage();
        this.stack_trace = formatStackTrace(exception.getStackTrace());

        Throwable cause = exception.getCause();
        if (cause != null) {
            this.cause_type = cause.getClass().getName();
            this.cause_message = cause.getMessage();
            this.cause_stack_trace = formatStackTrace(cause.getStackTrace());
        }

        this.summary = origin;
        this.repetition = 1L;
        this.created = new Date();
        this.tyrion = Server.server_version + " (" + Server.server_mode.name() + ")";

        if (request != null) {
            this.request = request.method() + " " + request.path();
            Model_Person person = Controller_Security.get_person();
            if (person != null) {
                this.person = person.mail;
            }
        }
    }

    @JsonIgnore
    public String prettyPrint() {
        StringBuilder pretty = new StringBuilder();

        pretty.append("Error at: ");
        pretty.append(this.summary);
        pretty.append("\n");

        pretty.append("Exception type: ");
        pretty.append(this.type);
        pretty.append("\n");

        pretty.append("Exception message: ");
        pretty.append(this.message);
        pretty.append("\n");

        if (this.request != null) {
            pretty.append("Request: ");
            pretty.append(this.request);
            pretty.append("\n");
        }

        if (this.person != null) {
            pretty.append("User: ");
            pretty.append(this.person);
            pretty.append("\n");
        }

        pretty.append("Time: ");
        pretty.append(this.created.toString());
        pretty.append("\n");

        pretty.append("Tyrion: ");
        pretty.append(this.tyrion);
        pretty.append("\n");

        pretty.append("Repetitions: ");
        pretty.append(this.tyrion);
        pretty.append("\n");

        pretty.append("Stack trace:\n");
        pretty.append(this.stack_trace);

        if (cause_type != null && cause_message != null && cause_stack_trace != null) {
            pretty.append("Caused by: ");
            pretty.append(this.cause_type);
            pretty.append("\n");

            pretty.append("Cause message: ");
            pretty.append(this.cause_message);
            pretty.append("\n");

            pretty.append("Cause stack trace:\n");
            pretty.append(this.cause_stack_trace);
        }

        return pretty.toString();
    }

    public static String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            builder.append("  at ");
            builder.append(element);
            builder.append("\n");
        }
        return builder.toString();
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

    public static Finder<String, Model_ServerError> find = new Finder<>(Model_ServerError.class);
}
