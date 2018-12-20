package utilities.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import utilities.logger.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 *
 */
public class SerializerLocalDateTime extends StdSerializer<LocalDateTime>{

    private static final Logger logger = new Logger(SerializerLocalDateTime.class);

    public SerializerLocalDateTime() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumber(value.toEpochSecond(ZoneOffset.UTC));
    }

}
