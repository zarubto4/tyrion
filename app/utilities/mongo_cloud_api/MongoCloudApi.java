package utilities.mongo_cloud_api;


import com.google.inject.Inject;
import com.mongodb.client.MongoIterable;
import com.typesafe.config.Config;
import controllers._BaseFormFactory;
import io.ebeaninternal.server.core.Message;
import play.libs.ws.WSAuthScheme;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import io.swagger.util.Json;
import utilities.Server;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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


    public SwaggerMongoCloudUser createUser(UUID product_id, String databaseName) throws Exception{

            UUID password = UUID.randomUUID();

            SwaggerMongoCloudUserRole role = new SwaggerMongoCloudUserRole();
            role.databaseName = databaseName;
            role.roleName = "dbAdmin";
            SwaggerMongoCloudUser user = new SwaggerMongoCloudUser();
            user.databaseName = "admin";
            user.username =  product_id.toString();
            user.password =  password.toString();
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

    public void addRole(String username, String database) throws Exception{
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

        setRolesForUser(username, newRoles);
    }

    public void removeRole(String username, String database) throws Exception {
        SwaggerMongoCloudUser user = getUser(username);
        SwaggerMongoCloudUserRole[] newRoles = Arrays.stream( user.roles )
                                                     .filter( role -> !role.databaseName.equals(database) )
                                                     .toArray( SwaggerMongoCloudUserRole[]::new );

        setRolesForUser(username, newRoles);
    }


    private SwaggerMongoCloudUser getUser(String username) throws Exception {
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


    //Will throw IllegalArgumentException in case database with such name doesn't exist
    public List<String> getCollections(String databaseName) throws IllegalArgumentException {
        MongoIterable<String> collections = Server.mongoClient
                                                  .getDatabase(databaseName)   //throws IllegalArgumentException
                                                  .listCollectionNames();
        List<String> result = new ArrayList<>();
        for (String collectionName : collections ){
            result.add(collectionName);
        }
        return result;
    }

    public void createCollection(String databaseId, String collectionName) throws IllegalArgumentException{
        Server.mongoClient
              .getDatabase(databaseId) //throws IllegalArgumentException
              .createCollection(collectionName);
    }

    private void setRolesForUser(String username, SwaggerMongoCloudUserRole[] roles) throws Exception{
        SwaggerMongoCloudUser updatedUser = new SwaggerMongoCloudUser();
        updatedUser.roles = roles;
        WSResponse response =  ws.url(getBaseConnectionString() + "/databaseUsers/admin/" + username)
                .setAuth(getMongoCloudLogin(), getApiKey(), WSAuthScheme.DIGEST)
                .setContentType("application/json")
                .patch(Json.pretty(updatedUser))
                .toCompletableFuture().get();
        switch (response.getStatus()) {
            case 200:
                return;
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

