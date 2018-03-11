package filters;

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Date;
import java.util.concurrent.CompletionStage;

import java.lang.reflect.Method;

public class ActionCreator implements play.http.ActionCreator {

    @Override
    public Action createAction(Http.Request request, Method actionMethod) {
        return new Action.Simple() {
            @Override
            public CompletionStage<Result> call(Http.Context ctx) {
                // Request Latency Tester
                ctx.args.put("tyrion_response_measurement_time", new Date().getTime());
                ctx.args.put("tyrion_response_measurement_method", request.method());
                ctx.args.put("tyrion_response_measurement_path", request.path());
                ctx.args.put("tyrion_response_measurement_uri", request.uri());
                return delegate.call(ctx);
            }
        };
    }
}