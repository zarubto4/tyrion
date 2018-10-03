package utilities.errors.Exceptions;

public class Result_Error_NotFound extends _Base_Result_Exception {


    // Private Values
    private Class entity;

    public Result_Error_NotFound(Class entity) {
        super("Could not find " + entity.getSimpleName());
        this.entity = entity;
    }

    public Result_Error_NotFound(Class entity, String message) {
        super(message);
        this.entity = entity;
    }

    public Class getEntity() {
        return entity;
    }
}
