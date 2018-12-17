package websocket.messages.homer_hardware_with_tyrion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Model_Hardware;
import play.libs.Json;
import websocket.messages.common.abstract_class.WS_AbstractMessage;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WS_Message_Hardware_set_settings extends WS_AbstractMessage {

    // MessageType
    @JsonIgnore public static final String message_type = "hardware_settings";

/* INCOMING VALUES FOR FORM --------------------------------------------------------------------------------------------*/

    public String full_id;
    public UUID uuid;
    public String key;

/* MAKE REQUEST  -------------------------------------------------------------------------------------------------------*/

    @JsonIgnore
    public  ObjectNode make_request(List<Model_Hardware> devices, String key, Boolean value) {

        List<UUID> hardware_ids = devices.stream().map(Model_Hardware::getId).collect(Collectors.toList());

        Set_CONF_Boolean_Parameter settings = new Set_CONF_Boolean_Parameter();
        settings.key = key;
        settings.value = value;

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Hardware.CHANNEL);
        request.set("uuid_ids", Json.toJson(hardware_ids) );
        request.set("settings_list", Json.toJson(Collections.singletonList(settings)));

        return request;
    }

    @JsonIgnore
    public  ObjectNode make_request(List<Model_Hardware> devices, String key, String value) {

        List<UUID> hardware_ids = devices.stream().map(Model_Hardware::getId).collect(Collectors.toList());

        Set_CONF_String_Parameter settings = new Set_CONF_String_Parameter();
        settings.key = key;
        settings.value = value;

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Hardware.CHANNEL);
        request.set("uuid_ids", Json.toJson(hardware_ids));
        request.set("settings_list", Json.toJson(Collections.singletonList(settings)));

        return request;
    }

    @JsonIgnore
    public  ObjectNode make_request(List<Model_Hardware> devices, String key, Integer value) {
        List<UUID> hardware_ids = devices.stream().map(Model_Hardware::getId).collect(Collectors.toList());

        Set_CONF_Integer_Parameter settings = new Set_CONF_Integer_Parameter();
        settings.key = key;
        settings.value = value;

        // Potvrzení Homer serveru, že je vše v pořádku
        ObjectNode request = Json.newObject();
        request.put("message_type", message_type);
        request.put("message_channel", Model_Hardware.CHANNEL);
        request.set("uuid_ids", Json.toJson(hardware_ids) );
        request.set("settings_list", Json.toJson(Collections.singletonList(settings)));

        return request;
    }

/* HELP CLASS  -------------------------------------------------------------------------------------------------------*/

    /**
     * Každý objekt zastupuje nastavení konkrétní hodnoty na hardwaru,
     * rozdělujeme je, aby nikdy nedošlo k záměně a aktualizaci hodnot postupně - nikoliv masivní dávnou.
     */

    class Set_CONF_Boolean_Parameter {
        @JsonProperty public String key;
        @JsonProperty public String type = "boolean";   // Boolean // String // Integer
        @JsonProperty public Boolean value;
    }
    class Set_CONF_String_Parameter {
        @JsonProperty public String key;
        @JsonProperty public String type = "string";   // Boolean // String // Integer
        @JsonProperty public String value;
    }
    class Set_CONF_Integer_Parameter {
        @JsonProperty public String key;
        @JsonProperty public String type = "number";   // Boolean // String // Integer
        @JsonProperty public Integer value;
    }
}
