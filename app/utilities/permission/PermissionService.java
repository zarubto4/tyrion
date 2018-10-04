package utilities.permission;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.ForbiddenException;
import exceptions.NotSupportedException;
import io.ebean.Expr;
import models.Model_Customer;
import models.Model_Person;
import models.Model_Project;
import models.Model_Role;
import org.ehcache.Cache;
import utilities.cache.ServerCache;
import utilities.enums.EntityType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.logger.Logger;
import utilities.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class PermissionService {

    private static final Logger logger = new Logger(PermissionService.class);

    private Cache<UUID, CachePermissionList> cache;

    @Inject
    public PermissionService(ServerCache serverCache) {
        this.cache = serverCache.getCache("PermissionServiceCache", UUID.class, CachePermissionList.class, 500, 3600, true);
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

        if (model instanceof Personal) {
            id = ((Personal) model).getPerson().id;
        } else if (model instanceof UnderCustomer) {
            id = ((UnderCustomer) model).getCustomer().id;
        } else if (model instanceof UnderProject) {

            Model_Project project = ((UnderProject) model).getProject();
            if (project != null) {
                id = project.id;
            } else if (model instanceof Publishable) {
                isPublic = ((Publishable) model).isPublic();
            }

        } else {
            id = model.id;
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

                // Try to find the permission in the DB
                Model_Role role = Model_Role.find.query()
                        .where()
                        .eq("persons.id", person.id)
                        .or(Expr.isNull("project"), Expr.eq("project.id", id))
                        .and(Expr.eq("permissions.action", action), Expr.eq("permissions.entity_type", entityType))
                        .setMaxRows(1)
                        .findOne();

                logger.trace("check - ({}) found role with the permission", modelName);

                if (role.project == null) {
                    this.cache.get(person.id).add(new CachedPermission(null, entityType, action, true));
                } else {
                    this.cache.get(person.id).add(new CachedPermission(id, entityType, action, true));
                }

            } catch (Result_Error_NotFound e) {

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
}
