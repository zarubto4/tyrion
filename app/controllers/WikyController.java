package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

//@Security.Authenticated(Secured.class)
public class WikyController extends Controller {


    @Inject WSClient ws;

    public Result test(){


        WSRequest wsrequest = ws.url("http://localhost:9000/test2");
        WSRequest complexRequest = wsrequest.setQueryString("130094907374823")
                .setQueryParameter("access_token", "CAAH4t62Qd2cBAJs6e4a2NaPvjakxrb77W41AwxSlN5EMjW4g6f0IXpgzYLPbhRSmoSdAfnO7P8Sls04fAWOyuZBMdpAKR7SZB6cruHGb6Ls9CkrZBJgsdS7elIMI2nKtZBzjdlcN9tkHNrZC345c2qcPjEl3ZBRlhWZCDqQBBH5OqTbZCJu8wx53BwoAUE0cr0UzgA6hCZA7aNWoTlkMmOdYVf4CPfXyTm9QZD")
                .setQueryParameter("fields", "first_name");

        F.Promise<JsonNode> jsonPromise = wsrequest.get().map(rsp -> { return rsp.asJson();});
        JsonNode jsonNode1 = jsonPromise.get(10000);

        System.out.println("Json " + jsonNode1.toString());

        return ok();
    }

    public Result test2(String fields){
        System.out.println("55555555555555555555555555555555555555555555555555555555555555555555555555555555");

        System.out.println("URL: " + fields);

        return ok("{\"pepa\":\"asdasd\"}");

    }


}
