package models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.BaseController;
import io.ebean.Finder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import play.mvc.Http;
import utilities.authentication.Attributes;
import utilities.model.NamedModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Optional;
import java.util.UUID;

@Entity
@ApiModel(value = "ServerError", description = "Model of ServerError")
@Table(name = "ServerError")
public class Model_ServerError extends NamedModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
                                        public String type;
    @Column(columnDefinition = "TEXT")  public String message;
    @Column(columnDefinition = "TEXT")  public String stack_trace;

                                        public String request;
                                        public String person;
                                        public String tyrion;
                                        public Long repetition;

                                        public String cause_type;
    @Column(columnDefinition = "TEXT")  public String cause_message;
    @Column(columnDefinition = "TEXT")  public String cause_stack_trace;

                                        public String youtrack_url;

/* JSON PROPERTY VALUES ------------------------------------------------------------------------------------------------*/

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    public Model_ServerError(Throwable exception, String origin, Http.RequestHeader request) {
        super();
        this.type = exception.getClass().getName();
        this.message = exception.getMessage();
        this.stack_trace = formatStackTrace(exception.getStackTrace());

        Throwable cause = exception.getCause();
        if (cause != null) {
            this.cause_type = cause.getClass().getName();
            this.cause_message = cause.getMessage();
            this.cause_stack_trace = formatStackTrace(cause.getStackTrace());
        }

        this.name = origin;
        this.repetition = 1L;
        // this.tyrion = Server.server_version + " (" + Server.server_mode.name() + ")";

        if (request != null) {
            this.request = request.method() + " " + request.path();
            Optional<Model_Person> person = request.attrs().getOptional(Attributes.PERSON);
            person.ifPresent(p -> this.person = p.email);
        }
    }

    @JsonIgnore @Override
    public String prettyPrint() {
        StringBuilder pretty = new StringBuilder();

        pretty.append("Error at: ");
        pretty.append(this.name);
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
        pretty.append(this.created);
        pretty.append("\n");

        pretty.append("Tyrion: ");
        pretty.append(this.tyrion);
        pretty.append("\n");

        pretty.append("Repetitions: ");
        pretty.append(this.repetition);
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

    @JsonIgnore
    public boolean read_permission() { return BaseController.person().has_permission("ServerError_read");}
    
    @JsonProperty @ApiModelProperty(required = true)
    public boolean edit_permission()  {  return BaseController.person().has_permission("ServerError_edit");}
    
    @JsonProperty @ApiModelProperty(required = true)
    public boolean delete_permission() {  return BaseController.person().has_permission("ServerError_delete");}

    public enum Permission { ServerError_read, ServerError_edit, ServerError_delete }


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_ServerError getById(String id) {
        return getById(UUID.fromString(id));
    }
    
    public static Model_ServerError getById(UUID id) {
        return find.byId(id);
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Finder<UUID, Model_ServerError> find = new Finder<>(Model_ServerError.class);
}
