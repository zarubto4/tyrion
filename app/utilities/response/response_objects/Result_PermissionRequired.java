package utilities.response.response_objects;

public class Result_PermissionRequired {

    public Integer code = 403;
    public String state = "forbidden_Global";
    public String statut = "You need required permission";
    public String message;
}
