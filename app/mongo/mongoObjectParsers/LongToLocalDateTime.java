package mongo.mongoObjectParsers;

import com.mongodb.DBObject;
import xyz.morphia.converters.SimpleValueConverter;
import xyz.morphia.converters.TypeConverter;
import xyz.morphia.mapping.MappedField;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

import static java.time.ZoneId.systemDefault;

public class LongToLocalDateTime extends TypeConverter implements SimpleValueConverter {

    public LongToLocalDateTime() {
        super(LocalDateTime.class);
    }


    @Override
    public Object decode(final Class<?> targetClass, final Object val, final MappedField optionalExtraInfo) {
        if (val == null) {
            return null;
        }

        if (val instanceof LocalDateTime) {
            return val;
        }

        if (val instanceof Long) {

            if( (Long) val > 1009823963935L ) {
                return  LocalDateTime.ofInstant(Instant.ofEpochSecond( (Long) val / 1000),
                        TimeZone.getDefault().toZoneId());
            }

            return  LocalDateTime.ofInstant(Instant.ofEpochSecond( (Long) val),
                    TimeZone.getDefault().toZoneId());
        }

        if (val instanceof Date) {
            return LocalDateTime.ofInstant(((Date) val).toInstant(), systemDefault());
        }

        throw new IllegalArgumentException("Can't convert to LocalDateTime from " + val);
    }

    @Override
    public Object encode(final Object value, final MappedField optionalExtraInfo) {
        if (value == null) {
            return null;
        }
        return Date.from(((LocalDateTime) value).atZone(systemDefault()).toInstant());
    }
}