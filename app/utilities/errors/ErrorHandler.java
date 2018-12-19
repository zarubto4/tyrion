package utilities.errors;

import controllers._BaseController;
import play.http.HttpErrorHandler;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public class ErrorHandler implements HttpErrorHandler {

    @Override
    public CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
        switch (statusCode) {
            case 400 : return CompletableFuture.completedFuture(_BaseController.badRequest(message));
            case 404 : return CompletableFuture.completedFuture(_BaseController.notFound(message));
            default  : return CompletableFuture.completedFuture(_BaseController.customResult(statusCode, message));
        }
    }

    @Override
    public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {
        if (exception instanceof CompletionException && exception.getCause() != null) {
            return CompletableFuture.completedFuture(_BaseController.controllerServerError(exception.getCause()));
        } else {
            return CompletableFuture.completedFuture(_BaseController.controllerServerError(exception));
        }
    }
}
