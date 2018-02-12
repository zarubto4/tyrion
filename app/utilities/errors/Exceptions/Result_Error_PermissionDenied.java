package utilities.errors.Exceptions;


public class Result_Error_PermissionDenied extends _Base_Result_Exception {
    public Result_Error_PermissionDenied() { super(); }
    public Result_Error_PermissionDenied(String message) { super(message); }
    public Result_Error_PermissionDenied(String message, Throwable cause) { super(message, cause); }
    public Result_Error_PermissionDenied(Throwable cause) { super(cause); }
}
