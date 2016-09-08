package models.person;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class ChangePropertyToken extends Model {
/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
    @OneToOne @JoinColumn(name = "person_id")                   public Person person;
                                                                public String change_property_token;
                                                                public Date time_of_creation;
                                                                public String property;
                                                                public String value;

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void  setChangePropertyToken(){
        while(true){ // I need Unique Value
            this.change_property_token = UUID.randomUUID().toString();
            if (ChangePropertyToken.find.where().eq("change_property_token",this.change_property_token).findUnique() == null) break;
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,ChangePropertyToken> find = new Finder<>(ChangePropertyToken.class);

}
