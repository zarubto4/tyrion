package common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import utilities.network.LocalAddress;

@Singleton
public class PortalConfig {

    private final String mainUrl;
    private final String redirectSuccessUrl;
    private final String redirectFailUrl;
    private final String passwordResetUrl;
    private final String invitationUrl;
    private final String propertyChangeFailUrl;

    @Inject
    public PortalConfig(Config config, ServerConfig serverConfig, LocalAddress localAddress) {

        if (serverConfig.isDevelopment()) {
            this.mainUrl = "http://" + localAddress.get() + ":8080";
        } else {
            String mode = serverConfig.getMode().toString().toLowerCase();
            this.mainUrl = "https://" + config.getString("Becki." + mode + ".mainUrl");
        }

        this.redirectSuccessUrl = this.mainUrl + "/" + config.getString("Becki.redirectOk");
        this.redirectFailUrl = this.mainUrl + "/" + config.getString("Becki.redirectFail");
        this.passwordResetUrl = this.mainUrl + "/" + config.getString("Becki.passwordReset ");
        this.invitationUrl = this.mainUrl + "/" + config.getString("Becki.invitationToCollaborate");
        this.propertyChangeFailUrl = this.mainUrl + "/" + config.getString("Becki.redirectFail");
    }

    public String getMainUrl() {
        return mainUrl;
    }

    public String getRedirectSuccessUrl() {
        return redirectSuccessUrl;
    }

    public String getRedirectFailUrl() {
        return redirectFailUrl;
    }

    public String getInvitationUrl() {
        return invitationUrl;
    }

    public String getPasswordResetUrl() {
        return passwordResetUrl;
    }

    public String getPropertyChangeFailUrl() {
        return propertyChangeFailUrl;
    }
}
