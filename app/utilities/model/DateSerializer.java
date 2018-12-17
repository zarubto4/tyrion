package utilities.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.inject.Inject;
import utilities.logger.Logger;
import utilities.network.NetworkStatusService;
import utilities.network.Networkable;

import java.io.IOException;
import java.util.Date;

/**
 *
 */
public class DateSerializer extends StdSerializer<Date>{

    private static final Logger logger = new Logger(DateSerializer.class);

    public DateSerializer() {
        super(Date.class);
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumber(value.getTime() / 1000);
    }
}
