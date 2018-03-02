package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum Approval {

    @EnumValue("PENDING")       PENDING,
    @EnumValue("APPROVED")      APPROVED,
    @EnumValue("DISAPPROVED")   DISAPPROVED,
    @EnumValue("EDITED")        EDITED
}