package utilities.hardware;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import exceptions.FailedMessageException;
import exceptions.NeverConnectedException;
import exceptions.ServerOfflineException;
import models.Model_Hardware;
import models.Model_HardwareType;
import models.Model_HomerServer;
import play.libs.concurrent.HttpExecutionContext;
import utilities.document_mongo_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.enums.FirmwareType;
import utilities.enums.NetworkStatus;
import utilities.enums.UpdateType;
import utilities.hardware.update.UpdateService;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import utilities.synchronization.SynchronizationService;
import websocket.WebSocketService;
import websocket.interfaces.Homer;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Model_Hardware_Temporary_NotDominant_record;

import java.util.UUID;

public class HardwareService {

    private static final Logger logger = new Logger(HardwareService.class);

    private final HttpExecutionContext httpExecutionContext;
    private final WebSocketService webSocketService;
    private final DominanceService dominanceService;
    private final NetworkStatusService networkStatusService;
    private final Provider<UpdateService> updateService;

    @Inject
    public HardwareService(WebSocketService webSocketService, Provider<UpdateService> updateService, DominanceService dominanceService,
                           HttpExecutionContext httpExecutionContext, NetworkStatusService networkStatusService) {
        this.httpExecutionContext = httpExecutionContext;
        this.webSocketService = webSocketService;
        this.dominanceService = dominanceService;
        this.updateService = updateService;
        this.networkStatusService = networkStatusService;
    }

    public HardwareInterface getInterface(Model_Hardware hardware) {
        Model_HomerServer server = hardware.get_connected_server();
        if (server != null) {
            Homer homer = this.webSocketService.getInterface(server.id);
            if (homer != null) {
                return new HardwareInterface(hardware, homer, this.httpExecutionContext);
            } else {
                throw new ServerOfflineException();
            }
        } else {
            throw new NeverConnectedException();
        }
    }

    public HardwareConfigurator getConfigurator(Model_Hardware hardware) {

        HardwareInterface hardwareInterface = null;

        try {
            hardwareInterface = this.getInterface(hardware);
        } catch (ServerOfflineException e) {
            // nothing - hardware is unreachable at this moment
        }

        return new HardwareConfigurator(hardware, hardwareInterface, this);
    }

    public void activate(Model_Hardware hardware) {
        WS_Model_Hardware_Temporary_NotDominant_record record = this.dominanceService.getNondominant(hardware.full_id);
        if (this.dominanceService.setDominant(hardware)) {
            this.networkStatusService.setStatus(hardware, NetworkStatus.OFFLINE);
            try {
                if (record != null) {
                    this.getInterface(hardware).changeUUIDOnServer(record.random_temporary_hardware_id)
                            .whenComplete((message, exception) -> {
                                if (exception != null) {
                                    logger.internalServerError(exception);
                                }
                            });
                }
            } catch (NeverConnectedException e) {
                this.networkStatusService.setStatus(hardware, NetworkStatus.NOT_YET_FIRST_CONNECTED);
            } catch (ServerOfflineException e) {
                this.networkStatusService.setStatus(hardware, NetworkStatus.UNKNOWN_LOST_CONNECTION_WITH_SERVER);
            }

            // TODO send echo

            hardware.make_log_activated(); // TODO injection
        } else {
            // TODO notification - cannot be activated
        }
    }

    /**
     * Call for set default firmware on Hardware (probably aprove from notification from users)
     * @param hardware
     */
    public void setDefaultFirmware(Model_Hardware hardware) {
        DM_Board_Bootloader_DefaultConfig config = hardware.bootloader_core_configuration();
        config.decision_for_default_firmware = true;
        hardware.update_bootloader_configuration(config);

        Model_HardwareType type = hardware.hardware_type;
        if( type.main_c_program() != null && type.main_c_program().default_main_version != null) {
            this.updateService.get().update(hardware, type.main_c_program().default_main_version, FirmwareType.FIRMWARE, UpdateType.MANUALLY_BY_USER_INDIVIDUAL);
        } else {

            config = hardware.bootloader_core_configuration();
            config.decision_for_default_firmware = false;
            hardware.update_bootloader_configuration(config);

        }
    }

    /**
     * Reject request to set to default firmware
     */
    public void rejectDefaultFirmware(Model_Hardware hardware) {
        DM_Board_Bootloader_DefaultConfig config = hardware.bootloader_core_configuration();
        config.decision_for_default_firmware = false;
        hardware.update_bootloader_configuration(config);

    }

    public void deactivate(Model_Hardware hardware) {
        this.dominanceService.setNondominant(hardware);
        this.networkStatusService.setStatus(hardware, NetworkStatus.FREEZED);
        try {
            HardwareInterface hardwareInterface = this.getInterface(hardware);
            hardwareInterface.changeUUIDOnServer(hardware.getId(), this.dominanceService.rememberNondominant(hardware.full_id, hardware.connected_server_id))
                    .whenComplete((message, exception) -> {
                        if (exception != null) {
                            logger.internalServerError(exception);
                        } else {
                            logger.info("deactivate - id changed on homer server");
                        }
                    });
        } catch (NeverConnectedException|ServerOfflineException e) {
            // nothing
        }
        // TODO send echo
    }
}
