package utilities.loggy;


import com.avaje.ebean.Model;
import javax.persistence.*;

// TODO dej to do databáze - a zároveň do souboru  - tam bude bug trace se všema píčovinama okolo

@Entity
public class LoggyError extends Model {

@Id public String id; // nebo ID - to je fuk - ale měl by si podle tohohle "ID" umět dohledat hledáním v souboru konrkétní zaznamenanou chybu
    public String summary;
    public String description;  // sem dáš všechno co lze zobrazit (ten tvůj složený StringBuilder)
    public String youtrack_url; // URL na chybu pokud je nahraná na youtrack

    public static Finder<String,LoggyError> find = new Finder<>(LoggyError.class);

    public LoggyError(String id, String summary, String description, String youtrack_url) {
        this.id = id;
        this.summary = summary;
        this.description = description;
        this.youtrack_url = youtrack_url;
    }
}
