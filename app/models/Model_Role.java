package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Expr;
import io.swagger.annotations.ApiModel;
import utilities.cache.CacheFinder;
import utilities.cache.CacheFinderField;
import utilities.enums.EntityType;
import utilities.logger.Logger;
import utilities.model.NamedModel;
import utilities.model.Publishable;
import utilities.model.UnderProject;
import utilities.permission.Action;
import utilities.permission.Permissible;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@ApiModel(value = "Role", description = "Model of Role")
@Table(name="Role")
public class Model_Role extends NamedModel implements Permissible, UnderProject, Publishable {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_Role.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @ManyToOne public Model_Project project;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY) public List<Model_Person> persons = new ArrayList<>();
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }) public List<Model_Permission> permissions = new ArrayList<>();

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public Model_Project getProject() {
        return isLoaded("project") ? project : Model_Project.find.query().nullable().where().eq("roles.id", id).findOne();
    }

    @JsonIgnore @Override
    public boolean isPublic() {
        return getProject() == null;
    }

    /* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public boolean delete() {

        this.persons = null;
        this.update();

        this.refresh();
        return super.delete();
    }

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION Description ----------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Override
    public EntityType getEntityType() {
        return EntityType.ROLE;
    }

    @JsonIgnore @Override
    public List<Action> getSupportedActions() {
        return Arrays.asList(Action.CREATE, Action.READ, Action.UPDATE, Action.DELETE);
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

    public static Model_Role getByName(String name) {
        return find.query().where().eq("name" , name).findOne();
    }

    public static Model_Role createProjectAdminRole() {

        List<Model_Permission> permissions = Model_Permission.find.query()
                .where()
                .in("entity_type", EntityType.PROJECT, EntityType.FIRMWARE, EntityType.FIRMWARE_VERSION,
                        EntityType.LIBRARY, EntityType.LIBRARY_VERSION, EntityType.WIDGET, EntityType.WIDGET_VERSION,
                        EntityType.GRID_PROJECT, EntityType.GRID_PROGRAM, EntityType.GRID_PROGRAM_VERSION,
                        EntityType.BLOCK, EntityType.BLOCK_VERSION, EntityType.BLOCKO_PROGRAM, EntityType.BLOCKO_PROGRAM_VERSION,
                        EntityType.INSTANCE, EntityType.INSTANCE_SNAPSHOT, EntityType.HARDWARE, EntityType.HARDWARE_GROUP,
                        EntityType.HARDWARE_UPDATE, EntityType.ROLE
                )
                .findList();


        Model_Role role = new Model_Role();
        role.name = "Project Admin";
        role.permissions.addAll(permissions);

        return role;
    }

    public static Model_Role createProjectMemberRole() {

        List<Model_Permission> permissions = Model_Permission.find.query()
                .where()
                .not(Expr.and(Expr.eq("entity_type", EntityType.PROJECT), Expr.in("action", Arrays.asList(Action.ACTIVATE, Action.INVITE))))
                .in("entity_type", EntityType.PROJECT, EntityType.FIRMWARE, EntityType.FIRMWARE_VERSION,
                        EntityType.LIBRARY, EntityType.LIBRARY_VERSION, EntityType.WIDGET, EntityType.WIDGET_VERSION,
                        EntityType.GRID_PROJECT, EntityType.GRID_PROGRAM, EntityType.GRID_PROGRAM_VERSION,
                        EntityType.BLOCK, EntityType.BLOCK_VERSION, EntityType.BLOCKO_PROGRAM, EntityType.BLOCKO_PROGRAM_VERSION,
                        EntityType.INSTANCE, EntityType.INSTANCE_SNAPSHOT, EntityType.HARDWARE, EntityType.HARDWARE_GROUP,
                        EntityType.HARDWARE_UPDATE
                )
                .findList();

        Model_Role role = new Model_Role();
        role.name = "Project Member";
        role.permissions.addAll(permissions);

        return role;
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @CacheFinderField(Model_Role.class)
    public static CacheFinder<Model_Role> find = new CacheFinder<>(Model_Role.class);
}
