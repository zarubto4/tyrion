package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_MProgram_SnapShot_settings {

    @EnumValue("absolutely_public")                                 absolutely_public,
    @EnumValue("only_for_project_members")                          only_for_project_members,
    // @EnumValue("only_for_project_members_and_imitated_emails")   only_for_project_members_and_imitated_emails,

    // POZOR v DATAB8ZI už jsou uložené parametry navíc dopředu se kterými se počítalo v návrhu - jen tu záměrně kvuli swagru nejsou uvedeny

    // Not in database - only for frontend
    @EnumValue("not_in_instance")      not_in_instance,
}
