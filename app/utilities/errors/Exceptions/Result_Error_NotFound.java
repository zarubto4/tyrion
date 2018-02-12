package utilities.errors.Exceptions;

public class Result_Error_NotFound extends _Base_Result_Exception {


    // Private Values
    private Class class_not_found;

    public Result_Error_NotFound(Class class_not_found) {
        super();
        this.class_not_found = class_not_found;
    }

    public Result_Error_NotFound(Class class_not_found, String message) {
        super(message);
        this.class_not_found = class_not_found;
    }

    public Class getClass_not_found() {
        return class_not_found;
    }
}
