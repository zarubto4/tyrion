package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Date;

@Entity
public class ForUploadProgram extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  @JsonIgnore public String id;

    @JsonIgnore @ManyToOne     public Homer homer;
    @JsonIgnore @ManyToOne     public B_Program program;
                               public Date whenDate;
                               public Date untilDate;

    @JsonProperty public String homerId(){
        return homer.homerId;
    }


}
