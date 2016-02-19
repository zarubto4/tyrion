package utilities.deadbolt.actions;

import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.JavaAnalyzer;
import be.objectify.deadbolt.java.actions.RestrictAction;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import play.Configuration;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import utilities.deadbolt.understand.Roles;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


public class CustomRestrictAction extends Action<CustomRestrict>
{
    final JavaAnalyzer analyzer;
    final SubjectCache subjectCache;
    final HandlerCache handlerCache;
    final Configuration playConfig;

    final ExecutionContextProvider ecProvider;

    @Inject
    public CustomRestrictAction(JavaAnalyzer analyzer, SubjectCache subjectCache, HandlerCache handlerCache, Configuration playConfig, ExecutionContextProvider ecProvider) {
        this.analyzer = analyzer;
        this.subjectCache = subjectCache;
        this.handlerCache = handlerCache;
        this.playConfig = playConfig;
        this.ecProvider = ecProvider;
    }

    @Override
    public F.Promise<Result> call(Http.Context context) throws Throwable {

        final CustomRestrict outerConfig = configuration;
        RestrictAction restrictionsAction = new RestrictAction(analyzer, subjectCache, handlerCache, playConfig, configuration.config(), this.delegate, ecProvider)
        {
            @Override
            public List<String[]> getRoleGroups()
            {
                List<String[]> roleGroups = new ArrayList<String[]>();
                for (RoleGroup roleGroup : outerConfig.value())
                {
                    Roles[] value = roleGroup.value();
                    String[] group = new String[value.length];
                    for (int i = 0; i < value.length; i++)
                    {
                        group[i] = value[i].getName();
                    }
                    roleGroups.add(group);
                }
                return roleGroups;
            }
        };
        return restrictionsAction.call(context);
    }
}
