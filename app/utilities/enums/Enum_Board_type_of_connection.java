package utilities.enums;

import com.avaje.ebean.annotation.EnumValue;

public enum Enum_Board_type_of_connection {

    @EnumValue("in_person_instance")  in_person_instance,
    @EnumValue("connected_to_server_unregistered") connected_to_server_unregistered,
    @EnumValue("under_project_virtual_instance") under_project_virtual_instance,
    @EnumValue("connected_to_byzance") connected_to_byzance
}
