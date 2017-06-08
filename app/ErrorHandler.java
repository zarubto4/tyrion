import play.http.HttpErrorHandler;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import utilities.logger.Server_Logger;
import utilities.response.GlobalResult;


public class ErrorHandler implements HttpErrorHandler{
    @Override
    public F.Promise<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
        switch (statusCode){

            case 400 : return F.Promise.promise(() -> GlobalResult.result_badRequest(message));
            case 404 : return F.Promise.promise(() -> GlobalResult.result_notFound(message));

            default: return F.Promise.promise(() -> GlobalResult.result_custom(statusCode, message));
        }
    }

    @Override
    public F.Promise<Result> onServerError(Http.RequestHeader request, Throwable exception) {

        return F.Promise.promise(() -> Server_Logger.result_internalServerError(exception, request));
    }
}
