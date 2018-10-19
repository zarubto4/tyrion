package utilities.permission;

import io.ebean.annotation.EnumValue;

/**
 * This enum should cover all actions that can be performed on a object.
 */
public enum Action {

    // BASIC operations
    @EnumValue("CREATE")            CREATE,
    @EnumValue("READ")              READ,
    @EnumValue("UPDATE")            UPDATE,
    @EnumValue("DELETE")            DELETE,

    // UNUSUAL operations
    @EnumValue("ACTIVATE")          ACTIVATE,
    @EnumValue("INVITE")            INVITE,
    @EnumValue("PUBLISH")           PUBLISH,
    @EnumValue("DEPLOY")            DEPLOY,
}
