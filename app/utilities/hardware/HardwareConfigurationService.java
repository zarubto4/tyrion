package utilities.hardware;

import com.google.inject.Inject;
import exceptions.ServerOfflineException;
import models.Model_Hardware;
import utilities.document_mongo_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.notifications.NotificationService;

public class HardwareConfigurationService {

    private final HardwareService hardwareService;
    private final NotificationService notificationService;

    @Inject
    public HardwareConfigurationService(HardwareService hardwareService, NotificationService notificationService) {
        this.hardwareService = hardwareService;
        this.notificationService = notificationService;
    }

    public HardwareConfigurator getConfigurator(Model_Hardware hardware) {

        HardwareInterface hardwareInterface = null;

        try {
            hardwareInterface = this.hardwareService.getInterface(hardware);
        } catch (ServerOfflineException e) {
            // nothing - hardware is unreachable at this moment
        }

        return new HardwareConfigurator(hardware, hardwareInterface);
    }

    public void configured(Model_Hardware hardware, String parameter) {

        DM_Board_Bootloader_DefaultConfig configuration = hardware.bootloader_core_configuration();

        configuration.pending.remove(parameter.toLowerCase());

        hardware.update_bootloader_configuration(configuration);

        this.notificationService.modelUpdated(Model_Hardware.class, hardware.getId(), hardware.get_project_id());
    }
}
