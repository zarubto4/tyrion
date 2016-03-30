package utilities.loggy;

public class LoggyError {

    String d;
    String s;
    public String url = null;

    public LoggyError(String summary, String description) {
        d = description;
        s = summary;
    }

    public String getSummmary() {
        return s;
    }

    public String getDescription() {
        return d;
    }

}
