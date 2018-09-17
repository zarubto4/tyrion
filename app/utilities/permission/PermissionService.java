package utilities.permission;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.ForbiddenException;
import exceptions.NotSupportedException;
import io.ebean.Expr;
import models.Model_Person;
import models.Model_Project;
import models.Model_Role;
import org.ehcache.Cache;
import utilities.cache.ServerCache;
import utilities.enums.EntityType;
import utilities.errors.Exceptions.Result_Error_NotFound;
import utilities.logger.Logger;
import utilities.model.BaseModel;
import utilities.model.Personal;
import utilities.model.UnderProject;

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
     * @param person
     * @param model
     * @param action
     * @throws ForbiddenException
     * @throws NotSupportedException
     */
    public void check(Model_Person person, BaseModel model, Action action) throws ForbiddenException, NotSupportedException {

        if (person == null) {
            throw new NullPointerException("Person argument was null.");
        }

        if (model == null) {
            throw new NullPointerException("Model argument was null.");
        }

        if (!(model instanceof Permissible)) {
            throw new NotSupportedException("This model does not support permissions.");
        }

        EntityType entityType = ((Permissible) model).getEntityType();

        logger.debug("check - checking permission for type: {}, person id: {}, model id: {}, action: {}", entityType.name(), person.id, model.id, action.name());

        if (!cache.containsKey(person.id)) {
            cache.put(person.id, new CachePermissionList());
        }

        UUID id = null;

        if (model instanceof Personal) {
            id = ((Personal) model).getPerson().id;
        } else if (model instanceof UnderProject) {
            id = ((UnderProject) model).getProject().id;
        } else if (model instanceof Model_Project) {
            id = model.id;
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

            logger.trace("check - found cached permission");

            CachedPermission cachedPermission = optionalCachedPermission.get();
            if (!cachedPermission.permitted) {
                throw new ForbiddenException();
            }
        } else {
            try {

                logger.trace("check - finding the permission in the DB");

                // Try to find the permission in the DB
                Model_Role role = Model_Role.find.query()
                        .where()
                        .eq("persons.id", person.id)
                        .or(Expr.isNull("project"), Expr.eq("project.id", id))
                        .and(Expr.eq("permissions.action", action), Expr.eq("permissions.entity_type", entityType))
                        .setMaxRows(1)
                        .findOne();

                if (role.project == null) {
                    this.cache.get(person.id).add(new CachedPermission(null, entityType, action, true));
                } else {
                    this.cache.get(person.id).add(new CachedPermission(model.id, entityType, action, true));
                }

            } catch (Result_Error_NotFound e) {

                // If there is no such permission

                if (model instanceof Personal && ((Personal) model).getPerson().id.equals(person.id)) {
                    this.cache.get(person.id).add(new CachedPermission(model.id, entityType, action, true));
                } else {

                    this.cache.get(person.id).add(new CachedPermission(model.id, entityType, action, false));
                    throw new ForbiddenException();
                }
            }
        }

        logger.debug("check - access allowed for type: {}, person id: {}, model id: {}, action: {}", entityType.name(), person.id, model.id, action.name());
    }
}
