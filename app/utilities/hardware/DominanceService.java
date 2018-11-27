package utilities.hardware;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.NotFoundException;
import models.Model_Hardware;
import org.ehcache.Cache;
import utilities.cache.CacheService;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Model_Hardware_Temporary_NotDominant_record;

import java.util.UUID;

@Singleton
public class DominanceService {

    private final Cache<UUID, Boolean> cache;
    private final Cache<String, WS_Model_Hardware_Temporary_NotDominant_record> cacheTemporary;

    @Inject
    public DominanceService(CacheService cacheService) {
        this.cache = cacheService.getCache("Dominance", UUID.class, Boolean.class, 500, 3600, true);
        this.cacheTemporary = cacheService.getCache("TemporaryId", String.class, WS_Model_Hardware_Temporary_NotDominant_record.class, 500, 360000, true);
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

            if (this.cacheTemporary.containsKey(hardware.full_id)) {
                hardware.connected_server_id = this.cacheTemporary.get(hardware.full_id).homer_server_id;
                this.cacheTemporary.remove(hardware.full_id);
            }

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

    public UUID rememberNondominant(String fullId, UUID serverId) {
        Model_Hardware hardware = this.getDominant(fullId);
        if (hardware != null) {
            return hardware.id;
        } else {
            WS_Model_Hardware_Temporary_NotDominant_record record;
            if (this.cacheTemporary.containsKey(fullId)) {
                record = this.cacheTemporary.get(fullId);
                record.homer_server_id = serverId;
            } else {
                record = new WS_Model_Hardware_Temporary_NotDominant_record();
                record.homer_server_id = serverId;
                record.random_temporary_hardware_id = UUID.randomUUID();
                this.cacheTemporary.put(fullId, record);
            }

            return record.random_temporary_hardware_id;
        }
    }

    public boolean hasNondominant(String fullId) {
        return this.cacheTemporary.containsKey(fullId);
    }

    public WS_Model_Hardware_Temporary_NotDominant_record getNondominant(String fullId) {
        return this.cacheTemporary.get(fullId);
    }
}
