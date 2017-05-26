import play.http.HttpErrorHandler;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;


public class ErrorHandler implements HttpErrorHandler{
    @Override
    public F.Promise<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
        return null;
    }

    @Override
    public F.Promise<Result> onServerError(Http.RequestHeader request, Throwable exception) {
        return null;
    }
}
