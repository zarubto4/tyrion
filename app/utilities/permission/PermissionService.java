package utilities.permission;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.ForbiddenException;
import exceptions.NotSupportedException;
import io.ebean.Expr;
import models.*;
import org.ehcache.Cache;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import play.db.ebean.EbeanDynamicEvolutions;
import utilities.cache.CacheService;
import utilities.enums.EntityType;
import exceptions.NotFoundException;
import utilities.logger.Logger;
import utilities.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class PermissionService {

    private static final Logger logger = new Logger(PermissionService.class);

    private Cache<UUID, CachePermissionList> cache;

    @Inject
    public PermissionService(CacheService cacheService, EbeanDynamicEvolutions ebeanDynamicEvolutions) {
        this.cache = cacheService.getCache("PermissionServiceCache", UUID.class, CachePermissionList.class, 500, 3600, true);
        this.setPermissions();
        this.setAdministrator();
    }

    public void checkCreate(Model_Person person, BaseModel model) throws ForbiddenException, NotSupportedException {
        this.check(person, model, Action.CREATE);
    }

    public void checkRead(Model_Person person, BaseModel model) throws ForbiddenException, NotSupportedException {
        this.check(person, model, Action.READ);
    }

    public void checkUpdate(Model_Person person, BaseModel model) throws ForbiddenException, NotSupportedException {
        this.check(person, model, Action.UPDATE);
    }

    public void checkDelete(Model_Person person, BaseModel model) throws ForbiddenException, NotSupportedException {
        this.check(person, model, Action.DELETE);
    }

    public boolean isAdmin(Model_Person person) {
        return Model_Role.find.query().where().eq("persons.id", person.id).eq("name", "SuperAdmin").findCount() > 0; // TODO maybe more robust solution
    }

    /**
     * Grants the given person with the permission.
     * @param person to be granted
     * @param model to grant the permission for
     * @param permission that is granted
     */
    public void grant(Model_Person person, BaseModel model, Action permission) {
        // TODO
    }

    public void revoke(Model_Person person, BaseModel model, Action permission) {
        // TODO
    }

    /**
     * Core method for checking permissions.
     * This method first tries to find a cached permission and if none is present,
     * it will try to look up some in the DB.
     * Method needs to be synchronized, because multiple threads can access it concurrently,
     * which can result it ConcurrentModificationException for example.
     * @param person who want to access the model
     * @param model model that the permission is checked against
     * @param action to be performed on the model
     * @throws ForbiddenException if the access was denied
     * @throws NotSupportedException if the given model does not support permissions
     */
    public synchronized void check(Model_Person person, BaseModel model, Action action) throws ForbiddenException, NotSupportedException {

        if (person == null) {
            throw new NullPointerException("Person argument was null.");
        }

        if (model == null) {
            throw new NullPointerException("Model argument was null.");
        }

        String modelName = model.getClass().getSimpleName();

        if (!(model instanceof Permissible)) {
            throw new NotSupportedException("This model (" + modelName + ") does not support permissions.");
        }

        EntityType entityType = ((Permissible) model).getEntityType();

        logger.debug("check - ({}) checking permission for person id: {}, model id: {}, action: {}", modelName, person.id, model.id, action.name());

        if (!cache.containsKey(person.id)) {
            cache.put(person.id, new CachePermissionList());
        }

        UUID id = null;
        boolean isPublic = false;

        System.out.println("Model je " + model.getClass().getSimpleName());

        if (model instanceof Personal) {
            id = ((Personal) model).getPerson().id;
        } else if (model instanceof Model_Project) {
            id = model.id;
        } else if (model instanceof UnderCustomer) {
            id = ((UnderCustomer) model).getCustomer().id;
        } else if (model instanceof UnderProject) {
            Model_Project project = ((UnderProject) model).getProject();
            if (project != null) {
                id = project.id;
            }
        } else {
            id = model.id;
        }

        if (model instanceof Publishable) {
            isPublic = ((Publishable) model).isPublic();
        }

        // TODO handle public ones better and other operations also
        if (isPublic && action == Action.READ) {
            logger.debug("check - ({}) allowed read for public model", modelName);
            return;
        }

        // id needs to be final
        final UUID lambdaId = id;

        // Try to find cached permission (with same modelId, entityType and action)
        List<CachedPermission> cachedPermissions = cache.get(person.id);
        Optional<CachedPermission> optionalCachedPermission = cachedPermissions
                .stream()
                .filter(cachedPermission -> cachedPermission.entityType == entityType && cachedPermission.action == action
                            && ((lambdaId == null && cachedPermission.modelId == null) || (lambdaId != null && cachedPermission.modelId == null && cachedPermission.permitted) || cachedPermission.modelId == lambdaId)
                )
                .findAny();

        if (optionalCachedPermission.isPresent()) {

            logger.trace("check - ({}) found cached permission", modelName);

            CachedPermission cachedPermission = optionalCachedPermission.get();
            if (!cachedPermission.permitted) {
                throw new ForbiddenException();
            }
        } else {
            try {

                logger.trace("check - ({}) finding the permission in the DB", modelName);

                System.out.println("PermissionService:: Hledám oprávnění podle role:: pro " + person.email);
                System.out.println("PermissionService:: Hledám oprávnění action " + action);
                System.out.println("PermissionService:: Hledám oprávnění entityType " + entityType);
                System.out.println("PermissionService:: Hledám oprávnění project id " + id);
                System.out.println("PermissionService:: Hledám oprávnění person id " +person.id);

                // Try to find the permission in the DB
                Model_Role role = Model_Role.find.query()
                        .where()
                        .eq("persons.id", person.id)
                        .or(
                                Expr.isNull("project"),
                                Expr.eq("project.id", id)
                            )
                        .and(
                                Expr.eq("permissions.action", action),
                                Expr.eq("permissions.entity_type", entityType))
                        .setMaxRows(1)
                        .findOne();

                logger.trace("check - ({}) found role with the permission", modelName);

                if (role.project == null) {

                    System.out.println("PermissionService:: Project je null Role ID " + role.id + "  role.name " + role.name );
                    this.cache.get(person.id).add(new CachedPermission(null, entityType, action, true));

                } else {

                    System.out.println("PermissionService:: Project není null");
                    this.cache.get(person.id).add(new CachedPermission(id, entityType, action, true));
                }

            } catch (NotFoundException e) {

                System.out.println("PermissionService:: not found");

                // If there is no such permission

                if ((model instanceof Personal && resolvePersonal(person, (Personal) model))
                        || (model instanceof UnderCustomer && resolveUnderCustomer(person, (UnderCustomer) model))
                        || (model instanceof Model_Person && person.id.equals(id))
                        || (model instanceof Model_Customer && ((Model_Customer) model).isEmployee(person))) {
                    this.cache.get(person.id).add(new CachedPermission(id, entityType, action, true));
                } else {
                    this.cache.get(person.id).add(new CachedPermission(id, entityType, action, false));
                    throw new ForbiddenException();
                }

            }
        }

        logger.debug("check - ({}) access allowed for person id: {}, model id: {}, action: {}", modelName, person.id, model.id, action.name());
    }

    private boolean resolvePersonal(Model_Person person, Personal personal) {
        return personal.getPerson().id.equals(person.id);
    }

    private boolean resolveUnderCustomer(Model_Person person, UnderCustomer underProduct) {
        return underProduct.getCustomer().isEmployee(person);
    }

    private void setPermissions() {
        long start = System.currentTimeMillis();

        // Get classes in 'models' package
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("models"))
                .setScanners(new SubTypesScanner()));

        // Get classes that implements Permittable
        Set<Class<? extends Permissible>> classes = reflections.getSubTypesOf(Permissible.class);

        logger.trace("setPermissions - found {} classes", classes.size());

        List<Model_Permission> permissions = Model_Permission.find.all();

        classes.forEach(cls -> {
            try {
                Permissible permissible = cls.newInstance();
                EntityType entityType = permissible.getEntityType();
                List<Action> actions = permissible.getSupportedActions();

                actions.forEach(action -> {
                    if (permissions.stream().noneMatch(p -> p.action == action && p.entity_type == entityType)) {
                        Model_Permission permission = new Model_Permission();
                        permission.entity_type = entityType;
                        permission.action = action;
                        permission.save();
                    }
                });

            } catch (Exception e) {
                logger.internalServerError(e);
            }
        });

        logger.trace("setPermissions - scanning for permissions took: {} ms", System.currentTimeMillis() - start);

        // Set default project roles (temporary)
        List<Model_Project> projects = Model_Project.find.query().where().isEmpty("roles").findList();
        projects.forEach(project -> {

            List<Model_Person> persons = Model_Person.find.query().where().eq("projects.id", project.id).findList();
            Model_Role adminRole = Model_Role.createProjectAdminRole();
            adminRole.project = project;
            if(adminRole.persons == null) adminRole.persons = new ArrayList<>();
            adminRole.persons.addAll(persons);
            adminRole.save();

            Model_Role memberRole = Model_Role.createProjectMemberRole();
            memberRole.project = project;
            memberRole.save();
        });
    }

    private void setAdministrator() {
        // For Developing
        Model_Role role;

        try {
            role = Model_Role.getByName("SuperAdmin");

            logger.trace("setAdministrator - role SuperAdmin exists");

        } catch (NotFoundException e) {

            logger.warn("setAdministrator - SuperAdmin role was not found, creating it");

            role = new Model_Role();
            role.name = "SuperAdmin";
            role.save();
        }

        logger.info("setAdministrator - updating permissions in the role");

        List<UUID> permissionIds = role.permissions.stream().map(permission -> permission.id).collect(Collectors.toList());

        logger.trace("setAdministrator - role contains {} permission(s)", permissionIds.size());

        List<Model_Permission> permissions = Model_Permission.find.query().where().notIn("id", permissionIds).findList();

        logger.trace("setAdministrator - role is missing {} permission(s)", permissions.size());

        if (!permissions.isEmpty()) {
            logger.debug("setAdministrator - adding {} permission(s)", permissions.size());
            role.permissions.addAll(permissions);
            role.update();
        }

        Model_Person person;

        try {
            person = Model_Person.getByEmail("admin@byzance.cz");

            logger.trace("setAdministrator - admin is already created");

        } catch (NotFoundException e) {

            logger.warn("setAdministrator - creating first admin account: admin@byzance.cz, password: 123456789");

            person = new Model_Person();
            person.first_name = "Admin";
            person.last_name = "Byzance";
            person.validated = true;
            person.nick_name = "Syndibád";
            person.email = "admin@byzance.cz";
            person.setPassword("123456789");
            person.save();
        }

        if (!role.persons.contains(person)) {
            logger.info("setAdministrator - adding admin account to role");
            role.persons.add(person);
            role.update();
        }
    }
}
