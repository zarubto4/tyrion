package utilities.web_socket.message_objects.common;

public abstract class WS_AbstractMessageBoard extends WS_AbstractMessage {

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
}
