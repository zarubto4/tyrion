package utilities.swagger.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import play.data.validation.Constraints;
import utilities.Server;
import utilities.enums.GridAccess;

import java.util.UUID;

public class Swagger_InstanceSnapShotConfigurationProgram {

    public Swagger_InstanceSnapShotConfigurationProgram() {}

    @Constraints.Required public UUID grid_program_version_id;
    @Constraints.Required public UUID grid_program_id;

    // Model_BProgramVersionSnapGridProjectProgram.id je connection_token!!!!!
    @Constraints.Required public String connection_token;      // Token, pomocí kterého se vrátí konkrétní aplikace s podporou propojení na websocket
    @Constraints.Required public GridAccess snapshot_settings; // Typ Aplikace

    @JsonProperty
    public String connection_url() {
        return Server.grid_app_main_url + "/" + connection_token;
    }
}
