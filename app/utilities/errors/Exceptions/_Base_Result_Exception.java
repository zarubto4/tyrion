package utilities.errors.Exceptions;

public abstract class _Base_Result_Exception extends RuntimeException {
    _Base_Result_Exception() { super(); }
    _Base_Result_Exception(String message) { super(message); }
    _Base_Result_Exception(String message, Throwable cause) { super(message, cause); }
    _Base_Result_Exception(Throwable cause) { super(cause); }
}
