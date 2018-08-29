package utilities.permission;

import exceptions.ForbiddenException;
import models.Model_Permission;
import models.Model_Person;
import org.ehcache.Cache;
import utilities.model.BaseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PermissionManager {

    private Cache<UUID, List<CachedPermission>> cache;

    public void check(Model_Person person, BaseModel model, Permission permission) throws ForbiddenException {

        if (person == null) {
            throw new NullPointerException("Person argument was null.");
        }

        if (model == null) {
            throw new NullPointerException("Model argument was null.");
        }

        if (!cache.containsKey(person.id)) {
            cache.put(person.id, new ArrayList<>());
        }

        List<CachedPermission> cachedPermissions = cache.get(person.id);
        Optional<CachedPermission> optionalCachedPermission = cachedPermissions.stream().filter(cachedPermission -> cachedPermission.modelId == model.id && cachedPermission.permission == permission).findAny();
        if (optionalCachedPermission.isPresent()) {
            CachedPermission cachedPermission = optionalCachedPermission.get();
            if (!cachedPermission.permitted) {
                throw new ForbiddenException();
            }
        } else {
            Model_Permission modelPermission = Model_Permission.find.query().where().eq("person.id", person.id).eq("permission", permission).findOne();

        }
    }
}
