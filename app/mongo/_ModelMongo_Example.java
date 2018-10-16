package mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import io.swagger.annotations.ApiModel;
import models.Model_Person;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.utils.IndexType;
import utilities.cache.InjectCache;
import utilities.cache.CacheMongoFinder;
import utilities.logger.Logger;
import utilities.model._Abstract_MongoModel;

import java.util.Date;
import java.util.List;

/**
 * Toto je schéma nového Modelu,
 * extenduje ho abstraktní třída MongoModel, která zajištuje všechny náležitosti jako je "Save" "Update" "Delete"
 */
@ApiModel( // Swagger annotation
        value = "ExampleModelName",
        description = "Model of ExampleModelName - Swagger annotation documentation"
)
@Entity("EXAMPLE_EXAMPLE")
@Indexes({
        @Index(
                fields = {
                        @Field("example_name"),

                        /**
                         * The $** value tells MongoDB to create a text index on all the text fields in a document.
                         * A more targeted index can be created, if desired, by explicitly listing which fields to index.
                         * Once the index is defined, we can start querying against it like this test does:
                         */
                        @Field(value = "$**", type = IndexType.TEXT), // for super_long_text_for_blog
                }
        )
})
public class _ModelMongo_Example extends _Abstract_MongoModel {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = new Logger(_ModelMongo_Example.class);

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    public String      example_name;
    public Double      example_salary;
    public Integer     example_age;
    public boolean     example_boolean;
    public Date        born;
    public String      super_long_text_for_blog;

    public AdditionalInformation information;
    public List<Repository> repositories;

/* JSON PROPERTY METHOD && VALUES --------------------------------------------------------------------------------------*/

/* JSON IGNORE METHOD && VALUES ----------------------------------------------------------------------------------------*/

/* SAVE && UPDATE && DELETE --------------------------------------------------------------------------------------------*/

/* HELP CLASSES --------------------------------------------------------------------------------------------------------*/

    /**
     * Embedded pomáhá dělat podobjekty a řeší komplikace s vyhledáváním v těchto objektech
     */
    @Embedded
    class AdditionalInformation {

        public String first;
        public String last;

        public AdditionalInformation(){ }
    }

    @Embedded
    class Repository {

        public String first;
        public String last;

        public Repository(){ }
    }

/* NOTIFICATION --------------------------------------------------------------------------------------------------------*/

/* NO SQL JSON DATABASE ------------------------------------------------------------------------------------------------*/

/* BLOB DATA  ----------------------------------------------------------------------------------------------------------*/

/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

/* SPECIAL QUERY -------------------------------------------------------------------------------------------------------*/


    @JsonIgnore
    public _ModelMongo_Example get_something_in_query() {

        _ModelMongo_Example example = find.query().field("example_age").greaterThan(10).get();        // Stejný výsledek jako následující řádek
        _ModelMongo_Example example2 = find.query().filter("example_age >", 10).get();     // Stejný výsledek jako předchozí řádek

        // ------------------------------------------------------------------------------------------------------------------------------------------------------

        // Kombinace
        _ModelMongo_Example example3 = find.query().field("born").lessThan(new Date()).field("salary").greaterThan(10000).get();

        // ------------------------------------------------------------------------------------------------------------------------------------------------------

        // find by @Reference
        Model_Person peson = new Model_Person(); // Je fiktivně uložený v fatabázi
        List<_ModelMongo_Example> roles = _ModelMongo_Example.find.query().field("users").equal(peson).asList(); // Pak do query místo m:n join vazby dáme přímo model (z něj si anotace vytáhnou @id

        // ------------------------------------------------------------------------------------------------------------------------------------------------------

        /**
         * You can use Morphia to map queries you might have already written using
         * the raw Java API against your objects, or to access features which are not yet present in Morphia.
         */



        DBObject query = BasicDBObjectBuilder.start()
                .add("albums", new BasicDBObject("$elemMatch",
                        new BasicDBObject("$and", new BasicDBObject[]{
                                new BasicDBObject("albumId", id),
                                new BasicDBObject("album", new BasicDBObject("$exists", false))
                        }))
                ).get();


        // ------------------------------------------------------------------------------------------------------------------------------------------------------


        Query<_ModelMongo_Example> query_2 = _ModelMongo_Example.find.query();

        query_2.or(
                query_2.criteria("email").equal("dsfasdfsdf"),
                query_2.criteria("role_id").equal("dsfasdfsdfsd")
        );

        // ------------------------------------------------------------------------------------------------------------------------------------------------------


        // ------------------------------------------------------------------------------------------------------------------------------------------------------

        return null;
    }

/* CACHE ---------------------------------------------------------------------------------------------------------------*/

/* FINDER --------------------------------------------------------------------------------------------------------------*/

    @Override @JsonIgnore
    public CacheMongoFinder<?> getFinder() { return find; }

    @JsonIgnore @InjectCache(value = _ModelMongo_Example.class, keyType = ObjectId.class)
    public static CacheMongoFinder<_ModelMongo_Example> find = new CacheMongoFinder<>(_ModelMongo_Example.class);

}
