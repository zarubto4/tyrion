package common;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.google.inject.Inject;
import utilities.network.LastOnlineSerializer;
import utilities.network.NetworkStatusSerializer;
import utilities.permission.PermissionSerializer;
import utilities.project.ProjectStatsSerializer;

public class InjectedHandlerInstantiator extends HandlerInstantiator {

    private final PermissionSerializer permissionSerializer;
    private final NetworkStatusSerializer networkStatusSerializer;
    private final LastOnlineSerializer lastOnlineSerializer;
    private final ProjectStatsSerializer projectStatsSerializer;

    @Inject
    public InjectedHandlerInstantiator(PermissionSerializer permissionSerializer, NetworkStatusSerializer networkStatusSerializer,
                                       LastOnlineSerializer lastOnlineSerializer, ProjectStatsSerializer projectStatsSerializer) {
        this.permissionSerializer = permissionSerializer;
        this.networkStatusSerializer = networkStatusSerializer;
        this.lastOnlineSerializer = lastOnlineSerializer;
        this.projectStatsSerializer = projectStatsSerializer;
    }

    @Override
    public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
        return null;
    }

    @Override
    public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
        return null;
    }

    @Override
    public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {

        if (serClass == PermissionSerializer.class) {
            return this.permissionSerializer;
        }

        if (serClass == NetworkStatusSerializer.class) {
            return this.networkStatusSerializer;
        }

        if (serClass == LastOnlineSerializer.class) {
            return this.lastOnlineSerializer;
        }

        if (serClass == ProjectStatsSerializer.class) {
            return this.projectStatsSerializer;
        }

        return null;
    }

    @Override
    public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
        return null;
    }

    @Override
    public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
        return null;
    }
}
