package utilities.permission;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.inject.Inject;
import controllers._BaseController;
import exceptions.ForbiddenException;
import exceptions.NotSupportedException;
import utilities.logger.Logger;
import utilities.model.BaseModel;

import java.io.IOException;

/**
 * This class will populate permission fields in serialized json.
 */
public class PermissionSerializer extends StdSerializer<Boolean> implements ContextualSerializer {

    private static final Logger logger = new Logger(PermissionSerializer.class);

    private final PermissionService permissionService;

    private PermissionSerializer updatePermissionSerializer;

    private PermissionSerializer deletePermissionSerializer;

    private Action action;

    @Inject
    public PermissionSerializer(PermissionService permissionService) {
        super(Boolean.class);
        this.permissionService = permissionService;
        this.updatePermissionSerializer = new PermissionSerializer(permissionService, Action.UPDATE);
        this.deletePermissionSerializer = new PermissionSerializer(permissionService, Action.DELETE);
    }

    private PermissionSerializer(PermissionService permissionService, Action action) {
        super(Boolean.class);
        this.permissionService = permissionService;
        this.action = action;
    }

    @Override
    public void serialize(Boolean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (gen.getCurrentValue() instanceof BaseModel && gen.getCurrentValue() instanceof Permissible) {

            BaseModel model = (BaseModel) gen.getCurrentValue();

            boolean permitted = true;

            try {
                if (this.action == Action.UPDATE) {
                    this.permissionService.checkUpdate(_BaseController.person(), model);
                } else if (this.action == Action.DELETE) {
                    this.permissionService.checkDelete(_BaseController.person(), model);
                } else {
                    throw new NotSupportedException("Unsupported action: " + this.action.name());
                }
            } catch (ForbiddenException e) {
                permitted = false;
            } catch (Exception e) {
                logger.internalServerError(e);
            }

            gen.writeBoolean(permitted);
        } else {
            gen.writeBoolean(true);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            JsonPermission jsonPermission = property.getAnnotation(JsonPermission.class);
            if (jsonPermission != null) {
                if (jsonPermission.value() == Action.UPDATE) {
                    return this.updatePermissionSerializer;
                } else if (jsonPermission.value() == Action.DELETE) {
                    return this.deletePermissionSerializer;
                }
            }
        }
        return this;
    }
}
