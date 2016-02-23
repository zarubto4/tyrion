package models.project.global;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@Entity
public class Home extends Model {

 /* DATABASE VALUES ---------------------------------------------------------------------------------------------- */
    @Id @GeneratedValue public String id;
                        public String Name;
    public Project project;

 /* FINDER --------------------------------------------------------------------------------------------------------*/
    public static Finder<String,Home> find = new Finder<String,Home>(Home.class);

    public Home(){}

}
