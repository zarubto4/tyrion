package utilities.hardware;

import com.google.inject.Inject;
import models.Model_Hardware;

import java.util.UUID;

public class IdConverter {

    private final DominanceService dominanceService;

    @Inject
    public IdConverter(DominanceService dominanceService) {
        this.dominanceService = dominanceService;
    }

    public UUID convert(String fullId) {
        Model_Hardware hardware = this.dominanceService.getDominant(fullId);
        if (hardware != null) {
            return hardware.id;
        } else {
            return null;
        }
    }
}
