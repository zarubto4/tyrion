package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import io.swagger.annotations.Api;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import utilities.aaa_test.Garfield;
import utilities.logger.Class_Logger;
import utilities.response.GlobalResult;

import java.util.LinkedHashMap;
import java.util.Map;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLString;
import static utilities.scheduler.jobs.Job_SpendingCredit.spend;

@Api(value = "Not Documented API - InProgress or Stuck")
public class Controller_Wiky extends Controller {

// LOGGER ##############################################################################################################

    private static final Class_Logger terminal_logger = new Class_Logger(Controller_Wiky.class);

     public Result test1(){
         try {

             return ok();
         }catch (Exception e){
             terminal_logger.internalServerError(e);
             return badRequest();
         }

     }



    public Result test2(Map body){
        try {

            GraphQLSchema schema = GraphQLSchema.newSchema()
                                    .query(Garfield.object)
                                    .build();

            String query = (String) body.get("query");

            Map<String, Object> variables = (Map<String, Object>) body.get("variables");


            ExecutionResult executionResult = GraphQL.newGraphQL(schema).build().execute(query,(Object) null, variables);


            Map<String, Object> result = new LinkedHashMap<>();
            if (executionResult.getErrors().size() > 0) {
                result.put("errors", executionResult.getErrors());
                System.out.print(" Error :: " + executionResult.getErrors()  );
            }

            result.put("data", executionResult.getData());

            return ok(Json.toJson(result));

        }catch (Exception e){
            e.printStackTrace();
            return badRequest();
        }

    }

}
