package utilities.deadbolt.understand;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;


@Singleton
public class HandlerDefaultCache implements HandlerCache
{
    private final DeadboltHandler defaultHandler = new DefaultHandler();
    private final Map<String, DeadboltHandler> handlers = new HashMap<>();

    public HandlerDefaultCache()
    {
        System.out.println("HandlerDefaultCache.HandlerDefaultCache");
        handlers.put(HandlerKeys.DEFAULT.key,       defaultHandler);
        handlers.put(HandlerKeys.ALTERNATIVE.key,   new AlternativeHandler());
    }

    @Override
    public DeadboltHandler apply(final String key)
    {
        System.out.println("HandlerDefaultCache.apply");
        return handlers.get(key);
    }

    @Override
    public DeadboltHandler get()
    {
        System.out.println("HandlerDefaultCache.get");
        return defaultHandler;
    }
}
