package utilities.hardware;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HardwareOverviewService {

    private final HardwareService hardwareService;

    @Inject
    public HardwareOverviewService(HardwareService hardwareService) {
        this.hardwareService = hardwareService;
    }

    // TODO this class should keep cached state of the hardware for frontend usage, think about robust solution (cache invalidation, etc.)
}
