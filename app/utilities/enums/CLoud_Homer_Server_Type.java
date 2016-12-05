package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum  CLoud_Homer_Server_Type {

    @EnumValue("public_server")   public_server,
    @EnumValue("private_server")  private_server,

    @EnumValue("backup_server")     backup_server,
    @EnumValue("main_server")       main_server,
    @EnumValue("test_server")       test_server,


}
