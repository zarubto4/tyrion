package utilities.authentication;

import com.github.scribejava.apis.*;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;
import models.Model_AuthorizationToken;
import play.mvc.Http;
import utilities.Server;
import utilities.logger.Logger;
import java.util.Scanner;
import com.github.scribejava.apis.FacebookApi;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import java.util.Arrays;


public class Social {

    /* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(Model_AuthorizationToken.class);

/* METHOD  -------------------------------------------------------------------------------------------------------------*/
    // Tutoriál a popis na
    // https://github.com/scribejava/scribejava/issues

    public static OAuth20Service Facebook(String state){

        final OAuth20Service Facebook = new ServiceBuilder(Server.Facebook_apiKey)
                .apiKey(Server.Facebook_apiKey)
                .apiSecret(Server.Facebook_clientSecret)
                .state(state)
                .callback(Server.Facebook_callBack)
                .build(FacebookApi.instance());

        return Facebook;
    }

    public static OAuth20Service GitHub(String state){

        final OAuth20Service GitHub = new ServiceBuilder(Server.GitHub_apiKey)
                .apiKey(Server.GitHub_apiKey)
                .apiSecret(Server.GitHub_clientSecret)
                .state(state)
                .callback(Server.GitHub_callBack)
                .build(GitHubApi.instance());

        return GitHub;
    }

}
