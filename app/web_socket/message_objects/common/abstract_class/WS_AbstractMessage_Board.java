package web_socket.message_objects.common.abstract_class;

public abstract class WS_AbstractMessage_Board {

    public boolean online_status = false;       // Defaultně - musí být přepsán příchozím JSON

    public String firmware_build_id;            // Číslo Buildu

    public String backup_build_id;              // Číslo Buildu

    public String bootloader_build_id;          // Version name Bootloader


    public String interface_name;
    public String state;

    public boolean autobackup = false;

    public boolean save;
    public String short_id;
    public int count;

    public String  status = null;
    public String  error;
    public int errorCode;

}
