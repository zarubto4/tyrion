package utilities.login_entities;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.apis.VkontakteApi;
import com.github.scribejava.core.builder.ServiceBuilder;
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

    public static OAuthService Facebook(String state){

        final OAuthService Facebook = new ServiceBuilder()
                .provider(new FacebookApi())
                .apiKey(Server.Facebook_apiKey)
                .apiSecret(Server.Facebook_clientSecret)
                .state(state)
                .callback(Server.Facebook_callBack)
                .build();

        return Facebook;
    }

    public static OAuthService WordPress(String state){

        final OAuthService Facebook = new ServiceBuilder()
                .provider(new FacebookApi())
                .apiKey(Server.WordPress_apiKey)
                .apiSecret(Server.WordPress_clientSecret)
                .state(state)
                .callback(Server.WordPress_callBack)
                .build();

        return Facebook;
    }


    public static OAuthService GitHub(String state){

        final OAuthService GitHub = new ServiceBuilder()
                .provider(new GitHubApi())
                .apiKey(Server.GitHub_apiKey)
                .apiSecret(Server.GitHub_clientSecret)
                .state(state)
                .callback( Server.GitHub_callBack )
                .build();

        return GitHub;
    }

    public static OAuthService Twitter(String state){

        final OAuthService Twitter = new ServiceBuilder()
                .provider(new TwitterApi())
                .apiKey(Configuration.root().getString("Twitter.apiKey"))
                .apiSecret(Server.tyrion_serverAddress +  Configuration.root().getString("Twitter.clientSecret"))
                .build();

        return Twitter;
    }

    public static OAuthService Vkontakte(String state){

        final OAuthService Vkontakte = new ServiceBuilder()
                .provider(new VkontakteApi())
                .apiKey(Configuration.root().getString("Vkontakte.apiKey"))
                .apiSecret(Configuration.root().getString("Vkontakte.clientSecret"))
                .state(state)
                .callback( Server.tyrion_serverAddress + Configuration.root().getString("Vkontakte.callBack"))
                .build();

        return Vkontakte;
    }


}