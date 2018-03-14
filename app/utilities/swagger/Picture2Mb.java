package utilities.swagger;

import com.google.inject.Inject;
import play.http.HttpErrorHandler;
import play.mvc.BodyParser;

// Accept only 10KB of data.
public class Picture2Mb extends BodyParser.Json {

    @Inject
    public Picture2Mb(HttpErrorHandler errorHandler) {
        super(2000 * 1024, errorHandler);
    }

}
