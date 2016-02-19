/*
 * Copyright 2012 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package models.persons;

import be.objectify.deadbolt.core.models.Role;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class SecurityRole extends Model implements Role {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) public String id;
                                                            public String name;

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL, mappedBy = "roles")  @JoinTable(name = "person_roles") public List<Person> persons = new ArrayList<>();

    @JsonIgnore @ManyToMany(cascade = CascadeType.ALL) public List<PersonPermission>  permissions    = new ArrayList<>();


    public String getName()
    {
        return name;
    }
    public static SecurityRole findByName(String name) {return find.where().eq("name", name).findUnique();}

    public static final Finder<String, SecurityRole> find = new Finder<>(SecurityRole.class);
}
