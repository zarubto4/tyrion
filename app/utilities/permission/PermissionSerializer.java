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

    private PermissionSerializer publishPermissionSerializer;

    private Action action;

    @Inject
    public PermissionSerializer(PermissionService permissionService) {
        super(Boolean.class);
        this.permissionService = permissionService;
        this.updatePermissionSerializer = new PermissionSerializer(permissionService, Action.UPDATE);
        this.deletePermissionSerializer = new PermissionSerializer(permissionService, Action.DELETE);
        this.publishPermissionSerializer = new PermissionSerializer(permissionService, Action.PUBLISH);
    }

    private PermissionSerializer(PermissionService permissionService, Action action) {
        super(Boolean.class);
        this.permissionService = permissionService;
        this.action = action;
    }

    @Override
    public void serialize(Boolean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (gen.getCurrentValue() instanceof BaseModel && gen.getCurrentValue() instanceof Permissible && _BaseController.isAuthenticated()) {

            BaseModel model = (BaseModel) gen.getCurrentValue();

            boolean permitted = true;

            try {
                switch (this.action) {
                    case UPDATE: this.permissionService.checkUpdate(_BaseController.person(), model); break;
                    case DELETE: this.permissionService.checkDelete(_BaseController.person(), model); break;
                    case PUBLISH: this.permissionService.check(_BaseController.person(), model, Action.PUBLISH); break;
                    default: throw new NotSupportedException("Unsupported action: " + this.action.name());
                }
            } catch (ForbiddenException e) {
                permitted = false;
            } catch (Exception e) {
                logger.internalServerError(e);
            }

            gen.writeBoolean(permitted);
        } else if (!gen.canOmitFields()) {
            logger.trace("serialize - field cannot be omitted");
            gen.writeBoolean(true);
        } else {
            gen.writeNull();
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
                } else if (jsonPermission.value() == Action.PUBLISH) {
                    return this.publishPermissionSerializer;
                }
            }
        }
        return this;
    }
}
