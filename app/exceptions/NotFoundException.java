package exceptions;

/**
 * Exception which indicates that some entity was not found in the database.
 */
public class NotFoundException extends BaseException {

    private Class entity;

    public NotFoundException(Class entity) {
        super("Could not find " + entity.getSimpleName());
        this.entity = entity;
    }

    public NotFoundException(Class entity, String message) {
        super(message);
        this.entity = entity;
    }

    public Class getEntity() {
        return entity;
    }
}
