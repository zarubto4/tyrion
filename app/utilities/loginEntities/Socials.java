package utilities.loginEntities;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.apis.VkontakteApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuthService;
import play.Configuration;

public class Socials {

    public static OAuthService Facebook(String state){

        final OAuthService Facebook = new ServiceBuilder()
                .provider(new FacebookApi())
                .apiKey(Configuration.root().getString("Facebook.apiKey"))
                .apiSecret(Configuration.root().getString("Facebook.clientSecret"))
                .state(state)
                .callback(Configuration.root().getString("Facebook.callBack"))
                .build();

        return Facebook;
    }

    public static OAuthService GitHub(String state){

        final OAuthService GitHub = new ServiceBuilder()
                .provider(new GitHubApi())
                .apiKey(Configuration.root().getString("GitHub.apiKey"))
                .apiSecret(Configuration.root().getString("GitHub.clientSecret"))
                .state(state)
                .callback(Configuration.root().getString("GitHub.callBack"))
                .build();

        return GitHub;
    }

    public static OAuthService Twitter(String state){

        final OAuthService Twitter = new ServiceBuilder()
                .provider(new TwitterApi())
                .apiKey(Configuration.root().getString("Twitter.apiKey"))
                .apiSecret(Configuration.root().getString("Twitter.clientSecret"))
                .build();

        return Twitter;
    }

    public static OAuthService Vkontakte(String state){

        final OAuthService Vkontakte = new ServiceBuilder()
                .provider(new VkontakteApi())
                .apiKey(Configuration.root().getString("Vkontakte.apiKey"))
                .apiSecret(Configuration.root().getString("Vkontakte.clientSecret"))
                .state(state)
                .callback(Configuration.root().getString("Vkontakte.callBack"))
                .build();

        return Vkontakte;
    }


}