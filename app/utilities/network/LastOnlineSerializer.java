package utilities.network;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.inject.Inject;
import utilities.logger.Logger;

import java.io.IOException;

/**
 * This class will populate network status fields in serialized json.
 */
public class LastOnlineSerializer extends StdSerializer<Long> {

    private static final Logger logger = new Logger(LastOnlineSerializer.class);

    private final NetworkStatusService networkStatusService;

    @Inject
    public LastOnlineSerializer(NetworkStatusService networkStatusService) {
        super(Long.class);
        this.networkStatusService = networkStatusService;
    }

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (gen.getCurrentValue() instanceof Networkable) {
            Long last = this.networkStatusService.getLastOnline((Networkable) gen.getCurrentValue());
            if (last != null && last > 0) {
                gen.writeNumber(last);
            } else {
                gen.writeNull();
            }
        } else if (!gen.canOmitFields()) {
            logger.trace("serialize - field cannot be omitted");
            gen.writeNumber(0);
        } else {
            gen.writeNull();
        }
    }
}
