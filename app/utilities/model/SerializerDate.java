package utilities.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import utilities.logger.Logger;

import java.io.IOException;
import java.util.Date;

/**
 *
 */
public class SerializerDate extends StdSerializer<Date>{

    private static final Logger logger = new Logger(SerializerDate.class);

    public SerializerDate() {
        super(Date.class);
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumber(value.getTime() / 1000);
    }
}
