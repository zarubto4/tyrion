package utilities.hardware;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.inject.Inject;
import models.Model_Hardware;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_overview_Board;

import java.io.IOException;

public class IpAddressSerializer extends StdSerializer<String> {

    private final HardwareOverviewService hardwareOverviewService;

    @Inject
    public IpAddressSerializer(HardwareOverviewService hardwareOverviewService) {
        super(String.class);
        this.hardwareOverviewService = hardwareOverviewService;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (gen.getCurrentValue() instanceof Model_Hardware) {

            WS_Message_Hardware_overview_Board overview = this.hardwareOverviewService.getOverview((Model_Hardware) gen.getCurrentValue());

            if (overview != null) {
                gen.writeString(overview.ip);
                return;
            }
        }

        gen.writeNull();
    }
}
