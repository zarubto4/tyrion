package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_MProgram_SnapShot_settings {

    @EnumValue("absolutely_public")                                 absolutely_public,
    @EnumValue("public_with_token")                                 public_with_token,
    @EnumValue("only_for_project_members")                          only_for_project_members,
    @EnumValue("only_for_project_members_and_imitated_emails")      only_for_project_members_and_imitated_emails,

}
