package utilities.deadbolt.security;

import be.objectify.deadbolt.java.TemplateFailureListener;
import be.objectify.deadbolt.java.cache.HandlerCache;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import utilities.deadbolt.understand.HandlerDefaultCache;

import javax.inject.Singleton;


public class CustomDeadboltHook extends Module
{
    @Override
    public Seq<Binding<?>> bindings(final Environment environment, final Configuration configuration) {
        System.out.println("Jsem v before CustomDeadboltHook.CustomDeadboltHook");
        return seq(bind(TemplateFailureListener.class).to(MyCustomTemplateFailureListener.class).in(Singleton.class), bind(HandlerCache.class).to(HandlerDefaultCache.class).in(Singleton.class));
    }
}
