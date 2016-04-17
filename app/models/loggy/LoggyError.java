package models.loggy;


import com.avaje.ebean.Model;
import javax.persistence.*;

@Entity
public class LoggyError extends Model {

@Id public String id; // nebo ID - to je fuk - ale měl by si podle tohohle "ID" umět dohledat hledáním v souboru konrkétní zaznamenanou chybu

    @Column(columnDefinition = "TEXT")
    public String summary;

    @Column(columnDefinition = "TEXT")
    public String description;  // sem dáš všechno co lze zobrazit (ten tvůj složený StringBuilder)

    public String youtrack_url; // URL na chybu pokud je nahraná na youtrack

    public static Finder<String,LoggyError> find = new Finder<>(LoggyError.class);

    public LoggyError(String id, String summary, String description) {
        this.id = id;
        this.summary = summary;
        this.description = description;
    }

    public void setYoutrack_url(String url) {
        youtrack_url = url;
    }
}
