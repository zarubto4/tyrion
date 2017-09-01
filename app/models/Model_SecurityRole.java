
package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.Controller_Security;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import utilities.logger.Class_Logger;
import utilities.swagger.outboundClass.Swagger_C_program_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Person_Middle_Detail;
import utilities.swagger.outboundClass.Swagger_Person_Short_Detail;
import utilities.swagger.outboundClass.Swagger_Role_Short_Detail;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@ApiModel(value = "SecurityRole", description = "Model of SecurityRole")
@Table(name="SecurityRole")
public class Model_SecurityRole extends Model {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Model_SecurityRole.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/
    @Id @ApiModelProperty(required = true) public String id;
        @ApiModelProperty(required = true) public String name;
        @ApiModelProperty(required = true) public String description;

    @JsonIgnore @ManyToMany(mappedBy = "roles",fetch = FetchType.LAZY)  @JoinTable(name = "person_roles") public List<Model_Person> persons = new ArrayList<>();
    @ManyToMany() @OrderBy(value ="permission_key") public List<Model_Permission> person_permissions = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

    @JsonProperty @Transient @ApiModelProperty(required = true) public List<Swagger_Person_Middle_Detail> persons() {  List<Swagger_Person_Middle_Detail> l = new ArrayList<>();  for( Model_Person m  : persons)   l.add(m.get_private_short_person()); return l;  }

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @Transient @JsonIgnore public Swagger_Role_Short_Detail get_group_short_detail(){

        try {
            Swagger_Role_Short_Detail help = new Swagger_Role_Short_Detail();

            help.id = id;
            help.name = name;
            help.description = description;

            help.delete_permission = delete_permission();
            help.update_permission = update_permission();

            return help;
        }catch (Exception e){
            terminal_logger.internalServerError(e);
            return null;

        }
    }

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public void save() {

        terminal_logger.debug("save :: Creating new Object");
        while (true) { // I need Unique Value
            this.id = UUID.randomUUID().toString();
            if (Model_SecurityRole.find.byId(this.id) == null) break;
        }
        super.save();
    }

    @JsonIgnore @Override public void update() {

        terminal_logger.debug("update :: Update object value: {}",  this.id);
        super.update();

    }

    @JsonIgnore @Override public void delete() {

        this.persons = null;
        this.update();

        this.refresh();
        super.delete();

        terminal_logger.internalServerError(new Exception("This object is not legitimate to remove."));
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore   @Transient                                    public boolean create_permission(){  return  Controller_Security.get_person().has_permission("SecurityRole_create"); }
    @JsonIgnore   @Transient                                    public boolean read_permission()  {  return  Controller_Security.get_person().has_permission("SecurityRole_read"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean update_permission(){  return  Controller_Security.get_person().has_permission("SecurityRole_update"); }
    @JsonProperty @Transient @ApiModelProperty(required = true) public boolean delete_permission(){  return  Controller_Security.get_person().has_permission("SecurityRole_delete");}

    public enum permissions{SecurityRole_create, SecurityRole_read, SecurityRole_update , SecurityRole_delete}


/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public static Model_SecurityRole get_byId(String id) {

        terminal_logger.warn("CACHE is not implemented - TODO");
        return find.byId(id);

    }


/* FINDER --------------------------------------------------------------------------------------------------------------*/

    public static Model_SecurityRole findByName(String name) {return find.where().eq("name", name).findUnique();}
    public static final Model.Finder<String, Model_SecurityRole> find = new Finder<>(Model_SecurityRole.class);
}
