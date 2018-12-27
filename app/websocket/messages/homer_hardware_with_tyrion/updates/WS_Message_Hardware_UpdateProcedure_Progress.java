package websocket.messages.homer_hardware_with_tyrion.updates;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.Inject;
import exceptions.BadRequestException;
import models.Model_Hardware;
import models.Model_HardwareUpdate;
import models.Model_Project;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.libs.Json;
import utilities.enums.Enum_HardwareHomerUpdate_state;
import utilities.enums.FirmwareType;
import utilities.enums.HardwareUpdateState;
import utilities.errors.ErrorCode;
import utilities.hardware.HardwareService;
import utilities.hardware.update.UpdateService;
import utilities.homer.HomerService;
import utilities.models_update_echo.EchoHandler;
import utilities.notifications.NotificationService;
import utilities.scheduler.SchedulerService;
import websocket.messages.common.abstract_class.WS_AbstractMessage_Instance;
import websocket.messages.tyrion_with_becki.WSM_Echo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Constraints.Validate
public class WS_Message_Hardware_UpdateProcedure_Progress extends WS_AbstractMessage_Instance implements  Constraints.Validatable<List<ValidationError>>  {

    // MessageType
    @JsonIgnore
    public static final String message_type = "update_hardware_progress";

    /* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    @Constraints.Required
    public UUID tracking_id = null;

    @Constraints.Required
    public UUID tracking_group_id = null;


    public Integer percentage_progress = null;
    public String phase = null;
    public String full_id = null;


    public Enum_HardwareHomerUpdate_state get_phase() {
        return Enum_HardwareHomerUpdate_state.get_state(phase);
    }

    /* Validate Response  -------------------------------------------------------------------------------------------------------*/

    @Override
    public List<ValidationError> validate() throws BadRequestException {

        List<ValidationError> errors = new ArrayList<>();

        if(error_code == null || error_message == null ) {
            return errors;
        }


        if(phase == null) {
            errors.add(new ValidationError("phase", "Its required if message is not error!"));
        }

        if(percentage_progress == null) {
            errors.add(new ValidationError("percentage_progress", "Its required if message is not error!"));
        }

        return errors;

    }


}