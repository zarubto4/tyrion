package utilities.login_entities;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.apis.VkontakteApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;
import play.Configuration;
import utilities.Server;
import utilities.logger.Class_Logger;

public class Socials {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(Socials.class);

/* METHOD  -------------------------------------------------------------------------------------------------------------*/
    // Toturiál a popis na
    // https://github.com/scribejava/scribejava/issues

    public static OAuth20Service Facebook(String state){

        final OAuth20Service Facebook = new ServiceBuilder()
                .apiKey(Server.Facebook_apiKey)
                .apiSecret(Server.Facebook_clientSecret)
                .state(state)
                .callback(Server.Facebook_callBack)
                .build(FacebookApi.instance());

        return Facebook;
    }

    public static OAuth20Service GitHub(String state){

        final OAuth20Service GitHub = new ServiceBuilder()
                .apiKey(Server.GitHub_apiKey)
                .apiSecret(Server.GitHub_clientSecret)
                .state(state)
                .callback( Server.GitHub_callBack )
                .build(GitHubApi.instance());

        return GitHub;
    }


}