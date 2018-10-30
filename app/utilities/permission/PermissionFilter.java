package utilities.permission;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.google.inject.Inject;
import controllers._BaseController;
import utilities.logger.Logger;

public class PermissionFilter extends SimpleBeanPropertyFilter {

    private static final Logger logger = new Logger(PermissionFilter.class);

    protected PermissionService permissionService;

    @Inject
    public PermissionFilter(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
        if (pojo instanceof Permissible && _BaseController.isAuthenticated()) {
            WithPermission withPermission = writer.getAnnotation(WithPermission.class);
            if (withPermission != null) {
                if (!this.permissionService.isAdmin(_BaseController.person())) {
                    logger.trace("serializeAsField - filtering out {} from {}", writer.getName(), pojo.getClass().getSimpleName());
                    return;
                }
                // TODO more variability
            }
        } else {
            JsonPermission jsonPermission = writer.getAnnotation(JsonPermission.class);
            if (jsonPermission != null) {
                logger.trace("serializeAsField - filtering out {} from {}", writer.getName(), pojo.getClass().getSimpleName());
                return;
            }
        }

        writer.serializeAsField(pojo, jgen, provider);
    }
}
