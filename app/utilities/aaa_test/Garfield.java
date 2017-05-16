package utilities.aaa_test;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.*;
import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;


public class Garfield {


    private static int a = 0;

    public String getName(){
        return Integer.toString(++a);
    }

    public String getSub_name(){
        return Integer.toString(++a)+  "asdsd";
    }

    public String getHomePlanet(){
        return Integer.toString(++a)+  " Pluto";
    }



    public static DataFetcher<String> fooDataFetcher = new DataFetcher<String>() {

        @Override
        public String get(DataFetchingEnvironment environment) {
            // environment.getSource() is the value of the surrounding
            // object. In this case described by objectType
            String value = Integer.toString(++a) + "fetcher"; // Perhaps getting from a DB or whatever
            return value;
        }

    };

    public static  GraphQLObjectType object = newObject()
            .name(Garfield.class.getSimpleName().substring(6))
            .description("A humanoid creature in the Star Wars universe.")
            .field(newFieldDefinition()
                    .name("id")
                    .description("The id of Object")
                    .type(nonNull(GraphQLID)))
            .field(newFieldDefinition()
                    .name("name")
                    .description("The name of the human.")
                    .type(GraphQLString))
            .field(newFieldDefinition()
                    .name("sub_name")
                    .description("The sub_name of the human.")
                    .type(GraphQLString))
            .field(newFieldDefinition()
                    .name("homePlanet")
                    .description("The home planet of the human, or null if unknown.")
                    .type(GraphQLString))
            .field(newFieldDefinition()
                    .name("foo")
                    .type(GraphQLString)
                    .dataFetcher(fooDataFetcher))
            .build();
    
}
