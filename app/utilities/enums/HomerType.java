package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum HomerType {

    @EnumValue("PUBLIC")     PUBLIC,
    @EnumValue("PRIVATE")    PRIVATE,
    @EnumValue("BACKUP")     BACKUP,
    @EnumValue("MAIN")       MAIN,
    @EnumValue("TEST")       TEST,
}
