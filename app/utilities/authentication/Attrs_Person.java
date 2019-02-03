package utilities.authentication;

import models.Model_Person;
import play.libs.typedmap.TypedKey;

public class Attrs_Person {
    public final static TypedKey<Model_Person> PERSON = TypedKey.<Model_Person>create("person");
}
