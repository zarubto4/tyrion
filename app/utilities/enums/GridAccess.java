package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum GridAccess {

    @EnumValue("PUBLIC")    PUBLIC,
    @EnumValue("PROJECT")   PROJECT,
    // @EnumValue("only_for_project_members_and_imitated_emails")   only_for_project_members_and_imitated_emails,

    // POZOR v DATAB8ZI už jsou uložené parametry navíc dopředu se kterými se počítalo v návrhu - jen tu záměrně kvuli swagru nejsou uvedeny

    // Not in database - only for frontend
    @EnumValue("TESTING")   TESTING,
}
