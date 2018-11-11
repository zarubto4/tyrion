package utilities.mongo_cloud_api;


import com.google.inject.Inject;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import play.libs.ws.WSAuthScheme;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import io.swagger.util.Json;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.UUID;

public class MongoCloudApi {

    private WSClient ws;
    private _BaseFormFactory formFactory;
    private Config config;

    @Inject
    public MongoCloudApi(WSClient ws, _BaseFormFactory formFactory, Config config) {
        this.ws = ws;
        this.formFactory = formFactory;
        this.config = config;
    }

    public String getConnectionStringForUser(SwaggerMongoCloudUser user){

        return "mongodb://4344786c-910e-445e-a9eb-503899048073:b864c324-3c10-43c1-b354-6f9a43264312@testcluster-shard-00-00-waqlo.gcp.mongodb.net:27017,testcluster-shard-00-01-waqlo.gcp.mongodb.net:27017,testcluster-shard-00-02-waqlo.gcp.mongodb.net:27017/test?ssl=true&replicaSet=TestCluster-shard-0&authSource=admin&retryWrites=true";
    }

    public SwaggerMongoCloudUser createUser(UUID product_id, String databaseName) throws Exception{

            UUID password = UUID.randomUUID();

            SwaggerMongoCloudUserRole role = new SwaggerMongoCloudUserRole();
            role.databaseName = databaseName;
            role.roleName = "readWrite";
            SwaggerMongoCloudUser user = new SwaggerMongoCloudUser();
            user.databaseName = "admin";
            user.username = "" + product_id;
            user.password = "" + password;
            user.roles = new SwaggerMongoCloudUserRole[]{role};


            WSResponse response =  ws.url(getBaseConnectionString() + "/databaseUsers")
                    .setAuth(getMongoCloudLogin(),
                            getApiKey(),
                            WSAuthScheme.DIGEST).setContentType("application/json").post(Json.pretty(user)).toCompletableFuture().get();



            switch (response.getStatus()) {
                case 201:
                    SwaggerMongoCloudUser createdUser = formFactory.formFromJsonWithValidation(SwaggerMongoCloudUser.class, response.asJson());
                    createdUser.password = "" + password;
                    return createdUser;
                    default :
                        System.out.println(response.getStatus());
                        throw new Exception();
            }

    }

    public SwaggerMongoCloudUser addRole(String username, String database) throws Exception{
            SwaggerMongoCloudUser user = getUser(username);
            SwaggerMongoCloudUserRole newRole = new SwaggerMongoCloudUserRole();
            newRole.databaseName = database;
            newRole.roleName = "readWrite";
            SwaggerMongoCloudUserRole[] newRoles;
            if(user.roles == null) {
                newRoles = new SwaggerMongoCloudUserRole[]{newRole};
            } else {
                newRoles = Arrays.copyOf(user.roles, user.roles.length+1);
                newRoles[user.roles.length] = newRole;
            }

            SwaggerMongoCloudUser updatedUser = new SwaggerMongoCloudUser();
            updatedUser.roles = newRoles;
            WSResponse response =  ws.url(getBaseConnectionString() + "/databaseUsers/admin/" + user.username)
                    .setAuth(getMongoCloudLogin(),
                            getApiKey(),
                            WSAuthScheme.DIGEST).setContentType("application/json").patch(Json.pretty(updatedUser)).toCompletableFuture().get();

            switch (response.getStatus()) {
                case 200:
                    SwaggerMongoCloudUser updated = new SwaggerMongoCloudUser();
                    updated.username = user.username;
                    updated.password = user.password;
                    updated.groupId  = user.groupId;
                    updated.roles    = newRoles;
                    return updated;
                 default:
                     throw new Exception();
            }
    }

    public SwaggerMongoCloudUser getUser(String username) throws Exception {
        WSResponse response =  ws.url(getBaseConnectionString() + "/databaseUsers/admin/" + username)
                .setAuth(getMongoCloudLogin(),
                        getApiKey(),
                        WSAuthScheme.DIGEST).setContentType("application/json").get().toCompletableFuture().get();

        switch (response.getStatus()) {
            case 200:
                return formFactory.formFromJsonWithValidation(SwaggerMongoCloudUser.class, response.asJson());
             default:
                throw new Exception();
        }
    }

    private String getBaseConnectionString() {
        return config.getString("mongoCloudAPI.baseConnectionString");
    }

    private String getMongoCloudLogin() {
        return config.getString("mongoCloudAPI.login");
    }

    private String getApiKey() {
        return config.getString("mongoCloudAPI.apiKey");
    }
}

