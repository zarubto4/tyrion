package web_socket.message_objects.common.abstract_class;

public abstract class WS_AbstractMessage_Board {

    public boolean online_status = false; // Defaultně - musí být přepsán příchozím JSON

    public String firmware_version_core;
    public String firmware_version_mbed;     //
    public String firmware_version_lib;
    public String firmware_build_id;         // Číslo Buildu
    public String firmware_build_datetime;   // Kdy bylo vybylděno

    public String bootloader_version_core;
    public String bootloader_version_mbed;
    public String bootloader_build_id;
    public String bootloader_build_datetime;

    public String interface_name;
    public String state;

    public boolean autobackup = false;

    public boolean save;
    public String short_id;
    public int count;

    public String  status = null;
    public String  error  = null;
    public Integer errorCode  = null;

}
