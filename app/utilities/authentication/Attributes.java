package utilities.authentication;

import models.Model_Person;
import play.libs.typedmap.TypedKey;

/**
 * This class holds definitions of attributes of a request.
 */
public class Attributes {
    public static final TypedKey<Model_Person> PERSON = TypedKey.<Model_Person>create("person");
}
