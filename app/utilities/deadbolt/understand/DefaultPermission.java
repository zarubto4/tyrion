
package utilities.deadbolt.understand;

import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import controllers.SecurityController;
import models.overflow.Post;
import models.persons.Person;
import models.project.b_program.B_Program;
import models.project.c_program.C_Program;
import models.project.global.Project;
import play.Logger;
import play.libs.F;
import play.mvc.Http;
import utilities.deadbolt.security.InterfaceHandler;

import java.util.*;

public class DefaultPermission implements DynamicResourceHandler {

    private static final Map<String, Optional<DynamicResourceHandler>> HANDLERS = new HashMap<>();

    private static final DynamicResourceHandler DENY = new DynamicResourceHandler() {

         @Override
         public F.Promise<Boolean> isAllowed(String s, String s1, DeadboltHandler deadboltHandler, Http.Context context) {
             System.out.println("DefaultPermission.DynamicResourceHandler.isAllowed");
             return F.Promise.pure(false);
         }

         @Override
         public F.Promise<Boolean> checkPermission(String s, DeadboltHandler deadboltHandler, Http.Context context) {
                System.out.println("DefaultPermission.DynamicResourceHandler.checkPermission");
                return F.Promise.pure(false);
         }
    };


    static {


        HANDLERS.put("project.owner", Optional.of(new InterfaceHandler() {
            public F.Promise<Boolean> isAllowed(final String name, final String meta, final DeadboltHandler deadboltHandler, final Http.Context context)
            {
                Map<String, String[]> queryStrings = context.request().queryString();
                String[] requestedNames = queryStrings.get("projectId");

                if(requestedNames.length != 1) Logger.error("Chyba nenalezena příslušná Query"); // TODO Logger

                Project project = Project.find.where().eq("projectId", requestedNames[0]).where().eq("ownersOfProject.id", SecurityController.getPerson(context).id ).findUnique();
                if(project == null)  return F.Promise.promise(() -> false);

                return F.Promise.promise(() -> true);
            }

        }));

        HANDLERS.put("project.b_program_owner", Optional.of(new InterfaceHandler() {
            public F.Promise<Boolean> isAllowed(final String name, final String meta, final DeadboltHandler deadboltHandler, final Http.Context context)
            {

                Map<String, String[]> queryStrings = context.request().queryString();
                String[] requestedNames = queryStrings.get("b_program_id");

                if(requestedNames.length != 1) Logger.error("Chyba nenalezena příslušná Query"); // TODO Logger

                B_Program program = B_Program.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson(context).id).where().eq("id",requestedNames[0]).findUnique();
                if(program == null) return F.Promise.promise(() -> false);

                return F.Promise.promise(() -> true);
            }

        }));

        HANDLERS.put("project.c_program_owner", Optional.of(new InterfaceHandler() {
            public F.Promise<Boolean> isAllowed(final String name, final String meta, final DeadboltHandler deadboltHandler, final Http.Context context)
            {

                Map<String, String[]> queryStrings = context.request().queryString();
                String[] requestedNames = queryStrings.get("c_program_id");

                System.out.println("Velikost pole "+ requestedNames.length);
                System.out.println("Co tam je "+ requestedNames[0]);
              //  if(requestedNames.length != 1) Logger.error("Chyba nenalezena příslušná Query"); // TODO Logger

                C_Program program = C_Program.find.where().eq("project.ownersOfProject.id", SecurityController.getPerson(context).id).where().eq("id",requestedNames[0]).findUnique();
                if(program == null) return F.Promise.promise(() -> false);

                return F.Promise.promise(() -> true);
            }

        }));


        HANDLERS.put("post.author", Optional.of(new InterfaceHandler() {
            public F.Promise<Boolean> isAllowed(final String name, final String meta, final DeadboltHandler deadboltHandler, final Http.Context context)
            {
                Map<String, String[]> queryStrings = context.request().queryString();
                String[] requestedNames = queryStrings.get("post_id");

                if(requestedNames.length != 1) Logger.error("Chyba nenalezena příslušná Query"); // TODO Logger

                Post post = Post.find.where().eq("author.id", SecurityController.getPerson(context).id).where().eq("post_id",requestedNames[0]).findUnique();
                if(post == null) return F.Promise.promise(() -> false);

                return F.Promise.promise(() -> true);
            }

        }));



        HANDLERS.put("test2", Optional.of(new InterfaceHandler() {

            public F.Promise<Boolean> isAllowed(final String name, final String meta, final DeadboltHandler deadboltHandler, final Http.Context context)
            {
                return deadboltHandler.getSubject(context).map(subjectOption -> {

                            if ( new DeadboltAnalyzer().hasRole(subjectOption, Roles.user.getName() )) return true;
                          // if ( new DeadboltAnalyzer().hasRole(subjectOption, Roles.superAdmin.getName() )) return true;


                            Map<String, String[]> queryStrings = context.request().queryString();
                            String[] requestedNames = queryStrings.get("projectId");

                            Project project = Project.find.byId(requestedNames[0]);
                            Person  person  = SecurityController.getPerson(context);

                            if(project == null || person == null || !project.ownersOfProject.contains(person))  return false;

                            return true;

                });
            }

        }));

        HANDLERS.put("viewProfile", Optional.of(new InterfaceHandler() {

                         public F.Promise<Boolean> isAllowed(final String name, final String meta, final DeadboltHandler deadboltHandler, final Http.Context context) {

                             System.out.println("DefaultPermission - viewProfile ");


                             return deadboltHandler.getSubject(context).map(subjectOption -> {

                                            final boolean[] allowed = {false};
                                                       if (new DeadboltAnalyzer().hasRole(subjectOption, "admin"))
                                                       {
                                                           allowed[0] = true;
                                                       }
                                                       else
                                                       {
                                                           subjectOption.ifPresent(subject -> {
                                                               // for the purpose of this example, we assume a call to view profile is probably
                                                               // a get request, so the query string is used to provide info
                                                               Map<String, String[]> queryStrings = context.request().queryString();
                                                               String[] requestedNames = queryStrings.get("userName");
                                                               allowed[0] = requestedNames != null
                                                                       && requestedNames.length == 1
                                                                       && requestedNames[0].equals(subject.getIdentifier());
                                                           });
                                                       }

                                                       return allowed[0];
                                                   });
                         }
        }));
    }


    public F.Promise<Boolean> isAllowed(final String name, final String meta, final DeadboltHandler deadboltHandler, final Http.Context context) {

        return HANDLERS.get(name).orElseGet(() -> {
                           Logger.error("No handler available for " + name);
                           return DENY;
                       }).isAllowed(name, meta, deadboltHandler, context);
    }

    public F.Promise<Boolean> checkPermission(final String permissionValue, final DeadboltHandler deadboltHandler, final Http.Context ctx)
    {
        return deadboltHandler.getSubject(ctx).map(subjectOption -> {

                                  final boolean[] permissionOk = {false};

                                subjectOption.ifPresent(subject -> {
                                      List<? extends Permission> permissions = subject.getPermissions();
                                      for (Iterator<? extends Permission> iterator = permissions.iterator(); !permissionOk[0] && iterator.hasNext(); )
                                      {
                                          Permission permission = iterator.next();
                                          permissionOk[0] = permission.getValue().contains(permissionValue);
                                      }
                                  });

                                  return permissionOk[0];
                              });
    }
}
