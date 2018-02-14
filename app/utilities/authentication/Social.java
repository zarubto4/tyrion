package utilities.authentication;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuthService;
import models.Model_AuthorizationToken;
import utilities.Server;
import utilities.logger.Logger;

import java.util.Arrays;


public class Social {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_AuthorizationToken.class);

/* METHOD  -------------------------------------------------------------------------------------------------------------*/
    // Tutoriál a popis na
    // https://github.com/scribejava/scribejava/issues

    public static OAuthService Facebook(String state){

        final OAuthService Facebook = new ServiceBuilder()
                .apiKey(Server.Facebook_apiKey)
                .apiSecret(Server.Facebook_clientSecret)
                .state(state)
                .callback(Server.Facebook_callBack)
                .build();

        return Facebook;
    }

    public static OAuthService GitHub(String state){

        final OAuthService GitHub = new ServiceBuilder()
                .apiKey(Server.GitHub_apiKey)
                .apiSecret(Server.GitHub_clientSecret)
                .state(state)
                .callback( Server.GitHub_callBack )
                .build();

        return GitHub;
    }

}
