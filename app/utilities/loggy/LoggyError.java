package utilities.loggy;

import java.util.Date;

public class LoggyError {

    public String hash_identification;
    public Date   date_of_create = new Date();
    public String description;
    public String summary;
    public String level = "error";
    public String method_name = "asdfsf";
    public String class_name = "sdafsdf";


    public String url = null;

    public LoggyError(String summary, String description) {
        this.description = description;
        this.summary = summary;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

}
