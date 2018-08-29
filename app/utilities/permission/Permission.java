package utilities.permission;

import io.ebean.annotation.EnumValue;

public enum Permission {

    @EnumValue("PRODUCT_CREATE")            PRODUCT_CREATE,
    @EnumValue("PRODUCT_READ")              PRODUCT_READ,
    @EnumValue("PRODUCT_UPDATE")            PRODUCT_UPDATE,
    @EnumValue("PRODUCT_DELETE")            PRODUCT_DELETE,

    @EnumValue("PROJECT_CREATE")            PROJECT_CREATE,
    @EnumValue("PROJECT_READ")              PROJECT_READ,
    @EnumValue("PROJECT_UPDATE")            PROJECT_UPDATE,
    @EnumValue("PROJECT_DELETE")            PROJECT_DELETE


    // TODO All permissions
}
