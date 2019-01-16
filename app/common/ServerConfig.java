package common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import utilities.enums.ServerMode;

@Singleton
public class ServerConfig {

    private final ServerMode mode;
    private final String version;

    @Inject
    public ServerConfig(Config config) {
        this.mode = config.getEnum(ServerMode.class, "server.mode");
        this.version = config.getString("api.version");
    }

    public boolean isDevelopment() {
        return this.mode.equals(ServerMode.DEVELOPER);
    }

    public boolean isStage() {
        return this.mode.equals(ServerMode.STAGE);
    }

    public boolean isProduction() {
        return this.mode.equals(ServerMode.PRODUCTION);
    }

    public ServerMode getMode() {
        return mode;
    }

    public String getVersion() {
        return version;
    }
}
