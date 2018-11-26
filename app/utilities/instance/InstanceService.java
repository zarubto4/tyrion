package utilities.instance;

import com.google.inject.Inject;
import exceptions.ServerOfflineException;
import models.Model_Hardware;
import models.Model_HomerServer;
import models.Model_Instance;
import models.Model_InstanceSnapshot;
import utilities.homer.HomerInterface;
import utilities.homer.HomerService;
import utilities.logger.Logger;
import utilities.models_update_echo.EchoHandler;
import websocket.WebSocketService;
import websocket.interfaces.Homer;
import websocket.messages.homer_hardware_with_tyrion.helps_objects.WS_Message_Homer_Hardware_ID_UUID_Pair;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import java.util.ArrayList;
import java.util.List;

public class InstanceService {

    private static final Logger logger = new Logger(InstanceService.class);

    private final WebSocketService webSocketService;
    private final HomerService homerService;

    @Inject
    public InstanceService(WebSocketService webSocketService, HomerService homerService) {
        this.webSocketService = webSocketService;
        this.homerService = homerService;
    }

    public InstanceInterface getInterface(Model_Instance instance) {
        Model_HomerServer server = instance.getServer();

        Homer homer = this.webSocketService.getInterface(server.id);
        if (homer != null) {
            return new InstanceInterface(instance, homer);
        } else {
            throw new ServerOfflineException();
        }
    }

    public void deploy(Model_InstanceSnapshot snapshot) {

        Model_Instance instance = snapshot.getInstance();
        instance.current_snapshot_id = snapshot.id;
        instance.update();

        HomerInterface homerInterface = this.homerService.getInterface(instance.getServer());
        homerInterface.addInstance(instance); // TODO only if it does not exist

        notification_instance_set_wait_for_server(person);

        InstanceInterface instanceInterface = this.getInterface(instance);
        instanceInterface.setProgram();

        List<Model_Hardware> hardware = snapshot.getRequiredHardware();

        List<WS_Message_Homer_Hardware_ID_UUID_Pair> pairs = new ArrayList<>();

        hardware.forEach(hw -> {
            WS_Message_Homer_Hardware_ID_UUID_Pair pair = new WS_Message_Homer_Hardware_ID_UUID_Pair();
            pair.full_id = hw.full_id;
            pair.uuid = hw.id.toString(); // Must be string!
            pairs.add(pair);
        });

        instanceInterface.setHardware(pairs);
        // TODO instanceInterface.setTerminals();


        // TODO WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Instance.class, get_instance_id(), true, instance.getProjectId());

        // Only if there are hardware for update
        if (instance.current_snapshot().getProgram().interfaces.size() > 0) {
            this.override_all_actualization_hardware_request();
            this.create_and_start_actualization_hardware_request();

        }

        // TODO WS_Message_Online_Change_status.synchronize_online_state_with_becki_project_objects(Model_Hardware.class, get_instance_id(), true, instance.getProjectId());

        logger.warn("Sending Update for Instance ID: {}", this.id);
        new Thread(() -> EchoHandler.addToQueue(new WSM_Echo(Model_Instance.class, instance.getProject().id, get_instance_id()))).start();

    }

    public void shutdown() {

    }
}
