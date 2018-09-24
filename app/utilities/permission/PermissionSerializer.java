package utilities.permission;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import utilities.model.BaseModel;

import java.io.IOException;

public class PermissionSerializer extends StdSerializer<Boolean> {

    public PermissionSerializer() {
        super(Boolean.class);
    }

    @Override
    public void serialize(Boolean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (gen.getCurrentValue() instanceof BaseModel) {

        }
    }
}
