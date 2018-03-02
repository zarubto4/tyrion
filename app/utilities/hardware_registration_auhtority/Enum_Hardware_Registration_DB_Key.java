package utilities.hardware_registration_auhtority;

import io.ebean.annotation.EnumValue;

public enum Enum_Hardware_Registration_DB_Key {

    @EnumValue("full_id")             full_id,
    @EnumValue("mac_address")         mac_address,
    @EnumValue("hash_for_adding")     registration_hash,

}
