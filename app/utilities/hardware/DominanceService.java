package utilities.hardware;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.NotFoundException;
import models.Model_Hardware;
import org.ehcache.Cache;
import utilities.cache.CacheService;

import java.util.UUID;

@Singleton
public class DominanceService {

    private final Cache<UUID, Boolean> cache;

    @Inject
    public DominanceService(CacheService cacheService) {
        this.cache = cacheService.getCache("Dominance", UUID.class, Boolean.class, 500, 3600, true);
    }

    public Model_Hardware getDominant(String fullId) {
        Model_Hardware hardware = Model_Hardware.find.query().nullable().where().eq("full_id", fullId).eq("dominant_entity", true).findOne();
        if (hardware != null) {
            this.cache.put(hardware.id, true);
        }
        return hardware;
    }

    public boolean setDominant(Model_Hardware hardware) {
        if (Model_Hardware.find.query().nullable().where().eq("full_id", hardware.full_id).eq("dominant_entity", true).findCount() > 0) {
            return false;
        } else {
            hardware.dominant_entity = true;
            hardware.update();
            this.cache.put(hardware.id, true);
            return true;
        }
    }

    public void setNondominant(Model_Hardware hardware) {

        hardware.dominant_entity = false;
        hardware.update();
        this.cache.put(hardware.id, false);
    }

    public boolean isDominant(UUID id) {
        if (this.cache.containsKey(id)) {
            return this.cache.get(id);
        } else {
            try {
                Model_Hardware hardware = Model_Hardware.find.byId(id);
                this.cache.put(id, hardware.dominant_entity);
                return hardware.dominant_entity;
            } catch (NotFoundException e) {
                this.cache.put(id, false);
                return false;
            }
        }
    }
}
