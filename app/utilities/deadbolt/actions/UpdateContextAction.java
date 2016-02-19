
package utilities.deadbolt.actions;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class UpdateContextAction extends Action<UpdateContext>
{
    @Override
    public F.Promise<Result> call(final Http.Context context) throws Throwable
    {
        context.args.put("UpdateContext", configuration.value());
        return delegate.call(context);
    }
}
