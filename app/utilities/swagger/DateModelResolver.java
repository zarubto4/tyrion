package utilities.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.ModelResolver;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

public class DateModelResolver extends ModelResolver {

    public DateModelResolver(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public Property resolveProperty(Type type,
                                    ModelConverterContext context,
                                    Annotation[] annotations,
                                    Iterator<ModelConverter> next) {
        if (this.shouldIgnoreClass(type)) {
            return null;
        }

        if (type.getTypeName().equals("java.util.Date")) {
            return new DateProperty();
        }

        return resolveProperty(_mapper.constructType(type), context, annotations, next);
    }
}
