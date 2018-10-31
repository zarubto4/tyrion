package utilities.mongo_cloud_api;


import com.google.inject.Inject;
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

    @Inject
    public MongoCloudApi(WSClient ws, _BaseFormFactory formFactory) {
        this.ws = ws;
        this.formFactory = formFactory;
    }

    public String getConnectionStringForUser(SwaggerMongoCloudUser user){
        return "testConnectionString";
    }

    public SwaggerMongoCloudUser createUser(UUID product_id, String databaseName) {
        try {

            UUID password = UUID.randomUUID();

            SwaggerMongoCloudUserRole role = new SwaggerMongoCloudUserRole();
            role.databaseName = databaseName;
            role.roleName = "readWrite";

            SwaggerMongoCloudUser user = new SwaggerMongoCloudUser();
            user.username = "" + product_id;
            user.password = "" + password;
            user.roles = new SwaggerMongoCloudUserRole[]{role};

            WSResponse response =  ws.url("https://cloud.mongodb.com/api/atlas/v1.0/groups/5bcd86829ccf64e6ceceea27/databaseUsers")
                    .setAuth("shvachka.alexey@gmail.com",
                            "3288dd5f-2cf5-43b9-acce-3e15afb97e8e",
                            WSAuthScheme.DIGEST).post(Json.pretty(user)).toCompletableFuture().get();



            switch (response.getStatus()) {
                case 200:
                    response.asJson();
                    SwaggerMongoCloudUser createdUser = formFactory.formFromJsonWithValidation(SwaggerMongoCloudUser.class, response.asJson());
                    createdUser.password = "" + password;

                    return createdUser;
                    default :
                        throw new Exception();
            }

        } catch (Exception e) {
            return null;
        }
    }

    public SwaggerMongoCloudUser addRole(SwaggerMongoCloudUser user, String database) throws Exception{
            SwaggerMongoCloudUserRole newRole = new SwaggerMongoCloudUserRole();
            newRole.databaseName = database;
            newRole.roleName = "readWrite";
            SwaggerMongoCloudUserRole[] newRoles = Arrays.copyOf(user.roles, user.roles.length+1);
            newRoles[user.roles.length] = newRole;

            SwaggerMongoCloudUser updatedUser = new SwaggerMongoCloudUser();
            updatedUser.roles = newRoles;
            WSResponse response =  ws.url("https://cloud.mongodb.com/api/atlas/v1.0/groups/5bcd86829ccf64e6ceceea27/databaseUsers/admin/" + user.username)
                    .setAuth("shvachka.alexey@gmail.com",
                            "3288dd5f-2cf5-43b9-acce-3e15afb97e8e",
                            WSAuthScheme.DIGEST).patch(Json.pretty(updatedUser)).toCompletableFuture().get();

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

}

